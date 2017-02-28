/*
 * Copyright (C) 2009-2015 The Project Lombok Authors.
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import lombok.Lombok;
import lombok.javac.CommentCatcher;
import lombok.javac.LombokOptions;
import lombok.javac.apt.LombokProcessor;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
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
	private boolean force = false;
	private String classpath, sourcepath, bootclasspath;
	private LinkedHashMap<File, File> fileToBase = new LinkedHashMap<File, File>();
	private List<File> filesToParse = new ArrayList<File>();
	private Map<String, String> formatPrefs = new HashMap<String, String>();
	
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
		
		@Description("Files to delombok. Provide either a file, or a directory. If you use a directory, all files in it (recursive) are delombok-ed")
		@Sequential
		private List<String> input = new ArrayList<String>();
		
		@Description("Lombok will only delombok source files. Without this option, non-java, non-class files are copied to the target directory.")
		@Shorthand("n")
		private boolean nocopy;
		
		private boolean help;
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
		
		if (args.help || (args.input.isEmpty() && !args.formatHelp)) {
			if (!args.help) System.err.println("ERROR: no files or directories to delombok specified.");
			System.err.println(reader.generateCommandLineHelp("delombok"));
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
		if (args.nocopy) delombok.setNoCopy(true);
		if (args.print) {
			delombok.setOutputToStandardOut();
		} else {
			delombok.setOutput(new File(args.target));
		}
		
		if (args.classpath != null) delombok.setClasspath(args.classpath);
		if (args.sourcepath != null) delombok.setSourcepath(args.sourcepath);
		if (args.bootclasspath != null) delombok.setBootclasspath(args.bootclasspath);
		
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
				else {
					e.printStackTrace();
				}
				System.exit(1);
				return;
			}
		}
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
	
	private static <T> com.sun.tools.javac.util.List<T> toJavacList(List<T> list) {
		com.sun.tools.javac.util.List<T> out = com.sun.tools.javac.util.List.nil();
		ListIterator<T> li = list.listIterator(list.size());
		while (li.hasPrevious()) out = out.prepend(li.previous());
		return out;
	}
	
	public boolean delombok() throws IOException {
		LombokOptions options = LombokOptionsFactory.getDelombokOptions(context);
		options.deleteLombokAnnotations();
		options.putJavacOption("ENCODING", charset.name());
		if (classpath != null) options.putJavacOption("CLASSPATH", classpath);
		if (sourcepath != null) options.putJavacOption("SOURCE_PATH", sourcepath);
		if (bootclasspath != null) options.putJavacOption("BOOT_CLASS_PATH", bootclasspath);
		options.setFormatPreferences(new FormatPreferences(formatPrefs));
		options.put("compilePolicy", "check");
		
		CommentCatcher catcher = CommentCatcher.create(context);
		JavaCompiler compiler = catcher.getCompiler();
		
		List<JCCompilationUnit> roots = new ArrayList<JCCompilationUnit>();
		Map<JCCompilationUnit, File> baseMap = new IdentityHashMap<JCCompilationUnit, File>();
		
		Set<LombokProcessor> processors = Collections.singleton(new lombok.javac.apt.LombokProcessor());
		for (Method m : compiler.getClass().getMethods()) {
			if (!m.getName().equals("initProcessAnnotations")) continue;
			Object[] parameters;
			if (m.getParameterTypes().length == 1) {
				parameters = new Object[] {processors};
			} else {
				parameters = new Object[] {processors, Collections.emptySet(), Collections.emptySet()};
			}
			try {
				m.invoke(compiler, parameters);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		Object unnamedModule = null;
		Field modle = null;
		try {
			unnamedModule = Symtab.class.getField("unnamedModule").get(Symtab.instance(context));
			modle = JCCompilationUnit.class.getField("modle");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		for (File fileToParse : filesToParse) {
			@SuppressWarnings("deprecation") JCCompilationUnit unit = compiler.parse(fileToParse.getAbsolutePath());
			if (modle != null) {
				try {
					modle.set(unit, unnamedModule);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
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
		
		com.sun.tools.javac.util.List<JCCompilationUnit> trees = compiler.enterTrees(toJavacList(roots));
		
		JavaCompiler delegate = compiler.processAnnotations(trees);
		
		Object care = callAttributeMethodOnJavaCompiler(delegate, delegate.todo);
		
		callFlowMethodOnJavaCompiler(delegate, care);
		FormatPreferences fps = new FormatPreferences(formatPrefs);
		for (JCCompilationUnit unit : roots) {
			DelombokResult result = new DelombokResult(catcher.getComments(unit), unit, force || options.isChanged(unit), fps);
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
	
	private static Method attributeMethod;
	/** Method is needed because the call signature has changed between javac6 and javac7; no matter what we compile against, using delombok in the other means VerifyErrors. */
	private static Object callAttributeMethodOnJavaCompiler(JavaCompiler compiler, Todo arg) {
		if (attributeMethod == null) {
			try {
				attributeMethod = JavaCompiler.class.getDeclaredMethod("attribute", java.util.Queue.class);
			} catch (NoSuchMethodException e) {
				try {
					attributeMethod = JavaCompiler.class.getDeclaredMethod("attribute", com.sun.tools.javac.util.ListBuffer.class);
				} catch (NoSuchMethodException e2) {
					throw Lombok.sneakyThrow(e2);
				}
			}
		}
		try {
			return attributeMethod.invoke(compiler, arg);
		} catch (Exception e) {
			if (e instanceof InvocationTargetException) throw Lombok.sneakyThrow(e.getCause());
			throw Lombok.sneakyThrow(e);
		}
	}
	
	private static Method flowMethod;
	/** Method is needed because the call signature has changed between javac6 and javac7; no matter what we compile against, using delombok in the other means VerifyErrors. */
	private static void callFlowMethodOnJavaCompiler(JavaCompiler compiler, Object arg) {
		if (flowMethod == null) {
			try {
				flowMethod = JavaCompiler.class.getDeclaredMethod("flow", java.util.Queue.class);
			} catch (NoSuchMethodException e) {
				try {
					flowMethod = JavaCompiler.class.getDeclaredMethod("flow", com.sun.tools.javac.util.List.class);
				} catch (NoSuchMethodException e2) {
					throw Lombok.sneakyThrow(e2);
				}
			}
		}
		try {
			flowMethod.invoke(compiler, arg);
		} catch (Exception e) {
			if (e instanceof InvocationTargetException) throw Lombok.sneakyThrow(e.getCause());
			throw Lombok.sneakyThrow(e);
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
