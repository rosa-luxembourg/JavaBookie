package lu.uni.distributedsystems.project.bookie;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.code.gsonrmi.Parameter;
import com.google.code.gsonrmi.RpcRequest;
import com.google.code.gsonrmi.RpcResponse;
import com.google.code.gsonrmi.annotations.RMI;
import com.google.code.gsonrmi.serializer.ExceptionSerializer;
import com.google.code.gsonrmi.serializer.ParameterSerializer;
import com.google.code.gsonrmi.server.RpcTarget;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lu.uni.distributedsystems.gsonrmi.server.BaseServer;
import lu.uni.distributedsystems.gsonrmi.server.Interceptor;
import lu.uni.distributedsystems.gsonrmi.server.RpcSocketListener;
import lu.uni.distributedsystems.project.bookie.exceptions.UnkownGamblerException;
import lu.uni.distributedsystems.project.common.Bet;
import lu.uni.distributedsystems.project.common.Match;
import lu.uni.distributedsystems.project.common.PlaceBetResult;

// Your server implementation should extend the class BaseServer;
// this way, the setModeOfHost remote method to control how requests
// will be processed is added automatically
public class BookieServer extends BaseServer {

	// the enclosing bookie
	private Bookie bookie;
	// port on which the bookie's JSON-RPC server listens
	private int bookiePort;
	// responses to processBet requests, mapped by gamblerID (we keep only one response per gambler)
	private Map<String, RpcResponse> processedResponses;
	private RpcSocketListener socketLis;
	
	public BookieServer(Bookie bookie, int bookiePort) {
		this.bookie = bookie;
		this.bookiePort = bookiePort;
		processedResponses = new HashMap<String, RpcResponse>();
	}
	
	// the @RMI annotation exposes some method for remote invocation
	@RMI
	public String sayHelloToBookie(String gamblerID) {
		// implementation of the sayHelloToBookie method, which
		// can be invoked remotely with one argument of
		// type String, returning another String
		System.out.println("sayHelloToBookie(" + gamblerID + ")");
		
		try {
			bookie.getGamblerConnection(gamblerID).sayHello();
		} catch (UnkownGamblerException e) {
			e.getMessage();
		}
		
		return "Bookie says: Hello, gambler " + gamblerID;
	}

	// boilerplate code for a gambler who wants to connect to a bookie,
	// specifying at which IP and port to reach the gambler.
	@RMI
	public String connect(String gamblerID, String gamblerIP, int gamblerPort) {
		bookie.createNewGamblerConnection(gamblerID, gamblerIP, gamblerPort);
		return bookie.getBookieID();
	}

	
	// TODO insert the methods that a Gambler can execute via RPC on this Bookie
	
	// method to be invoked by a gambler wanting to place a bet on a match
	// the gambler must provide his/her ID, the ID of the match, the name of the team
	// he/she is betting on, the amount of the bet and the odds
	// a message accepting or rejecting the bet will be returned to the gambler
	@RMI
	public synchronized String placeBet (String gamblerID, int matchID, String team, float odds, int stake){
		
		int limit;
		
		// check if match exists and is opened and, if so, the limit that has been set for placed bets
		if (!bookie.getOpenMatches().containsKey(matchID)){
			return PlaceBetResult.REJECTED_UNKNOWN_MATCH.toString();
		}
		if (!bookie.getOpenMatches().get(matchID).isOpened()){
			return PlaceBetResult.REJECTED_CLOSED_MATCH.toString();
		}
		
		limit = bookie.getOpenMatches().get(matchID).getLimit();
		
		// check if the team specified by gambler corresponds to one of the teams playing in the match (or to the draw option) and
		// if the odds sent by the gambler are correct
		if (!team.equals(bookie.getOpenMatches().get(matchID).getTeamA()) && !team.equals(bookie.getOpenMatches().get(matchID).getTeamB()) && !team.equalsIgnoreCase("draw")){
			return PlaceBetResult.REJECTED_UNKNOWN_TEAM.toString();
		} else if ((team.equals(bookie.getOpenMatches().get(matchID).getTeamA())) && (odds != bookie.getOpenMatches().get(matchID).getOddsA())
					|| (team.equals(bookie.getOpenMatches().get(matchID).getTeamB())) && (odds != bookie.getOpenMatches().get(matchID).getOddsB())
					|| (team.equalsIgnoreCase("draw") && odds != bookie.getOpenMatches().get(matchID).getOddsDraw())){
			return PlaceBetResult.REJECTED_ODDS_MISMATCH.toString();
		}		
		// check if the gambler has already placed a bet for this match - reject bet, if that is the case
		for (Bet b : bookie.getPlacedBets()){
			if (b.getMatchID() == matchID) {
				if (b.getGamblerID().equals(gamblerID)){
					return PlaceBetResult.REJECTED_ALREADY_PLACED_BET.toString();
				}
			}
		}
		// if the bet the gambler wants to place goes over the limit, reject it and inform
		// gambler about current value of limit
		if (stake > limit){
			return PlaceBetResult.REJECTED_LIMIT_EXCEEDED.toString() + " You can only bet a maximum amount of " + limit;
		}
		// the bet seems valid, let's register it and send confirmation to the gambler
		Bet placedBet = new Bet(matchID, stake, team, odds, gamblerID, bookie.getBookieID());
		bookie.getPlacedBets().add(placedBet);
		// update the limit in the openMatches map such that gamblers
		// can receive updated info when they need to consult the matches
		int newLimit = limit-placedBet.getAmount();
		bookie.getOpenMatches().get(matchID).setLimit(newLimit);
		return PlaceBetResult.ACCEPTED.toString();
	}
	
	// method to be invoked by a gambler wanting to get list of matches opened
	// on connected bookies
	@RMI
	public Map<Integer, Match> showMatches(String gamblerID){
		return bookie.getOpenMatches();
	}
	
	// method to be invoked by a gambler wishing to be
	// permanently disconnected from bookie
	@RMI
	public boolean gamblerExiting(String gamblerID){
		try {
			bookie.removeGamblerConnection(gamblerID);
			System.out.println("Gambler " + gamblerID + " was disconnected");
			return true;
		} catch (UnkownGamblerException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}
	
	public void start() {
		// start a JSON-RPC server; all methods tagged with the @RMI annotation will
		// be invokable remotely; all requests as well as all responses will be
		// passed on to the interceptor; please note that this class extends BaseServer
		// and thus will expose an additional method setModeOfHost to control how
		// requests will be processed

		// create an instance of Gson, the primary class for using the Gson libraries
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Exception.class, new ExceptionSerializer())
				.registerTypeAdapter(Parameter.class, new ParameterSerializer())
				.create();
		
		
		// This creates an Interceptor that has the two functions interceptRequest and interceptResponse
		//Follwoing the code for an Interceptor which simply prints all intercepted requests
		// and responses, just for illustration purposes
		
		Interceptor interceptor = new Interceptor() {
			
			// TODO replace the simple interceptor with a more meaningful implementation
			
			@Override
			// returns null - response is generated by handling the request
			// but in case we already have a response for that request, 
			// we just send it without having to process the request again 
			public RpcResponse interceptRequest(RpcRequest request) {
				String requestID = gson.toJson(request.id);
				Parameter[] requestParam = request.params;
				String gamblerID = gson.toJson(requestParam[0]);
				
				System.out.println("intercepted request: " + gson.toJson(request));
				if (processedResponses.containsKey(gamblerID) && gson.toJson(processedResponses.get(gamblerID).id).equals(requestID)){
					System.out.println("Sending stored response...");
					return processedResponses.get(gamblerID);
				}
				System.out.println("Processing new request...");
				return null;
			}
			
			@Override
			public void interceptResponse(RpcRequest request, RpcResponse response) {
				
				// HINT 1: 	this message returns an RPCResponse object, in case that it shall do nothing with it, simply return null.
				// HINT 2: 	If you want to have more details on the interceptors you can have a look at  
				//			the code in the project GSONRMI, in the package lu.uni.distributedsystems.gsonrmi.server, in the class: RpcConnectionHandler.java
				
				// placeBet is the only nonidempotent remote procedure
				// only when the gambler is invoking the placeBet method will the bookie need to restrain
				// from processing the bet again and simply return the already generated response
				// so we will store those responses to send them if we get a second identical request after a 
				// placed bet in DISCONNECT_BEFORE_REPLY mode
				// only a maximum of one response per gambler will be stored. we keep only the last response
				// since, as clients can make only one request at a time, the bookie server can interpret
				// each NEW request as an acknowledgement of the previous reply
				Parameter[] requestParam = request.params;
				String gamblerID = gson.toJson(requestParam[0]);
				if (request.method.equals("placeBet")){				
				    processedResponses.put(gamblerID, response);
				}
				
				System.out.println("intercepted response: " + gson.toJson(response) + " for request: " + gson.toJson(request));
			}

		};

		
		// launch a socket listener accepting and handling JSON-RPC requests
		try {
			socketLis = new RpcSocketListener(bookiePort, new RpcTarget(this, gson), gson, interceptor);
			socketLis.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shutDown(){
		try {
			socketLis.shutdown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
