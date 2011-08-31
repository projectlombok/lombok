package R4;

import java.io.*;
import java.io.InputStream;

import lombok.Cleanup;
/*1: RenameClass(R4CleanUp0New) :1*/
public class R4CleanUp0 {
	
	int oldName = 5;
	/*:1:*/
	/*2: RenameClass(R4ConAllArgs0) :2*/
	/*:2:*/
	public void method() throws IOException {
		
		@Cleanup
		InputStream in = new FileInputStream("");
		@Cleanup
		OutputStream out = new FileOutputStream("");
		
		byte[] b = new byte[10000];
		while (true) {
			int r = in.read(b);
			if (r == -1)
				break;
			out.write(b, oldName, r);
		}
	}
}
