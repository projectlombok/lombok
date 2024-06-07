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