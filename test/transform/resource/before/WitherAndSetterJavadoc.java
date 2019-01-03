import lombok.experimental.Wither;
class WitherAndSetterJavadoc {
	/**
	 * Some value.
	 * @param the new value
	 */
	@lombok.Setter @lombok.experimental.Wither int i;

	/**
	 * Some other value.
	 *
	 * --- SETTER ---
	 * Set some other value.
	 * @param the new other value
	 *
	 * --- WITHER ---
	 * Reinstantiate with some other value.
	 * @param the new other value
	 */
	@lombok.Setter @lombok.experimental.Wither int j;

	WitherAndSetterJavadoc(int i, int j) {
		this.i = i;
		this.j = j;
	}
}
