package lu.uni.distributedsystems.project.bookie;

import java.util.Iterator;

import com.google.code.gsonrmi.Parameter;
import com.google.code.gsonrmi.RpcResponse;

import lu.uni.distributedsystems.project.common.Bet;
import lu.uni.distributedsystems.project.common.JsonRpcConnection;
import lu.uni.distributedsystems.project.common.Match;

public class GamblerConnection extends JsonRpcConnection {
	
	private String gamblerID; // gambler-ID of the gambler on the other side of this connection
	private Bookie bookie;   // the enclosing bookie
	// used in combination with bookieID to assign id's to requests send to gamblers
	private int responseSeqNum; 
	
	// A GamblerConnection is used to communicate with a connected Gambler.
	public GamblerConnection(Bookie bookie, String gamblerID, String gamblerIP, int gamblerPort) {
		// initialize JsonRpcConnection base class
		super(gamblerIP, gamblerPort);
		this.bookie = bookie;
		this.gamblerID = gamblerID;
		responseSeqNum = 0;
	}

	// This sample method is just there for illustration purposes
	public void sayHello() {
		String requestID = this.bookie.getBookieID() + ++responseSeqNum;
		bookie.setLastRquestID(requestID);
		bookie.setLastRquestName("sayHello");
		Parameter[] params = new Parameter[] { new Parameter(bookie.getBookieID()) };
		RpcResponse response = handleJsonRpcRequest(requestID, "sayHelloToGambler", params);
		
		// show hello message returned by bookie
		String sayHelloResponse = response.result.getValue(String.class, getGson());
		System.out.println("Gambler " + gamblerID + " sent response: " + sayHelloResponse);
	}
	
	
	// TODO insert the needed methods to communicate with a Gambler
	
	// Send information about the start of a new match to all connected gamblers.
	// In order to do that, matchStarted invokes the RPC someMethodName method on the gamblers
	// sending as parameter a Match object containing all the information on the match
	public void matchStarted(Match startedMatch){
		String requestID;
		if(noResponse && bookie.getLastRequestName().equals("matchStarted")){
			requestID = bookie.getLastRequestID();
		} else {
			requestID = this.bookie.getBookieID() + ++responseSeqNum;
			bookie.setLastRquestID(requestID);
			bookie.setLastRquestName("matchStarted");
		}
		Parameter[] params = new Parameter[] { new Parameter(bookie.getBookieID()),
												new Parameter(startedMatch)};
		RpcResponse response = handleJsonRpcRequest(requestID, "matchStarted", params);
		
		// gambler sends boolean to confirm receiving response to his/her request
		boolean confirmation = response.result.getValue(Boolean.class, getGson());
		System.out.println("Gambler " + gamblerID + " sent response: " + confirmation);
	}
	
	public void newOdds(int matchID, String team, float newOdds){
		String requestID = this.bookie.getBookieID() + ++responseSeqNum;
		Parameter[] params = new Parameter[] { new Parameter(bookie.getBookieID()),
												new Parameter(matchID),
												new Parameter(team),
												new Parameter(newOdds)};
		RpcResponse response = handleJsonRpcRequest(requestID, "setOdds", params);

		// gambler sends boolean to confirm receiving response to his/her request
		boolean confirmation = response.result.getValue(Boolean.class, getGson());
		System.out.println("Gambler " + gamblerID + " sent response: " + confirmation);
	}
	
	public void endBet(int betID, int matchID, String winningTeam, float amountWon){
		String requestID = this.bookie.getBookieID() + ++responseSeqNum;
		Parameter[] params = new Parameter[] { new Parameter(bookie.getBookieID()),
				new Parameter(matchID),
				new Parameter(winningTeam),
				new Parameter(amountWon)};
		RpcResponse response = handleJsonRpcRequest(requestID, "endBet", params);
		
		// gambler sends boolean to confirm receiving response to his/her request
		boolean confirmation = response.result.getValue(Boolean.class, getGson());
		System.out.println("Gambler " + gamblerID + " sent response: " + confirmation);
		// if we receive a confirmation from the gambler that he has received
		// and processed the endBet info, we can remove the bet from the opened bets
		if(confirmation){
			Iterator<Bet> iterator = bookie.getPlacedBets().iterator();
			while(iterator.hasNext()){
				Bet b = iterator.next();
				if (b.getId() == betID){
					iterator.remove();
				}
			}
			// if all bets for that game have been removed, then we can also remove the game
			boolean openBets = false;
			for (Bet b : bookie.getPlacedBets()){
				if (b.getMatchID() == matchID){
					openBets = true;
				}
			}
			if (!openBets){
				bookie.getOpenMatches().remove(matchID);
			}
		}
	}

}
