package R2;

import lombok.Data;

@Data
public class R2Data0 {
	int oldName;
	public void someMethod(int oldName){
		/*1: RenameLocalVariable (oldName, newfield) :1*/
		oldName=0;
		/*:1:*/
		System.out.println(oldName);
	}

}
