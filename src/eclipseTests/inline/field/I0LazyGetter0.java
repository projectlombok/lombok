package inline.inlineField;

import lombok.*;

public class I0LazyGetter0 {
	/*1: InlineField(target) :1*/
	@Getter(lazy=true)
	private static final int target = 0;
	/*:1:*/
	public void someMethod() {
		System.out.println(target);
	}
}