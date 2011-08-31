package R2;
import lombok.ToString;

@ToString
public class R2ToString0 {
	
	public int oldName = 0;

	
	public static void method(R2ToString0 object) {
		/*1: RenameLocalVariable(oldName, newField) :1*/
		object = new R2ToString0();
		/*:1:*/
		System.out.println(object.toString());
	}

}
