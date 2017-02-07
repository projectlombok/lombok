import java.beans.PropertyChangeSupport;
import lombok.Setter;
import lombok.experimental.Accessors;
class SetterBoundPlain {
	private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	@Setter
	@Accessors( bound=true )
	int foo;
}