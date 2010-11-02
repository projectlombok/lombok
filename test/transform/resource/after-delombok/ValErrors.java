public class ValErrors {
	public void nullType() {
		final val a = null;
	}
	public void unresolvableExpression() {
		final val c = d;
	}
}