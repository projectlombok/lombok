package I2;

import lombok.Data;

@Data
public class I2Data0 {
	int oldName;
	public void someMethod(){
		/*1: InlineLocalVariable (oldName) :1*/
		String oldName="inlined";
		/*:1:*/
		System.out.println(oldName);
	}

}
