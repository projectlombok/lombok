/*
 * Copyright (C) 2013-2014 The Project Lombok Authors.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigurationDataType {
	private static final Map<Class<?>, ConfigurationValueParser> SIMPLE_TYPES;
	static {
		Map<Class<?>, ConfigurationValueParser> map = new HashMap<Class<?>, ConfigurationValueParser>();
		map.put(String.class, new ConfigurationValueParser() {
			@Override public Object parse(String value) {
				return value;
			}
			
			@Override public String description() {
				return "string";
			}
			
			@Override public String exampleValue() {
				return "<text>";
			}
		});
		map.put(Integer.class, new ConfigurationValueParser() {
			@Override public Object parse(String value) {
				return Integer.parseInt(value);
			}
			
			@Override public String description() {
				return "int";
			}
			
			@Override public String exampleValue() {
				return "<int>";
			}
		});
		map.put(Long.class, new ConfigurationValueParser() {
			@Override public Object parse(String value) {
				return Long.parseLong(value);
			}
			
			@Override public String description() {
				return "long";
			}
			
			@Override public String exampleValue() {
				return "<long>";
			}
		});
		map.put(Double.class, new ConfigurationValueParser() {
			@Override public Object parse(String value) {
				return Double.parseDouble(value);
			}
			
			@Override public String description() {
				return "double";
			}
			
			@Override public String exampleValue() {
				return "<double>";
			}
		});
		map.put(Boolean.class, new ConfigurationValueParser() {
			@Override public Object parse(String value) {
				return Boolean.parseBoolean(value);
			}
			
			@Override public String description() {
				return "boolean";
			}
			
			@Override public String exampleValue() {
				return "[false | true]";
			}
		});
		map.put(TypeName.class, new ConfigurationValueParser() {
			@Override public Object parse(String value) {
				return TypeName.valueOf(value);
			}
			
			@Override public String description() {
				return "type-name";
			}
			
			@Override public String exampleValue() {
				return "<fully.qualified.Type>";
			}
		});
		SIMPLE_TYPES = map;
	}
	
	private static ConfigurationValueParser enumParser(Object enumType) {
		final Class<?> type = (Class<?>) enumType;
		@SuppressWarnings("rawtypes") final Class rawType = type;
		
		return new ConfigurationValueParser() {
			@SuppressWarnings("unchecked")
			@Override public Object parse(String value) {
				try {
					return Enum.valueOf(rawType, value);
				} catch (Exception e) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < value.length(); i++) {
						char c = value.charAt(i);
						if (Character.isUpperCase(c) && i > 0) sb.append("_");
						sb.append(Character.toUpperCase(c));
					}
					return Enum.valueOf(rawType, sb.toString());
				}
			}
			
			@Override public String description() {
				return "enum (" + type.getName() + ")";
			}
			
			@Override public String exampleValue() {
				ExampleValueString evs = type.getAnnotation(ExampleValueString.class);
				if (evs != null) return evs.value();
				return Arrays.toString(type.getEnumConstants()).replace(",", " |");
			}
		};
	}
	
	private final boolean isList;
	private final ConfigurationValueParser parser;
	
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
		
		if (SIMPLE_TYPES.containsKey(argumentType)) {
			return new ConfigurationDataType(isList, SIMPLE_TYPES.get(argumentType));
		}
		
		if (isEnum(argumentType)) {
			return new ConfigurationDataType(isList, enumParser(argumentType));
		}
		
		throw new IllegalArgumentException("Unsupported type parameter in " + type);
	}
	
	private ConfigurationDataType(boolean isList, ConfigurationValueParser parser) {
		this.isList = isList;
		this.parser = parser;
	}
	
	public boolean isList() {
		return isList;
	}
	
	ConfigurationValueParser getParser() {
		return parser;
	}
	
	@Override
	public String toString() {
		if (isList) return "list of " + parser.description();
		return parser.description();
	}
	
	private static boolean isEnum(Type argumentType) {
		return argumentType instanceof Class && ((Class<?>) argumentType).isEnum();
	}
}