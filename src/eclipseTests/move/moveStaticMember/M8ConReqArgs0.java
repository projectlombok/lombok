package move.moveStaticMember;

import lombok.*;

@RequiredArgsConstructor
public class M8ConReqArgs0 {
	public targetClass targetfield;
	/*1: MoveStaticElement(field, targetfield) :1*/
	public static int a = 0;
	/*:1:*/
	
	/*2: MoveStaticElement(parameter, targetparam) :2*/
	public static int method(targetClass targetparam){
		return a;
	}
	/*:2:*/
}
