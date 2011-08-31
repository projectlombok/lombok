package move.moveInstanceMethod;

public class M1Refactoring {
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