import java.util.Optional;

import lombok.experimental.Adapter;

public class AdapterOf {

    @Adapter(of = {FooInterface.class, GooInterface.class})
    public static abstract class AdapterClass implements FooInterface, GooInterface, HooInterface {

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

    public static interface GooInterface {
		Optional<String> gooMethod();
    }

    public static interface HooInterface {
    	void wontImplement();
    }
}