/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.file;

import javax.tools.JavaFileManager;

import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.util.Context;

import java.nio.charset.Charset;
import java.util.Map;

public abstract class BaseFileManager implements JavaFileManager {
	protected BaseFileManager(Charset charset) {}
	public void setContext(Context context) {}
	public boolean handleOptions(Map<Option, String> deferredFileManagerOptions) { return false; }
}
