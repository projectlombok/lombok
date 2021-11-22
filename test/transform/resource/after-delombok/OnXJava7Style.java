//version :7
public class OnXJava7Style {
	@interface Foo {
		String value() default "";
	}
	@interface Bar {
		String stuff() default "";
	}
	@interface Array {
		String[] value() default {};
	}
	String a;
	String b;
	String c;
	String d;
	String e;
	String f;
	String g;
	String h;
	String i;
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
	@Array
	@java.lang.SuppressWarnings("all")
	public String getF() {
		return this.f;
	}

	@Array
	@java.lang.SuppressWarnings("all")
	public String getG() {
		return this.g;
	}

	@Array({})
	@java.lang.SuppressWarnings("all")
	public String getH() {
		return this.h;
	}

	@Array({"a", "b"})
	@java.lang.SuppressWarnings("all")
	public String getI() {
		return this.i;
	}
}
