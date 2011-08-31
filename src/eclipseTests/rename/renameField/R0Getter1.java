//ignore
package R0;
import lombok.*;
public class R0Getter1 {
	
	
	/*1: RenameField(oldName, newfield) :1*/
	@Getter
	public int oldName = 1;
	/*:1:*/
}

class GetterUser {
	void use() {
		int f = new R0Getter1().getOldName();
	}
}
