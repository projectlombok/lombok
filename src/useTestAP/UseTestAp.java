@org.projectlombok.testAp.ExampleAnnotation
public class UseTestAp {
	@lombok.Getter String test;

	public void confirmGetTestExists() {
		System.out.println(getTest());
	}
}
