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
package lombok.core;

import java.lang.reflect.Type;

public class LombokConfiguration {
	
	private LombokConfiguration() {
		// prevent instantiation
	}
	
	/*
	 * Typical usage: use this as a supertypetoken.
	 */
	public abstract static class ConfigurationKey<T> {
		private final String keyName;
		
		public ConfigurationKey(String keyName) {
			this.keyName = keyName;
			System.out.println("registering " + keyName);
		}
	}
	
	@SuppressWarnings("unchecked") 
	static <T> T read(ConfigurationKey<T> key, AST<?, ?, ?> ast) {
		Type it = key.getClass().getGenericSuperclass();
		if (key.keyName.equals("lombok.log.varName")) return (T)"loggertje";
		if (key.keyName.equals("lombok.log.static")) return (T)Boolean.FALSE;
		return null;
	}
}
