public class EqualsAndHashCodeInAnonymousClass {
	Object annonymous = new Object() {
		class Inner {
			private String string;
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public boolean equals(final java.lang.Object o) {
				if (o == this) return true;
				if (!(o instanceof Inner)) return false;
				final Inner other = (Inner) o;
				if (!other.canEqual((java.lang.Object) this)) return false;
				final java.lang.Object this$string = this.string;
				final java.lang.Object other$string = other.string;
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
				final java.lang.Object $string = this.string;
				result = result * PRIME + ($string == null ? 43 : $string.hashCode());
				return result;
			}
		}
	};
}
