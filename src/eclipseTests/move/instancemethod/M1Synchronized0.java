package move.moveInstanceMethod;
import lombok.*;

public class M1Synchronized0 {
	public targetClass target = new targetClass();
	/*1: MoveInstanceMethod(field, target) :1*/
	@Synchronized
	public targetClass method() {
		/*:1:*/
		return target;
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	@Synchronized
	public void method(targetClass target){		
	}
	/*:2:*/
	class targetClass{
		
	}
}