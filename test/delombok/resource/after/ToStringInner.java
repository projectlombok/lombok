class ToStringOuter {
	final int x;
	String name;
	class ToStringInner {
		final int y;
		@java.lang.Override
		public java.lang.String toString() {
			return "ToStringOuter.ToStringInner(y=" + y + ")";
		}
	}
	static class ToStringStaticInner {
		final int y;
		@java.lang.Override
		public java.lang.String toString() {
			return "ToStringOuter.ToStringStaticInner(y=" + y + ")";
		}
	}
	class ToStringMiddle {
		class ToStringMoreInner {
			final String name;
			@java.lang.Override
			public java.lang.String toString() {
				return "ToStringOuter.ToStringMiddle.ToStringMoreInner(name=" + name + ")";
			}
		}
	}
	
	@java.lang.Override
	public java.lang.String toString() {
		return "ToStringOuter(x=" + x + ", name=" + name + ")";
	}
}