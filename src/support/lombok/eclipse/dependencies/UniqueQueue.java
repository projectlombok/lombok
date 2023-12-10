package lombok.eclipse.dependencies;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * A incomplete implementation of a queue that maintains a history of all queued items and ensures unique elements.
 * Elements cannot be added to the queue if they already exist in the history.
 */
public class UniqueQueue<T> extends ArrayDeque<T> {
	private Set<T> added = new HashSet<>();
	
	@Override
	public boolean add(T e) {
		if (added.add(e)) {
			return super.add(e);
		}
		return false;
	}
}
