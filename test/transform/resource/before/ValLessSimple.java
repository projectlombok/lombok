public class ValLessSimple {
	private short field2 = 5;
	
	private String method() {
		return "method";
	}
	
	private double method2() {
		return 2.0;
	}
	
	{
		System.out.println("Hello");
		val z = 20;
		val x = 10;
		val a = z;
		val y = field2;
	}
	
	private void testVal(String param) {
		val fieldV = field;
		val a = 10;
		val b = 20;
		{
			val methodV = method();
			val foo = fieldV + methodV;
		}
	}
	
	private String field = "field";
}
