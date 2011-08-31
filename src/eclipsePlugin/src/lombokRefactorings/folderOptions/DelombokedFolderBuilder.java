package lombokRefactorings.folderOptions;

import lombokRefactorings.TestTypes;
import lombokRefactorings.folderOptions.TestFolderBuilder.FolderBuilderException;

import org.eclipse.core.resources.IFolder;

public interface DelombokedFolderBuilder {
	FinalFolderBuilder refactor(TestTypes folder) throws FolderBuilderException;
	IFolder build() throws FolderBuilderException;
}
