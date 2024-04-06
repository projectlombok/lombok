package pkg;

import lombok.Getter;
import lombok.Setter;

public class Javadoc {
	/**
	 * Some text
	 *
	 * **SETTER**
	 * Setter section
	 * @param fieldName Hello, World3
	 * **GETTER**
	 * Getter section
	 * @return Sky is blue3
	 */
	@Getter
	@Setter
	private int field;
	
	public void usage() {
		getField();
		setField(0);
	}
}