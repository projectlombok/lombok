package lombokRefactorings.folderOptions;

import lombokRefactorings.folderOptions.TestFolderBuilder.FolderBuilderException;

import org.eclipse.core.resources.IFolder;

public interface FinalFolderBuilder {
	IFolder build() throws FolderBuilderException;
}
