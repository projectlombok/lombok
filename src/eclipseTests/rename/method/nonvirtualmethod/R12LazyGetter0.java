package R12;

import lombok.Getter;

public class R12LazyGetter0 {

	@Getter(lazy=true) int fieldt = 0;
	@Getter(lazy=true) int a;
	
	/*1: RenameNonVirtualMethod(getSomething) :1*/
	private int getFieldt(){
		return fieldt;
	}
	/*:1:*/
	
	private int getA(){
		
		return a;
	}
	
}
