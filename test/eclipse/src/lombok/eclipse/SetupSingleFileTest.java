package lombok.eclipse;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.jdt.core.JavaModelException;
import org.junit.runner.Description;

public class SetupSingleFileTest extends SetupTest {
	
	@Override
	protected void starting(Description description) {
		super.starting(description);
		
		try {
			copyRootFiles();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void copyRootFiles() throws JavaModelException, FileNotFoundException {
		for (File file : root.listFiles()) {
			createCompilationUnit(file, packageFragment);
		}
		
	}
}
