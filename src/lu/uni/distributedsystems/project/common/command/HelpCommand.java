package lu.uni.distributedsystems.project.common.command;


/*
 * This is the implementation for the command line interface of the Help command
 * 
 * Every command extends the class Command that is used by the commandProcessor.
 * 
 */
public class HelpCommand extends Command {

	/* 	The constructor calls its parent constructor that registers the String, by which the command can be called
	 *	from the console, and itself with the commandProcessor.
	 */
	public HelpCommand(CommandProcessor commandProcessor) {
		super(commandProcessor, "help");
	}

	
	/*
	 * This method is called from the commandProcessor when he reads the commands string from the Console (entered by the User).
	 * The process invokes the printing of all Help information from all Commands registered with the CommandProcessor
	 */
	@Override
	public void process(String[] args) {
		System.out.println("The following commands are available:");
		getCommandProcessor().showHelp();
	}

	// This method is invoked by the Help Command to display a user guide for this command.
	@Override
	public void showHelp() {
		System.out.println("help - print this list of available commands");
	}

}
