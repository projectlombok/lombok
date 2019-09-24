/*
 * Copyright (C) 2010-2019 The Project Lombok Authors.
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
package lombok.bytecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility to read the constant pool, header, and inheritance information of any class file.
 */
public class ClassFileMetaData {
	private static final byte UTF8 = 1;
	private static final byte INTEGER = 3;
	private static final byte FLOAT = 4;
	private static final byte LONG = 5;
	private static final byte DOUBLE = 6;
	private static final byte CLASS = 7;
	private static final byte STRING = 8;
	private static final byte FIELD = 9;
	private static final byte METHOD = 10;
	private static final byte INTERFACE_METHOD = 11;
	private static final byte NAME_TYPE = 12;
	// New in java7: support for methodhandles and invokedynamic
	private static final byte METHOD_HANDLE = 15;
	private static final byte METHOD_TYPE = 16;
	private static final byte DYNAMIC = 17;
	private static final byte INVOKE_DYNAMIC = 18;
	// New in java9: support for modules
	private static final byte MODULE = 19;
	private static final byte PACKAGE = 20;
	
	private static final int NOT_FOUND = -1;
	private static final int START_OF_CONSTANT_POOL = 8; 
	
	private final byte[] byteCode;
	
	private final int maxPoolSize; 
	private final int[] offsets;
	private final byte[] types;
	private final String[] utf8s;
	private final int endOfPool;
	
	public ClassFileMetaData(byte[] byteCode) {
		this.byteCode = byteCode;
		
		maxPoolSize = readValue(START_OF_CONSTANT_POOL);
		offsets = new int[maxPoolSize];
		types = new byte[maxPoolSize];
		utf8s = new String[maxPoolSize];
		int position = 10;
		for (int i = 1; i < maxPoolSize; i++) {
			byte type = byteCode[position];
			types[i] = type;
			position++;
			offsets[i] = position;
			switch (type) {
			case UTF8:
				int length = readValue(position);
				position += 2;
				utf8s[i] = decodeString(position, length);
				position += length;
				break;
			case CLASS:
			case STRING:
			case METHOD_TYPE:
			case MODULE:
			case PACKAGE:
				position += 2;
				break;
			case METHOD_HANDLE:
				position += 3;
				break;
			case INTEGER:
			case FLOAT:
			case FIELD:
			case METHOD:
			case INTERFACE_METHOD:
			case NAME_TYPE:
			case INVOKE_DYNAMIC:
			case DYNAMIC:
				position += 4;
				break;
			case LONG:
			case DOUBLE:
				position += 8;
				i++;
				break;
			case 0:
				break;
			default:
				throw new AssertionError("Unknown constant pool type " + type);
			}
		}
		endOfPool = position;
	}
	
	private String decodeString(int pos, int size) {
		int end = pos + size;
		
		// the resulting string might be smaller
		char[] result = new char[size];
		int length = 0;
		while (pos < end) {
			int first = (byteCode[pos++] & 0xFF);
			if (first < 0x80) {
				result[length++] = (char)first;
			} else if ((first & 0xE0) == 0xC0) {
				int x = (first & 0x1F) << 6;
				int y = (byteCode[pos++] & 0x3F);
				result[length++] = (char)(x | y);
			} else {
				int x = (first & 0x0F) << 12;
				int y = (byteCode[pos++] & 0x3F) << 6;
				int z = (byteCode[pos++] & 0x3F);
				result[length++] = (char)(x | y | z);
			}
		}
		return new String(result, 0, length);
	}
	
	/**
	 * Checks if the constant pool contains the provided 'raw' string. These are used as source material for further JVM types, such as string constants, type references, etcetera.
	 */
	public boolean containsUtf8(String value) {
		return findUtf8(value) != NOT_FOUND;
	}
	
	/**
	 * Checks if the constant pool contains a reference to the provided class.
	 * 
	 * NB: Most uses of a type do <em>NOT</em> show up as a class in the constant pool.
	 *    For example, the parameter types and return type of any method you invoke or declare, are stored as signatures and not as type references,
	 *    but the type to which any method you invoke belongs, is. Read the JVM Specification for more information.
	 * 
	 * @param className must be provided JVM-style, such as {@code java/lang/String}
	 */
	public boolean usesClass(String className) {
		return findClass(className) != NOT_FOUND;
	}
	
	/**
	 * Checks if the constant pool contains a reference to a given field, either for writing or reading.
	 * 
	 * @param className must be provided JVM-style, such as {@code java/lang/String}
	 */
	public boolean usesField(String className, String fieldName) {
		int classIndex = findClass(className);
		if (classIndex == NOT_FOUND) return false;
		int fieldNameIndex = findUtf8(fieldName);
		if (fieldNameIndex == NOT_FOUND) return false;
		
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == FIELD && readValue(offsets[i]) == classIndex) {
				int nameAndTypeIndex = readValue(offsets[i] + 2);
				if (readValue(offsets[nameAndTypeIndex]) == fieldNameIndex) return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the constant pool contains a reference to a given method, with any signature (return type and parameter types).
	 * 
	 * @param className must be provided JVM-style, such as {@code java/lang/String}
	 */
	public boolean usesMethod(String className, String methodName) {
		int classIndex = findClass(className);
		if (classIndex == NOT_FOUND) return false;
		int methodNameIndex = findUtf8(methodName);
		if (methodNameIndex == NOT_FOUND) return false;
		
		for (int i = 1; i < maxPoolSize; i++) {
			if (isMethod(i) && readValue(offsets[i]) == classIndex) {
				int nameAndTypeIndex = readValue(offsets[i] + 2);
				if (readValue(offsets[nameAndTypeIndex]) == methodNameIndex) return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the constant pool contains a reference to a given method.
	 * 
	 * @param className must be provided JVM-style, such as {@code java/lang/String}
	 * @param descriptor must be provided JVM-style, such as {@code (IZ)Ljava/lang/String;}
	 */
	public boolean usesMethod(String className, String methodName, String descriptor) {
		int classIndex = findClass(className);
		if (classIndex == NOT_FOUND) return false;
		int nameAndTypeIndex = findNameAndType(methodName, descriptor);
		if (nameAndTypeIndex == NOT_FOUND) return false;
		
		for (int i = 1; i < maxPoolSize; i++) {
			if (isMethod(i) && 
					readValue(offsets[i]) == classIndex && 
					readValue(offsets[i] + 2) == nameAndTypeIndex) return true;
		}
		return false;
	}
	
	/**
	 * Checks if the constant pool contains the provided string constant, which implies the constant is used somewhere in the code.
	 * 
	 * NB: String literals get concatenated by the compiler.
	 * NB2: This method does NOT do any kind of normalization.
	 */
	public boolean containsStringConstant(String value) {
		int index = findUtf8(value);
		if (index == NOT_FOUND) return false;
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == STRING && readValue(offsets[i]) == index) return true; 
		}
		return false;
	}
	
	/**
	 * Checks if the constant pool contains the provided long constant, which implies the constant is used somewhere in the code.
	 * 
	 * NB: compile-time constant expressions are evaluated at compile time.
	 */
	public boolean containsLong(long value) {
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == LONG && readLong(i) == value) return true;
		}
		return false;
	}
	
	/**
	 * Checks if the constant pool contains the provided double constant, which implies the constant is used somewhere in the code.
	 * 
	 * NB: compile-time constant expressions are evaluated at compile time.
	 */
	public boolean containsDouble(double value) {
		boolean isNan = Double.isNaN(value);
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == DOUBLE) {
				double d = readDouble(i);
				if (d == value || (isNan && Double.isNaN(d))) return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the constant pool contains the provided int constant, which implies the constant is used somewhere in the code.
	 * 
	 * NB: compile-time constant expressions are evaluated at compile time.
	 */
	public boolean containsInteger(int value) {
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == INTEGER && readInteger(i) == value) return true;
		}
		return false;
	}
	
	/**
	 * Checks if the constant pool contains the provided float constant, which implies the constant is used somewhere in the code.
	 * 
	 * NB: compile-time constant expressions are evaluated at compile time.
	 */
	public boolean containsFloat(float value) {
		boolean isNan = Float.isNaN(value);
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == FLOAT) {
				float f = readFloat(i);
				if (f == value || (isNan && Float.isNaN(f))) return true;
			}
		}
		return false;
	}
	
	private long readLong(int index) {
		int pos = offsets[index];
		return ((long)read32(pos)) << 32 | (read32(pos + 4) & 0x00000000FFFFFFFFL);
	}
	
	private double readDouble(int index) {
		return Double.longBitsToDouble(readLong(index));
	}
	
	private int readInteger(int index) {
		return read32(offsets[index]);
	}
	
	private float readFloat(int index) {
		return Float.intBitsToFloat(readInteger(index));
	}
	
	private int read32(int pos) {
		return (byteCode[pos] & 0xFF) << 24 | (byteCode[pos + 1] & 0xFF) << 16 | (byteCode[pos + 2] & 0xFF) << 8 | (byteCode[pos + 3] & 0xFF);
	}
	
	/**
	 * Returns the name of the class in JVM format, such as {@code java/lang/String}
	 */
	public String getClassName() {
		return getClassName(readValue(endOfPool + 2));
	}
	
	/**
	 * Returns the name of the superclass in JVM format, such as {@code java/lang/Object}
	 * 
	 * NB: If you try this on Object itself, you'll get {@code null}.<br />
	 * NB2: For interfaces and annotation interfaces, you'll always get {@code java/lang/Object}
	 */
	public String getSuperClassName() {
		return getClassName(readValue(endOfPool + 4));
	}
	
	/**
	 * Returns the name of all implemented interfaces.
	 */
	public List<String> getInterfaces() {
		int size = readValue(endOfPool + 6);
		if (size == 0) return Collections.emptyList();
		
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			result.add(getClassName(readValue(endOfPool + 8 + (i * 2))));
		}
		return result;
	}
	
	/**
	 * A {@code toString()} like utility to dump all contents of the constant pool into a string.
	 * 
	 * NB: No guarantees are made about the exact layout of this string. It is for informational purposes only, don't try to parse it.<br />
	 * NB2: After a double or long, there's a JVM spec-mandated gap, which is listed as {@code (cont.)} in the returned string.
	 */
	public String poolContent() {
		StringBuilder result = new StringBuilder();
		for (int i = 1; i < maxPoolSize; i++) {
			result.append(String.format("#%02x: ", i));
			int pos = offsets[i];
			switch(types[i]) {
			case UTF8:
				result.append("Utf8 ").append(utf8s[i]);
				break;
			case CLASS:
				result.append("Class ").append(getClassName(i));
				break;
			case STRING:
				result.append("String \"").append(utf8s[readValue(pos)]).append("\"");
				break;
			case INTEGER:
				result.append("int ").append(readInteger(i));
				break;
			case FLOAT:
				result.append("float ").append(readFloat(i));
				break;
			case FIELD:
				appendAccess(result.append("Field "), i);
				break;
			case METHOD:
			case INTERFACE_METHOD:
				appendAccess(result.append("Method "), i);
				break;
			case NAME_TYPE:
				appendNameAndType(result.append("Name&Type "), i);
				break;
			case LONG:
				result.append("long ").append(readLong(i));
				break;
			case DOUBLE:
				result.append("double ").append(readDouble(i));
				break;
			case METHOD_HANDLE:
				result.append("MethodHandle...");
				break;
			case METHOD_TYPE:
				result.append("MethodType...");
				break;
			case DYNAMIC:
				result.append("Dynamic...");
				break;
			case INVOKE_DYNAMIC:
				result.append("InvokeDynamic...");
				break;
			case 0:
				result.append("(cont.)");
				break;
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	private void appendAccess(StringBuilder result, int index) {
		int pos = offsets[index];
		result.append(getClassName(readValue(pos))).append(".");
		appendNameAndType(result, readValue(pos + 2));
	}
	
	private void appendNameAndType(StringBuilder result, int index) {
		int pos = offsets[index];
		result.append(utf8s[readValue(pos)]).append(":").append(utf8s[readValue(pos + 2)]);
	}
	
	private String getClassName(int classIndex) {
		if (classIndex < 1) return null;
		return utf8s[readValue(offsets[classIndex])];
	}
	
	private boolean isMethod(int i) {
		byte type = types[i];
		return type == METHOD || type == INTERFACE_METHOD;
	}
	
	private int findNameAndType(String name, String descriptor) {
		int nameIndex = findUtf8(name);
		if (nameIndex == NOT_FOUND) return NOT_FOUND;
		int descriptorIndex = findUtf8(descriptor);
		if (descriptorIndex == NOT_FOUND) return NOT_FOUND;
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == NAME_TYPE && 
					readValue(offsets[i]) == nameIndex && 
					readValue(offsets[i] + 2) == descriptorIndex) return i;
		}
		return NOT_FOUND;
	}
	
	private int findUtf8(String value) {
		for (int i = 1; i < maxPoolSize; i++) {
			if (value.equals(utf8s[i])) {
				return i;
			}
		}
		return NOT_FOUND;
	}
	
	private int findClass(String className) {
		int index = findUtf8(className);
		if (index == -1) return NOT_FOUND;
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == CLASS && readValue(offsets[i]) == index) return i;
		}
		return NOT_FOUND;
	}
	
	private int readValue(int position) {
		return ((byteCode[position] & 0xFF) << 8) | (byteCode[position + 1] & 0xFF);
	}
}
