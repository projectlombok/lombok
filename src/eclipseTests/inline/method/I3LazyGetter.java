package I3;
import lombok.*;


public class I3LazyGetter {
	@Getter(lazy=true)
	private final int oldName = 1;

	
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
