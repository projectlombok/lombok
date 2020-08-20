package a;

class ExtensionMethodNames {
	
	public void instanceCalls() {
		a.Extensions.ext((new Test()));
		Test t = new Test();
		a.Extensions.ext(t);
		Test Test = new Test();
		a.Extensions.ext(Test);
	}
	
	public void staticCalls() {
		Test.ext();
		a.Test.ext();
	}
}

class Extensions {
	public static void ext(Test t) {
	}
}

class Test {
	public static void ext() {
	}
}