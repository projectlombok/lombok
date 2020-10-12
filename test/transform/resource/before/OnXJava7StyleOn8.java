//platform ecj,eclipse
//version 8:

public class OnXJava7StyleOn8 {
	@interface Foo {
		String value() default "";
	}

	@interface Bar {
		String stuff() default "";
	}

	@lombok.Getter(onMethod=@__(@Foo)) String a;
	@lombok.Setter(onMethod=@__(@Foo())) String b;
	@lombok.Setter(onParam=@__(@Foo("a"))) String c;
	@lombok.Setter(onParam=@__(@Bar(stuff="b"))) String d;
	@lombok.Getter(onMethod=@__({@Foo(value="c"), @Bar(stuff="d")})) String e;
}
