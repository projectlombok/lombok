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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringResolver implements ConfigurationResolver {
	
	private static final Pattern LINE = Pattern.compile("(?:clear\\s+([^=]+))|(?:(\\S*?)\\s*([-+]?=)\\s*(.*?))");
	
	public StringResolver(String content) {
		Map<String, ConfigurationDataType> registeredKeys = ConfigurationKey.registeredKeys();
		for (String line : content.trim().split("\\s*\\n\\s*")) {
			if (line.isEmpty() || line.startsWith("#")) continue;
			Matcher matcher = LINE.matcher(line);
			System.out.println("\nLINE: " + line);
			
			String operator = null;
			String keyName = null;
			String value = null;
			
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
						System.out.printf("!! %s %s %s", keyName, operator, value);
					}
				}
			} else {
				System.out.println("no match:" + line);
			}
		}
	}
	
	@Override public <T> T resolve(ConfigurationKey<T> key) {
		return null;
	}
	
	public static void main(String[] args) {
		ConfigurationKey<String> AAP = new ConfigurationKey<String>("aap") {};
		ConfigurationKey<List<String>> NOOT = new ConfigurationKey<List<String>>("noot") {};
		
		ConfigurationResolver resolver = new StringResolver(" aap = 3 \naap += 4\n\r noot+=mies\nnoot=wim\nclear noot\nclear aap\r\n#foo-= bar\nblablabla\na=b=c\n\n\nclear  test\n\nclear  \nclear test=");
		String aapValue = resolver.resolve(AAP);
	}
}
