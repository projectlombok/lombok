public class Switch17 {
	String switchPatternMatching(Object o) {
		return switch (o) {
			case Integer i -> String.format("int %d", i);
			case Long l -> String.format("long %d", l);
			case Double d -> String.format("double %f", d);
			case String s -> String.format("String %s", s);
			default -> o.toString();
		};
	}

	String switchNull(Object o) {
		return switch (o) {
			case null, default -> "?";
		};
	}

	String switchGuardPattern(Object o) {
		return switch (o) {
			case String s && s.length() > 1 -> s;
			default -> o.toString();
		};
	}

	String switchParenthesizedPattern(Object o) {
		return switch (o) {
			case (String s) -> s;
			default -> o.toString();
		};
	}
}