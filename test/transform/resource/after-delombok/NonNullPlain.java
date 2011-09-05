import java.lang.annotation.*;

class NonNullPlain {
	@lombok.NonNull
	int i;
	@lombok.NonNull
	String s;
	@NotNull
	Object o;
	
	@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
	@Retention(RetentionPolicy.CLASS)
	public @interface NotNull {
	}
	
	@java.beans.ConstructorProperties({"i", "s", "o"})
	@java.lang.SuppressWarnings("all")
	public NonNullPlain(@lombok.NonNull final int i, @lombok.NonNull final String s, @NotNull final Object o) {
		if (s == null) throw new java.lang.NullPointerException("s");
		if (o == null) throw new java.lang.NullPointerException("o");
		this.i = i;
		this.s = s;
		this.o = o;
	}
	
	@lombok.NonNull
	@java.lang.SuppressWarnings("all")
	public int getI() {
		return this.i;
	}
	
	@lombok.NonNull
	@java.lang.SuppressWarnings("all")
	public String getS() {
		return this.s;
	}
	
	@NotNull
	@java.lang.SuppressWarnings("all")
	public Object getO() {
		return this.o;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setI(@lombok.NonNull final int i) {
		this.i = i;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setS(@lombok.NonNull final String s) {
		if (s == null) throw new java.lang.NullPointerException("s");
		this.s = s;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setO(@NotNull final Object o) {
		if (o == null) throw new java.lang.NullPointerException("o");
		this.o = o;
	}
}
