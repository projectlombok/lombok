import lombok.experimental.Adapter;

public class AdapterNoInterfaces {

    @Adapter
    public static class AdapterClass {

		public String getStatus() {
	    	return "TestStatus";
		}
    }

}