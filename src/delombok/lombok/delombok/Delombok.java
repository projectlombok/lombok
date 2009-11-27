package lombok.delombok;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

import lombok.delombok.CommentPreservingParser.ParseResult;

import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.Excludes;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Mandatory;
import com.zwitserloot.cmdreader.Parameterized;
import com.zwitserloot.cmdreader.Sequential;
import com.zwitserloot.cmdreader.Shorthand;

public class Delombok {
	private Charset charset = Charset.defaultCharset();
	private CommentPreservingParser parser = new CommentPreservingParser();
	private PrintStream feedback = System.err;
	private boolean verbose;
	private boolean force = false;
	
	/** If null, output to standard out. */
	private File output = null;
	
	private static class CmdArgs {
		@Shorthand("v")
		@Description("Print the name of each file as it is being delombok-ed.")
		@Excludes("quiet")
		private boolean verbose;
		
		@Shorthand("q")
		@Description("No warnings or errors will be emitted to standard error")
		@Excludes("verbose")
		private boolean quiet;
		
		@Shorthand("e")
		@Description("Sets the encoding of your source files. Defaults to the system default charset. Example: \"UTF-8\"")
		@Parameterized
		private String encoding;
		
		@Shorthand("p")
		@Description("Print delombok-ed code to standard output instead of saving it in target directory")
		private boolean print;
		
		@Shorthand("d")
		@Description("Directory to save delomboked files to")
		@Mandatory(onlyIfNot="print")
		@Parameterized
		private String target;
		
		@Description("Files to delombok. Provide either a file, or a directory. If you use a directory, all files in it (recursive) are delombok-ed")
		@Sequential
		@Parameterized
		private List<String> input = new ArrayList<String>();
		
		private boolean help;
	}
	
	public static void main(String[] rawArgs) {
		CmdReader<CmdArgs> reader = CmdReader.of(CmdArgs.class);
		CmdArgs args;
		try {
			args = reader.make(rawArgs);
		} catch (InvalidCommandLineException e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println(reader.generateCommandLineHelp("delombok"));
			System.exit(1);
			return;
		}
		
		if (args.help || args.input.isEmpty()) {
			if (args.input.isEmpty()) System.err.println("ERROR: no files or directories to delombok specified.");
			System.err.println(reader.generateCommandLineHelp("delombok"));
			System.exit(args.input.isEmpty() ? 1 : 0);
			return;
		}
		
		Delombok delombok = new Delombok();
		
		if (args.quiet) delombok.setFeedback(new PrintStream(new OutputStream() {
			@Override public void write(int b) throws IOException {
				//dummy - do nothing.
			}
		}));
		
		if (args.encoding != null) {
			try {
				delombok.setCharset(args.encoding);
			} catch (UnsupportedCharsetException e) {
				System.err.println("ERROR: Not a known charset: " + args.encoding);
				System.exit(1);
				return;
			}
		}
		
		if (args.verbose) delombok.setVerbose(true);
		if (args.print) delombok.setOutputToStandardOut();
		else delombok.setOutput(new File(args.target));
		
		for (String in : args.input) {
			try {
				File f = new File(in);
				if (f.isFile()) {
					delombok.delombok(f.getParentFile(), f.getName());
				} else if (f.isDirectory()) {
					delombok.delombok(f);
				} else if (!f.exists()) {
					if (!args.quiet) System.err.println("WARNING: does not exist - skipping: " + f);
				} else {
					if (!args.quiet) System.err.println("WARNING: not a standard file or directory - skipping: " + f);
				}
			} catch (Exception e) {
				if (!args.quiet) {
					String msg = e.getMessage();
					if (msg != null && msg.startsWith("DELOMBOK: ")) System.err.println(msg.substring("DELOMBOK: ".length()));
					else {
						e.printStackTrace();
					}
					System.exit(1);
					return;
				}
			}
		}
	}
	
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
		if (dir.isFile() || (!dir.isDirectory() && dir.getName().endsWith(".java"))) throw new IllegalArgumentException(
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
		String name = suffix;
		
		if (dir.isDirectory()) {
			if (loop >= 100) {
				feedback.printf("Over 100 subdirectories? I'm guessing there's a loop in your directory structure. Skipping: %s\n", suffix);
			} else {
				for (File f : dir.listFiles()) {
					delombok0(base, suffix + (suffix.isEmpty() ? "" : File.separator) + f.getName(), loop + 1);
				}
			}
		} else if (dir.isFile()) {
			String extension = getExtension(dir);
			if (extension.equals("java")) delombok(base, name);
			else if (extension.equals("class")) skipClass(name);
			else copy(base, name);
		} else if (!dir.exists()) {
			feedback.printf("Skipping %s because it does not exist.\n", canonical(dir));
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
		File outFile = new File(base, fileName);
		outFile.getParentFile().mkdirs();
		FileInputStream in = new FileInputStream(outFile);
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
