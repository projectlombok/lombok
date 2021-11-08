package lombok.eclipse.cleanup;

import static lombok.eclipse.RefactoringUtils.performRefactoring;

import java.util.Map.Entry;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring;
import org.eclipse.jdt.internal.corext.fix.CleanUpRegistry;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.fix.CodeStyleCleanUp;
import org.eclipse.jdt.internal.ui.fix.MapCleanUpOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupBeforeAfterTest;

@RunWith(EclipseRunner.class)
public class CleanupTest {
	
	@Rule
	public SetupBeforeAfterTest setup = new SetupBeforeAfterTest();
	
	@Test
	public void useThis() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("A.java");
		
		CleanUpRegistry cleanUpRegistry = JavaPlugin.getDefault().getCleanUpRegistry();
		MapCleanUpOptions options = cleanUpRegistry.getDefaultOptions(CleanUpConstants.DEFAULT_CLEAN_UP_OPTIONS);
		for (Entry<String, String> entry : options.getMap().entrySet()) {
			entry.setValue(MapCleanUpOptions.FALSE);
		}
		options.setOption(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS, MapCleanUpOptions.TRUE);
		options.setOption(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_ALWAYS, MapCleanUpOptions.TRUE);
		
		CleanUpRefactoring ref = new CleanUpRefactoring();
		ref.addCompilationUnit(cu);
		ref.addCleanUp(new CodeStyleCleanUp(options.getMap()));
		
		performRefactoring(ref);
	}
}
