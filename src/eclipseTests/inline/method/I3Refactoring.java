package I3;

public class I3Refactoring {

	public void someMethod() {
		/*1: InlineMethod(anotherMethod) :1*/
		String oldName = anotherMethod();
		/*:1:*/
		System.out.println(oldName);
	}
	public String anotherMethod(){
		String oldName="inlined";
		return oldName;
	}
}
