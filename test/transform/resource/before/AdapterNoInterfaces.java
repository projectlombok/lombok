//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
import lombok.experimental.Adapter;

public class AdapterNoInterfaces {

    @Adapter
    public static class AdapterClass {

		public String getStatus() {
	    	return "TestStatus";
		}
    }

}