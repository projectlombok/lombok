package R12;

import lombok.Getter;

public class R12Getter0 {

	@Getter int fieldt = 0;
	@Getter int a;
	
	/*1: RenameNonVirtualMethod(getSomething) :1*/
	private int getFieldt(){
		return fieldt;
	}
	/*:1:*/
	
	private int getA(){
		
		return a;
	}
	
}

