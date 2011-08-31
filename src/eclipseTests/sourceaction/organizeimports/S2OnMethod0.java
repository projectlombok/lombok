package S2;
import nonExistingPackage.*;

import lombok.Getter;

public class S2OnMethod0 {

	@Getter(onMethod=@Deprecated) int name = 1;
	/*1: OrganizeImports() :1*/
	private int getName(){
		return name;
	}
	/*:1:*/
}
