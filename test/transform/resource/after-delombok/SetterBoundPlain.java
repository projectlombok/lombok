import java.beans.PropertyChangeSupport;

class SetterBoundPlain {
	@java.lang.SuppressWarnings("all")
	public static final java.lang.String PROP_FOO = "foo";

	private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
	int foo;

	@java.lang.SuppressWarnings("all")
	public void setFoo(final int foo) {
		final int old = this.foo;
		this.foo = foo;
		this.propertySupport.firePropertyChange(PROP_FOO, old, this.foo);
	}
}