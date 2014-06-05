import lombok.Setter;
import lombok.experimental.Accessors;
class SetterAccessorsPropertyNameConstantPlain {
	@Setter
	@Accessors( propertyNameConstant=true )
	int foo;

	@Setter
	@Accessors( propertyNameConstant=true )
	String camelCase;
}