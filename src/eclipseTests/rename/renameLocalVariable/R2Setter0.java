package R2;

import lombok.Setter;

public class R2Setter0 {

	@Setter int name = 1;
	
	public int setName() {
		/*1: RenameLocalVariable(local, newLocal) :1*/
		int local = 1;
		/*:1:*/
		return local;
	}
}
