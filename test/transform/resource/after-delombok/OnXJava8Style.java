//version 8:
public class OnXJava8Style {
	@interface Foo {
		String value() default "";
	}
	@interface Bar {
		String stuff() default "";
	}
	String a;
	String b;
	String c;
	String d;
	String e;
	@Foo
	@java.lang.SuppressWarnings("all")
	public String getA() {
		return this.a;
	}
	@Foo
	@java.lang.SuppressWarnings("all")
	public void setB(final String b) {
		this.b = b;
	}
	@java.lang.SuppressWarnings("all")
	public void setC(@Foo("a") final String c) {
		this.c = c;
	}
	@java.lang.SuppressWarnings("all")
	public void setD(@Bar(stuff = "b") final String d) {
		this.d = d;
	}
	@Foo("c")
	@Bar(stuff = "d")
	@java.lang.SuppressWarnings("all")
	public String getE() {
		return this.e;
	}
}
