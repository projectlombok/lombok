package S1;
import nonExistingPackage.*;

import lombok.Getter;

public class S1OnMethod0 {

	@Getter(onMethod=@Deprecated) int name = 1;
	/*1: ToggleComment() :1*/
	private int getName(){
		return name;
	}
	/*:1:*/
}
