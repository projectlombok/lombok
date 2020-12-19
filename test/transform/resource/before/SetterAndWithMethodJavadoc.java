import lombok.With;
class SetterAndWithMethodJavadoc {
	/**
	 * Some value.
	 * @param the new value
	 */
	@lombok.Setter @lombok.With int i;

	/**
	 * Some other value.
	 *
	 * --- SETTER ---
	 * Set some other value.
	 * @param the new other value
	 *
	 * --- WITH ---
	 * Reinstantiate with some other value.
	 * @param the other new other value
	 */
	@lombok.Setter @lombok.With int j;

	SetterAndWithMethodJavadoc(int i, int j) {
		this.i = i;
		this.j = j;
	}
}
