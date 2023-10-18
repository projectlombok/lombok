//version 8:
//CONF: lombok.nonNull.useForeignAnnotations = false
import java.lang.annotation.*;

class NonNullNoJavaxPlain {
	@javax.annotation.Nonnull
	int i;
	@javax.annotation.Nonnull
	String s;

	@java.lang.SuppressWarnings("all")
	public NonNullNoJavaxPlain() {
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
		this.s = s;
	}
}
