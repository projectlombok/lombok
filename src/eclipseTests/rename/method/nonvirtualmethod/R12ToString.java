package R12;

import lombok.ToString;

@ToString
public class R12ToString {

	int field = 0;
	
	/*1: RenameNonVirtualMethod(newMethodName) :1*/
	private String method(){
		return "The value of my field is " + field;
	}
	/*:1:*/
	
}
