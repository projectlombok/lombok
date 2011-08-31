package R2;

import lombok.Getter;

public class R2Getter0 {

	@Getter int name = 1;
	
	public int getName() {
		/*1: RenameLocalVariable(local, newLocal) :1*/
		int local = 1;
		/*:1:*/
		return local;
	}
}
