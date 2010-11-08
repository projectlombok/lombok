import java.io.*;
class CleanupPlain {
	void test() throws Exception {
		InputStream in = new FileInputStream("in");
		try {
			OutputStream out = new FileOutputStream("out");
			try {
				if (in.markSupported()) {
					out.flush();
				}
			} finally {
				if (lombok.Lombok.preventNullAnalysis(out) != null) {
					out.close();
				}
			}
		} finally {
			if (lombok.Lombok.preventNullAnalysis(in) != null) {
				in.close();
			}
		}
	}
}
