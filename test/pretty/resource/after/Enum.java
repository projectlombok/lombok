enum Ranks {
	CLUBS, HEARTS, DIAMONDS, SPADES;
}
enum Complex {
	RED("ff0000"), GREEN("00ff00"), BLUE("0000f");
	private final String webColour;
	Complex(String webColour) {
		this.webColour = webColour;
	}
	public String getWebColour() {
		return webColour;
	}
}
enum Complexer {
	RED {
		public void foo() {
		}
		public void bar() {
		}
	},
	GREEN("foo") {
		public void foo() {
		}
	};
	public abstract void foo();
	Complexer(String colour) {
	}
	Complexer() {
	}
}
