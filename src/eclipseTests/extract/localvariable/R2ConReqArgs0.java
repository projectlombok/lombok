package R2;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class R2ConReqArgs0 {

	public void someMethod(){
		int oldName;
		/*1: ExtractLocalVariable(newName) :1*/
		oldName = 5;
		/*:1:*/
	}
	
}
