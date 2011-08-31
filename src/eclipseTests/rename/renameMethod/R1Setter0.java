package R1;

import lombok.Setter;

public class R1Setter0 {

	@Setter int field = 0;
	
	/*1: RenameMethod(newMethodName) :1*/
	public void setField(int newField) {
		
		this.field = newField;
	}
	/*:1:*/
}
