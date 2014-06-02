// version 8:
interface ValDefault {
	int size();
	
	default void method() {
		lombok.val x = 1;
		lombok.val size = size();
	}
}