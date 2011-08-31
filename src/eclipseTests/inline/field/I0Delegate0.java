package inline.inlineField;



import java.util.HashMap;

import lombok.*;

public class I0Delegate0 {
	/*1: InlineField(object) :1*/
	@Delegate
	public String object = "keep it gangsta";
	/*:1:*/
	Object someObject = object; 
}