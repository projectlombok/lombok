package lombok.eclipse;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.runner.Description;

public class SetupBeforeAfterTest extends SetupTest {
	
	private File before;
	private File after;
	
	@Override
	protected void starting(Description description) {
		super.starting(description);
		
		before = new File(root, "/before/");
		after = new File(root, "/after/");
		
		try {
			copyBeforeFiles();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void succeeded(Description description) {
		try {
			compareWithAfter();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	private void compareWithAfter() throws FileNotFoundException, JavaModelException {
		for (ICompilationUnit result : packageFragment.getCompilationUnits()) {
			assertEquals(getContent(new File(after, result.getElementName())), result.getSource());
		}
	}
	
	void copyBeforeFiles() throws JavaModelException, FileNotFoundException {
		for (File file : before.listFiles()) {
			createCompilationUnit(file, packageFragment);
		}
		
	}
}
