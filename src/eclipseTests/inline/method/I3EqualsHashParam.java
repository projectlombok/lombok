package I3;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(exclude={"field"})
public class I3EqualsHashParam {
	int field=0;
	int field2=0;
	public void someMethod() {
		/*1: InlineMethod(anotherMethod) :1*/
		String oldName = anotherMethod();
		/*:1:*/
		System.out.println(oldName);
	}
	public String anotherMethod(){
		String oldName="inlined";
		return oldName;
	}
	
}
