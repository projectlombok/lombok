class Getter1 {
	boolean foo;
	boolean hasFoo() {
		return true;
	}
}
class Getter2 {
	boolean foo;
	boolean isFoo() {
		return true;
	}
}
class Getter3 {
	boolean foo;
	boolean getFoo() {
		return true;
	}
}
class Getter4 {
	String foo;
	String hasFoo() {
		return null;
	}
	public String getFoo() {
		return foo;
	}
}
class Getter5 {
	String foo;
	String isFoo() {
		return null;
	}
	public String getFoo() {
		return foo;
	}
}
class Getter6 {
	String foo;
	String getFoo() {
		return null;
	}
}
class Getter7 {
	String foo;
	boolean hasFoo() {
		return false;
	}
	public String getFoo() {
		return foo;
	}
}
class Getter8 {
	String foo;
	boolean isFoo() {
		return false;
	}
	public String getFoo() {
		return foo;
	}
}
class Getter9 {
	String foo;
	boolean getFoo() {
		return false;
	}
}