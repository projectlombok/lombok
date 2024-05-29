//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
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