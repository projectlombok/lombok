package R2;

public class R2Refactoring {

	public void method(String local) {
		
		/*1: RenameLocalVariable(local,newLocal) :1*/
		local = "s"; 
		/*:1:*/
		System.out.println(local);
	}
}
