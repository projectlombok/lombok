// version 8:
interface DefaultMethod {
	int size();
	
	default boolean isEmpty() {
		return size() == 0;
	}
	
	default strictfp void run() {
	}
}