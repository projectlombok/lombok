//ignore
package R4;
import lombok.*;
public class R4Getter1 {
	
	
	/*1: RenameClass(R4Getter1) :1*/
	@Getter
	public int oldName = 1;
	/*:1:*/
}

class GetterUser {
	void use() {
		int f = new R4Getter1().getOldName();
	}
}
