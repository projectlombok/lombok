package R2;
import lombok.*;


public class R2LazyGetter {
	@Getter(lazy=true)
	private final int oldName = 1;
	public void someMethod(int oldName){
		/*1: RenameLocalVariable (oldName, newfield) :1*/
		oldName = this.oldName;
		/*:1:*/
		System.out.println(oldName);
	}
}
