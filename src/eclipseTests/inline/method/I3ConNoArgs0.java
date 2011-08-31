package I3;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class I3ConNoArgs0 {
	
	public void someMethod() {
		/*1: InlineMethod(anotherMethod) :1*/
		int oldName = anotherMethod();
		/*:1:*/
		System.out.println(oldName);
	}
	public int anotherMethod(){
		String oldName="inlined";
		System.out.println(oldName);
		return oldName.length();

}

}
