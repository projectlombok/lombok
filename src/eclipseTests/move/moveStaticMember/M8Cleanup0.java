package move.moveStaticMember;
import java.io.*;

import lombok.*;

@Data
public class M8Cleanup0 {
	public static targetClass target = new targetClass();
	
	/*1: MoveStaticElement(field, target) :1*/
	public static int a=0;
	/*:1:*/
	
	/*2: MoveStaticElement(parameter, target) :2*/
	public static int method(targetClass target) throws IOException {
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
	}
}