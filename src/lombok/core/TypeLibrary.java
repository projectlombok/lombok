package lombok.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TypeLibrary {
	private final Map<String, Set<String>> simpleToQualifiedMap = new HashMap<String, Set<String>>();
	
	public void addType(String fullyQualifiedTypeName) {
		int idx = fullyQualifiedTypeName.lastIndexOf('.');
		if ( idx == -1 ) throw new IllegalArgumentException(
				"Only fully qualified types are allowed (and stuff in the default package is not palatable to us either!)");
		
		final String simpleName = fullyQualifiedTypeName.substring(idx +1);
		final String packageName = fullyQualifiedTypeName.substring(0, idx);
		
		if ( simpleToQualifiedMap.put(fullyQualifiedTypeName, Collections.singleton(fullyQualifiedTypeName)) != null ) return;
		
		addToMap(simpleName, fullyQualifiedTypeName);
		addToMap(packageName + ".*", fullyQualifiedTypeName);
	}
	
	private TypeLibrary addToMap(String keyName, String fullyQualifiedTypeName) {
		Set<String> existing = simpleToQualifiedMap.get(keyName);
		Set<String> set = (existing == null) ? new HashSet<String>() : new HashSet<String>(existing);
		set.add(fullyQualifiedTypeName);
		simpleToQualifiedMap.put(keyName, Collections.unmodifiableSet(set));
		return this;
	}
	
	public Collection<String> findCompatible(String typeReference) {
		Set<String> result = simpleToQualifiedMap.get(typeReference);
		return result == null ? Collections.<String>emptySet() : result;
	}
}
