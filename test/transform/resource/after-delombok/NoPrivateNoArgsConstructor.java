public class NoPrivateNoArgsConstructor {
	public static class NoPrivateNoArgsConstructorData {
		private final int i;
		@java.lang.SuppressWarnings("all")
		public NoPrivateNoArgsConstructorData(final int i) {
			this.i = i;
		}
		@java.lang.SuppressWarnings("all")
		public int getI() {
			return this.i;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData)) return false;
			final NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData other = (NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			if (this.getI() != other.getI()) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getI();
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorData(i=" + this.getI() + ")";
		}
	}
	public static final class NoPrivateNoArgsConstructorValue {
		private final int i;
		@java.lang.SuppressWarnings("all")
		public NoPrivateNoArgsConstructorValue(final int i) {
			this.i = i;
		}
		@java.lang.SuppressWarnings("all")
		public int getI() {
			return this.i;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorValue)) return false;
			final NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorValue other = (NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorValue) o;
			if (this.getI() != other.getI()) return false;
			return true;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getI();
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "NoPrivateNoArgsConstructor.NoPrivateNoArgsConstructorValue(i=" + this.getI() + ")";
		}
	}
}