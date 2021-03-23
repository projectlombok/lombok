package lombok.ant;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

public class SimpleTestFormatter implements JUnitResultFormatter {
	private PrintStream out = System.out;
	private Test lastMarked = null;
	
	@Override
	public void addError(Test test, Throwable error) {
		lastMarked = test;
		logResult(test, "ERR");
		printThrowable(error, false, 2);
	}
	
	private void printThrowable(Throwable throwable, boolean cause, int indent) {
		String msg = throwable.getMessage();
		char[] prefixChars = new char[indent];
		Arrays.fill(prefixChars, ' ');
		String prefix = new String(prefixChars);
		
		if (msg == null || msg.isEmpty()) {
			out.println(prefix + (cause ? "Caused by " : "") + throwable.getClass());
		} else {
			out.println(prefix + (cause ? "Caused by " : "") + throwable.getClass() + ": " + msg);
		}
		StackTraceElement[] elems = throwable.getStackTrace();
		if (elems != null) for (StackTraceElement elem : elems) {
			out.println(prefix + "  " + elem);
		}
		
		Throwable c = throwable.getCause();
		if (c != null) printThrowable(c, true, indent + 2);
	}
	
	@Override
	public void addFailure(Test test, AssertionFailedError failure) {
		lastMarked = test;
		logResult(test, "FAIL");
		out.println(failure.getMessage());
	}
	
	@Override
	public void endTest(Test test) {
		if (test != lastMarked) logResult(test, "PASS");
	}
	
	@Override
	public void startTest(Test test) { }
	
	@Override
	public void endTestSuite(JUnitTest testSuite) throws BuildException { }
	
	@Override
	public void setOutput(OutputStream out) {
		this.out = new PrintStream(out);
	}
	
	@Override
	public void setSystemError(String msg) {
		if (msg.trim().isEmpty()) return;
		out.println(msg);
	}
	
	@Override
	public void setSystemOutput(String msg) {
		if (msg.trim().isEmpty()) return;
		out.println(msg);
	}
	
	@Override
	public void startTestSuite(JUnitTest testSuite) throws BuildException { }
	
	private void logResult(Test test, String result) {
		out.println("[" + result + "] " + String.valueOf(test));
		out.flush();
	}
}
