package R11;

import lombok.Getter;

public class R11LazyGetter0 {

	@Getter(lazy=true) int fieldt = 0;
	@Getter(lazy=true) int a;
	
	/*1: RenameVirtualMethod(getSomething) :1*/
	public int getFieldt(){
		return fieldt;
	}
	/*:1:*/
	
	public int getA(){
		
		return a;
	}
	
}
