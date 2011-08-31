package S1;
import nonExistingPackage.*;

import lombok.ToString;

@ToString
public class S1ToString0 {

	int field = 0;
	
	/*1: ToggleComment() :1*/
	private String method(){
		return "The value of my field is " + field;
	}
	/*:1:*/
	
}
