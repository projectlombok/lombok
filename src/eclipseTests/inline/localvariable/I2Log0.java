package I2;

import lombok.extern.java.Log;
import java.util.logging.*;

@Log
public class I2Log0 {
	
	
	public static void main(String[] args) {
		/*1: InlineLocalVariable(warning) :1*/
		String[] array = {"ABC"};
		String warning = array[0];
		/*:1:*/
		log.warning(warning);
	}
		
}
