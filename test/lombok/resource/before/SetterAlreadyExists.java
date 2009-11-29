class Setter1 {
	@lombok.Setter boolean foo;
	void setFoo(boolean foo) {
	}
}
class Setter2 {
	@lombok.Setter boolean foo;
	void setFoo(String foo) {
	}
}
class Setter3 {
	@lombok.Setter String foo;
	void setFoo(boolean foo) {
	}
}
class Setter4 {
	@lombok.Setter String foo;
	void setFoo(String foo) {
	}
}
class Setter5 {
	@lombok.Setter String foo;
	void setFoo() {
	}
}
class Setter6 {
	@lombok.Setter String foo;
	void setFoo(String foo, int x) {
	}
}
class Setter7 {
	@lombok.Setter String foo;
	static void setFoo() {
	}
}
