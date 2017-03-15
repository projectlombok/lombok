//VERSION 7:
import static java.lang.String.*;

import java.io.*;

class ExoticJava<V> {
	public <T> ExoticJava(T genericsInConstructor, V genericsInType) {
		System.out.println(new <String>ExoticJava<Integer>("Hello", 5));
	}
	;;;;
	public void test() {
		int x = 5, y[] = {10};
		;
		class MethodLocal implements Serializable, java.util.RandomAccess {
			@SuppressWarnings({"unchecked", "rawtypes"})
			strictfp public final int foo() {
				int x = super.hashCode();
				x <<= 5;
				do {
					x <<= 5;
				} while (Boolean.FALSE);
				return x;
			}
		}
		
		for (int i = 10, j[] = {20}; i < 5; i++, j[0]++) {
			String z = "";
			try (PrintWriter pw = new PrintWriter(System.out); PrintWriter p2 = new PrintWriter(System.out)) {
				pw.println();
			} finally {
				synchronized (z) {
					System.out.println(z);
				}
			}
			
			if ((y == null)) {}
			if (((y == null))) ;
			{;}
			java.util.List<String> list = new java.util.ArrayList<>();
			assert Boolean.TRUE : "That's weird";
			double d = -1.8e12;
			long loooong = 0x1234ABCD;
			int octal = 0127;
		}
	}
}