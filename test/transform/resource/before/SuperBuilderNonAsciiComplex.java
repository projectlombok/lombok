public class SuperBuilderNonAsciiComplex {
	@lombok.SuperBuilder
	public static class 부모<T> {
		Long a;
		T z;
	}
	
	@lombok.SuperBuilder
	public static class 자식 extends SuperBuilderNonAsciiComplex.부모<String> {
		String b;
	}
	
	public static void test() {
		부모<String> x = 자식.builder().b("").a(5L).build();
	}
}
