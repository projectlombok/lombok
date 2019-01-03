// unchanged
/**
 * Doc on package
 */
package testPackage;

/** Weird doc */
/**
 * Doc on class
 */
class JavadocGenerally {
	/**
	 * Doc on field
	 * <pre>
	 * 	// code
	 * </pre>
	 */
	private int someField;

	/**
	 * Doc on field
	 * <pre>
	 * 	// code
	 * </pre>
	 * @param someBeanField the new value for the field
	 * @return the value of the field
	 */
	@lombok.Getter
	@lombok.Setter
	private int someBeanField;

	/**
	 * This will remain on the field
	 * @param foo is not removed when there's a GETTER section
	 * @return is not removed when there's a GETTER section
	 *
	 * --- GETTER ---
	 * This will be moved to the getter
	 * @param foo is not removed from a GETTER section
	 * @return is not removed from a GETTER section
	 */
	@lombok.Getter
	private int someGetterField;

	/**
	 * This will remain on the field
	 * @param foo is not removed when there's a SETTER section
	 * @return is not removed when there's a SETTER section
	 *
	 * --- SETTER ---
	 * This will be moved to the setter
	 * @param foo is not removed from a SETTER section
	 * @return is not removed from a SETTER section
	 */
	@lombok.Setter
	private int someSetterField;

	/**
	 * This will remain on the field
	 * @param foo is not removed
	 * @return is not removed
	 *
	 * --- GETTER ---
	 * This will be moved to the getter
	 * @param foo is not removed from a GETTER section
	 * @return is not removed from a GETTER section
	 *
	 * --- SETTER ---
	 * This will be moved to the setter
	 * @param foo is not removed from a SETTER section
	 * @return is not removed from a SETTER section
	 */
	@lombok.Getter
	@lombok.Setter
	private int someGetterSetterField;

	/**
	 * Doc on method
	 */
	public void test() {
	}

	/**
	 * Doc on inner
	 */
	public interface TestingInner {
	}
}
