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
	 */
	private int someBeanField;
	/**
	 * This will remain on the field
	 * @param foo is not removed when there's a GETTER section
	 * @return is not removed when there's a GETTER section
	 */
	private int someGetterField;
	/**
	 * This will remain on the field
	 * @param foo is not removed when there's a SETTER section
	 * @return is not removed when there's a SETTER section
	 */
	private int someSetterField;
	/**
	 * This will remain on the field
	 * @param foo is not removed
	 * @return is not removed
	 */
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
	/**
	 * Doc on field
	 * <pre>
	 * 	// code
	 * </pre>
	 *
	 * @return the value of the field
	 */
	@java.lang.SuppressWarnings("all")
	public int getSomeBeanField() {
		return this.someBeanField;
	}
	/**
	 * Doc on field
	 * <pre>
	 * 	// code
	 * </pre>
	 * @param someBeanField the new value for the field
	 */
	@java.lang.SuppressWarnings("all")
	public void setSomeBeanField(final int someBeanField) {
		this.someBeanField = someBeanField;
	}
	/**
	 * This will be moved to the getter
	 * @param foo is not removed from a GETTER section
	 * @return is not removed from a GETTER section
	 */
	@java.lang.SuppressWarnings("all")
	public int getSomeGetterField() {
		return this.someGetterField;
	}
	/**
	 * This will be moved to the setter
	 * @param foo is not removed from a SETTER section
	 * @return is not removed from a SETTER section
	 */
	@java.lang.SuppressWarnings("all")
	public void setSomeSetterField(final int someSetterField) {
		this.someSetterField = someSetterField;
	}
	/**
	 * This will be moved to the getter
	 * @param foo is not removed from a GETTER section
	 * @return is not removed from a GETTER section
	 */
	@java.lang.SuppressWarnings("all")
	public int getSomeGetterSetterField() {
		return this.someGetterSetterField;
	}
	/**
	 * This will be moved to the setter
	 * @param foo is not removed from a SETTER section
	 * @return is not removed from a SETTER section
	 */
	@java.lang.SuppressWarnings("all")
	public void setSomeGetterSetterField(final int someGetterSetterField) {
		this.someGetterSetterField = someGetterSetterField;
	}
}
