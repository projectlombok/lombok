package lombok.website;

import com.petebevin.markdown.MarkdownProcessor;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CompileChangelog {
	public static void main(String[] args) {
		try {
			FileInputStream in = new FileInputStream(args[0]);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[65536];
			while (true) {
				int r = in.read(b);
				if ( r == -1 ) break;
				out.write(b, 0, r);
			}
			in.close();
			String markdown = new String(out.toByteArray(), "UTF-8");
			String html = new MarkdownProcessor().markdown(markdown);
			FileOutputStream file = new FileOutputStream(args[1]);
			file.write(html.getBytes("UTF-8"));
			file.close();
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
