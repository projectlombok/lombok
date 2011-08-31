package move.moveInstanceMethod;

import java.util.HashMap;

import lombok.*;

public class M1val0 {
	public targetClass target = new targetClass();

	/*1: MoveInstanceMethod(field, target) :1*/
	public int method() {
		/*:1:*/
		val map = new HashMap<Integer, targetClass>();
		map.put(0, target);
		map.put(5, target);
		for (val entry : map.entrySet()) {
			System.out.printf("%d: %s\n", entry.getKey(), entry.getValue());
		}
		return 0;
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	public int method(targetClass target) {
		/*:1:*/
		val map = new HashMap<Integer, targetClass>();
		map.put(0, this.target);
		map.put(5, this.target);
		for (val entry : map.entrySet()) {
			System.out.printf("%d: %s\n", entry.getKey(), entry.getValue());
		}
		return 0;
	}
	class targetClass{
		
	}
}