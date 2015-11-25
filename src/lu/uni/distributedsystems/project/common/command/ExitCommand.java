package lu.uni.distributedsystems.project.common.command;

import lu.uni.distributedsystems.project.bookie.Bookie;

/*
 * This is the implementation for the command line interface of the exit command.
 * 
 * Every command extends the class Command that is used by the commandProcessor.
 * 
 */
public class ExitCommand extends Command {

	public ExitCommand(CommandProcessor commandProcessor, Bookie b) {
		super(commandProcessor, "exit");
		this.b = b;
	}
	private Bookie b;

	@Override
	public void process(String[] args) {
		System.out.println("Exiting ...");
		// tell associated CommandProcessor to exit
		b.shutdown();
		getCommandProcessor().exit();
	}

	@Override
	public void showHelp() {
		System.out.println("exit — terminate the application");
	}

}
