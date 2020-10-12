package com.sun.tools.javac.main;

import java.util.Map;

import com.sun.tools.javac.util.Context;

public class Arguments {
	public static final Context.Key<Arguments> argsKey = new Context.Key<Arguments>();
	public static Arguments instance(Context context) { return null; }
	public void init(String ownName, String... argv) {}
	public Map<Option, String> getDeferredFileManagerOptions() { return null; }
	public boolean validate() { return false; }
	
	// JDK15
	public void init(String ownName, Iterable<String> args) {}
}
