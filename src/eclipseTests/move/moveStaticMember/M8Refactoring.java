package move.moveStaticMember;


public class M8Refactoring {
	public targetClass target = new targetClass();
	/*1: MoveStaticElement(field, target) :1*/
	public static void method() {
		/*:1:*/
	}
	
	/*2: MoveStaticElement(parameter, target) :2*/
	public static void method(targetClass target){		
	}
	/*:2:*/
}