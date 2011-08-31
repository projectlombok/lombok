package I2;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class I2ConAllArgs0 {

	public void someMethod(){
		/*1: InlineLocalVariable (oldName) :1*/
		String oldName="inlined";
		/*:1:*/
		System.out.println(oldName);
	}


}
