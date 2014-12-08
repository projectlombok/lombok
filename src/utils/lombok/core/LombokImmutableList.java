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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class LombokImmutableList<T> implements Iterable<T> {
	private Object[] content;
	private static final LombokImmutableList<?> EMPTY = new LombokImmutableList<Object>(new Object[0]);
	
	@SuppressWarnings("unchecked")
	public static <T> LombokImmutableList<T> of() {
		return (LombokImmutableList<T>) EMPTY;
	}
	
	public static <T> LombokImmutableList<T> of(T a) {
		return new LombokImmutableList<T>(new Object[] {a});
	}
	
	public static <T> LombokImmutableList<T> of(T a, T b) {
		return new LombokImmutableList<T>(new Object[] {a, b});
	}
	
	public static <T> LombokImmutableList<T> of(T a, T b, T c) {
		return new LombokImmutableList<T>(new Object[] {a, b, c});
	}
	
	public static <T> LombokImmutableList<T> of(T a, T b, T c, T d) {
		return new LombokImmutableList<T>(new Object[] {a, b, c, d});
	}
	
	public static <T> LombokImmutableList<T> of(T a, T b, T c, T d, T e) {
		return new LombokImmutableList<T>(new Object[] {a, b, c, d, e});
	}
	
	public static <T> LombokImmutableList<T> of(T a, T b, T c, T d, T e, T f, T... g) {
		Object[] rest = g == null ? new Object[] {null} : g;
		Object[] val = new Object[rest.length + 6];
		System.arraycopy(rest, 0, val, 6, rest.length);
		val[0] = a;
		val[1] = b;
		val[2] = c;
		val[3] = d;
		val[4] = e;
		val[5] = f;
		return new LombokImmutableList<T>(val);
	}
	
	public static <T> LombokImmutableList<T> copyOf(Collection<? extends T> list) {
		return new LombokImmutableList<T>(list.toArray());
	}
	
	public static <T> LombokImmutableList<T> copyOf(Iterable<? extends T> iterable) {
		List<T> list = new ArrayList<T>();
		for (T o : iterable) list.add(o);
		return copyOf(list);
	}
	
	public static <T> LombokImmutableList<T> copyOf(T[] array) {
		Object[] content = new Object[array.length];
		System.arraycopy(array, 0, content, 0, array.length);
		return new LombokImmutableList<T>(content);
	}
	
	private LombokImmutableList(Object[] content) {
		this.content = content;
	}
	
	public LombokImmutableList<T> replaceElementAt(int idx, T newValue) {
		Object[] newContent = content.clone();
		newContent[idx] = newValue;
		return new LombokImmutableList<T>(newContent);
	}
	
	public LombokImmutableList<T> append(T newValue) {
		int len = content.length;
		Object[] newContent = new Object[len + 1];
		System.arraycopy(content, 0, newContent, 0, len);
		newContent[len] = newValue;
		return new LombokImmutableList<T>(newContent);
	}
	
	public LombokImmutableList<T> prepend(T newValue) {
		int len = content.length;
		Object[] newContent = new Object[len + 1];
		System.arraycopy(content, 0, newContent, 1, len);
		newContent[0] = newValue;
		return new LombokImmutableList<T>(newContent);
	}
	
	public int indexOf(T val) {
		int len = content.length;
		if (val == null) {
			for (int i = 0; i < len; i++) if (content[i] == null) return i;
			return -1;
		}
		
		for (int i = 0; i < len; i++) if (val.equals(content[i])) return i;
		return -1;
	}
	
	public LombokImmutableList<T> removeElement(T val) {
		int idx = indexOf(val);
		return idx == -1 ? this : removeElementAt(idx);
	}
	
	public LombokImmutableList<T> removeElementAt(int idx) {
		int len = content.length;
		Object[] newContent = new Object[len - 1];
		if (idx > 0) System.arraycopy(content, 0, newContent, 0, idx);
		if (idx < len - 1) System.arraycopy(content, idx + 1, newContent, idx, len - idx - 1);
		return new LombokImmutableList<T>(newContent);
	}
	
	public boolean isEmpty() {
		return content.length == 0;
	}
	
	public int size() {
		return content.length;
	}
	
	@SuppressWarnings("unchecked")
	public T get(int idx) {
		return (T) content[idx];
	}
	
	public boolean contains(T in) {
		if (in == null) {
			for (Object e : content) if (e == null) return true;
			return false;
		}
		
		for (Object e : content) if (in.equals(e)) return true;
		return false;
	}
	
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int idx = 0;
			@Override public boolean hasNext() {
				return idx < content.length;
			}
			
			@SuppressWarnings("unchecked")
			@Override public T next() {
				if (idx < content.length) return (T) content[idx++];
				throw new NoSuchElementException();
			}
			
			@Override public void remove() {
				throw new UnsupportedOperationException("List is immutable");
			}
		};
	}
	
	@Override public String toString() {
		return Arrays.toString(content);
	}
	
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof LombokImmutableList)) return false;
		if (obj == this) return true;
		return Arrays.equals(content, ((LombokImmutableList<?>) obj).content);
	}
	
	@Override public int hashCode() {
		return Arrays.hashCode(content);
	}
}
