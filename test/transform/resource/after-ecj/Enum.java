enum PlainColour {
	RED, BLUE, GREEN
}

enum Colour {
	private final int red;
	private final int blue;
	private final int green;
	private final String name;
	
	RED(255, 0, 0, "red"),
	BLUE(0, 255, 0, "blue"),
	GREEN(0, 0, 255, "green");
	
	private Colour(int red, int blue, int green, String name) {
		this.red = red;
		this.blue = blue;
		this.green = green;
		this.name = name;
	}
	
	public static Colour findByRed(int red) {
		for(Colour c : values()) {
			if (c.red == red) {
				return c;
			}
		}
		return null;
	}
	
	public static Colour findByBlue(int blue) {
		for(Colour c : values()) {
			if (c.blue == blue) {
				return c;
			}
		}
		return null;
	}
	
	public static Colour findByGreen(int green) {
		for(Colour c : values()) {
			if (c.green == green) {
				return c;
			}
		}
		return null;
	}
	
	public static Colour findByName(String name) {
		for(Colour c : values()) {
			if (c.name.equals(name)) {
				return c;
			}
		}
		return null;
	}
	
}
