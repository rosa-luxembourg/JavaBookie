package lu.uni.distributedsystems.project.bookie.exceptions;

public class UnknownGameException extends Exception {

	private static final long serialVersionUID = -206321416257101113L;
	
	public UnknownGameException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownGameException(String message) {
		super(message);
	}

}
