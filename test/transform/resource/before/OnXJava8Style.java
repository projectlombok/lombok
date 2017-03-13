//version 8:
public class OnXJava8Style {
	@interface Foo {
		String value() default "";
	}

	@interface Bar {
		String stuff() default "";
	}

	@lombok.Getter(onMethod_=@Foo) String a;
	@lombok.Setter(onMethod_=@Foo()) String b;
	@lombok.Setter(onParam_=@Foo("a")) String c;
	@lombok.Setter(onParam_=@Bar(stuff="b")) String d;
	@lombok.Getter(onMethod_={@Foo(value="c"), @Bar(stuff="d")}) String e;
}
