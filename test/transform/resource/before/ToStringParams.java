package lombok.test;
import lombok.ToString;
@ToString(fqn = true, prefix = "\n{\n    \"", separator = "\" : \"", infix = "\",\n    \"", suffix = "\"\n}")
class ToStringOuter {
	int x;
	String name;

	@ToString(fqn = true)
	class ToStringInner {
		int y;
	}

	@ToString(separator = " = ")
	static class ToStringStaticInner {
		int y;
	}

	class ToStringMiddle {
		@ToString(fqn = true, prefix = "[\n\t", callSuper = true, includeFieldNames = false, separator = " = ", infix = ",\n\t", suffix = "\n]")
		class ToStringMoreInner extends ToStringInner {
			String name;
		}
	}
}