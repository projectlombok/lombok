//platform !ecj: Javadoc copying not supported on ecj
import lombok.Setter;
class SetterDeprecated {
	
	@Deprecated
	@Setter int annotation;
	
	/**
	 * @deprecated
	 */
	@Setter int javadoc;
}
