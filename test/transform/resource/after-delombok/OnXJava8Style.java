//version 8:
public class OnXJava8Style {
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
	@lombok.Generated
	public String getA() {
		return this.a;
	}
	@Foo
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setB(final String b) {
		this.b = b;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setC(@Foo("a") final String c) {
		this.c = c;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setD(@Bar(stuff = "b") final String d) {
		this.d = d;
	}
	@Foo("c")
	@Bar(stuff = "d")
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getE() {
		return this.e;
	}
	@Array
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getF() {
		return this.f;
	}
	@Array
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getG() {
		return this.g;
	}
	@Array({})
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getH() {
		return this.h;
	}
	@Array({"a", "b"})
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getI() {
		return this.i;
	}
}
