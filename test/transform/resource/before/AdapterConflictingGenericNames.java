//platform !eclipse: Requires a 'full' eclipse with intialized workspace, and we don't (yet) have that set up properly in the test run.
import lombok.experimental.Adapter;

import java.util.List;

public class AdapterConflictingGenericNames<T> {

    @Adapter(silent = true)
    public static class AdapterClass implements FooInterface {

    }

    public static interface FooInterface {
	
		<T> List<T> getListOfType(Class<T> clazz);
		
    }
    
}