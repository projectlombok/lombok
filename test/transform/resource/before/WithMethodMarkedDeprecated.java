//platform !ecj: Javadoc copying not supported on ecj
import lombok.With;

class WithMethodMarkedDeprecated {
	
	@Deprecated
	@With int annotation;
	
	/**
	 * @deprecated
	 */
	@With int javadoc;
	
	WithMethodMarkedDeprecated(int annotation, int javadoc) {
	}
}