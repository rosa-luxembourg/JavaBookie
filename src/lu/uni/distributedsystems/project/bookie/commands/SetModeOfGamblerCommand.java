package lu.uni.distributedsystems.project.bookie.commands;

import lu.uni.distributedsystems.gsonrmi.server.ServiceMode;
import lu.uni.distributedsystems.project.bookie.Bookie;
import lu.uni.distributedsystems.project.bookie.exceptions.UnkownGamblerException;
import lu.uni.distributedsystems.project.common.command.CommandProcessor;
import lu.uni.distributedsystems.project.common.command.SetModeCommand;


/*
 * This is the implementation for the command line interface set_mode |Gambler ID| |ServiceMode|.
 *  
 * The SetModeOfGamblerCommand extends the SetModeCommand.
 * 
 * To know more about the Set mode functionality refer to the Getting Started Document.
 */
public class SetModeOfGamblerCommand extends SetModeCommand {
	
	private Bookie bookie;

	/* 	The constructor calls its parent constructor that registers the String, by which the command can be called
	 *	from the console, and itself with the commandProcessor.
	 */
	public SetModeOfGamblerCommand(CommandProcessor commandProcessor, Bookie bookie) {
		super(commandProcessor);
		this.bookie = bookie;
	}
	
	/*
	 * This method is called from the commandProcessor when he reads the commands string from the Console (entered by the User).
	 * The process splits the needed arguments and calls the corresponding method in the Bookie Class. 
	 */
	@Override
	public void processSetMode(String partnerID, ServiceMode serviceMode) {
		try {
			bookie.setModeOfGambler(partnerID, serviceMode);
		} catch (UnkownGamblerException e) {
			e.getMessage();
		}
	}

	// This method is invoked by the Help Command to display a user guide for this command.
	@Override
	public void showHelp() {
		System.out.println("set_mode [gambler-id] [service-mode] — sets mode of gambler");
		System.out.println("    Determines how the gambler with the specified gambler-id handles subsequent requests.");
		System.out.println("    possible service-modes are: RELIABLE (0), DISCONNECT_BEFORE_PROCESSING (1), DISCONNECT_BEFORE_REPLY (2), and RANDOM (3)");
		System.out.println("    the service-mode can be specified either by name or by ordinal");
	}

}
