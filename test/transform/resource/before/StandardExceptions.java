import lombok.experimental.StandardException;

@StandardException class EmptyException extends Exception {
}
@StandardException class NoArgsException extends Exception {
	public NoArgsException() {
	}
}
