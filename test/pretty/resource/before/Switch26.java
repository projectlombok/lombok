// version 26:
public class Switch26 {
	String switchPatternMatching(int i) {
		return switch (i) {
			case 1 -> "one";
			case 5 -> "five";
			case int y when y >= 0 -> "+" + y;
			case int y -> "-" + -y;
		};
	}
}
