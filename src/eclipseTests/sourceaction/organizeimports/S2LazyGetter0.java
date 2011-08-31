package S2;
import nonExistingPackage.*;

import lombok.Getter;

public class S2LazyGetter0 {

	@Getter(lazy=true) int fieldt = 0;
	@Getter(lazy=true) int a;
	
	/*1: OrganizeImports() :1*/
	private int getFieldt(){
		return fieldt;
	}
	/*:1:*/
	
	private int getA(){
		
		return a;
	}
	
}
