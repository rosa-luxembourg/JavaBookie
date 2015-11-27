package lu.uni.distributedsystems.project.common.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * Reads command lines from the console and triggers execution
 * of the associated Command instance. Requires previous registration
 * of all possible Command objects. This first token entered on
 * a command line determines the command to be executed. The
 * remaining tokens will be passed as arguments to the Command's
 * process method.
 * A CommandProcessor is executed in the context of a separate Thread.
 */
public class CommandProcessor extends Thread {

	// directory of registered commands
	private Map<String, Command> commands;

	// the scanner that reads from the console
	private Scanner scanner;
	
	// process commands as long as running is true
	private boolean running;

	/**
	 * Create a CommandProcessor as a separate thread. Need to invoke
	 * the start method on a CommandProcessor to launch command processing.
	 * Registers two commands by default: exit and help.
	 */
	public CommandProcessor(Scanner scanner) {
		this.scanner = scanner;
		commands = new HashMap<String, Command>();
		running = true;
		//new ExitCommand(this);
		new HelpCommand(this);
	}
	
	/**
	 * Returns the associated scanner.
	 * @return associated scanner
	 */
	public Scanner getScanner() {
		return scanner;
	}
	
	/**
	 * Register a Command object.
	 * @param commandName	name used to invoke the Command
	 * @param command		Command to be invoked
	 */
	public void register(String commandName, Command command) {
		commands.put(commandName, command);
	}
	
	/**
	 * Finish command processing.
	 */
	public void exit() {
		running = false;
	}
	
	public void showHelp() {
		for (Command cmd : commands.values()) {
			cmd.showHelp();
		}
	}

	/**
	 * Main loop accepting and processing commands entered via the console
	 */
	@Override
	public void run() {
		while (running) {
			// read next command line to be executed from the console
			String commandLine = scanner.nextLine();
			Command command;

			// split the entire command into tokens, separated by whitespace
		    String[] tokens = commandLine.split("\\s+");
		    
		    // tokens[0] contains the command; try to find matching command
		    if ((command = commands.get(tokens[0])) != null) {
		    	// create arguments array by removing command token
		    	String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
		    	
		    	try {
			    	// process the command
			    	command.process(args);
		    	}
		    	catch (Exception ex) {
		    		System.err.println("Error executing the command. Maybe a syntax error? You might want to try:");
		    		command.showHelp();
		    	}
		    }
		    else
		    	System.err.println("unknown command: " + tokens[0] + ". You might find the 'help' command useful.");
		}
	}
	
}
