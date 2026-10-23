package test.lombok;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SimpleNestedAnnotation {

	@TA
	private final int someVal;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TA {
	}

	@java.lang.SuppressWarnings("all")
	public SimpleNestedAnnotation(@TA final int someVal) {
		this.someVal = someVal;
	}

	@TA
	@java.lang.SuppressWarnings("all")
	public int getSomeVal() {
		return this.someVal;
	}
}
