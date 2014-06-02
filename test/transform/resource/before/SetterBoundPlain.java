import lombok.Setter;
import lombok.experimental.Accessors;
class SetterBoundPlain {
	@Setter
	@Accessors( propertyNameConstant=true )
	int foo;
}