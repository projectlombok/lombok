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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

public final class ConfigurationDataType {
	private static final List<Class<?>> SIMPLE_TYPES = Arrays.<Class<?>>asList(String.class, Integer.class, Boolean.class, Long.class, Byte.class, Short.class, Character.class, Float.class, Double.class);
	
	private final boolean isList;
	private final Class<?> elementType;
	
	public static ConfigurationDataType toDataType(Class<? extends ConfigurationKey<?>> keyClass) {
		if (keyClass.getSuperclass() != ConfigurationKey.class) {
			throw new IllegalArgumentException("No direct subclass of ConfigurationKey: " + keyClass.getName());
		}
		
		Type type = keyClass.getGenericSuperclass();
		if (!(type instanceof ParameterizedType)) {
			throw new IllegalArgumentException("Missing type parameter in "+ type);
		}
		
		ParameterizedType parameterized = (ParameterizedType) type;
		Type argumentType = parameterized.getActualTypeArguments()[0];
		
		boolean isList = false;
		if (argumentType instanceof ParameterizedType) {
			ParameterizedType parameterizedArgument = (ParameterizedType) argumentType;
			if (parameterizedArgument.getRawType() == List.class) {
				isList = true;
				argumentType = parameterizedArgument.getActualTypeArguments()[0];
			}
		}
		
		if (SIMPLE_TYPES.contains(argumentType) || isEnum(argumentType)) {
			return new ConfigurationDataType(isList, (Class<?>)argumentType);
		}
		
		if (argumentType instanceof ParameterizedType) {
			ParameterizedType parameterizedArgument = (ParameterizedType) argumentType;
			if (parameterizedArgument.getRawType() == Class.class) {
				Type classType = parameterizedArgument.getActualTypeArguments()[0];
				if (!(classType instanceof WildcardType)) {
					throw new IllegalArgumentException("Illegal specific Class type parameter in " + type);
				}
				WildcardType wildcard = (WildcardType) classType;
				if (wildcard.getLowerBounds().length != 0 || wildcard.getUpperBounds().length != 1 || wildcard.getUpperBounds()[0] != Object.class) {
					throw new IllegalArgumentException("Illegal bound wildcard Class type parameter in " + type);
				}
				return new ConfigurationDataType(isList, Class.class);
			}
		}
		
		if (argumentType == Class.class) {
			return new ConfigurationDataType(isList, Class.class);
		}
		
		throw new IllegalArgumentException("Unsupported type parameter in " + type);
	}
	
	private ConfigurationDataType(boolean isList, Class<?> elementType) {
		this.isList = isList;
		this.elementType = elementType;
	}
	
	public boolean isList() {
		return isList;
	}
	
	public Class<?> getElementType() {
		return elementType;
	}
	
	@Override
	public int hashCode() {
		return (isList ? 1231 : 1237) + elementType.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ConfigurationDataType)) return false;
		ConfigurationDataType other = (ConfigurationDataType) obj;
		return isList == other.isList && elementType == other.elementType;
	}
	
	@Override
	public String toString() {
		if (isList) return "java.util.List<" + elementType.getName() + ">";
		return elementType.getName();
	}
	
	private static boolean isEnum(Type argumentType) {
		return argumentType instanceof Class && ((Class<?>) argumentType).isEnum();
	}
}