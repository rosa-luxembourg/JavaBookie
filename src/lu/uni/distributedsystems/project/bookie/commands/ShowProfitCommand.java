package lu.uni.distributedsystems.project.bookie.commands;

import lu.uni.distributedsystems.project.bookie.Bookie;
import lu.uni.distributedsystems.project.common.command.Command;
import lu.uni.distributedsystems.project.common.command.CommandProcessor;

public class ShowProfitCommand extends Command {
	
	private Bookie bookie;

	/* 	The constructor calls its parent constructor that registers the String, by which the command can be called
	 *	from the console, and itself with the commandProcessor.
	 */
	public ShowProfitCommand(CommandProcessor commandProcessor, Bookie bookie) {
		super(commandProcessor, "show_profit");
		this.bookie = bookie;
	}

	/*
	 * This method is called from the commandProcessor when he reads the commands string from the Console (entered by the User).
	 * The process splits the needed arguments and calls the corresponding method in the Bookie Class. 
	 */
	@Override
	public void process(String[] args) {
		bookie.showProfit();
	}

	// This method is invoked by the Help Command to display a user guide for this command.
	@Override
	public void showHelp() {
		System.out.println("show_profit - displays the current profit of this bookie");
	}

}
