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

import java.util.List;

import javax.lang.model.SourceVersion;

import lombok.core.configuration.ConfigurationKey;

public class LombokConfiguration {
	
	private LombokConfiguration() {
		// prevent instantiation
	}
	
	
	static <T> T read(ConfigurationKey<T> key, AST<?, ?, ?> ast) {
//		if (key.keyName.equals("lombok.log.varName")) return (T)"loggertje";
//		if (key.keyName.equals("lombok.log.static")) return (T)Boolean.FALSE;
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		try { new ConfigurationKey<List<String>>("List<String>") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<Integer>("Integer") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<Class<?>>("Class<?>") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<SourceVersion>("SourceVersion") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<Class>("Class") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<Class<Number>>("Class<Number>") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<Class<? extends Number>>("Class<? extends Number>") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<Class<? super String>>("Class<? super String>") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<Number>("Number") {}; } catch (Exception e) { e.printStackTrace();}
		try { class Between extends ConfigurationKey<String> {
				public Between() {
					super("between");
				}
			};
			new Between(){};
		} catch (Exception e) { e.printStackTrace();}
		
		try { new ConfigurationKey<String>("more than once") {}; } catch (Exception e) { e.printStackTrace();}
		try { new ConfigurationKey<Integer>("more than once") {}; } catch (Exception e) { e.printStackTrace();}

		System.out.println(System.identityHashCode(ConfigurationKey.registeredKeys()));
		System.out.println(System.identityHashCode(ConfigurationKey.registeredKeys()));
		
		ConfigurationKey<?> first = null;
		try { first = new ConfigurationKey<Integer>("anint") {}; } catch (Exception e) { e.printStackTrace();}
		System.out.println(System.identityHashCode(ConfigurationKey.registeredKeys()));
		System.out.println(System.identityHashCode(ConfigurationKey.registeredKeys()));
		
		ConfigurationKey<?> second = null;
		try { second = new ConfigurationKey<Integer>("anint") {}; } catch (Exception e) { e.printStackTrace();}
		System.out.println(System.identityHashCode(ConfigurationKey.registeredKeys()));
		System.out.println(System.identityHashCode(ConfigurationKey.registeredKeys()));
		
		System.out.println(first == second);
		System.out.println(first.getClass() == second.getClass());
		System.out.println(first.equals(second));
	}
}
