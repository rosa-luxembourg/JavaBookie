package lu.uni.distributedsystems.project.bookie.commands;

import lu.uni.distributedsystems.project.bookie.Bookie;
import lu.uni.distributedsystems.project.common.command.Command;
import lu.uni.distributedsystems.project.common.command.CommandProcessor;


/*
 * This is the implementation for the command line interface end_bet_phase |Match-ID| |Team|.
 * To know more about this command look in the Project Description under Command-line interface of the bookie.
 * 
 * Every command extends the class Command that is used by the commandProcessor.
 * 
 */
public class EndBetPhaseCommand extends Command {
	
	private Bookie bookie;

	/* 	The constructor calls its parent constructor that registers the String, by which the command can be called
	 *	from the console, and itself with the commandProcessor.
	 */
	public EndBetPhaseCommand(CommandProcessor commandProcessor, Bookie bookie) {
		super(commandProcessor, "end_bet_phase");
		this.bookie = bookie;
	}

	/*
	 * This method is called from the commandProcessor when he reads the commands string from the Console (entered by the User).
	 * The process splits the needed arguments and calls the corresponding method in the Bookie Class. 
	 */
	@Override
	public void process(String[] args) {
		// extract arguments
		int matchID = Integer.parseInt(args[0]);
		String winningTeam = args[1];
		
		bookie.endBetPhase(matchID, winningTeam);
	}

	// This method is invoked by the Help Command to display a user guide for this command.
	@Override
	public void showHelp() {
		System.out.println("end_bet_phase [match-id] [team]");
		System.out.println("    Terminates a match, determining the team that won. All connected");
		System.out.println("    gamblers are informed about the outcome. The payouts are calculated");
		System.out.println("    and transferred to the winners.");
	}

}
