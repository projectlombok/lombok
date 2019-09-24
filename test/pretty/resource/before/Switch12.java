// version 12:12
public class Switch12 {
	public void basic() {
		switch (5) {
		case 1:
		case 2:
			System.out.println("OK");
			break;
		default:
		}
	}

	public void multiCase() {
		switch (5) {
		case 1, 2:
			System.out.println("OK");
		default:
		}
	}

	public int switchExpr() {
		return switch (5) {
		case 1, 2 -> 0;
		case 3 -> {
			break 10;
		}
		default -> 10;
		} + 10;
	}	
}
