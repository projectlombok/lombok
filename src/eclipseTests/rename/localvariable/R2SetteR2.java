package R2;

import lombok.Setter;

public class R2SetteR2 {

	@Setter int name = 1;
	
	public int setName(int local) {
		/*1: RenameLocalVariable(local, newLocal) :1*/
		local = 1;
		/*:1:*/
		return local;
	}
}
