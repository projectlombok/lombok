import lombok.Setter;
import lombok.experimental.Accessors;
class SetterAccessorsPropertyNameConstantPlain {
	@Setter
	@Accessors( propertyNameConstant=true )
	int foo;
}