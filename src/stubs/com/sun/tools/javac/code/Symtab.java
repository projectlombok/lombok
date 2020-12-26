/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.code;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.ModuleSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.util.Context;

public class Symtab {
	// Shared by JDK6-9
	public ClassSymbol methodClass;
	public Type iterableType;
	public Type objectType;
	public static Symtab instance(Context context) {return null;}
	public Type unknownType;
	public TypeSymbol noSymbol;
	public Type stringType;
	public Type throwableType;

	// JDK 9
	public ModuleSymbol unnamedModule;
}
