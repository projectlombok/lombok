public class SuperBuilderNonAscii {
	@lombok.SuperBuilder
	public static class 부모 {
		Long a;
	}
	
	@lombok.SuperBuilder
	public static class 자식 extends 부모 {
		String b;
	}
	
	public static void test() {
		자식 x = 자식.builder().b("").a(5L).build();
	}
}
