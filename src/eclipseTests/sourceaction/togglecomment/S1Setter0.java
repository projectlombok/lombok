package S1;
import nonExistingPackage.*;

import lombok.Setter;

public class S1Setter0 {

	@Setter int field = 0;
	
	/*1: ToggleComment() :1*/
	private void setField(int newField) {
		
		this.field = newField;
	}
	/*:1:*/
}
