package com.sun.tools.javac.util;

import java.util.Set;

import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.main.OptionName;
import com.sun.tools.javac.main.JavacOption;

public class Options {
	public Options(Context context) {}
	public static final Context.Key<Options> optionsKey = new Context.Key<Options>();
	public static Options instance(Context context) { return null; }
	public String get(String key) { return null; }
	public String get(Option opt) { return null; }
	public String get(OptionName name) { return null; }
	public String get(JavacOption.Option opt) { return null; }
	public void putAll(Options o) {}
	public void put(String key, String value) {}
	public Set<String> keySet() { return null; }
}
