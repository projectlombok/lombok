package R11;

import lombok.Getter;

public class R11Getter0 {

	@Getter int fieldt = 0;
	@Getter int a;
	
	/*1: RenameVirtualMethod(getSomething) :1*/
	public int getFieldt(){
		return fieldt;
	}
	/*:1:*/
	
	public int getA(){
		
		return a;
	}
	
}

