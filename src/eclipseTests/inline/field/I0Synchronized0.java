package inline.inlineField;

import lombok.*;
public class I0Synchronized0 {
	
	/*1: InlineField(target) :1*/
	static final int target = 0;
	/*:1:*/
	
	@Synchronized
	public int method(){
		return target;
	}
}