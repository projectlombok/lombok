/*
 * Copyright (C) 2014 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.core.AST.Kind;
import lombok.core.LombokNode;

/**
 * This class is intended to hold the implementation-independent code generation code
 *
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
