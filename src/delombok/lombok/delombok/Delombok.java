package lombok.delombok;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import lombok.delombok.CommentPreservingParser.ParseResult;

public class Delombok {
	private Charset charset = Charset.defaultCharset();
	private CommentPreservingParser parser = new CommentPreservingParser();
	private PrintStream feedback = System.err;
	private boolean verbose;
	private boolean force = false;
	
	/** If null, output to standard out. */
	private File output = null;
	
	public void setCharset(String charsetName) throws UnsupportedCharsetException {
		charset = Charset.forName(charsetName);
	}
	
	public void setForceProcess(boolean force) {
		this.force = force;
	}
	
	public void setFeedback(PrintStream feedback) {
		this.feedback = feedback;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public void setOutput(File dir) {
		if (dir.isFile()) throw new IllegalArgumentException(
				"DELOMBOK: delombok will only write to a directory. " +
				"If you want to delombok a single file, use -p to output to standard output, then redirect this to a file:\n" +
				"delombok MyJavaFile.java -p >MyJavaFileDelombok.java");
		output = dir;
	}
	
	public void setOutputToStandardOut() {
		this.output = null;
	}
	
	public void delombok(File base) throws IOException {
		delombok0(base, "", 0);
	}
	
	private void delombok0(File base, String suffix, int loop) throws IOException {
		File dir = suffix.isEmpty() ? base : new File(base, suffix);
		String name = suffix + File.separator + dir.getName();
		
		if (dir.isDirectory()) {
			if (loop >= 100) {
				feedback.printf("Over 100 subdirectories? I'm guessing there's a loop in your directory structure. Skipping: %s\n", suffix);
			} else {
				dir.mkdir();
				delombok0(base, name, loop + 1);
			}
		} else if (dir.isFile()) {
			String extension = getExtension(dir);
			if (extension.equals(".java")) delombok(base, name);
			else if (extension.equals(".class")) skipClass(name);
			else copy(base, name);
		} else {
			feedback.printf("Skipping %s because it is a special file type.\n", canonical(dir));
		}
	}
	
	private void skipClass(String fileName) {
		if (verbose) feedback.printf("Skipping class file: %s\n", fileName);
	}
	
	private void copy(File base, String fileName) throws IOException {
		if (output == null) {
			feedback.printf("Skipping resource file: %s\n", fileName);
			return;
		}
		if (verbose) feedback.printf("Copying resource file: %s\n", fileName);
		byte[] b = new byte[65536];
		FileInputStream in = new FileInputStream(new File(base, fileName));
		try {
			FileOutputStream out = new FileOutputStream(new File(output, fileName));
			try {
				while (true) {
					int r = in.read(b);
					if (r == -1) break;
					out.write(b, 0, r);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}
	
	public void delombok(String file, Writer writer) throws IOException {
		ParseResult result = parser.parse(file, force);
		
		result.print(writer);
	}
	
	public void delombok(File base, String fileName) throws IOException {
		if (output != null && canonical(base).equals(canonical(output))) throw new IOException(
				"DELOMBOK: Output file and input file refer to the same filesystem location. Specify a separate path for output.");
		
		ParseResult result = parser.parse(new File(base, fileName).getAbsolutePath(), force);
		
		if (verbose) feedback.printf("File: %s [%s]\n", fileName, result.isChanged() ? "delombok-ed" : "unchanged");
		
		Writer rawWriter = output == null ? createStandardOutWriter() : createFileWriter(output, fileName);
		BufferedWriter writer = new BufferedWriter(rawWriter);
		
		try {
			result.print(writer);
		} finally {
			writer.close();
		}
	}
	
	private static String canonical(File dir) {
		try {
			return dir.getCanonicalPath();
		} catch (Exception e) {
			return dir.getAbsolutePath();
		}
	}
	
	private static String getExtension(File dir) {
		String name = dir.getName();
		int idx = name.lastIndexOf('.');
		return idx == -1 ? "" : name.substring(idx+1);
	}
	
	private Writer createFileWriter(File base, String fileName) throws IOException {
		File outFile = new File(base, fileName);
		outFile.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(outFile);
		return new OutputStreamWriter(out, charset);
	}
	
	private Writer createStandardOutWriter() {
		return new OutputStreamWriter(System.out, charset);
	}
}
