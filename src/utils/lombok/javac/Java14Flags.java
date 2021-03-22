package lombok.javac;

public class Java14Flags {
	private Java14Flags() { }

	/**
	 * Flag to indicate that a class is a record. The flag is also used to mark fields that are
	 * part of the state vector of a record and to mark the canonical constructor
	 */
	public static final long RECORD = 1L<<61; // ClassSymbols, MethodSymbols and VarSymbols

	/**
	 * Flag to mark a record constructor as a compact one
	 */
	public static final long COMPACT_RECORD_CONSTRUCTOR = 1L<<51; // MethodSymbols only

	/**
	 * Flag to mark a record field that was not initialized in the compact constructor
	 */
	public static final long UNINITIALIZED_FIELD= 1L<<51; // VarSymbols only

	/** Flag is set for compiler-generated record members, it could be appplied to
	 *  accessors and fields
	 */
	public static final int GENERATED_MEMBER = 1<<24; // MethodSymbols and VarSymbols
}
