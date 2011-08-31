package S2;
import nonExistingPackage.*;

import lombok.Getter;

public class S2OnMethod1 {

	@Getter int name = 1;
	
	@Deprecated
	/*1: OrganizeImports() :1*/
	private int getName(){
		return name;
	}


	/*:1:*/
	
	
}
