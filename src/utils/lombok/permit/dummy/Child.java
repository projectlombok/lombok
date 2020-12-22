package lombok.permit.dummy;

@SuppressWarnings("all")
public abstract class Child extends Parent {
	private transient volatile boolean foo;
	private transient volatile Object[] bar;
	private transient volatile Object baz;
	
}
