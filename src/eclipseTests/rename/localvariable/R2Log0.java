package R2;

import lombok.extern.java.Log;
import java.util.logging.*;

@Log
public class R2Log0 {
	
	
	public static void main(String[] args) {
		/*1: RenameLocalVariable(args, newName) :1*/
		args = new String[0];
		/*:1:*/
		log.warning(args);
	}
		
}
