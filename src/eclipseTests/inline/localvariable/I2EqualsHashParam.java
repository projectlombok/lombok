package I2;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(exclude={"field"})
public class I2EqualsHashParam {

	public void someMethod(){
		/*1: InlineLocalVariable(oldName) :1*/
		int oldName  = 1;
		/*:1:*/
		System.out.println(oldName);
	}
	
}
