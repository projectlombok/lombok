package R2;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(exclude={"field"})
public class R2EqualsHashParam {

	public void someMethod(int oldName){
		/*1: RenameLocalVariable(oldName, newfield) :1*/
		oldName  = 1;
		/*:1:*/
		System.out.println(oldName);
	}
	
}
