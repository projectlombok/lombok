package R2;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class R2ConReqArgs0 {

	public void someMethod(int oldName){
		
		
		/*1: RenameLocalVariable (oldName, newfield) :1*/
		oldName=0;
		/*:1:*/
		System.out.println(oldName);
	}
	
}
