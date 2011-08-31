package move.moveInstanceMethod;
import lombok.*;

public class M1SneakyThrows0 {
	public targetClass target = new targetClass();
	/*1: MoveInstanceMethod(field, target) :1*/
	@SneakyThrows
	public targetClass method() {
		/*:1:*/
		return target;
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	@SneakyThrows
	public void method(targetClass target){		
	}
	/*:2:*/
	class targetClass{
		
	}
}