package I2;
import lombok.*;


@EqualsAndHashCode
public class I2EqualsHash0 {
	public void someMethod(){
		/*1: InlineLocalVariable (oldName) :1*/
		String oldName="inlined";
		/*:1:*/
		System.out.println(oldName);
	}
}
