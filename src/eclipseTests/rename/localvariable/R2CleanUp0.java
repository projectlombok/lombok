package R2;

import java.io.*;
import java.io.InputStream;

import lombok.Cleanup;

public class R2CleanUp0 {
	
	
	public void method(int oldName) throws IOException {
		/*1: RenameLocalVariable(oldName, newName) :1*/
		oldName = 5;
		/*:1:*/
		
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
