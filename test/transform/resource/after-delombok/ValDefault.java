// version 8:
interface ValDefault {
	int size();
	default void method() {
		final int x = 1;
		final int size = size();
	}
}
