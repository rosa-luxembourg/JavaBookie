package lu.uni.distributedsystems.project.common.command;

/*
 * This is the implementation for the command line interface of the exit command.
 * 
 * Every command extends the class Command that is used by the commandProcessor.
 * 
 */
public class ExitCommand extends Command {

	public ExitCommand(CommandProcessor commandProcessor) {
		super(commandProcessor, "exit");
	}

	@Override
	public void process(String[] args) {
		System.out.println("Exiting ...");
		// tell associated CommandProcessor to exit
		getCommandProcessor().exit();
	}

	@Override
	public void showHelp() {
		System.out.println("exit — terminate the application");
	}

}
