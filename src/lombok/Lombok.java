package lombok;

public class Lombok {
	/**
	 * Throws any throwable 'sneakily' - you don't need to catch it, nor declare that you throw it onwards.
	 * The exception is still thrown - javac will just stop whining about it.
	 * 
	 * Example usage:
	 * 
	 * <pre>public void run() {
	 *     throw sneakyThrow(new IOException("You don't need to catch me!"));
	 * }</pre>
	 * 
	 * NB: The exception is not wrapped, ignored, swallowed, or redefined. The JVM actually does not know or care
	 * about the concept of a 'checked exception'. All this method does is hide the act of throwing a checked exception
	 * from the java compiler.
	 * 
	 * @param t The throwable to throw without requiring you to catch its type.
	 * @return A dummy RuntimeException; this method never returns normally, it <em>always</em> throws an exception!
	 */
	public static RuntimeException sneakyThrow(Throwable t) {
		if ( t == null ) throw new NullPointerException("t");
		Lombok.<RuntimeException>sneakyThrow0(t);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T)t;
	}
}
