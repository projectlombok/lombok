package R12;

import lombok.Getter;

public class R12OnMethod1 {

	@Getter int name = 1;
	
	@Deprecated
	/*1: RenameNonVirtualMethod(getSomething) :1*/
	private int getName(){
		return name;
	}


	/*:1:*/
	
	
}
