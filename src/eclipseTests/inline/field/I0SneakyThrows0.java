package inline.inlineField;

import lombok.*;
public class I0SneakyThrows0 {
	
	/*1: InlineField(target) :1*/
	static final int target = 0;
	/*:1:*/
	
	@SneakyThrows
	public int method(){
		return target;
	}
}