package I2;

import lombok.Setter;

public class I2Setter0 {

	@Setter int name = 1;
	
	public int setName() {
		/*1: InlineLocalVariable(local) :1*/
		int local = 1;
		/*:1:*/
		return local;
	}
}
