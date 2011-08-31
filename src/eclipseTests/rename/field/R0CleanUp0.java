package R0;

import java.io.*;
import java.io.InputStream;

import lombok.Cleanup;

public class R0CleanUp0 {
	
	/*1: RenameField(oldName, newField) :1*/
	int oldName = 5;
	/*:1:*/
	
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
