package lombokRefactorings.folderOptions;

import lombokRefactorings.TestTypes;

public interface TestFolderBuilder {
	
	DelombokedFolderBuilder delombok(TestTypes folder) throws FolderBuilderException;
	RefactoredFolderBuilder refactor(TestTypes folder) throws FolderBuilderException;
	
	public class FolderBuilderException extends Exception {
		
		
		public FolderBuilderException(Throwable t) {
			super(t);
		}

		private static final long serialVersionUID = 2364570955157463811L;
	}
}
