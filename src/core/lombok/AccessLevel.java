/*
 * Copyright (C) 2009-2025 The Project Lombok Authors.
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
package lombok;

/**
 * Represents an AccessLevel. Used e.g. to specify the access level for generated methods and fields.
 */
public enum AccessLevel {
	/** Represents the {@code public} access level. */
	PUBLIC,
	
	/**
	 * Acts exactly like {@code PACKAGE} - the package private access level.
	 * @deprecated This value was created at a time when a module-level access keyword was planned as a way of being prepared for the future. But that's not the direction java went in; a 'module access level' is not likely to ever exist. This enum acts like {@code PACKAGE} in every way.
	 */
	@Deprecated MODULE,
	
	/** Represents the {@code protected} access level (any code in the same package as well as any subtype). */
	PROTECTED,
	
	/** Represents the default access level: package private. (any code in the same package). */
	PACKAGE,
	
	/** Represents the {@code private} access level. */
	PRIVATE,
	
	/** Represents not generating anything or the complete lack of a method. */
	NONE;
}
