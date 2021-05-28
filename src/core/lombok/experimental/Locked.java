package lombok.experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Uses a {@link java.util.concurrent.locks.Lock} as a more flexible alternative to the 'synchronized' keyword with
 * optional increased throughput when making use of read and write locks.
 * <p>
 * For non-static methods, a field named {@code lock} is used, and for static methods,
 * {@code LOCK} is used. These will be generated if needed and if they aren't already present. The contents
 * of the fields will be serializable.
 * <p>
 * Because {@link Locked} uses a different type of lock from {@link Locked.Read} and {@link Locked.Write}, using both in
 * the same class using the default names will result in a compile time error.
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/Locked">the project lombok features page for &#64;Locked</a>.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Locked {
	/**
	 * Locks using a {@link ReadWriteLock#readLock()}.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Read {
		/**
		 * Optional: specify the name of a different field to lock on. If this field doesn't already exist, it is generated
		 * automatically.
		 *
		 * @return Name of the field to lock on (blank = generate one).
		 */
		String value() default "";
	}

	/**
	 * Locks using a {@link ReadWriteLock#writeLock()}.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Write {
		/**
		 * Optional: specify the name of a different field to lock on. If this field doesn't already exist, it is generated
		 * automatically.
		 *
		 * @return Name of the field to lock on (blank = generate one).
		 */
		String value() default "";
	}

	/**
	 * Optional: specify the name of a different field to lock on. If this field doesn't already exist, it is generated
	 * automatically.
	 *
	 * @return Name of the field to lock on (blank = generate one).
	 */
	String value() default "";
}
