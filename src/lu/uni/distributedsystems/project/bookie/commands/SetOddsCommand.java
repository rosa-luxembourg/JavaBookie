package lu.uni.distributedsystems.project.bookie.commands;

import lu.uni.distributedsystems.project.bookie.Bookie;
import lu.uni.distributedsystems.project.bookie.exceptions.UnknownGameException;
import lu.uni.distributedsystems.project.bookie.exceptions.UnknownTeamException;
import lu.uni.distributedsystems.project.common.command.Command;
import lu.uni.distributedsystems.project.common.command.CommandProcessor;

/*
 * This is the implementation for the command line interface set_odds |match-id| |team| |new odds|.
 * To know more about this command look in the Project Description under Command-line interface of the bookie.
 * 
 * Every command extends the class Command that is used by the commandProcessor.
 * 
 */
public class SetOddsCommand extends Command {
	
	private Bookie bookie;

	/* 	The constructor calls its parent constructor that registers the String, by which the command can be called
	 *	from the console, and itself with the commandProcessor.
	 */
	public SetOddsCommand(CommandProcessor commandProcessor, Bookie bookie) {
		super(commandProcessor, "set_odds");
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
		String team = args[1];
		float newOdds = Float.parseFloat(args[2]);
		
		try {
			bookie.setOdds(matchID, team, newOdds);
		} catch (UnknownGameException e) {
			System.err.println(e.getMessage());
		} catch (UnknownTeamException e) {
			System.err.println(e.getMessage());
		}
	}

	// This method is invoked by the Help Command to display a user guide for this command.
	@Override
	public void showHelp() {
		System.out.println("set_odds [match-id] [team] [new odds]");
		System.out.println("    Changes the odds for a team in a running match,");
		System.out.println("    and notifies all connected gamblers.");
	}
}
