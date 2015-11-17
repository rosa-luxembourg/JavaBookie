package lu.uni.distributedsystems.project.bookie.exceptions;

public class AlreadyClosedGameException extends Exception {

	private static final long serialVersionUID = -4947933298155423565L;
	
	public AlreadyClosedGameException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyClosedGameException(String message) {
		super(message);
	}

}
