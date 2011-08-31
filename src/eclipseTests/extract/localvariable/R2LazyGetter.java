package R2;
import lombok.*;


public class R2LazyGetter {
	@Getter(lazy=true)
	private final int oldName = 1;
	public void someMethod(){
		int oldName;
		/*1: ExtractLocalVariable(newName) :1*/
		oldName = 3+3;
		/*:1:*/
		System.out.println(oldName);
	}
}
