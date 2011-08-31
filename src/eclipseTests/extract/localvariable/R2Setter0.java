package R2;

import lombok.Setter;

public class R2Setter0 {

	@Setter int name = 1;
	
	public void someMethod(){
		int oldName;
		/*1: ExtractLocalVariable(newName) :1*/
		oldName = this.name;
		/*:1:*/
		System.out.println(oldName);
	}
}
