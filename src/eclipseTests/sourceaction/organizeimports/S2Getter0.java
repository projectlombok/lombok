package S2;
import nonExistingPackage.*;

import lombok.Getter;

public class S2Getter0 {

	@Getter int fieldt = 0;
	@Getter int a;
	
	/*1: OrganizeImports() :1*/
	private int getFieldt(){
		return fieldt;
	}
	/*:1:*/
	
	private int getA(){
		
		return a;
	}
	
}

