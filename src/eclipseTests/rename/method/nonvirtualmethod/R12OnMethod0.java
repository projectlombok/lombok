package R12;

import lombok.Getter;

public class R12OnMethod0 {

	@Getter(onMethod=@Deprecated) int name = 1;
	/*1: RenameNonVirtualMethod(getSomething) :1*/
	private int getName(){
		return name;
	}
	/*:1:*/
}
