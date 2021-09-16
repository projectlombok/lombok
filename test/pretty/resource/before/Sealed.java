// version 15:
public class Sealed {
	public abstract sealed class Parent permits Child1, Child2 {
	}

	public final class Child1 extends Parent {
	}

	public abstract non-sealed class Child2 extends Parent {
	}

	public sealed interface SealedInterface permits ChildInterface1 {
	}

	public non-sealed interface ChildInterface1 extends SealedInterface {
	}
}