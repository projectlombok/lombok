//version 8:
import java.lang.annotation.*;

class NonNullJavaxPlain {
	@javax.annotation.Nonnull
	int i;
	@javax.annotation.Nonnull
	String s;

	@java.lang.SuppressWarnings("all")
	public NonNullJavaxPlain(@javax.annotation.Nonnull final int i, @javax.annotation.Nonnull final String s) {
		if (s == null) {
			throw new java.lang.NullPointerException("s is marked non-null but is null");
		}
		this.i = i;
		this.s = s;
	}

	@javax.annotation.Nonnull
	@java.lang.SuppressWarnings("all")
	public int getI() {
		return this.i;
	}

	@javax.annotation.Nonnull
	@java.lang.SuppressWarnings("all")
	public String getS() {
		return this.s;
	}

	@java.lang.SuppressWarnings("all")
	public void setI(@javax.annotation.Nonnull final int i) {
		this.i = i;
	}

	@java.lang.SuppressWarnings("all")
	public void setS(@javax.annotation.Nonnull final String s) {
		if (s == null) {
			throw new java.lang.NullPointerException("s is marked non-null but is null");
		}
		this.s = s;
	}
}
