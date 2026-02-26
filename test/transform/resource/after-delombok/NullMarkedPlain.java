//version 9:
import java.lang.annotation.*;
@org.jspecify.annotations.NullMarked
class NullMarkedPlain {
	int i;
	String s;
	@org.jspecify.annotations.Nullable
	Object o;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public NullMarkedPlain() {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getI() {
		return this.i;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getS() {
		return this.s;
	}
	@org.jspecify.annotations.Nullable
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public Object getO() {
		return this.o;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setI(final int i) {
		this.i = i;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setS(final String s) {
		if (s == null) {
			throw new java.lang.NullPointerException("s is marked non-null but is null");
		}
		this.s = s;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setO(@org.jspecify.annotations.Nullable final Object o) {
		this.o = o;
	}
}
