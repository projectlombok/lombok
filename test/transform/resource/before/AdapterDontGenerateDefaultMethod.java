import lombok.experimental.Adapter;

public class AdapterDontGenerateDefaultMethod {

    @Adapter
    public static class AdapterClass implements FooInterface {

		@Override
		public String getStatus() {
	    	return "TestStatus";
		}
    }

    public static interface FooInterface {
		void doStuff();
	
		default int countMyObjects(String userName) throws IllegalArgumentException { return 1; }
	
		String getStatus() throws IllegalStateException;
    }
}