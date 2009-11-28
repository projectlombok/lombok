class Getter1 {
	@lombok.Getter boolean foo;
	boolean hasFoo() {
		return true;
	}
}
class Getter2 {
	@lombok.Getter boolean foo;
	boolean isFoo() {
		return true;
	}
}
class Getter3 {
	@lombok.Getter boolean foo;
	boolean getFoo() {
		return true;
	}
}
class Getter4 {
	@lombok.Getter String foo;
	String hasFoo() {
		return null;
	}
}
class Getter5 {
	@lombok.Getter String foo;
	String isFoo() {
		return null;
	}
}
class Getter6 {
	@lombok.Getter String foo;
	String getFoo() {
		return null;
	}
}
class Getter7 {
	@lombok.Getter String foo;
	boolean hasFoo() {
		return false;
	}
}
class Getter8 {
	@lombok.Getter String foo;
	boolean isFoo() {
		return false;
	}
}
class Getter9 {
	@lombok.Getter String foo;
	boolean getFoo() {
		return false;
	}
}