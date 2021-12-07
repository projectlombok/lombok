public class ValueInAnonymousClass {
	Object annonymous = new Object() {

		final class Inner {
			private final String string;

			@java.lang.SuppressWarnings("all")
			public Inner(final String string) {
				this.string = string;
			}

			@java.lang.SuppressWarnings("all")
			public String getString() {
				return this.string;
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public boolean equals(final java.lang.Object o) {
				if (o == this) return true;
				if (!(o instanceof Inner)) return false;
				final Inner other = (Inner) o;
				final java.lang.Object this$string = this.getString();
				final java.lang.Object other$string = other.getString();
				if (this$string == null ? other$string != null : !this$string.equals(other$string)) return false;
				return true;
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				final java.lang.Object $string = this.getString();
				result = result * PRIME + ($string == null ? 43 : $string.hashCode());
				return result;
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "Inner(string=" + this.getString() + ")";
			}
		}
	};
}
