/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.comp;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

// JDK9+
public class ArgumentAttr extends JCTree.Visitor {
	public static ArgumentAttr instance(Context context) { return null; }
}