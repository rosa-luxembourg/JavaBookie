package lu.uni.distributedsystems.project.bookie;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import lu.uni.distributedsystems.gsonrmi.server.ServiceMode;
import lu.uni.distributedsystems.project.bookie.commands.EndBetPhaseCommand;
import lu.uni.distributedsystems.project.bookie.commands.SetModeOfGamblerCommand;
import lu.uni.distributedsystems.project.bookie.commands.SetOddsCommand;
import lu.uni.distributedsystems.project.bookie.commands.ShowBetsCommand;
import lu.uni.distributedsystems.project.bookie.commands.StartBetPhaseCommand;
import lu.uni.distributedsystems.project.common.command.*;



public class Bookie {
	
	// unique bookie-id, such as Charlie, Diane, ...
	private String bookieID;
	// bookie's JSON-RPC server
	private BookieServer bookieServer;
	// directory of all known gambler connections
	private Map<String, GamblerConnection> gamblerConnections;
	
	// construct a bookie
	public Bookie(String bookieID, int bookiePort) {
		this.bookieID = bookieID;
		this.gamblerConnections = new HashMap<String, GamblerConnection>();

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
	}
	
	public GamblerConnection getGamblerConnection(String gamblerID) {
		// need to perform sanity check here; gambler might be unknown ...
		return gamblerConnections.get(gamblerID);
	}
	
	
	
	private void shutdown()  {
		// shut down all gambler connections
		for (GamblerConnection gamblerConnection : gamblerConnections.values())
			gamblerConnection.closeConnection();
		// TODO must shut down JSON-RPC server
	}
	
	
	
	public void setModeOfGambler(String gamblerID, ServiceMode serviceMode) {
		// need to perform sanity check here; gambler might be unknown ...
		getGamblerConnection(gamblerID).setModeOfHost(serviceMode);
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
		
	}

	
	/* From the Project Description:
	 * Changes the odds for a team in a running match, and notifies all connected gamblers.
	 * 
	 * Refer to the project example scenario for further information about the odds.
	 * 
	 * The setOdds method is called from the SetOddsCommand entered in the Command Line
	 * 
	 */
	public void setOdds(int matchID, String team, float newOdds) {
		
		// TODO set the new odds for a team
		
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
	public void endBetPhase(int matchID, String winningTeam) {
		
		// TODO terminate a match, set its winner, inform all connected gamblers,
		
		// TODO calculate payouts and transfer to winners
		
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
