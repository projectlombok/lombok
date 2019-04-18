class A {
	@lombok.AllArgsConstructor
	class B {
		String s;
	}
}

class C {
	@lombok.Value 
	class D {
		A a;
		
		A.B test(String s) {
			return a.new B(s) {};
		}
	}
}