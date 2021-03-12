//version 8:
import java.lang.annotation.*;

@lombok.EqualsAndHashCode
class EqualsAndHashCodeAnnotated {
	@Annotated int primitive;
	@Annotated Object object;
	
	int @Annotated [] primitiveValues;
	int @Annotated [] @Annotated [] morePrimitiveValues;
	
	Integer @Annotated [] objectValues;
	Integer @Annotated [] @Annotated [] moreObjectValues;
	
	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.SOURCE)
	@interface Annotated {
	}
}
