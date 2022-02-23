//CONF: lombok.addNullAnnotations = javax
import javax.annotation.ParametersAreNonnullByDefault;
@lombok.EqualsAndHashCode
@ParametersAreNonnullByDefault
class EqualsAndHashCodeWithNonNullByDefault {
	int x;
	boolean[] y;
	Object[] z;
	String a;
	String b;
}