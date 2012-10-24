package lombok;

public final class CompilerMessage {
	/** Line Number (starting at 1) */
	final long line;
	
	/** Position is either column number, OR position in file starting from the first byte. */
	final long position;
	final boolean isError;
	final String message;
	
	public CompilerMessage(long line, long position, boolean isError, String message) {
		this.line = line;
		this.position = position;
		this.isError = isError;
		this.message = message;
	}
	
	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isError ? 1231 : 1237);
		result = prime * result + (int) (line ^ (line >>> 32));
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (int) (position ^ (position >>> 32));
		return result;
	}
	
	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CompilerMessage other = (CompilerMessage) obj;
		if (isError != other.isError) return false;
		if (line != other.line) return false;
		if (message == null) {
			if (other.message != null) return false;
		} else if (!message.equals(other.message)) return false;
		if (position != other.position) return false;
		return true;
	}
	
	public CompilerMessageMatcher asCompilerMessageMatcher() {
		return new CompilerMessageMatcher(line, position, message);
	}
	
	@Override public String toString() {
		return String.format("%d:%d %s %s", line, position, isError ? "ERROR" : "WARNING", message);
	}
}
