package R2;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(exclude={"field"})
public class R2EqualsHashParam {

	public void someMethod(){
		int oldName;
		/*1: ExtractLocalVariable(newName) :1*/
		oldName = 5;
		/*:1:*/
	}
	
}
