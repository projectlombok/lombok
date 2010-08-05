/*
 * Copyright © 2010 Reinier Zwitserloot and Roel Spilker.
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
	private static final byte IMETHOD = 11;
	private static final byte NAME_TYPE = 12;
	
	private static final int NOT_FOUND = -1;
	
	private final byte[] byteCode;
	
	private final int maxPoolSize; 
	private final int[] offsets;
	private final byte[] types;
	private final String[] utf8s;
	private final int endOfPool;
	
	public ClassFileMetaData(byte[] byteCode) {
		this.byteCode = byteCode;
		
		maxPoolSize = readValue(8) + 1;
		offsets = new int[maxPoolSize];
		types = new byte[maxPoolSize];
		utf8s = new String[maxPoolSize];
		int position = 10;
		for (int i = 1; i < maxPoolSize - 1; i++) {
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
				position += 2;
				break;
			case INTEGER:
			case FLOAT:
			case FIELD:
			case METHOD:
			case IMETHOD:
			case NAME_TYPE:
				position += 4;
				break;
			case LONG:
			case DOUBLE:
				position += 8;
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
		StringBuilder result = new StringBuilder(size);
		while (pos < end) {
			int first = (byteCode[pos++] & 0xFF);
			if (first < 0x80) {
				result.append((char)first);
			} else if ((first & 0xE0) == 0xC0) {
				int x = (first & 0x1F) << 6;
				int y = (byteCode[pos++] & 0x3F);
				result.append((char)(x | y));
			} else {
				int x = (first & 0x0F) << 12;
				int y = (byteCode[pos++] & 0x3F) << 6;
				int z = (byteCode[pos++] & 0x3F);
				result.append((char)(x | y | z));
			}
		}
		return result.toString();
	}
	
	public boolean containsUtf8(String value) {
		return findUtf8(value) != NOT_FOUND;
	}
	
	public boolean usesClass(String className) {
		return findClass(className) != NOT_FOUND;
	}
	
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
	
	public boolean containsStringConstant(String value) {
		int index = findUtf8(value);
		if (index == NOT_FOUND) return false;
		for (int i = 1; i < maxPoolSize; i++) {
			if (types[i] == STRING && readValue(offsets[i]) == index) return true; 
		}
		return false;
	}
	
	public String getClassName() {
		return getClassName(readValue(endOfPool + 2));
	}
	
	public String getSuperClassName() {
		return getClassName(readValue(endOfPool + 4));
	}
	
	public List<String> getInterfaces() {
		int size = readValue(endOfPool + 6);
		if (size == 0) return Collections.emptyList();
		
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			result.add(getClassName(readValue(endOfPool + 8 + (i * 2))));
		}
		return result;
	}
	
	private String getClassName(int classIndex) {
		if (classIndex < 1) return null;
		return utf8s[readValue(offsets[classIndex])];
	}
	
	private boolean isMethod(int i) {
		byte type = types[i];
		return type == METHOD || type == IMETHOD;
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