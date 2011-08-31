package move.moveInstanceMethod;
import lombok.*;

public class M1Setter0 {
	@Setter
	public targetClass target = new targetClass();
	/*1: MoveInstanceMethod(field, target) :1*/
	public int method() {
		/*:1:*/
		return 0;
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	public void method(targetClass target){		
	}
	/*:2:*/
	class targetClass{
		
	}
}