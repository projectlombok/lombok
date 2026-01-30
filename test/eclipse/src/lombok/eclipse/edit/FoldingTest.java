/*
 * Copyright (C) 2022-2025 The Project Lombok Authors.
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
package lombok.eclipse.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.ui.text.folding.DefaultJavaFoldingStructureProvider;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.eclipse.EclipseRunner;
import lombok.eclipse.SetupSingleFileTest;
import lombok.eclipse.TestUtils;
import lombok.permit.Permit;

@RunWith(EclipseRunner.class)
public class FoldingTest {
	
	@Rule
	public SetupSingleFileTest setup = new SetupSingleFileTest();
	
	@BeforeClass
	public static void versionCheck() {
		TestUtils.assumeMinJdtVersion(43);
	}
	
	@Test
	public void locked() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("LockedFolding.java");
		
		Map<?, ?> foldingMap = getFoldingMap(cu);
		
		assertThat(foldingMap.size(), is(6));
	}
	
	@Test
	public void nonNull() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("NonNullFolding.java");
		
		Map<?, ?> foldingMap = getFoldingMap(cu);
		
		assertThat(foldingMap.size(), is(6));
	}
	
	@Test
	public void cleanup() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("CleanupFolding.java");
		
		Map<?, ?> foldingMap = getFoldingMap(cu);
		
		assertThat(foldingMap.size(), is(6));
	}
	
	@Test
	public void synch() throws Exception {
		ICompilationUnit cu = setup.getPackageFragment().getCompilationUnit("SynchronizedFolding.java");
		
		Map<?, ?> foldingMap = getFoldingMap(cu);
		
		assertThat(foldingMap.size(), is(6));
	}
	
	private Map<?, ?> getFoldingMap(ICompilationUnit cu) throws Exception {
		Class<?> contextClass = Class.forName("org.eclipse.jdt.ui.text.folding.DefaultJavaFoldingStructureProvider$FoldingStructureComputationContext");
		Constructor<?> contextConstructor = Permit.getConstructor(contextClass, DefaultJavaFoldingStructureProvider.class, IDocument.class, ProjectionAnnotationModel.class, boolean.class, IScanner.class);
		Method processMethod = Permit.getMethod(DefaultJavaFoldingStructureProvider.class, "processCompilationUnit", ICompilationUnit.class, contextClass);
		Field foldingMapField = Permit.getField(contextClass, "fMap");
		
		DefaultJavaFoldingStructureProvider defaultJavaFoldingStructureProvider = new DefaultJavaFoldingStructureProvider();
		Object context = Permit.newInstance(contextConstructor, defaultJavaFoldingStructureProvider, new Document(cu.getSource()), new ProjectionAnnotationModel(), true, null);
		Permit.invoke(processMethod, defaultJavaFoldingStructureProvider, cu, context);
		
		return (Map<?, ?>) Permit.get(foldingMapField, context);
	}
	
}
