import lombok.AccessLevel;
import lombok.experimental.StandardException;
@StandardException class EmptyException extends Exception {
  public @java.lang.SuppressWarnings("all") @lombok.Generated EmptyException() {
    this((java.lang.String) null, (java.lang.Throwable) null);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated EmptyException(final java.lang.String message) {
    this(message, (java.lang.Throwable) null);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated EmptyException(final java.lang.Throwable cause) {
    this(((cause != null) ? cause.getMessage() : null), cause);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated EmptyException(final java.lang.String message, final java.lang.Throwable cause) {
    super(message);
    if ((cause != null))
        super.initCause(cause);
  }
}
@StandardException(access = AccessLevel.PROTECTED) class NoArgsException extends Exception {
  public NoArgsException() {
    super();
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated NoArgsException(final java.lang.String message) {
    this(message, (java.lang.Throwable) null);
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated NoArgsException(final java.lang.Throwable cause) {
    this(((cause != null) ? cause.getMessage() : null), cause);
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated NoArgsException(final java.lang.String message, final java.lang.Throwable cause) {
    super(message);
    if ((cause != null))
        super.initCause(cause);
  }
}
