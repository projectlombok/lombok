// version 13:
public class Switch13 {
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

	public int switchExpr1() {
		return switch (5) {
			case 1, 2 -> 0;
			case 3 -> {
				yield 10;
			}
			default -> 10;
		} + 10;
	}
	
	public int switchExpr2() {
		return switch (5) {
		case 1, 2:
			System.out.println("Hello");
		case 3:
			yield 10;
		default:
			yield 20;
		} + 10;
	}
	
	public void arrowSwitch() {
		switch (5) {
			case 1, 2 -> System.out.println("Hello");
			case 3 -> {
				System.out.println("");
				break;
			}
		}
	}
	
	public void emptySwitch() {
		switch (5) {
		}
	}
}
