/*
 * Copyright (C) 2015 The Project Lombok Authors.
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
package lombok.javac.apt;

import static javax.tools.StandardLocation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;

import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Options;

import lombok.permit.Permit;

/**
 * This processor should not be used. It used to be THE processor. This class is only there to warn people that something went wrong, and for the
 * lombok developers to see if what the reason for those failures is. 
 */
@Deprecated
@SupportedAnnotationTypes("*")
public class Processor extends AbstractProcessor {
	
	/** {@inheritDoc} */
	@Override public void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		if (System.getProperty("lombok.disable") != null) {
			return;
		}
		procEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Wrong usage of 'lombok.javac.apt.Processor'. " + report(procEnv));
	}
	
	private String report(ProcessingEnvironment procEnv) {
		String data = collectData(procEnv);
		try {
			return writeFile(data);
		} catch (Exception e) {
			return "Report:\n\n" + data;
		}
	}
	
	private String writeFile(String data) throws IOException {
		File file = File.createTempFile("lombok-processor-report-", ".txt");
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
		writer.write(data);
		writer.close();
		return "Report written to '" + file.getCanonicalPath() + "'\n";
	}

	private String collectData(ProcessingEnvironment procEnv) {
		StringBuilder message = new StringBuilder();
		message.append("Problem report for usages of 'lombok.javac.apt.Processor'\n\n");
		listOptions(message, procEnv);
		findServices(message, procEnv.getFiler());
		addStacktrace(message);
		listProperties(message);
		return message.toString();
	}
	
	private void listOptions(StringBuilder message, ProcessingEnvironment procEnv) {
		try {
			JavacProcessingEnvironment environment = (JavacProcessingEnvironment) procEnv;
			Options instance = Options.instance(environment.getContext());
			Field field = Permit.getField(Options.class, "values");
			@SuppressWarnings("unchecked") Map<String, String> values = (Map<String, String>) field.get(instance);
			if (values.isEmpty()) {
				message.append("Options: empty\n\n");
				return;
			}
			message.append("Compiler Options:\n");
			for (Map.Entry<String, String> value : values.entrySet()) {
				message.append("- ");
				string(message, value.getKey());
				message.append(" = ");
				string(message, value.getValue());
				message.append("\n");
			}
			message.append("\n");
		} catch (Exception e) {
			message.append("No options available\n\n");
		}
		
	}

	private void findServices(StringBuilder message, Filer filer) {
		try {
			Field filerFileManagerField = Permit.getField(JavacFiler.class, "fileManager");
			JavaFileManager jfm = (JavaFileManager) filerFileManagerField.get(filer);
			ClassLoader processorClassLoader = jfm.hasLocation(ANNOTATION_PROCESSOR_PATH) ? jfm.getClassLoader(ANNOTATION_PROCESSOR_PATH) : jfm.getClassLoader(CLASS_PATH);
			Enumeration<URL> resources = processorClassLoader.getResources("META-INF/services/javax.annotation.processing.Processor");
			if (!resources.hasMoreElements()) {
				message.append("No processors discovered\n\n");
				return;
			}
			message.append("Discovered processors:\n");
			while (resources.hasMoreElements()) {
				URL processorUrl = resources.nextElement();
				message.append("- '").append(processorUrl).append("'");
				InputStream content = (InputStream) processorUrl.getContent();
				if (content != null) try {
					InputStreamReader reader = new InputStreamReader(content, "UTF-8");
					StringWriter sw = new StringWriter();
					char[] buffer = new char[8192];
					int read = 0;
					while ((read = reader.read(buffer))!= -1) {
						sw.write(buffer, 0, read);
					}
					String wholeFile = sw.toString();
					if (wholeFile.contains("lombok.javac.apt.Processor")) {
						message.append(" <= problem\n");
					} else {
						message.append(" (ok)\n");
					}
					message.append("    ").append(wholeFile.replace("\n", "\n    ")).append("\n");
				} finally {
					content.close();
				}
			}
		} catch (Exception e) {
			message.append("Filer information unavailable\n");
		}
		message.append("\n");
	}

	private void addStacktrace(StringBuilder message) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements != null) {
			message.append("Called from\n");
			for (int i = 1; i < stackTraceElements.length; i++) {
				StackTraceElement element = stackTraceElements[i];
				if (!element.getClassName().equals("lombok.javac.apt.Processor")) message.append("- ").append(element).append("\n"); 
			}
		} else {
			message.append("No stacktrace available\n");
		}
		message.append("\n");
	}

	private void listProperties(StringBuilder message) {
		Properties properties = System.getProperties();
		ArrayList<String> propertyNames = new ArrayList<String>(properties.stringPropertyNames());
		Collections.sort(propertyNames);
		message.append("Properties: \n");
		for (String propertyName : propertyNames) {
			if (propertyName.startsWith("user.")) continue;
			message.append("- ").append(propertyName).append(" = ");
			string(message, System.getProperty(propertyName));
			message.append("\n");
		}
		message.append("\n");
	}
	
	private static void string(StringBuilder sb, String s) {
		if (s == null) {
			sb.append("null");
			return;
		}
		sb.append("\"");
		for (int i = 0; i < s.length(); i++) sb.append(escape(s.charAt(i)));
		sb.append("\"");
	}
	
	private static String escape(char ch) {
		switch (ch) {
		case '\b': return "\\b";
		case '\f': return "\\f";
		case '\n': return "\\n";
		case '\r': return "\\r";
		case '\t': return "\\t";
		case '\'': return "\\'";
		case '\"': return "\\\"";
		case '\\': return "\\\\";
		default:
			if (ch < 32) return String.format("\\%03o", (int) ch);
			return String.valueOf(ch);
		}
	}

	/**
	 * We just return the latest version of whatever JDK we run on. Stupid? Yeah, but it's either that or warnings on all versions but 1.
	 */
	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.values()[SourceVersion.values().length - 1];
	}

	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		return false;
	}
}
