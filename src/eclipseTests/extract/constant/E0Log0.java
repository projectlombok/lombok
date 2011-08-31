package E0;

import lombok.extern.java.Log;
import java.util.logging.*;

@Log
public class E0Log0 {
	
	/*1: ExtractConstant(5, CONSTANT) :1*/
	public static int oldName = 0;
	/*:1:*/
	
	public static void main(String[] args) {
		log.warning(String.valueOf(oldName));
	}
		
}
