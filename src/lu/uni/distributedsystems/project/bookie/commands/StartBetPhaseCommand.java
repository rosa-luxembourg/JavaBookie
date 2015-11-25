package lu.uni.distributedsystems.project.bookie.commands;

import lu.uni.distributedsystems.project.bookie.Bookie;
import lu.uni.distributedsystems.project.bookie.exceptions.InvalidTeamNameException;
import lu.uni.distributedsystems.project.common.command.Command;
import lu.uni.distributedsystems.project.common.command.CommandProcessor;

/*
 * This is the implementation for the command line interface start_bet_phase |team-A| |odds-a| |team-b| |odds-b| |limit|.
 * 
 * To know more about this command look in the Project Description under Command-line interface of the bookie.
 * 
 * Every command extends the class Command that is used by the commandProcessor.
 * 
 */
public class StartBetPhaseCommand extends Command {
	
	private Bookie bookie;

	/* 	The constructor calls its parent constructor that registers the String, by which the command can be called
	 *	from the console, and itself with the commandProcessor.
	 */
	public StartBetPhaseCommand(CommandProcessor commandProcessor, Bookie bookie) {
		super(commandProcessor, "start_bet_phase");
		this.bookie = bookie;
	}

	/*
	 * This method is called from the commandProcessor when he reads the commands string from the Console (entered by the User).
	 * The process splits the needed arguments and calls the corresponding method in the Bookie Class. 
	 */
	@Override
	public void process(String[] args) {
		// extract arguments
		String teamA = args[0];
		float oddsA = Float.parseFloat(args[1]);
		String teamB = args[2];
		float oddsB = Float.parseFloat(args[3]);
		float oddsDraw = Float.parseFloat(args[4]);
		int limit = Integer.parseInt(args[5]); 
		
		try {
			bookie.startBetPhase(teamA, oddsA, teamB, oddsB, oddsDraw, limit);
		} catch (InvalidTeamNameException e) {
			System.err.println(e.getMessage());
		}
	}

	// This method is invoked by the Help Command to display a user guide for this command.
	@Override
	public void showHelp() {
		System.out.println("start_bet_phase [team-A] [odds-a] [team-b] [odds-b] [odds-draw] [limit]");
		System.out.println("    Starts a new match between two teams, setting their initial odds, the odds of");
		System.out.println("    a draw and the limit on the total wager for this match. A unique Match-ID");
		System.out.println("    will be created. All connected gamblers are informed about the start of that");
		System.out.println("    new match, including all its parameters, i.e. the Match-ID, the participating");
		System.out.println("    teams, their odds, the odds of a draw taking place and the limit. From this");
		System.out.println("    point on, the bookie starts accepting bets for that match.");
	}

}
