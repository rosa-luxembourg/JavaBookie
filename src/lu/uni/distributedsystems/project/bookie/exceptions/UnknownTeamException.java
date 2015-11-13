package lu.uni.distributedsystems.project.bookie.exceptions;

public class UnknownTeamException extends Exception {

	private static final long serialVersionUID = -536503106225532664L;

	public UnknownTeamException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownTeamException(String message) {
		super(message);
	}
}
