/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.file;

import javax.tools.JavaFileObject;

public abstract class BaseFileObject implements JavaFileObject {
	protected BaseFileObject(JavacFileManager fileManager) {}
}
