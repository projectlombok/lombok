package inline.inlineField;

import java.io.*;
import lombok.*;
@RequiredArgsConstructor
public class I0Cleanup0 {
	
	/*1: InlineField(target) :1*/
	static final String target = "albalb";
	/*:1:*/
	int anotherField;
	
	public void method() throws IOException {
		@Cleanup
		InputStream in = new FileInputStream(target);
		@Cleanup
		OutputStream out = new FileOutputStream("blabla");
		byte[] b = new byte[10000];
		while (true) {
			int r = in.read(b);
			if (r == -1){
				break;
			}
			out.write(b, 0, r);
		}
	}
}