package lu.uni.distributedsystems.project.bookie;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import lu.uni.distributedsystems.gsonrmi.server.ServiceMode;
import lu.uni.distributedsystems.project.bookie.commands.EndBetPhaseCommand;
import lu.uni.distributedsystems.project.bookie.commands.SetModeOfGamblerCommand;
import lu.uni.distributedsystems.project.bookie.commands.SetOddsCommand;
import lu.uni.distributedsystems.project.bookie.commands.ShowBetsCommand;
import lu.uni.distributedsystems.project.bookie.commands.StartBetPhaseCommand;
import lu.uni.distributedsystems.project.bookie.exceptions.AlreadyClosedGameException;
import lu.uni.distributedsystems.project.bookie.exceptions.UnknownGameException;
import lu.uni.distributedsystems.project.bookie.exceptions.UnknownTeamException;
import lu.uni.distributedsystems.project.bookie.exceptions.UnkownGamblerException;
import lu.uni.distributedsystems.project.common.Bet;
import lu.uni.distributedsystems.project.common.Match;
import lu.uni.distributedsystems.project.common.command.*;



public class Bookie {
	
	// unique bookie-id, such as Charlie, Diane, ...
	private String bookieID;
	// bookie's JSON-RPC server
	private BookieServer bookieServer;
	// directory of all known gambler connections
	private Map<String, GamblerConnection> gamblerConnections;
	//set of open matches and placed bets
	private Map<Integer, Match> openMatches;
	//directory of bets placed on open matches (the key is the matchID)
	private Set<Bet> placedBets;
	
	// construct a bookie
	public Bookie(String bookieID, int bookiePort) {
		this.bookieID = bookieID;
		this.gamblerConnections = new HashMap<String, GamblerConnection>();
		this.openMatches = new HashMap<Integer, Match>();
		this.placedBets = new HashSet<Bet>();

		// create the bookie's JSON-RPC server
		bookieServer = new BookieServer(this, bookiePort);
		// start the bookie server, i.e. the remote interface which gamblers may invoke
		bookieServer.start();
	}
	
	public String getBookieID() {
		return bookieID;
	}
	
	

	public void createNewGamblerConnection(String gamblerID, String gamblerIP, int gamblerPort) {
		// create a new connection with a gambler at the given address
		GamblerConnection gamblerConnection = new GamblerConnection(this, gamblerID, gamblerIP, gamblerPort);
		
		// setup a socket connection to the gambler's JSON-RPC server
		gamblerConnection.establishSocketConnection();
		// register gambler connection
		gamblerConnections.put(gamblerID, gamblerConnection);
		
		//inform gambler about open matches
		if (!openMatches.isEmpty()){
			for (Match openMatch : openMatches.values()){
				gamblerConnection.matchStarted(openMatch);
			}
		}
	}
	
	public GamblerConnection getGamblerConnection(String gamblerID) throws UnkownGamblerException {
		// need to perform sanity check here; gambler might be unknown ...
		if (gamblerConnections.get(gamblerID) == null){
			throw new UnkownGamblerException("Gambler " + gamblerID + " is not registered with this bookie!");
		}
		return gamblerConnections.get(gamblerID);
	}
	
	public Set<Bet> getPlacedBets(){
		return placedBets;
	}
	
	public Map<Integer, Match> getOpenMatches(){
		return openMatches;
	}
	
	
	
	private void shutdown()  {
		// shut down all gambler connections
		for (GamblerConnection gamblerConnection : gamblerConnections.values())
			gamblerConnection.closeConnection();
		// TODO must shut down JSON-RPC server:
		bookieServer.shutDown();
	}
	
	
	
	public void setModeOfGambler(String gamblerID, ServiceMode serviceMode) throws UnkownGamblerException {
		// need to perform sanity check here; gambler might be unknown ...
		//check done at getGamblerConnection
		getGamblerConnection(gamblerID).setModeOfHost(this.bookieID, serviceMode);
	}

	/* From the Project Description:
	 * Starts a new match between two teams, setting their initial odds,
	 * and the limit on the total wager for this match. A unique Match-ID will be created.
	 * All connected gamblers are informed about the start of that new match, including all its parameters,
	 * i.e. the Match-ID, the participating teams, their odds, and the limit.
	 * From this point on, the bookie starts accepting bets for that match.
	 */
	public void startBetPhase(String teamA, float oddsA, String teamB, float oddsB, int limit) {
	
		// TODO, initiate the bet Phase: 
		// create new match with specified parameters
		Match startedMatch = new Match(this.bookieID, teamA, oddsA, teamB, oddsB, limit);
		openMatches.put(startedMatch.getId(), startedMatch);
		
		// inform gamblers
		for (GamblerConnection gamblerConnection : gamblerConnections.values()) {
		    gamblerConnection.matchStarted(startedMatch);
		}
	}

	
	/* From the Project Description:
	 * Changes the odds for a team in a running match, and notifies all connected gamblers.
	 * 
	 * Refer to the project example scenario for further information about the odds.
	 * 
	 * The setOdds method is called from the SetOddsCommand entered in the Command Line
	 * 
	 */
	public void setOdds(int matchID, String team, float newOdds) throws UnknownGameException, UnknownTeamException {
		
		// TODO set the new odds for a team
		// check if bookie has inputed valid gameID and team
		if (!openMatches.containsKey(matchID)){
			throw new UnknownGameException("There is no open game with ID " + matchID);
		} else if (!(openMatches.get(matchID).getTeamA().equals(team)) && !(openMatches.get(matchID).getTeamB().equals(team))){
			throw new UnknownTeamException("Team " + team + " is not playing on this match!");
		}
		// change the odds
		if (openMatches.get(matchID).getTeamA().equals(team)){
			openMatches.get(matchID).setOddsA(newOdds);
		} else {
			openMatches.get(matchID).setOddsB(newOdds);
		}
		// inform gamblers
		for (GamblerConnection gamblerConnection : gamblerConnections.values()) {
		    gamblerConnection.newOdds(matchID, team, newOdds);
		}
		
	}

	/* From the Project Description:
	 * 
	 * Shows a list of all open bets placed with this bookie.
	 * 
	 * 
	 * The showBets method is called from the ShowBetsCommand entered in the Command Line
	 */
	public void showBets() {
				
		// TODO show all (open) bets placed with this bookie
		for (Bet b : placedBets){
			System.out.println(b);
		}
	
	}

	/* From the Project Description:
	 * 
	 * Terminates a match, determining the team that won.
	 * All connected gamblers are informed about the outcome.
	 * The payouts are calculated and transferred to the winners.
	 * 
	 * 
	 * The endBetPhase method is called from the EndBetPhaseCommand entered in the Command Line
	 * 
	 */
	public void endBetPhase(int matchID, String winningTeam) throws UnknownGameException, UnknownTeamException, AlreadyClosedGameException{
		
		// TODO terminate a match, set its winner, inform all connected gamblers,
		
		// TODO calculate payouts and transfer to winners
		
		// check if bookie has inputed valid gameID and team and if game is still opened
		if (!openMatches.containsKey(matchID)){
			throw new UnknownGameException("There is no open game with ID " + matchID);
		} else if (!openMatches.get(matchID).isOpened()){
			throw new AlreadyClosedGameException("Game " + matchID + " is already closed!");
		} else if (!(openMatches.get(matchID).getTeamA().equals(winningTeam)) && !(openMatches.get(matchID).getTeamB().equals(winningTeam))){
			throw new UnknownTeamException("Team " + winningTeam + " is not playing on this match!");
		}
		// close match, inform all connected gamblers about winning team and amount won
		// delete bet (only when confirmation has been sent from gambler) and game (only
		// when all bets have been deleted) in gamblerConnection.endBet
		float amountWon;
		Iterator<Bet> iterator = placedBets.iterator();
		while(iterator.hasNext()){
			Bet b = iterator.next();
			if (b.getMatchID() == matchID && b.getTeam().equals(winningTeam)){
				amountWon = b.getAmount() * b.getOdds();
				int betID = b.getId(); 
				gamblerConnections.get(b.getGamblerID()).endBet(betID, matchID, winningTeam, amountWon);
			} else {
				amountWon = 0;
				int betID = b.getId();
				gamblerConnections.get(b.getGamblerID()).endBet(betID, matchID, winningTeam, amountWon);
			}
		}
		openMatches.remove(matchID);
	}
	
	
	/*
	 * The main class asks for the User to enter a Unique Name for the Bookie and the Port on which the Bookie's server listens.
	 * The Bookie Object is constructed, and initializes the Json RPC Server and starts it.
	 * The command processor is initialized and all the commands that can be invoked from the command Line are created.
	 * Finally the thread in which the command processor runs is started. 
	 */
	public static void main(String[] args) {
		Scanner consoleScanner = new Scanner(System.in);
		Bookie bookie = null;

		try {
			// let the user enter the bookie's configuration 
			System.out.print("Please enter unique Bookie-ID: ");
			String bookieID = consoleScanner.nextLine();
			System.out.print("Please enter port number for the bookie server: ");
			int bookiePort = Integer.parseInt(consoleScanner.nextLine());

			// create the bookie
			bookie = new Bookie(bookieID, bookiePort);

			// create a command processor and register all commands
			CommandProcessor commandProcessor = new CommandProcessor(consoleScanner);
			new SetModeOfGamblerCommand(commandProcessor, bookie);
			new StartBetPhaseCommand(commandProcessor, bookie);
			new SetOddsCommand(commandProcessor, bookie);
			new ShowBetsCommand(commandProcessor, bookie);
			new EndBetPhaseCommand(commandProcessor, bookie);
			// start the command processor such that the user can enter commands
			commandProcessor.start();
			// wait for the command processor to exit
			commandProcessor.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			consoleScanner.close();
			// make sure the socket connections are closed, whatever happens ... 
			if (bookie != null)
				bookie.shutdown();
		}
	}

}
