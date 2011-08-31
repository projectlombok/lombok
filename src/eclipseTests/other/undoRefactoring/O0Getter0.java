package other.undoRefactoring;

import lombok.*;

public class O0Getter0 {
	@Getter
	public static int a = 0;
	
	public static void method(){
		/*1: ExtractMethod(methodName) :1*/
		System.out.println(a);
		/*:1:*/
	}
	
	/*2: UndoRefactoring() :2*/
	/*:2:*/
}
