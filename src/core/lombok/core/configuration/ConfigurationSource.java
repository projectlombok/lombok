/*
 * Copyright (C) 2014-2020 The Project Lombok Authors.
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

public interface ConfigurationSource {
	
	Result resolve(ConfigurationKey<?> key);
	List<ConfigurationFile> imports();
	
	public static final class Result {
		private final Object value;
		private final boolean authoritative;
		
		public Result(Object value, boolean authoritative) {
			this.value = value;
			this.authoritative = authoritative;
		}
		
		public Object getValue() {
			return value;
		}
		
		public boolean isAuthoritative() {
			return authoritative;
		}
		
		@Override public String toString() {
			return String.valueOf(value) + (authoritative ? " (set)" : " (delta)");
		}
	}
	
	public static final class ListModification {
		private final Object value;
		private final boolean added;
		
		public ListModification(Object value, boolean added) {
			this.value = value;
			this.added = added;
		}
		
		public Object getValue() {
			return value;
		}
		
		public boolean isAdded() {
			return added;
		}
	}
}