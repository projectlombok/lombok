/*
 * Copyright (C) 2009-2019 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.delombok;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import lombok.Lombok;
import lombok.javac.CommentCatcher;
import lombok.javac.Javac;
import lombok.javac.JavacAugments;
import lombok.javac.LombokOptions;
import lombok.javac.apt.LombokProcessor;
import lombok.permit.Permit;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.file.BaseFileManager;
import com.sun.tools.javac.main.Arguments;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.Excludes;
import com.zwitserloot.cmdreader.FullName;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Mandatory;
import com.zwitserloot.cmdreader.Sequential;
import com.zwitserloot.cmdreader.Shorthand;

public class Delombok {
	private Charset charset = Charset.defaultCharset();
	private Context context = new Context();
	private Writer presetWriter;
	
	public void setWriter(Writer writer) {
		this.presetWriter = writer;
	}
	
	private PrintStream feedback = System.err;
	private boolean verbose;
	private boolean noCopy;
	private boolean onlyChanged;
	private boolean force = false;
	private boolean disablePreview;
	private String classpath, sourcepath, bootclasspath, modulepath, source;
	private LinkedHashMap<File, File> fileToBase = new LinkedHashMap<File, File>();
	private List<File> filesToParse = new ArrayList<File>();
	private Map<String, String> formatPrefs = new HashMap<String, String>();
	private List<AbstractProcessor> preLombokProcessors = new ArrayList<AbstractProcessor>();
	private List<AbstractProcessor> additionalAnnotationProcessors = new ArrayList<AbstractProcessor>();
	
	/** If null, output to standard out. */
	private File output = null;
	
	private static class CmdArgs {
		@Shorthand("v")
		@Description("Print the name of each file as it is being delombok-ed.")
		@Excludes("quiet")
		private boolean verbose;
		
		@Shorthand("f")
		@Description("Sets formatting rules. Use --format-help to list all available rules. Unset format rules are inferred by scanning the source for usages.")
		private List<String> format = new ArrayList<String>();
		
		@FullName("format-help")
		private boolean formatHelp;
		
		@Shorthand("q")
		@Description("No warnings or errors will be emitted to standard error")
		@Excludes("verbose")
		private boolean quiet;
		
		@Shorthand("e")
		@Description("Sets the encoding of your source files. Defaults to the system default charset. Example: \"UTF-8\"")
		private String encoding;
		
		@Shorthand("p")
		@Description("Print delombok-ed code to standard output instead of saving it in target directory")
		private boolean print;
		
		@Shorthand("d")
		@Description("Directory to save delomboked files to")
		@Mandatory(onlyIfNot={"print", "help", "format-help"})
		private String target;
		
		@Shorthand("c")
		@Description("Classpath (analogous to javac -cp option)")
		private String classpath;
		
		@Shorthand("s")
		@Description("Sourcepath (analogous to javac -sourcepath option)")
		private String sourcepath;
		
		@Description("override Bootclasspath (analogous to javac -bootclasspath option)")
		private String bootclasspath;
		
		@Description("Module path (analogous to javac --module-path option)")
		@FullName("module-path")
		private String modulepath;
		
		@Description("Source version (analogous to javac -source option)")
		private String source;
		
		@Description("Files to delombok. Provide either a file, or a directory. If you use a directory, all files in it (recursive) are delombok-ed")
		@Sequential
		private List<String> input = new ArrayList<String>();
		
		@Description("Lombok will only delombok source files. Without this option, non-java, non-class files are copied to the target directory.")
		@Shorthand("n")
		private boolean nocopy;
		
		@Description("Output only changed files (implies -n)")
		private boolean onlyChanged;
		
		@Description("By default lombok enables preview features if available (introduced in JDK 12). With this option, lombok won't do that.")
		@FullName("disable-preview")
		private boolean disablePreview;
		
		private boolean help;
	}
	
	static {
		LombokProcessor.addOpensForLombok();
	}
	
	private static String indentAndWordbreak(String in, int indent, int maxLen) {
		StringBuilder out = new StringBuilder();
		StringBuilder line = new StringBuilder();
		StringBuilder word = new StringBuilder();
		int len = in.length();
		for (int i = 0; i < (len + 1); i++) {
			char c = i == len ? ' ' : in.charAt(i);
			if (c == ' ') {
				if (line.length() + word.length() < maxLen) {
					line.append(word);
				} else {
					if (out.length() > 0) out.append("\n");
					for (int j = 0; j < indent; j++) out.append(" ");
					out.append(line);
					line.setLength(0);
					line.append(word.toString().trim());
				}
				word.setLength(0);
			}
			if (i < len) word.append(c);
		}
		if (line.length() > 0) {
			if (out.length() > 0) out.append("\n");
			for (int j = 0; j < indent; j++) out.append(" ");
			out.append(line);
		}
		
		return out.toString();
	}
	
	static String getPathOfSelf() {
		String url = Delombok.class.getResource("Delombok.class").toString();
		if (url.endsWith("lombok/delombok/Delombok.class")) {
			url = urlDecode(url.substring(0, url.length() - "lombok/delombok/Delombok.class".length()));
		} else if (url.endsWith("lombok/delombok/Delombok.SCL.lombok")) {
			url = urlDecode(url.substring(0, url.length() - "lombok/delombok/Delombok.SCL.lombok".length()));
		} else {
			return null;
		}
		if (url.startsWith("jar:file:") && url.endsWith("!/")) return url.substring(9, url.length() - 2);
		if (url.startsWith("file:")) return url.substring(5);
		return null;
	}
	
	private static String urlDecode(String in) {
		try {
			return URLDecoder.decode(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError("UTF-8 not supported");
		}
	}
	
	public static void main(String[] rawArgs) {
		try {
			rawArgs = fileExpand(rawArgs);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		CmdReader<CmdArgs> reader = CmdReader.of(CmdArgs.class);
		CmdArgs args;
		try {
			args = reader.make(rawArgs);
		} catch (InvalidCommandLineException e) {
			System.err.println("ERROR: " + e.getMessage());
			System.err.println(cmdHelp(reader));
			System.exit(1);
			return;
		}
		
		if (args.help || (args.input.isEmpty() && !args.formatHelp)) {
			if (!args.help) System.err.println("ERROR: no files or directories to delombok specified.");
			System.err.println(cmdHelp(reader));
			System.exit(args.help ? 0 : 1);
			return;
		}
		
		Delombok delombok = new Delombok();
		
		if (args.quiet) delombok.setFeedback(new PrintStream(new OutputStream() {
			@Override public void write(int b) throws IOException {
				//dummy - do nothing.
			}
		}));
		
		if (args.formatHelp) {
			System.out.println("Available format keys (to use, -f key:value -f key2:value2 -f ... ):");
			for (Map.Entry<String, String> e : FormatPreferences.getKeysAndDescriptions().entrySet()) {
				System.out.print("  ");
				System.out.print(e.getKey());
				System.out.println(":");
				System.out.println(indentAndWordbreak(e.getValue(), 4, 70));
			}
			System.out.println("Example: -f indent:4 -f emptyLines:indent");
			System.out.println("The '-f pretty' option is shorthand for '-f suppressWarnings:skip -f generated:skip -f danceAroundIdeChecks:skip -f generateDelombokComment:skip -f javaLangAsFQN:skip'");
			System.exit(0);
			return;
		}
		
		try {
			delombok.setFormatPreferences(formatOptionsToMap(args.format));
		} catch (InvalidFormatOptionException e) {
			System.out.println(e.getMessage() + " Try --format-help.");
			System.exit(1);
			return;
		}
		
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
		if (args.nocopy || args.onlyChanged) delombok.setNoCopy(true);
		if (args.disablePreview) delombok.setDisablePreview(true);
		if (args.onlyChanged) delombok.setOnlyChanged(true);
		if (args.print) {
			delombok.setOutputToStandardOut();
		} else {
			delombok.setOutput(new File(args.target));
		}
		
		if (args.classpath != null) delombok.setClasspath(args.classpath);
		if (args.sourcepath != null) delombok.setSourcepath(args.sourcepath);
		if (args.bootclasspath != null) delombok.setBootclasspath(args.bootclasspath);
		if (args.modulepath != null) delombok.setModulepath(args.modulepath);
		if (args.source != null) delombok.setSource(args.source);
		
		try {
			for (String in : args.input) {
				File f = new File(in).getAbsoluteFile();
				if (f.isFile()) {
					delombok.addFile(f.getParentFile(), f.getName());
				} else if (f.isDirectory()) {
					delombok.addDirectory(f);
				} else if (!f.exists()) {
					if (!args.quiet) System.err.println("WARNING: does not exist - skipping: " + f);
				} else {
					if (!args.quiet) System.err.println("WARNING: not a standard file or directory - skipping: " + f);
				}
			}
			
			delombok.delombok();
		} catch (Exception e) {
			if (!args.quiet) {
				String msg = e.getMessage();
				if (msg != null && msg.startsWith("DELOMBOK: ")) System.err.println(msg.substring("DELOMBOK: ".length()));
				else e.printStackTrace();
				System.exit(1);
				return;
			}
		}
	}
	
	private static String cmdHelp(CmdReader<CmdArgs> reader) {
		String x = reader.generateCommandLineHelp("delombok");
		int idx = x.indexOf('\n');
		return x.substring(0, idx) + "\n You can use @filename.args to read arguments from the file 'filename.args'.\n" + x.substring(idx);
	}
	
	private static String[] fileExpand(String[] rawArgs) throws IOException {
		String[] out = rawArgs;
		int offset = 0;
		for (int i = 0; i < rawArgs.length; i++) {
			if (rawArgs[i].length() > 0 && rawArgs[i].charAt(0) == '@') {
				String[] parts = readArgsFromFile(rawArgs[i].substring(1));
				String[] newOut = new String[out.length + parts.length - 1];
				System.arraycopy(out, 0, newOut, 0, i + offset);
				System.arraycopy(parts, 0, newOut, i + offset, parts.length);
				System.arraycopy(out, i + offset + 1, newOut, i + offset + parts.length, out.length - (i + offset + 1));
				offset += parts.length - 1;
				out = newOut;
			}
		}
		
		return out;
	}
	
	private static String[] readArgsFromFile(String file) throws IOException {
		InputStream in = new FileInputStream(file);
		StringBuilder s = new StringBuilder();
		try {
			InputStreamReader isr = new InputStreamReader(in, "UTF-8");
			try {
				char[] c = new char[4096];
				while (true) {
					int r = isr.read(c);
					if (r == -1) break;
					s.append(c, 0, r);
				}
			} finally {
				isr.close();
			}
		} finally {
			in.close();
		}
		
		List<String> x = new ArrayList<String>();
		StringBuilder a = new StringBuilder();
		int state = 1;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (state < 0) {
				state = -state;
				if (c != '\n') a.append(c);
				continue;
			}
			if (state == 1) {
				if (c == '\\') {
					state = -1;
					continue;
				}
				if (c == '"') {
					state = 2;
					continue;
				}
				if (c == '\'') {
					state = 3;
					continue;
				}
				if (Character.isWhitespace(c)) {
					String aa = a.toString();
					if (!aa.isEmpty()) x.add(aa);
					a.setLength(0);
					continue;
				}
				a.append(c);
				continue;
			}
			if (state == 2) {
				if (c == '\\') {
					state = -2;
					continue;
				}
				if (c == '"') {
					state = 1;
					x.add(a.toString());
					a.setLength(0);
					continue;
				}
				a.append(c);
				continue;
			}
			if (state == 3) {
				if (c == '\'') {
					state = 1;
					x.add(a.toString());
					a.setLength(0);
					continue;
				}
				a.append(c);
				continue;
			}
		}
		if (state == 1) {
			String aa = a.toString();
			if (!aa.isEmpty()) x.add(aa);
		} else if (state < 0) {
			throw new IOException("Unclosed backslash escape in @ file");
		} else if (state == 2) {
			throw new IOException("Unclosed \" in @ file");
		} else if (state == 3) {
			throw new IOException("Unclosed ' in @ file");
		}
		
		return x.toArray(new String[0]);
	}
	
	public static class InvalidFormatOptionException extends Exception {
		public InvalidFormatOptionException(String msg) {
			super(msg);
		}
	}
	
	public static Map<String, String> formatOptionsToMap(List<String> formatOptions) throws InvalidFormatOptionException {
		boolean prettyEnabled = false;
		Map<String, String> formatPrefs = new HashMap<String, String>();
		for (String format : formatOptions) {
			int idx = format.indexOf(':');
			if (idx == -1) {
				if (format.equalsIgnoreCase("pretty")) {
					prettyEnabled = true;
					continue;
				} else {
					throw new InvalidFormatOptionException("Format keys need to be 2 values separated with a colon.");
				}
			}
			String key = format.substring(0, idx);
			String value = format.substring(idx + 1);
			boolean valid = false;
			for (String k : FormatPreferences.getKeysAndDescriptions().keySet()) {
				if (k.equalsIgnoreCase(key)) {
					valid = true;
					break;
				}
			}
			if (!valid) throw new InvalidFormatOptionException("Unknown format key: '" + key + "'.");
			formatPrefs.put(key.toLowerCase(), value);
		}
		
		if (prettyEnabled) {
			if (!formatPrefs.containsKey("suppresswarnings")) formatPrefs.put("suppresswarnings", "skip");
			if (!formatPrefs.containsKey("generated")) formatPrefs.put("generated", "skip");
			if (!formatPrefs.containsKey("dancearoundidechecks")) formatPrefs.put("dancearoundidechecks", "skip");
			if (!formatPrefs.containsKey("generatedelombokcomment")) formatPrefs.put("generatedelombokcomment", "skip");
			if (!formatPrefs.containsKey("javalangasfqn")) formatPrefs.put("javalangasfqn", "skip");
		}
		
		return formatPrefs;
	}
	
	public void setFormatPreferences(Map<String, String> prefs) {
		this.formatPrefs = prefs;
	}
	
	public void setCharset(String charsetName) throws UnsupportedCharsetException {
		if (charsetName == null) {
			charset = Charset.defaultCharset();
			return;
		}
		charset = Charset.forName(charsetName);
	}
	
	public void setDiagnosticsListener(DiagnosticListener<JavaFileObject> diagnostics) {
		if (diagnostics != null) context.put(DiagnosticListener.class, diagnostics);
	}
	
	public void setForceProcess(boolean force) {
		this.force = force;
	}
	
	public void setFeedback(PrintStream feedback) {
		this.feedback = feedback;
	}
	
	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}
	
	public void setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
	}
	
	public void setBootclasspath(String bootclasspath) {
		this.bootclasspath = bootclasspath;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public void setNoCopy(boolean noCopy) {
		this.noCopy = noCopy;
	}
	
	public void setDisablePreview(boolean disablePreview) {
		this.disablePreview = disablePreview;
	}
	
	public void setOnlyChanged(boolean onlyChanged) {
		this.onlyChanged = onlyChanged;
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
	
	public void setModulepath(String modulepath) {
		this.modulepath = modulepath;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public void addDirectory(File base) throws IOException {
		addDirectory0(false, base, "", 0);
	}
	
	public void addDirectory1(boolean copy, File base, String name) throws IOException {
		File f = new File(base, name);
		if (f.isFile()) {
			String extension = getExtension(f);
			if (extension.equals("java")) addFile(base, name);
			else if (extension.equals("class")) skipClass(name);
			else copy(copy, base, name);
		} else if (!f.exists()) {
			feedback.printf("Skipping %s because it does not exist.\n", canonical(f));
		} else if (!f.isDirectory()) {
			feedback.printf("Skipping %s because it is a special file type.\n", canonical(f));
		}
	}
	
	private void addDirectory0(boolean inHiddenDir, File base, String suffix, int loop) throws IOException {
		File dir = suffix.isEmpty() ? base : new File(base, suffix);
		
		if (dir.isDirectory()) {
			boolean thisDirIsHidden = !inHiddenDir && new File(canonical(dir)).getName().startsWith(".");
			if (loop >= 100) {
				feedback.printf("Over 100 subdirectories? I'm guessing there's a loop in your directory structure. Skipping: %s\n", suffix);
			} else {
				File[] list = dir.listFiles();
				if (list.length > 0) {
					if (thisDirIsHidden && !noCopy && output != null) {
						feedback.printf("Only processing java files (not copying non-java files) in %s because it's a hidden directory.\n", canonical(dir));
					}
					for (File f : list) {
						addDirectory0(inHiddenDir || thisDirIsHidden, base, suffix + (suffix.isEmpty() ? "" : File.separator) + f.getName(), loop + 1);
					}
				} else {
					if (!thisDirIsHidden && !noCopy && !inHiddenDir && output != null && !suffix.isEmpty()) {
						File emptyDir = new File(output, suffix);
						emptyDir.mkdirs();
						if (verbose) feedback.printf("Creating empty directory: %s\n", canonical(emptyDir));
					}
				}
			}
		} else {
			addDirectory1(!inHiddenDir && !noCopy, base, suffix);
		}
	}
	
	private void skipClass(String fileName) {
		if (verbose) feedback.printf("Skipping class file: %s\n", fileName);
	}
	
	private void copy(boolean copy, File base, String fileName) throws IOException {
		if (output == null) {
			feedback.printf("Skipping resource file: %s\n", fileName);
			return;
		}
		
		if (!copy) {
			if (verbose) feedback.printf("Skipping resource file: %s\n", fileName);
			return;
		}
		
		if (verbose) feedback.printf("Copying resource file: %s\n", fileName);
		byte[] b = new byte[65536];
		File inFile = new File(base, fileName);
		FileInputStream in = new FileInputStream(inFile);
		try {
			File outFile = new File(output, fileName);
			outFile.getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(outFile);
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
	
	public void addFile(File base, String fileName) throws IOException {
		if (output != null && canonical(base).equals(canonical(output))) throw new IOException(
				"DELOMBOK: Output file and input file refer to the same filesystem location. Specify a separate path for output.");
		
		File f = new File(base, fileName);
		filesToParse.add(f);
		fileToBase.put(f, base);
	}
	
	public void addPreLombokProcessors(AbstractProcessor processor) {
		preLombokProcessors.add(processor);
	}

	public void addAdditionalAnnotationProcessor(AbstractProcessor processor) {
		additionalAnnotationProcessors.add(processor);
	}
	
	private static <T> com.sun.tools.javac.util.List<T> toJavacList(List<T> list) {
		com.sun.tools.javac.util.List<T> out = com.sun.tools.javac.util.List.nil();
		ListIterator<T> li = list.listIterator(list.size());
		while (li.hasPrevious()) out = out.prepend(li.previous());
		return out;
	}
	
	private static final Field MODULE_FIELD = getModuleField();
	private static Field getModuleField() {
		try {
			return Permit.getField(JCCompilationUnit.class, "modle");
		} catch (NoSuchFieldException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	public boolean delombok() throws IOException {
		LombokOptions options = LombokOptionsFactory.getDelombokOptions(context);
		options.deleteLombokAnnotations();
		options.putJavacOption("ENCODING", charset.name());
		if (classpath != null) options.putJavacOption("CLASSPATH", unpackClasspath(classpath));
		if (sourcepath != null) options.putJavacOption("SOURCEPATH", sourcepath);
		if (bootclasspath != null) options.putJavacOption("BOOTCLASSPATH", unpackClasspath(bootclasspath));
		if (source != null) options.putJavacOption("SOURCE", source);
		options.setFormatPreferences(new FormatPreferences(formatPrefs));
		options.put("compilePolicy", "check");
		
		if (Javac.getJavaCompilerVersion() >= 9) {
			Arguments args = Arguments.instance(context);
			List<String> argsList = new ArrayList<String>();
			if (classpath != null) {
				argsList.add("--class-path");
				argsList.add(options.get("--class-path"));
			}
			if (sourcepath != null) {
				argsList.add("--source-path");
				argsList.add(options.get("--source-path"));
			}
			if (bootclasspath != null) {
				argsList.add("--boot-class-path");
				argsList.add(options.get("--boot-class-path"));
			}
			if (source != null) {
				argsList.add("-source");
				if (Javac.getJavaCompilerVersion() < 12) {
					argsList.add(options.get("-source"));
				} else {
					argsList.add(options.get("--source"));
				}
			}
			if (charset != null) {
				argsList.add("-encoding");
				argsList.add(charset.name());
			}
			String pathToSelfJar = getPathOfSelf();
			if (pathToSelfJar != null) {
				argsList.add("--module-path");
				argsList.add((modulepath == null || modulepath.isEmpty()) ? pathToSelfJar : (pathToSelfJar + File.pathSeparator + modulepath));
			} else if (modulepath != null && !modulepath.isEmpty()) {
				argsList.add("--module-path");
				argsList.add(modulepath);
			}
			
			if (!disablePreview && Javac.getJavaCompilerVersion() >= 11) argsList.add("--enable-preview");
			if (Javac.getJavaCompilerVersion() >= 21) argsList.add("-proc:full");
			
			if (Javac.getJavaCompilerVersion() < 15) {
				String[] argv = argsList.toArray(new String[0]);
				args.init("javac", argv);
			} else {
				args.init("javac", argsList);
			}
			options.put("diags.legacy", "TRUE");
			options.put("allowStringFolding", "FALSE");
		} else {
			if (modulepath != null && !modulepath.isEmpty()) throw new IllegalStateException("DELOMBOK: Option --module-path requires usage of JDK9 or higher.");
		}
		
		CommentCatcher catcher = CommentCatcher.create(context, Javac.getJavaCompilerVersion() >= 13);
		JavaCompiler compiler = catcher.getCompiler();
		
		List<JCCompilationUnit> roots = new ArrayList<JCCompilationUnit>();
		Map<JCCompilationUnit, File> baseMap = new IdentityHashMap<JCCompilationUnit, File>();
		
		Set<AbstractProcessor> processors = new LinkedHashSet<AbstractProcessor>();
		processors.addAll(preLombokProcessors);
		processors.add(new lombok.javac.apt.LombokProcessor());
		processors.addAll(additionalAnnotationProcessors);
		
		if (Javac.getJavaCompilerVersion() >= 9) {
			JavaFileManager jfm_ = context.get(JavaFileManager.class);
			if (jfm_ instanceof BaseFileManager) {
				Arguments args = Arguments.instance(context);
				((BaseFileManager) jfm_).setContext(context); // reinit with options
				((BaseFileManager) jfm_).handleOptions(args.getDeferredFileManagerOptions());
			}
		}
		
		if (Javac.getJavaCompilerVersion() < 9) {
			compiler.initProcessAnnotations(processors);
		} else {
			compiler.initProcessAnnotations(processors, Collections.<JavaFileObject>emptySet(), Collections.<String>emptySet());
		}
		
		Object unnamedModule = null;
		if (Javac.getJavaCompilerVersion() >= 9) unnamedModule = Symtab.instance(context).unnamedModule;
		
		for (File fileToParse : filesToParse) {
			JCCompilationUnit unit = compiler.parse(fileToParse.getAbsolutePath());
			if (Javac.getJavaCompilerVersion() >= 9) try {
				MODULE_FIELD.set(unit, unnamedModule);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			baseMap.put(unit, fileToBase.get(fileToParse));
			roots.add(unit);
		}
		if (compiler.errorCount() > 0) {
			// At least one parse error. No point continuing (a real javac run doesn't either).
			return false;
		}
		
		for (JCCompilationUnit unit : roots) {
			catcher.setComments(unit, new DocCommentIntegrator().integrate(catcher.getComments(unit), unit));
		}
		
		if (Javac.getJavaCompilerVersion() >= 9) {
			compiler.initModules(com.sun.tools.javac.util.List.from(roots.toArray(new JCCompilationUnit[0])));
		}
		com.sun.tools.javac.util.List<JCCompilationUnit> trees = compiler.enterTrees(toJavacList(roots));
		
		JavaCompiler delegate;
		if (Javac.getJavaCompilerVersion() < 9) {
			delegate = compiler.processAnnotations(trees, com.sun.tools.javac.util.List.<String>nil());
		} else {
			delegate = compiler;
			Collection<String> c = com.sun.tools.javac.util.List.nil();
			compiler.processAnnotations(trees, c);
		}
		
		Object care = callAttributeMethodOnJavaCompiler(delegate, delegate.todo);
		
		callFlowMethodOnJavaCompiler(delegate, care);
		
		FormatPreferences fps = new FormatPreferences(formatPrefs);
		for (JCCompilationUnit unit : roots) {
			DelombokResult result = new DelombokResult(catcher.getComments(unit), catcher.getTextBlockStarts(unit), unit, force || options.isChanged(unit), fps);
			if (onlyChanged && !result.isChanged() && !options.isChanged(unit)) {
				if (verbose) feedback.printf("File: %s [%s]\n", unit.sourcefile.getName(), "unchanged (skipped)");
				continue;
			}
			ListBuffer<JCTree> newDefs = new ListBuffer<JCTree>();
			for (JCTree def : unit.defs) {
				if (def instanceof JCImport) {
					Boolean b = JavacAugments.JCImport_deletable.get((JCImport) def);
					if (b == null || !b.booleanValue()) newDefs.append(def);
				} else {
					newDefs.append(def);
				}
			}
			unit.defs = newDefs.toList();
			if (verbose) feedback.printf("File: %s [%s%s]\n", unit.sourcefile.getName(), result.isChanged() ? "delomboked" : "unchanged", force && !options.isChanged(unit) ? " (forced)" : "");
			Writer rawWriter;
			if (presetWriter != null) rawWriter = createUnicodeEscapeWriter(presetWriter);
			else if (output == null) rawWriter = createStandardOutWriter();
			else rawWriter = createFileWriter(output, baseMap.get(unit), unit.sourcefile.toUri());
			BufferedWriter writer = new BufferedWriter(rawWriter);
			try {
				result.print(writer);
			} finally {
				if (output != null) {
					writer.close();
				} else {
					writer.flush();
				}
			}
		}
		delegate.close();
		
		return true;
	}
	
	private String unpackClasspath(String cp) {
		String[] parts = cp.split(Pattern.quote(File.pathSeparator));
		StringBuilder out = new StringBuilder();
		for (String p : parts) {
			if (!p.endsWith("*")) {
				if (out.length() > 0) out.append(File.pathSeparator);
				out.append(p);
				continue;
			}
			File f = new File(p.substring(0, p.length() - 2));
			File[] files = f.listFiles();
			if (files == null) continue;
			for (File file : files) {
				if (file.isFile()) {
					if (out.length() > 0) out.append(File.pathSeparator);
					out.append(p, 0, p.length() - 1);
					out.append(file.getName());
				}
			}
		}
		return out.toString();
	}
	
	private static Method attributeMethod;
	/** Method is needed because the call signature has changed between javac6 and javac7; no matter what we compile against, using delombok in the other means VerifyErrors. */
	private static Object callAttributeMethodOnJavaCompiler(JavaCompiler compiler, Todo arg) {
		if (attributeMethod == null) {
			try {
				attributeMethod = Permit.getMethod(JavaCompiler.class, "attribute", java.util.Queue.class);
			} catch (NoSuchMethodException e) {
				try {
					attributeMethod = Permit.getMethod(JavaCompiler.class, "attribute", com.sun.tools.javac.util.ListBuffer.class);
				} catch (NoSuchMethodException e2) {
					throw Lombok.sneakyThrow(e2);
				}
			}
		}
		
		return Permit.invokeSneaky(attributeMethod, compiler, arg);
	}
	
	private static Method flowMethod;
	/** Method is needed because the call signature has changed between javac6 and javac7; no matter what we compile against, using delombok in the other means VerifyErrors. */
	private static void callFlowMethodOnJavaCompiler(JavaCompiler compiler, Object arg) {
		if (flowMethod == null) {
			try {
				flowMethod = Permit.getMethod(JavaCompiler.class, "flow", java.util.Queue.class);
			} catch (NoSuchMethodException e) {
				try {
					flowMethod = Permit.getMethod(JavaCompiler.class, "flow", com.sun.tools.javac.util.List.class);
				} catch (NoSuchMethodException e2) {
					throw Lombok.sneakyThrow(e2);
				}
			}
		}
		
		Permit.invokeSneaky(flowMethod, compiler, arg);
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
	
	private Writer createFileWriter(File outBase, File inBase, URI file) throws IOException {
		URI base = inBase.toURI();
		URI relative = base.relativize(base.resolve(file));
		File outFile;
		if (relative.isAbsolute()) {
			outFile = new File(outBase, new File(relative).getName());
		} else {
			outFile = new File(outBase, relative.getPath());
		}
		
		outFile.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(outFile);
		return createUnicodeEscapeWriter(out);
	}
	
	private Writer createStandardOutWriter() {
		return createUnicodeEscapeWriter(System.out);
	}
	
	private Writer createUnicodeEscapeWriter(Writer writer) {
		return new UnicodeEscapeWriter(writer, charset);
	}
	
	private Writer createUnicodeEscapeWriter(OutputStream out) {
		return new UnicodeEscapeWriter(new OutputStreamWriter(out, charset), charset);
	}
}
