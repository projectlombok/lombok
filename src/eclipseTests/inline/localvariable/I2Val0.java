package I2;

import lombok.val;

public class I2Val0 {

	public void method(){
		String newName = "Roel S";
		/*1: InlineLocalVariable (oldName) :1*/
		val greeting = new String("Hello "+newName);
		/*:1:*/
		System.out.println(greeting);
	}
	
}
