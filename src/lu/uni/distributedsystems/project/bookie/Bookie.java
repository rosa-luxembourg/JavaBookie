package lu.uni.distributedsystems.project.bookie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lu.uni.distributedsystems.gsonrmi.server.ServiceMode;
import lu.uni.distributedsystems.project.bookie.commands.EndBetPhaseCommand;
import lu.uni.distributedsystems.project.bookie.commands.SetModeOfGamblerCommand;
import lu.uni.distributedsystems.project.bookie.commands.SetOddsCommand;
import lu.uni.distributedsystems.project.bookie.commands.ShowBetsCommand;
import lu.uni.distributedsystems.project.bookie.commands.ShowMatchesCommand;
import lu.uni.distributedsystems.project.bookie.commands.ShowProfitCommand;
import lu.uni.distributedsystems.project.bookie.commands.StartBetPhaseCommand;
import lu.uni.distributedsystems.project.bookie.exceptions.AlreadyClosedGameException;
import lu.uni.distributedsystems.project.bookie.exceptions.InvalidTeamNameException;
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
	// keep track of money coming in and going out
	private double profit;
	// directory of all known gambler connections
	private Map<String, GamblerConnection> gamblerConnections;
	//directory of open matches, mapped by matchID
	private Map<Integer, Match> openMatches;
	//set of bets placed on open matches
	private Set<Bet> placedBets;
	
	// construct a bookie
	public Bookie(String bookieID, int bookiePort) {
		this.bookieID = bookieID;
		this.profit = 0;
		this.gamblerConnections = new ConcurrentHashMap<String, GamblerConnection>();
		this.openMatches = new ConcurrentHashMap<Integer, Match>();
		this.placedBets = Collections.newSetFromMap(new ConcurrentHashMap<Bet, Boolean>());

		// create the bookie's JSON-RPC server
		bookieServer = new BookieServer(this, bookiePort);
		// start the bookie server, i.e. the remote interface which gamblers may invoke
		bookieServer.start();
	}
	
	public String getBookieID() {
		return bookieID;
	}
	
	public double getProfit(){
		return profit;
	}
	
	public void addToProfit(double amount){
		profit += amount;
	}
	
	public void substractFromProfit(double amount){
		profit -= amount;
	}
	
	public void createNewGamblerConnection(String gamblerID, String gamblerIP, int gamblerPort) {
		// create a new connection with a gambler at the given address
		GamblerConnection gamblerConnection = new GamblerConnection(this, gamblerID, gamblerIP, gamblerPort);
		
		// setup a socket connection to the gambler's JSON-RPC server
		gamblerConnection.establishSocketConnection();
		// register gambler connection
		gamblerConnections.put(gamblerID, gamblerConnection);
		
	}
	
	public GamblerConnection getGamblerConnection(String gamblerID) throws UnkownGamblerException {
		// need to perform sanity check here; gambler might be unknown ...
		if (gamblerConnections.get(gamblerID) == null){
			throw new UnkownGamblerException("Gambler " + gamblerID + " is not registered with this bookie!");
		}
		return gamblerConnections.get(gamblerID);
	}
	
	public void removeGamblerConnection(String gamblerID) throws UnkownGamblerException{
		if (gamblerConnections.get(gamblerID) == null){
			throw new UnkownGamblerException("Gambler " + gamblerID + " is not registered with this bookie!");
		}
		gamblerConnections.remove(gamblerID);
	}
	
	public Map<String, GamblerConnection> getAllGamblerConnections() {
		return gamblerConnections;
	}
	
	public Set<Bet> getPlacedBets(){
		return placedBets;
	}
	
	public Map<Integer, Match> getOpenMatches(){
		return openMatches;
	}
	
	
	
	public void shutdown()  {
		// shut down all gambler connections
		for (GamblerConnection gamblerConnection : gamblerConnections.values()){
			gamblerConnection.bookieExiting();
			gamblerConnection.closeConnection();
		}			
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
	public void startBetPhase(String teamA, float oddsA, String teamB, float oddsB, float oddsDraw, int limit) throws InvalidTeamNameException {
	
		// TODO, initiate the bet Phase:
		// teams cannot be called "draw"!
		if (teamA.equalsIgnoreCase("draw") || teamB.equalsIgnoreCase("draw")){
			throw new InvalidTeamNameException("Draw is an invalid name for a team. Please enter a different name.");
		}
		// create new match with specified parameters
		Match startedMatch = new Match(this.bookieID, teamA, oddsA, teamB, oddsB, oddsDraw, limit);
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
		// check if bookie has inputed valid gameID and team (or draw option)
		if (!openMatches.containsKey(matchID)){
			throw new UnknownGameException("There is no open game with ID " + matchID);
		} else if (!(openMatches.get(matchID).getTeamA().equals(team)) && !(openMatches.get(matchID).getTeamB().equals(team)) && !(team.equalsIgnoreCase("draw"))){
			throw new UnknownTeamException("Team " + team + " is not playing on this match!");
		}
		// change the odds
		if (openMatches.get(matchID).getTeamA().equals(team)){
			openMatches.get(matchID).setOddsA(newOdds);
		} else if (openMatches.get(matchID).getTeamB().equals(team)) {
			openMatches.get(matchID).setOddsB(newOdds);
		} else {
			openMatches.get(matchID).setOddsDraw(newOdds);
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
	
	/* Shows a list of all matches opened by this bookie.
	 * 
	 * 
	 * The showMatches method is called from the ShowMatchesCommand entered in the Command Line
	 */
	public void showMatches() {
				
		for (Match m : openMatches.values()){
			System.out.println(m);
		}
	
	}
	
	/* Displays the current profit of the bookie.
	 * 
	 * 
	 * The showProfit method is called from the ShowProfitCommand entered in the Command Line
	 */
	public void showProfit() {
			System.out.println("Bookie " + bookieID + ", you currently have a profit of " + profit + "€");
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
		} else if (!(openMatches.get(matchID).getTeamA().equals(winningTeam)) && !(openMatches.get(matchID).getTeamB().equals(winningTeam)) && !(winningTeam.equalsIgnoreCase("draw"))){
			throw new UnknownTeamException("Team " + winningTeam + " is not playing on this match!");
		}
		// close match, inform all connected gamblers that have placed a bet on this game about winning team and amount won
		// delete bet (only when confirmation has been sent from gambler) and game (only
		// when all bets have been deleted)
		double amountWon;
		List<String> informedGamblers = new ArrayList<String>();
		Iterator<Bet> iterator = placedBets.iterator();
		while(iterator.hasNext()){
			Bet b = iterator.next();
			if (b.getMatchID() == matchID){
				int betID = b.getId(); 
				if (b.getTeam().equals(winningTeam)){
					BigDecimal amount = BigDecimal.valueOf(b.getAmount());
					BigDecimal odds = BigDecimal.valueOf(b.getOdds());
					amountWon = amount.multiply(odds).setScale(2, RoundingMode.HALF_UP).doubleValue();
				} else {
					amountWon = 0;
				}
				// if the gambler is no longer connected to the server
				// store the amount we owe him/her in order to send it when he reconnects
				informedGamblers.add(b.getGamblerID());
				if (gamblerConnections.containsKey(b.getGamblerID())){
					gamblerConnections.get(b.getGamblerID()).endBet(betID, matchID, winningTeam, amountWon);
				} else {
					b.setWinningTeam(winningTeam);
					b.setAmountDue(amountWon);
				}
				
			}
		}
		
		// remove payed bets
		Iterator<Bet> iterator2 = placedBets.iterator();
		while(iterator2.hasNext()){
			Bet b = iterator2.next();
			if (b.isPayed()){
				iterator2.remove();
			}
		}
		
		// if all bets for that game have been removed, then we can also remove the game
		boolean openBets = false;
		for (Bet b : placedBets){
			if (b.getMatchID() == matchID){
				openBets = true;
			}
		}
		if (!openBets){
			openMatches.remove(matchID);
		}
		
		// inform all other gamblers about the outcome of this match
		boolean inform = true;
		int betID = -1; 
		amountWon = 0;
		for (String gid : gamblerConnections.keySet()){
			for (String gambler : informedGamblers){
				if (gid.equals(gambler)){
					inform = false;
				}
			}
			if (inform){
				gamblerConnections.get(gid).endBet(betID, matchID, winningTeam, amountWon);
			}
		}
		// if game has not yet been removed (still bets to be payed), close game
		if(openMatches.containsKey(matchID)){
			openMatches.get(matchID).closeGame();
		}	
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
			new ExitCommand(commandProcessor, bookie);
			new ShowMatchesCommand(commandProcessor, bookie);
			new ShowProfitCommand(commandProcessor, bookie);
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
