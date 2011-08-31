package lombokRefactorings.folderOptions;

import java.io.IOException;

import lombokRefactorings.TestTypes;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.PartInitException;
@SuppressWarnings("static-access")
public class TestFolderBuilderImpl implements TestFolderBuilder, RefactoredFolderBuilder, DelombokedFolderBuilder, FinalFolderBuilder {
	private IFolder sourceFolder;
	private FolderManager manager;
	
	public static TestFolderBuilder create(FolderManager manager, TestTypes source) {
		return new TestFolderBuilderImpl(manager, source);
	}
	
	private TestFolderBuilderImpl(FolderManager manager, TestTypes source) {
		this.manager = manager;
		this.sourceFolder = manager.getFolder(source);
	}

	public TestFolderBuilderImpl delombok(TestTypes folder) throws FolderBuilderException {
		sourceFolder = delombok(manager.getFolder(folder));
		return this;
	}

	public TestFolderBuilderImpl refactor(TestTypes folder) throws FolderBuilderException {
		sourceFolder = refactor(manager.getFolder(folder));
		return this;
	}

	public IFolder build() throws FolderBuilderException {
		return sourceFolder;
	}
	
	private IFolder refactor(IFolder outputFolder) throws FolderBuilderException {
		try {
			FileGenerator.refactorFilesInFolder(sourceFolder, outputFolder);
		} catch (Exception e) {
			exceptionHandler(e);
		}
		return outputFolder;
	}

	private IFolder delombok(IFolder outputFolder) throws FolderBuilderException {
		try {
			FileGenerator.delombokFilesInFolder(sourceFolder, outputFolder);
		} catch (Exception e) {
			exceptionHandler(e);
		}
		return outputFolder;
	}
	
	private Exception exceptionHandler(Exception e) throws FolderBuilderException {
		
		if (e instanceof PartInitException
				|| e instanceof JavaModelException
				|| e instanceof CoreException
				|| e instanceof IOException) {
			throw new FolderBuilderException(e);
		}
		
		return e;
	}
}
