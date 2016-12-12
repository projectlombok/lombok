@org.projectlombok.testAp.ExampleAnnotation
//@lombok.experimental.Accessors(chain=true)
public class UseTestAp {
	@lombok.Setter @lombok.Getter String test;

	public void confirmGetTestExists() {
		System.out.println(getTest());
	}

	public UseTestAp returningSelf() {
		return this;
	}
}
