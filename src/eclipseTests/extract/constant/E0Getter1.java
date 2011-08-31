//ignore
package E0;
import lombok.*;
public class E0Getter1 {
	
	
	/*1: ExtractConstant(5, CONSTANT) :1*/
	@Getter
	public int oldName = 1;
	/*:1:*/
}

class GetterUser {
	void use() {
		int f = new E0Getter1().getOldName();
	}
}
