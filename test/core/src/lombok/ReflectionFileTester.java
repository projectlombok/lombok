package lombok;

import java.io.File;
import java.lang.reflect.Method;

import org.junit.Test;

public class ReflectionFileTester {
	
	private final File before;
	private final File after;
	
	public ReflectionFileTester(String beforePath, String afterPath) {
		before = new File(beforePath);
		after = new File(afterPath);
		if (!before.isDirectory()) {
			throw new IllegalArgumentException(beforePath + " is not a directory");
		}
		if (!after.isDirectory()) {
			throw new IllegalArgumentException(afterPath + " is not a directory");
		}
	}
	
	public boolean verify(Class<?> clazz) {
		boolean result = true;
		for (File f : before.listFiles()) {
			String fileName = f.getName();
			if (!fileName.endsWith(".java")) {
				continue;
			}
			String methodName = "test" + fileName.substring(0, fileName.length() - 5);
			try {
				Method method = clazz.getDeclaredMethod(methodName);
				if (method.getAnnotation(Test.class) == null) {
					result = false;
					System.err.printf("Class %s method %s is not a @Test method\n", clazz.getName(), methodName);
				}
			} 
			catch (NoSuchMethodException e) {
				result = false;
				System.err.printf("Class %s has no method %s\n", clazz.getName(), methodName);
			}
		}
		return result;
	}
	
	public void test() throws Exception {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace.length < 3) {
			throw new Error("No stacktrace available");
		}
		String methodName = stackTrace[2].getMethodName();
		if (!methodName.startsWith("test")) {
			throw new IllegalStateException("test() should be called from a methos that starts with 'test'");
		}
		String fileName = methodName.substring(4).concat(".java");
		File testFile = new File(before, fileName);
		TestViaDelombok.compareFile(after, testFile);
	}
}
