package R2;

import lombok.val;

public class R2Val0 {
		public void someMethod(){
			String oldName = "Able was I ere I saw Elba";
			/*1: ExtractLocalVariable(newName) :1*/
			val name = new String(oldName);
			/*:1:*/
			System.out.println(name);
		}
}