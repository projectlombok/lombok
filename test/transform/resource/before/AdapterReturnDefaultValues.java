import lombok.experimental.Adapter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdapterReturnDefaultValues {

    @Adapter(silent = true)
    public static class AdapterClass implements FooInterface {

    }

    public static interface FooInterface {
	
		int getPrimitiveInt();
		byte getPrimitiveByte();
		char getPrimitiveChar();
		double getPrimitiveDouble();
		short getPrimitiveShort();
		long getPrimitiveLong();
		float getPrimitiveFloat();
		boolean getPrimitiveBoolean();

		Integer getInt();
		Byte getByte();
		Character getChar();
		Double getDouble();
		Short getShort();
		Long getLong();
		Float getFloat();
		Boolean getBoolean();

		String[] getArray();

		Collection<String> getCollection();
		List<String> getList();
		Set<String> getSet();
		Map<String, Integer> getMap();
		SortedSet<String> getSortedSet();
		SortedMap<String, Integer> getSortedMap();
		Optional<String> getOptional();
		List getNonGenericList();
		ArrayList<String> getConcreteList();
		ConcurrentHashMap<String, Integer> getConcreteMap();
		Map<String, List<String>> getNestedCollection();

		NavigableSet<String> getNavigableSet();
		NavigableMap<String, Integer> getNavigableMap();

		Iterator<String> getIterator();
		ListIterator<String> getListIterator();

		String getStatusString();
		CompoundValue getCompoundValue();
		FooInterface fluentMethod(String input);

		void doNothing();
    }
    
    public static class CompoundValue {
    	private String string;
    	private Integer number;
    }
}