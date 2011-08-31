package I2;
import lombok.*;


public class I2LazyGetter {
	@Getter(lazy=true)
	private final int oldName = 1;
	public void someMethod(){
		/*1: InlineLocalVariable (oldName) :1*/
		int oldName = this.oldName;
		/*:1:*/
		System.out.println(oldName);
	}
}
