package lu.uni.distributedsystems.project.bookie.exceptions;

public class UnkownGamblerException extends Exception {

	private static final long serialVersionUID = -8404928484295573543L;

	public UnkownGamblerException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnkownGamblerException(String message) {
		super(message);
	}

}
