package R2;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class R2ConAllArgs0 {

	public void someMethod(int oldName){
		/*1: RenameLocalVariable (oldName, newfield) :1*/
		oldName = 0;
		/*:1:*/
		System.out.println(oldName);
	}


}
