package S1;
import nonExistingPackage.*;

import java.io.*;

import lombok.Cleanup;

public class S1CleanUp0 {
	
	/* 1: ToggleComment() :1 */
	private static void main(String[] args) throws IOException {
		@Cleanup
		InputStream in = new FileInputStream(args[0]);
		@Cleanup
		OutputStream out = new FileOutputStream(args[1]);
		byte[] b = new byte[10000];
		while (true) {
			int r = in.read(b);
			if (r == -1) break;
			out.write(b, 0, r);
		}
	}
	/* :1: */
}
