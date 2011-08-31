package S2;
import nonExistingPackage.*;

import lombok.Setter;

public class S2Setter0 {

	@Setter int field = 0;
	
	/*1: OrganizeImports() :1*/
	private void setField(int newField) {
		
		this.field = newField;
	}
	/*:1:*/
}
