package R12;

import lombok.Setter;

public class R12Setter0 {

	@Setter int field = 0;
	
	/*1: RenameNonVirtualMethod(newMethodName) :1*/
	private void setField(int newField) {
		
		this.field = newField;
	}
	/*:1:*/
}
