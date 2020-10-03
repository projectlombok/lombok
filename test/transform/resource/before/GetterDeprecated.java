//platform !ecj: Javadoc copying not supported on ecj
import lombok.Getter;
class GetterDeprecated {
	
	@Deprecated
	@Getter int annotation;
	
	/**
	 * @deprecated
	 */
	@Getter int javadoc;
}
