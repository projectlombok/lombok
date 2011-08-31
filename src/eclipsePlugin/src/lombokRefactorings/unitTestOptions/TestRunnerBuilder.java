package lombokRefactorings.unitTestOptions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import lombokRefactorings.projectOptions.ProjectCreator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class TestRunnerBuilder implements FinalizedRunnerBuilder {
	private static final String TEST_FILE = "Runner";
	private static final String IGNORE_TAG = "//ignore";
	
	private final IType testRunner;
	private boolean done = false;
	private final AstManager manager;

	public TestRunnerBuilder(IProject project, AstManager manager) throws CoreException {		
		this.manager = manager;
		IFolder folder = project.getFolder(ProjectCreator.getSourceFolderName());
		IFile testFile = folder.getFile(TEST_FILE + ".java");
		
		if (!testFile.exists()) {
			String string = "import junit.framework.TestCase;\n\nimport org.junit.*;\n\npublic class " + TEST_FILE + " extends TestCase {\n\n}";	
			InputStream source = new ByteArrayInputStream(string.getBytes());
			testFile.create(source, IResource.NONE, null);
		}
		
		this.testRunner = JavaCore.create(project).findType(TEST_FILE);
	}

	public FinalizedRunnerBuilder setTests(IFolder inputFolder) throws CoreException {
		if (done) {
			throw new IllegalStateException("TestRunner was already built");
		}
		
		addTests(inputFolder);
	
		return this;
	}
	
	private void addTests(IFolder inputFolder) throws CoreException {
		for (IResource resource : inputFolder.members()) {
			if (resource.getClass().getSimpleName().equals("Folder")) {
				if (!"expected".equalsIgnoreCase(resource.getName())) {
					setTests((IFolder) resource);
				}
			} else {
				addTest(new File(resource.getRawLocation().toString()));
			}
		}
	}
	
	private void addTest(File file) throws CoreException {
		String fileName = file.getName();

		String expected = escape(manager.getDelombokedThenRefactored(fileName));
		String inspected = escape(manager.getRefactoredThenDelomboked(fileName));

		StringBuilder sb = new StringBuilder();
		if (mustIgnore(file)) {
			sb.append("@Ignore\n");
		}
		sb.append("@Test\n");
		sb.append("public void test" + removeExtension(fileName) + "() {\n");
		sb.append("\tassertEquals(\"" + expected + "\"\n\t,\n\t\t\"" + inspected
				+ "\");\n");
		sb.append("}");

		createMethod(testRunner, sb.toString());
	}

	private String escape(String raw) {
		String result = new String(raw);
		result = result.replace("\"", "\\\"");
		result = result.replace("\n", "\\n\"+\n\t\t\"");
		return result;
	}

	private String removeExtension(String name) {
		return name.substring(0, name.indexOf("."));
	}

	private void createMethod(IType testRunner, String methodDescription) {
		try {
			testRunner.createMethod(methodDescription, null, true, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private boolean mustIgnore(File file) {
		BufferedReader reader = getReader(file);
	
		boolean ignore = false;
		try {
			String line;
			do {
				line = reader.readLine();
				if (IGNORE_TAG.equals(line)) {
					ignore = true;
					break;
				}				
			} while(line != null);
			
			reader.close();			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return ignore;
	}

	private BufferedReader getReader(File file) {
		try {
			return new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public IType build() {
		done = true;
		return testRunner;
	}
}