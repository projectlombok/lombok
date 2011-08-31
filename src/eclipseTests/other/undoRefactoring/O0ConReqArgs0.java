package myTests.other.undoRefactoring;

import lombok.*;
@RequiredArgsConstructor
public class O0ConReqArgs0 {
	public static int a;
	
	public static void method(){
		/*1: ExtractMethod(methodName) :1*/
		System.out.println(a);
		/*:1:*/
	}
	
	/*2: UndoRefactoring() :2*/
	/*:2:*/
}
