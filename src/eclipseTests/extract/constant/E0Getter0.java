package E0;
import lombok.*;
public class E0Getter0 {
	/*1: ExtractConstant(5, CONSTANT) :1*/
	@Getter
	public int oldName = 1;
	/*:1:*/
	
	public int newName = oldName = 5;
	
}
