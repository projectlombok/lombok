package R2;

import lombok.Getter;

public class R2Getter0 {

	@Getter int oldName = 1;
	
	public void someMethod(){
		/*1: ExtractLocalVariable(newName) :1*/
		oldName = 5;
		/*:1:*/
	}
}
