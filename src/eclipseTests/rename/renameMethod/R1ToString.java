package R1;

import lombok.ToString;

@ToString
public class R1ToString {

	int field = 0;
	
	/*1: RenameMethod(newMethodName) :1*/
	public String method(){
		return "The value of my field is " + field;
	}
	/*:1:*/
	
}
