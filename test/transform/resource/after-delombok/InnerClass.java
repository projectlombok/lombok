//version 8:
class A {
	class B {
		String s;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B(final String s) {
			this.s = s;
		}
	}
}
class C {
	final class D {
		private final A a;
		A.B test(String s) {
			return a.new B(s) {
			};
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public D(final A a) {
			this.a = a;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public A getA() {
			return this.a;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof C.D)) return false;
			final C.D other = (C.D) o;
			final java.lang.Object this$a = this.getA();
			final java.lang.Object other$a = other.getA();
			if (this$a == null ? other$a != null : !this$a.equals(other$a)) return false;
			return true;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final java.lang.Object $a = this.getA();
			result = result * PRIME + ($a == null ? 43 : $a.hashCode());
			return result;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "C.D(a=" + this.getA() + ")";
		}
	}
}
