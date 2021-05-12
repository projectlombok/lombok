public class ValAnonymousSubclassSelfReference {
	public void test() {
		int i = 0;
		final int j = 1;
		final int k = 2;
		new ValAnonymousSubclassSelfReference() {
		};
	}
}