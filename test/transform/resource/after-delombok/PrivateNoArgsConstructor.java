public class PrivateNoArgsConstructor {
	private static class Base {
	}
	public static class PrivateNoArgsConstructorNotOnExtends extends Base {
		private final int a;
		@java.lang.SuppressWarnings("all")
		public PrivateNoArgsConstructorNotOnExtends(final int a) {
			this.a = a;
		}
		@java.lang.SuppressWarnings("all")
		public int getA() {
			return this.a;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends)) return false;
			final PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			if (!super.equals(o)) return false;
			if (this.getA() != other.getA()) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = super.hashCode();
			result = result * PRIME + this.getA();
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PrivateNoArgsConstructor.PrivateNoArgsConstructorNotOnExtends(a=" + this.getA() + ")";
		}
	}
	public static class PrivateNoArgsConstructorOnExtendsObject extends Object {
		private final int b;
		@java.lang.SuppressWarnings("all")
		public PrivateNoArgsConstructorOnExtendsObject(final int b) {
			this.b = b;
		}
		@java.lang.SuppressWarnings("all")
		private PrivateNoArgsConstructorOnExtendsObject() {
			this.b = 0;
		}
		@java.lang.SuppressWarnings("all")
		public int getB() {
			return this.b;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject)) return false;
			final PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			if (this.getB() != other.getB()) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getB();
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PrivateNoArgsConstructor.PrivateNoArgsConstructorOnExtendsObject(b=" + this.getB() + ")";
		}
	}
	public static class PrivateNoArgsConstructorExplicitBefore {
		private final int c;
		@java.lang.SuppressWarnings("all")
		public PrivateNoArgsConstructorExplicitBefore() {
			this.c = 0;
		}
		@java.lang.SuppressWarnings("all")
		public int getC() {
			return this.c;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore)) return false;
			final PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			if (this.getC() != other.getC()) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getC();
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitBefore(c=" + this.getC() + ")";
		}
		@java.lang.SuppressWarnings("all")
		public PrivateNoArgsConstructorExplicitBefore(final int c) {
			this.c = c;
		}
	}
	public static class PrivateNoArgsConstructorExplicitAfter {
		private final int d;
		@java.lang.SuppressWarnings("all")
		public int getD() {
			return this.d;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter)) return false;
			final PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			if (this.getD() != other.getD()) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getD();
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitAfter(d=" + this.getD() + ")";
		}
		@java.lang.SuppressWarnings("all")
		public PrivateNoArgsConstructorExplicitAfter() {
			this.d = 0;
		}
		@java.lang.SuppressWarnings("all")
		public PrivateNoArgsConstructorExplicitAfter(final int d) {
			this.d = d;
		}
	}
	public static class PrivateNoArgsConstructorExplicitNone {
		private final int e;
		@java.lang.SuppressWarnings("all")
		public int getE() {
			return this.e;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone)) return false;
			final PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			if (this.getE() != other.getE()) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getE();
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PrivateNoArgsConstructor.PrivateNoArgsConstructorExplicitNone(e=" + this.getE() + ")";
		}
		@java.lang.SuppressWarnings("all")
		public PrivateNoArgsConstructorExplicitNone(final int e) {
			this.e = e;
		}
	}
	public static class PrivateNoArgsConstructorNoFields {
		@java.lang.SuppressWarnings("all")
		public PrivateNoArgsConstructorNoFields() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields)) return false;
			final PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields other = (PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields) o;
			if (!other.canEqual((java.lang.Object) this)) return false;
			return true;
		}
		@java.lang.SuppressWarnings("all")
		protected boolean canEqual(final java.lang.Object other) {
			return other instanceof PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int result = 1;
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PrivateNoArgsConstructor.PrivateNoArgsConstructorNoFields()";
		}
	}
}
