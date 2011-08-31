package R1;

import lombok.Getter;

public class R1Getter0 {

	@Getter int fieldt = 0;
	@Getter int a;
	
	/*1: RenameMethod(getSomething) :1*/
	public int getFieldt(){
		return fieldt;
	}
	/*:1:*/
	
	public int getA(){
		
		return a;
	}
	
}

