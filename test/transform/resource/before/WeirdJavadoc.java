import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class WeirdJavadoc {
	// Comment
	/* Weird comment /** */
	/**
	 * This is the real comment
	 * @param test Copy this
	 */
	/* Weird comment /** */
	@Builder
	WeirdJavadoc(String test) {
		
	}
	
	// Comment
	/* Weird comment /** */
	/**
	 * This is the real comment
	 * @param test Copy this
	 */
	/* Weird comment /** */
	@Getter
	@Setter
	private String test;
}
