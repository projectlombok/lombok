package lombokRefactorings.folderOptions;

import lombokRefactorings.TestTypes;
import lombokRefactorings.folderOptions.TestFolderBuilder.FolderBuilderException;

import org.eclipse.core.resources.IFolder;

public interface RefactoredFolderBuilder {
	FinalFolderBuilder delombok(TestTypes folder) throws FolderBuilderException;
	IFolder build() throws FolderBuilderException;
}
