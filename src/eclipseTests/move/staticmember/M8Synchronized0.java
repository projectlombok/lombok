package move.moveStaticMember;

import lombok.*;

public class M8Synchronized0 {
	public targetClass targetfield = new targetClass();
	/*1: MoveStaticElement(field, targetfield) :1*/
	public static int a = method();
	/*:1:*/
	
	/*2: MoveStaticElement(parameter, targetparam) :2*/
	@Synchronized
	public static int method(targetClass targetparam){
		return a;
	}
	/*:2:*/

	@Synchronized
	private static int method() {
		return 0;
	}
}
