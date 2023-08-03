import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@lombok.ToString
public class ToStringArray {
	int[] primitiveArray;
	
	Object[] objectArray;
}