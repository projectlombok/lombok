package move.moveInstanceMethod;
import lombok.*;

@ToString
public class M1ToString0 {
	public targetClass target = new targetClass();
	/*1: MoveInstanceMethod(field, target) :1*/
	public targetClass method() {
		/*:1:*/
		return target;
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	public void method(targetClass target){		
	}
	/*:2:*/
	class targetClass{
		
	}
}