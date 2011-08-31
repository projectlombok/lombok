package move.moveInstanceMethod;

import lombok.*;

public class M1Delegate0 {
	@Delegate
	private final targetClass target = new targetClass();
	/*1: MoveInstanceMethod(field, target) :1*/
	public targetClass method() {
		/*:1:*/
		return target;
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	public targetClass[] method(targetClass target){
		targetClass[] array = {this.target, target};
		return array;
	}
	/*:2:*/
	class targetClass{
		public void delegateMethod(){
			
		}
	}
}