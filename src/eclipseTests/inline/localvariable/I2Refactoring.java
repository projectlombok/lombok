package I2;

public class I2Refactoring {

	public void method() {
		
		/*1: InlineLocalVariable(local) :1*/
		String local = "s"; 
		/*:1:*/
		System.out.println(local);
	}
}
