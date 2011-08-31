package move.moveInstanceMethod;
import lombok.*;

public class M1Getter0 {
	@Getter
	public targetClass target = new targetClass();
	/*1: MoveInstanceMethod(field, target) :1*/
	public void method() {
		/*:1:*/
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	public void method(targetClass target){		
	}
	/*:2:*/
	class targetClass{
		
	}
}