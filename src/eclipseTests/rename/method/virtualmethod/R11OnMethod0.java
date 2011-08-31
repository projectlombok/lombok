package R11;

import lombok.Getter;

public class R11OnMethod0 {

	@Getter(onMethod=@Deprecated) int name = 1;
	/*1: RenameVirtualMethod(getSomething) :1*/
	public int getName(){
		return name;
	}
	/*:1:*/
}
