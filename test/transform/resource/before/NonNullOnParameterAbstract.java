//version 8:
abstract class NonNullOnParameterAbstract {
	public void test(@lombok.NonNull String arg) {
		System.out.println("Hey");
	}
	
	public abstract void test2(@lombok.NonNull String arg);
}