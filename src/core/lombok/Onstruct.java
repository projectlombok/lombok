package lombok;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link Onstruct} annotation declares variables based on getters of an
 * object.<br />
 * The variables names are the one specified. If the annotation has parameters
 * for prefix and suffix, those parameters are added to the variable
 * names.<br />
 * The getter is the existing method in the object's class verifying
 * <ol>
 * <li>return non-void type</li>
 * <li>requires no argument</li>
 * <li>match the variable name specified, prefixed by get|is, and ignoring case.
 * In the order :
 * <ol>
 * <li>getName is selected if exists</li>
 * <li>isName is selected if exists</li>
 * <li>getname (ignoring case) is selected if exists only ONE. compiling error
 * if several found</li>
 * <li>isname (ignoring case) is selected if exists only ONE. compiling error if
 * several found</li>
 * <li>name is selected if exists</li>
 * <li>name (ignoring case) is selected if exists only ONE. compiling error if
 * several found</li>
 * <li>if no matching method exists, error</li>
 * </ol>
 * </li>
 * </ol>
 *
 * <p>
 * It MUST only be applied to typed declarations. No garantee is present for var
 * declaration.
 * </p>
 *
 *
 * <p>
 * Before:
 *
 * <pre>
 * &#064;Onstruct(pre = "b_") Object author, name, editiondate, purchasable = mybook;
 * </pre>
 *
 * After:
 *
 * <pre>
 * var b_author = mybook.getAuthor();
 * var b_name = mybook.getName();
 * var b_editiondate = mybook.getEditionDate();
 * var b_purchasable = mybook.isPurchasable();
 * </pre>
 *
 */
@Target(ElementType.LOCAL_VARIABLE)
@Retention(RetentionPolicy.SOURCE)
public @interface Onstruct {
	
	//
	// variable generation
	//
	
	/**
	 * prefix to start the created var name with. Default is empty
	 */
	String pre() default "";
	
	/**
	 * suffix to append to created var name. Default is empty
	 */
	String suf() default "";

	/**
	 * if true, should camel case the variable name. Only applied when prefix is
	 * non blank. Default is false.
	 */
	boolean cml() default false;
	
	//
	// method generation
	//
	
	/**
	 * how to build the getter for a var name
	 */
	public enum SourceType {
		GET("get", true), BOOL("is", true), FLUENT("", false);
		
		/** prefix to start the getter method with*/
		public final String pre;

		/** should we uppercase the first letter of the variable in the method name ? */
		public final boolean cml;
		
		SourceType(String pre, boolean cml) {
			this.pre = pre;
			this.cml = cml;
		}
	}

	public SourceType source() default SourceType.GET;

	public enum Cml {
		CML(true), NOCML(false), SOURCE(null);
		;

		public final Boolean cml;
		
		Cml(Boolean cml) {
			this.cml = cml;
		}
	}

	/**
	 * can't set default value to null or non-constant values :/
	 */
	static final String NULLSTRING = "NULLSTRING";
	
	/**
	 * overwrite the {@link #source()} prefix to start the getter call by.
	 * Default is {@link #NULLSTRING} to not overwrite
	 */
	String methodPre() default NULLSTRING;

	/**
	 * Overwrite the {@link #source()}'s method camel case. If
	 * {@link Cml#SOURCE}(default), don't overwrite. If {@link Cml#CML}, should
	 * camel case the method call. If {@link Cml#NOCML}, don't camel case it.
	 */
	Cml methodCml() default Cml.SOURCE;
	
}
