package lu.uni.distributedsystems.project.bookie.exceptions;

public class InvalidTeamNameException extends Exception {

	private static final long serialVersionUID = 5308939530322117248L;

	public InvalidTeamNameException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTeamNameException(String message) {
		super(message);
	}
}
