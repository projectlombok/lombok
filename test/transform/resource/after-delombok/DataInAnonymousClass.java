public class DataInAnonymousClass {
	Object annonymous = new Object() {
		class Inner {
			private String string;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public Inner() {
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public String getString() {
				return this.string;
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public void setString(final String string) {
				this.string = string;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public boolean equals(final java.lang.Object o) {
				if (o == this) return true;
				if (!(o instanceof Inner)) return false;
				final Inner other = (Inner) o;
				if (!other.canEqual((java.lang.Object) this)) return false;
				final java.lang.Object this$string = this.getString();
				final java.lang.Object other$string = other.getString();
				if (this$string == null ? other$string != null : !this$string.equals(other$string)) return false;
				return true;
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected boolean canEqual(final java.lang.Object other) {
				return other instanceof Inner;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				final java.lang.Object $string = this.getString();
				result = result * PRIME + ($string == null ? 43 : $string.hashCode());
				return result;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "Inner(string=" + this.getString() + ")";
			}
		}
	};
}
