import java.io.*;
import lombok.*;
@RequiredArgsConstructor
public class E1Cleanup0 {

	static final String target = "albalb";
	int anotherField;
	
	public void someMethod() throws IOException {
		/* 1:ExtractMethod(anotherMethod):1*/
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
		/*:1:*/
	}
}