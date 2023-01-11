package lombok.eclipse;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import lombok.eclipse.cleanup.CleanupTest;
import lombok.eclipse.edit.SelectTest;
import lombok.eclipse.refactoring.ExtractInterfaceTest;
import lombok.eclipse.refactoring.RenameTest;

@RunWith(Suite.class)
@SuiteClasses({ExtractInterfaceTest.class, RenameTest.class, SelectTest.class, CleanupTest.class})
public class EclipseTests {
	
}
