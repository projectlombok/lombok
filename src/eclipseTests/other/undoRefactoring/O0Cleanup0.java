package myTests.other.undoRefactoring;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.*;
import myTests.move.moveStaticMember.targetClass;
@Data
public class O0Cleanup0 {
	
	
	public static int a=0;
	
	public static int method(targetClass target) throws IOException {
		/*1: ExtractMethod(newMethod) :1*/
		@Cleanup
		InputStream in = new FileInputStream("abc");
		@Cleanup
		OutputStream out = new FileOutputStream("def");
		/*:2:*/
		byte[] b = new byte[10000];
		while (true) {
			int r = in.read(b);
			if (r == -1){
				break;
			}
			out.write(b, 0, r);
		}
		return 0;
		/*:1:*/
	}
	/*2: UndoRefactoring() :2*/
	/*:2:*/
}
