/*
 * Copyright 2007 Perry Nguyen <pfnguyen@hanhuy.com> Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.hanhuy.panno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A simple javabean property annotation. Used to generate get/set pairs for
 * field members automatically. Valid options are: 'name', 'readOnly',
 * 'writeOnly', 'useGet', 'preCallMethod', and 'preCallThrows'.
 * <p>
 *'name' may be used to set an alternative name for the property instead of
 * defaulting to the field's name.
 * <p>
 *'readOnly' and 'writeOnly' may be set to true to create only a getter or a
 * setter, respectively.
 * <p>
 *'useGet' is used to generate a getX instead of isX method if the property is
 * a boolean.
 * <p>
 *'preCallMethod' is the name of the method to be invoked prior to setting the
 * actual property value. It may be used for validation, or firing off
 * PropertyChangeEvents. This method will be called as
 * <code>preCallMethod(this, String propertyName, oldValue, newValue)</code>;
 * since this occurs at compile time, there is no interface, so the types are
 * entirely up to you to choose and make work. The method may also be any
 * method, so long as it's accessible to the bean. Thus,
 * <code>preCallMethod=anotherObject.validateProperty</code> would be ok, so
 * long as <code>anotherObject</code> is a field within the bean. The object
 * navigation leading up to the preCallMethod <b>cannot</b> contain any method
 * invocations; e.g.
 * <code>preCallMethod=someBean.someMethod().myPreCallMethod</code> would be
 * illegal and cause an error.
 * <p>
 *'preCallThrows' specifies what exceptions can possibly be thrown by the
 * preCallMethod. Any non-RuntimeExceptions <b>must</b> be specified here, or
 * else it will result in a compile-time error. This information cannot be
 * determined reflectively, because at compile-time, there is no reflection
 * (reflection is for runtime use). All exception names must be imported and
 * they may not be referred by their qualified name, e.g. "Exception", not
 * "java.lang.Exception"
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Property {
	String name() default "";
	
	boolean readOnly() default false;
	
	boolean writeOnly() default false;
	
	boolean useGet() default false;
	
	String[] preCallThrows() default { /* nothing */};
	
	String preCallMethod() default "";
}
