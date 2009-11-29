package lombok;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class DirectoryRunner extends Runner {
	
	private final Description description;
	private final Map<String, Description> tests = new TreeMap<String, Description>();
	private final Throwable failure;
	private File beforeDirectory;
	private File afterDirectory;
	
	public DirectoryRunner(Class<?> testClass) {
		description = Description.createSuiteDescription(testClass);
		Throwable error = null;
		try {
			addTests(testClass);
		}
		catch (Throwable t) {
			error = t;
		}
		this.failure = error;
	}

	private void addTests(Class<?> testClass) throws Exception {
		Method beforeMethod = testClass.getDeclaredMethod("getBeforeDirectory");
		beforeDirectory = (File) beforeMethod.invoke(null);
		
		Method afterMethod = testClass.getDeclaredMethod("getAfterDirectory");
		afterDirectory = (File) afterMethod.invoke(null);
		
		for (File file : beforeDirectory.listFiles()) {
			Description testDescription = Description.createTestDescription(testClass, file.getName());
			description.addChild(testDescription);
			tests.put(file.getName(), testDescription);
		}
	}

	@Override
	public Description getDescription() {
		return description;
	}

	@Override
	public void run(RunNotifier notifier) {
		if (failure != null) {
			notifier.fireTestStarted(description);
			notifier.fireTestFailure(new Failure(description, failure));
			notifier.fireTestFinished(description);
			return;
		}
		
		for (Map.Entry<String, Description> entry : tests.entrySet()) {
			Description testDescription = entry.getValue();
			notifier.fireTestStarted(testDescription);
			try {
				if (!runTest(entry.getKey())) {
					notifier.fireTestIgnored(testDescription);
				}
			}
			catch (Throwable t) {
				notifier.fireTestFailure(new Failure(testDescription, t));
			}
			notifier.fireTestFinished(testDescription);
		}
	}

	private boolean runTest(String fileName) throws Throwable {
		File file = new File(beforeDirectory, fileName);
		if (mustIgnore(file)) {
			return false;
		}
		TestViaDelombok.compareFile(afterDirectory, file);
		return true;
	}

	private boolean mustIgnore(File file) throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		reader.close();
		return "//ignore".equals(line);
	}
}
