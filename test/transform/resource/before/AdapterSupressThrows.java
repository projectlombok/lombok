import lombok.experimental.Adapter;

public class AdapterSupressThrows {

    @Adapter(suppressThrows = true)
    public static class AdapterClass implements FooInterface {

		@Override
		public String getStatus() {
	    	return "TestStatus";
		}
    }

    public static interface FooInterface {
		void doStuff();
	
		int countMyObjects(String userName) throws IllegalArgumentException;
	
		String getStatus() throws IllegalStateException;
    }
}