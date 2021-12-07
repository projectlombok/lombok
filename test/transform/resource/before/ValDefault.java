// version 8:9
interface ValDefault {
	int size();
	
	default void method() {
		lombok.val x = 1;
		lombok.val size = size();
	}
}