package I3;

import java.io.*;

import lombok.Cleanup;

public class I3CleanUp0 {
	public void someMethod() throws IOException{
		/*1: InlineMethod(anotherMethod) :1*/
		int oldName = anotherMethod();
		/*:1:*/
	}
	
	public int anotherMethod() throws IOException {
		@Cleanup
		InputStream in = new FileInputStream("");
		@Cleanup
		OutputStream out = new FileOutputStream("");
		
		byte[] b = new byte[10000];
		while (true) {
			int r = in.read(b);
			if (r == -1)
				break;
			out.write(b, 0, r);
		}
		return 3;
	}
}
