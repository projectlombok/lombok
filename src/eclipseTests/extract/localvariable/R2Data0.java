package R2;

import lombok.Data;

@Data
public class R2Data0 {
	private int oldName = 0;
	public void someMethod(){
		int oldName;
		/*1: ExtractLocalVariable(newName) :1*/
		oldName = this.oldName ;
		/*:1:*/
	}

}
