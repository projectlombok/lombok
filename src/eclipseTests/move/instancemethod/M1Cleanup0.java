package move.moveInstanceMethod;
import java.io.*;

import lombok.*;

public class M1Cleanup0 {
	public targetClass target = new targetClass();
	
	/*1: MoveInstanceMethod(field, target) :1*/
	public int method() throws IOException {
		@Cleanup
		InputStream in = new FileInputStream("abc");
		@Cleanup
		OutputStream out = new FileOutputStream("def");
		byte[] b = new byte[10000];
		while (true) {
			int r = in.read(b);
			if (r == -1){
				break;
			}
			out.write(b, 0, r);
		}
		/*:1:*/
		return 0;
	}
	
	/*2: MoveInstanceMethod(parameter, target) :2*/
	public int method(targetClass target) throws IOException {
		@Cleanup
		InputStream in = new FileInputStream("abc");
		@Cleanup
		OutputStream out = new FileOutputStream("def");
		byte[] b = new byte[10000];
		while (true) {
			int r = in.read(b);
			if (r == -1){
				break;
			}
			out.write(b, 0, r);
		}
		/*:2:*/
		return 0;
	}
	class targetClass{
	
	}
}

