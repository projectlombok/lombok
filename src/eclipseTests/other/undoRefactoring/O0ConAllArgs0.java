package myTests.other.undoRefactoring;

import lombok.*;
@AllArgsConstructor
public class O0ConAllArgs0 {
	public static int a = 0;
	
	public static void method(){
		/*1: ExtractMethod(methodName) :1*/
		System.out.println(a);
		/*:1:*/
	}
	
	/*2: UndoRefactoring() :2*/
	/*:2:*/
}
