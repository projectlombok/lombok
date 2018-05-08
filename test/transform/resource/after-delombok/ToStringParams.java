package lombok.test;
class ToStringOuter {
	int x;
	String name;
	class ToStringInner {
		int y;
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "lombok.test.ToStringOuter.ToStringInner(y=" + this.y + ")";
		}
	}
	static class ToStringStaticInner {
		int y;
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "ToStringOuter.ToStringStaticInner(y = " + this.y + ")";
		}
	}
	class ToStringMiddle {
		class ToStringMoreInner extends ToStringInner {
			String name;
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "lombok.test.ToStringOuter.ToStringMiddle.ToStringMoreInner[\n\tsuper = " + super.toString() + ",\n\t" + this.name + "\n]";
			}
		}
	}
	
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "lombok.test.ToStringOuter\n{\n    \"x\" : \"" + this.x + "\",\n    \"name\" : \"" + this.name + "\"\n}";
	}
}
