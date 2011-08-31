package move.moveInstanceMethod;
import lombok.extern.java.Log;

@Log
public class M1Log0 {
	public targetClass target = new targetClass();
	/*1: MoveInstanceMethod(field, target) :1*/
	public targetClass method() {
		/*:1:*/
		log.warning("Oh no");
		return target;
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	public void method(targetClass target){
		log.warning("Oh no");
	}
	/*:2:*/
	class targetClass{
		
	}
}