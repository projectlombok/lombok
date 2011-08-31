package inline.inlineField;

import lombok.*;
import lombok.extern.java.Log;
@Log
public class I0Log0 {
	
	/*1: InlineField(target) :1*/
	static final int target = 0;
	/*:1:*/	
	public int method() {
		log.warning("Oh, no");
		return target;
	}
}