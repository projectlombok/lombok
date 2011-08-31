package R2;

import lombok.val;

public class R2Val0 {

	public void method(String newName){
		
		/*1: RenameLocalVariable (oldName, newName) :1*/
		val greeting = new String("Hallo "+newName);
		/*:1:*/
		System.out.println(greeting);
	}
	
}
