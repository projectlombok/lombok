/**
 *
 */
package lombok.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.core.AST.Kind;
import lombok.core.LombokNode;

/**
 * @author Alexandru Bledea
 * @since Oct 5, 2014
 */
public final class LombokGeneratorHelper {

	private final ImplementationDetails details;

	/**
	 * @param details
	 *            the implementation details for this class
	 */
	public LombokGeneratorHelper(final ImplementationDetails details) {
		this.details = details;
	}

	/**
	 * Given a list of field names and a node referring to a type, finds each name in the list that does not match a field within the type.
	 */
	public List<Integer> createListOfNonExistentFields(final List<String> list, final LombokNode<?, ?, ?> type,
			final boolean excludeStandard, final boolean excludeTransient) {
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		final boolean[] matched = new boolean[list.size()];

		for (final LombokNode<?, ?, ?> child : type.down()) {
			if (child.getKind() != Kind.FIELD) {
				continue;
			}
			if (excludeStandard) {
				if (details.isStandard(child)) {
					continue;
				}
				if (child.getName().startsWith("$")) {
					continue;
				}
			}
			if (excludeTransient && details.isTransient(child)) {
				continue;
			}
			final int idx = list.indexOf(child.getName());
			if (idx > -1) {
				matched[idx] = true;
			}
		}

		final List<Integer> problematic = new ArrayList<Integer>();
		for (int i = 0 ; i < list.size() ; i++) {
			if (!matched[i]) {
				problematic.add(i);
			}
		}
		return problematic;
	}

	/**
	 * @param whichOne
	 * @param where
	 */
	public static boolean isFlagSet(final int whichOne, final long where) {
		return (where & whichOne) != 0;
	}

	/**
	 * Implementation specific class for code generators
	 *
	 * @author Alexandru Bledea
	 * @since Oct 5, 2014
	 */
	public static interface ImplementationDetails {

		/**
		 * @param node
		 */
		boolean isTransient(LombokNode<?, ?, ?> node);

		/**
		 * @param child
		 * @return
		 */
		boolean isStandard(LombokNode<?, ?, ?> child);

	}

}
