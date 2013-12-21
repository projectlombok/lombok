/*
 * Copyright (C) 2013 The Project Lombok Authors.
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
package lombok.core.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringResolver implements ConfigurationResolver {
	
	private static final Pattern LINE = Pattern.compile("(?:clear\\s+([^=]+))|(?:(\\S*?)\\s*([-+]?=)\\s*(.*?))");
	
	private final Map<String, Result> values = new TreeMap<String, Result>(String.CASE_INSENSITIVE_ORDER);
	
	private static class Result {
		Object value;
		boolean owned;
		
		public Result(Object value, boolean owned) {
			this.value = value;
			this.owned = owned;
		}
		
		@Override public String toString() {
			return String.valueOf(value) + (owned ? " (set)" : " (delta)");
		}
	}
	
	public StringResolver(String content) {
		Map<String, ConfigurationDataType> registeredKeys = ConfigurationKey.registeredKeys();
		for (String line : content.trim().split("\\s*\\n\\s*")) {
			if (line.isEmpty() || line.startsWith("#")) continue;
			Matcher matcher = LINE.matcher(line);
			
			String operator = null;
			String keyName = null;
			String value;
			
			if (matcher.matches()) {
				if (matcher.group(1) == null) {
					keyName = matcher.group(2);
					operator = matcher.group(3);
					value = matcher.group(4);
				} else {
					keyName = matcher.group(1);
					operator = "clear";
					value = null;
				}
				ConfigurationDataType type = registeredKeys.get(keyName);
				if (type == null) {
					System.out.println("Unknown key " + keyName);
				} else {
					boolean listOperator = operator.equals("+=") || operator.equals("-=");
					if (listOperator && !type.isList()) {
						System.out.println(keyName + " is not a list");
					} else if (operator.equals("=") && type.isList()) {
						System.out.println(keyName + " IS a list");
					} else {
						processResult(keyName, operator, value, type);
					}
				}
			} else {
				System.out.println("no match:" + line);
			}
		}
		for (Result r : values.values()) {
			if (r.value instanceof List<?>) {
				r.value = Collections.unmodifiableList(((List<?>) r.value));
			}
		}
	}
	
	private void processResult(String keyName, String operator, String value, ConfigurationDataType type) {
		Object element = null;
		if (value != null) try {
			element = type.getParser().parse(value);
		}
		catch (Exception e) {
			// log the wrong value
			return;
		}
		
		if (operator.equals("clear") || operator.equals("=")) {
			if (element == null && type.isList()) {
				element = new ArrayList<Object>();
			}
			values.put(keyName, new Result(element, true));
		} else {
			Result result = values.get(keyName);
			@SuppressWarnings("unchecked")
			List<Object> list = result == null ? new ArrayList<Object>() : (List<Object>) result.value;
			if (result == null) values.put(keyName, new Result(list, false));
			list.remove(element);
			if (operator.equals("+=")) list.add(element);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override public <T> T resolve(ConfigurationKey<T> key) {
		Result result = values.get(key.getKeyName());
		T value = result == null ? null: (T)result.value;
		if (value == null && key.getType().isList()) {
			value = (T)Collections.emptyList();
		}
		return value;
	}
	
	enum Flag {
		WARNING, ERROR
	}
	
	private static final ConfigurationKey<String> AAP = new ConfigurationKey<String>("aap") {};
	private static final ConfigurationKey<List<String>> NOOT = new ConfigurationKey<List<String>>("noot") {};
	private static final ConfigurationKey<Integer> MIES = new ConfigurationKey<Integer>("mies") {};
	private static final ConfigurationKey<Boolean> WIM = new ConfigurationKey<Boolean>("wim") {};
	private static final ConfigurationKey<TypeName> ZUS = new ConfigurationKey<TypeName>("zus") {};
	private static final ConfigurationKey<Flag> JET = new ConfigurationKey<Flag>("jet") {};

	public static void main(String[] args) {
		print("aap=text\nnoot+=first\nmies=5\nwim=true\nzus=foo.bar.Baz\njet=error");
		print("noot+=first\nnoot+=second\n");
		print("clear noot");
		print("noot+=before-clear\nclear noot\nnoot+=first\nnoot+=second\nnoot+=third\nnoot+=first\nnoot-=second\n");
	}

	private static void print(String content) {
		StringResolver resolver = new StringResolver(content);
		System.out.println("\n\n================================================");
		System.out.println(content);
		System.out.println("================================================\n");
		System.out.println(resolver.values);
		System.out.println("================================================\n");
		String aap = resolver.resolve(AAP);
		System.out.println("aap:  "+ aap);
		List<String> noot = resolver.resolve(NOOT);
		System.out.println("noot: "+ noot);
		Integer mies = resolver.resolve(MIES);
		System.out.println("mies: "+ mies);
		Boolean wim = resolver.resolve(WIM);
		System.out.println("wim:  "+ wim);
		TypeName zus = resolver.resolve(ZUS);
		System.out.println("zus:  "+ zus);
		Flag jet = resolver.resolve(JET);
		System.out.println("jet:  "+ jet);
	}
}
