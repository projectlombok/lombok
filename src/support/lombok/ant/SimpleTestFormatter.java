package lombok.ant;

import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

public class SimpleTestFormatter implements JUnitResultFormatter {
    private PrintStream out = System.out;

    @Override
    public void addError(Test test, Throwable error) {
        logResult(test, "ERR");
        out.println(error.getMessage());
    }

    @Override
    public void addFailure(Test test, AssertionFailedError failure) {
        logResult(test, "FAIL");
        out.println(failure.getMessage());
    }

    @Override
    public void endTest(Test test) {
        logResult(test, "PASS");
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
    public void setSystemError(String err) {
        // don't echo test error output
    }

    @Override
    public void setSystemOutput(String out) {
        // don't echo test output
    }

    @Override
    public void startTestSuite(JUnitTest testSuite) throws BuildException { }

    private void logResult(Test test, String result) {
        out.println("[" + result + "] " + String.valueOf(test));
        out.flush();
    }
}
