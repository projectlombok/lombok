//version 8:
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@lombok.ToString
public class ToStringArrayTypeAnnotations {
	@TA int[] primitiveArray1;
	int @TA [] primitiveArray2;
	
	@TA Object[] objectArray1;
	Object @TA [] objectArray2;
	
	@Target({ElementType.TYPE_USE})
	@interface TA {
	}
}