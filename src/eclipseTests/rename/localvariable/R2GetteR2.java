package R2;

import lombok.Getter;

public class R2GetteR2 {

	@Getter int name = 1;
	
	public int getName(int local) {
		/*1: RenameLocalVariable(local, newLocal) :1*/
		local = 1;
		/*:1:*/
		return local;
	}
}
