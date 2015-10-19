package lu.uni.distributedsystems.project.common.command;

import lu.uni.distributedsystems.gsonrmi.server.ServiceMode;


/*
 * The SetModeCommand is implemented by the SetModeOfGamblerCommand.
 * 
 */
public abstract class SetModeCommand extends Command {

	public SetModeCommand(CommandProcessor commandProcessor) {
		super(commandProcessor, "set_mode");
	}

	@Override
	public void process(String[] args) {
		// extract arguments
		String bookieID = args[0];
		String serviceModeString = args[1];
		ServiceMode serviceMode = null;
		
		try {
			// Convert the argument that is provided from the Console to a matching ServiceMode
			serviceMode = ServiceMode.valueOf(args[1]);
		}
		catch (IllegalArgumentException ex) {
			// In case that the provided argument is an integer try to convert from an integer value
			try {
				int serviceModeOrdinal = Integer.parseInt(serviceModeString);
				
				serviceMode = ServiceMode.values()[serviceModeOrdinal];
			}
			catch (NumberFormatException ex2) {
				System.err.println("Wrong mode. Please try:");
				showHelp();
				return;
			}
		}
		processSetMode(bookieID, serviceMode);
	}
	
	public abstract void processSetMode(String partnerID, ServiceMode serviceMode);

}
