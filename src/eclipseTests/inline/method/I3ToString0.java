package I3;
import lombok.ToString;

@ToString
public class I3ToString0 {
	
	public int oldName = 0;

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
