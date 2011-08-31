package I3;
import java.util.Collection;

import lombok.*;


@EqualsAndHashCode
public class I3EqualsHash0 {
	
	public void someMethod() {
		/*1: InlineMethod(anotherMethod) :1*/
		String oldName = anotherMethod();
		/*:1:*/
		System.out.println(oldName);
	}
	public String anotherMethod(){
		/*1: InlineLocalVariable (oldName) :1*/
		String oldName="inlined";
		/*:1:*/
		return oldName;
	}
}
