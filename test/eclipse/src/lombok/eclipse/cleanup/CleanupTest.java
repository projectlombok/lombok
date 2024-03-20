/*
 * Copyright (C) 2022-2024 The Project Lombok Authors.
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
package lombok.eclipse.cleanup;

import static lombok.eclipse.RefactoringUtils.performRefactoring;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring;
import org.eclipse.jdt.internal.corext.fix.CleanUpRegistry;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.fix.MapCleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
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
		
		// Load the class dynamically to avoid compile errors when running with older versions of Eclipse
		Class<?> cleanUpClass;
		try {
			cleanUpClass = Class.forName("org.eclipse.jdt.internal.ui.fix.CodeStyleCleanUp");
		} catch (ClassNotFoundException e) {
			cleanUpClass = Class.forName("org.eclipse.jdt.internal.ui.fix.CodeStyleCleanUpCore");
		}
		Constructor<?> cleanUpConstructor = cleanUpClass.getConstructor(Map.class);
		ref.addCleanUp((ICleanUp) cleanUpConstructor.newInstance(options.getMap()));
		
		performRefactoring(ref);
	}
}
