abstract class DelegateOnMethods {
	public abstract Bar getBar();
	public static interface Bar {
		void bar(java.util.ArrayList<java.lang.String> list);
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void bar(final java.util.ArrayList<java.lang.String> list) {
		this.getBar().bar(list);
	}
}
