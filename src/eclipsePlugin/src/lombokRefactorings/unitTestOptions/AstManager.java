package lombokRefactorings.unitTestOptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.ast.ecj.EcjTreePrinter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

@SuppressWarnings("restriction")
public class AstManager {	
	private Map<String, String> refactoredThenDelombokedAstMap = new HashMap<String, String>();
	private Map<String, String> delombokedThenRefactoredAstMap = new HashMap<String, String>();
	private Map<String, String> failedFilesMap = new HashMap<String, String>();
	
	public void addFailure(String fileName, String refactoringName) {
		failedFilesMap.put(fileName, refactoringName);
	}
	
	public AstManager initializeMaps(IFolder refactoredThenDelombokedFolder, IFolder delombokedThenRefactoredFolder, IFolder expected) {
		try {
			fillAstMap(delombokedThenRefactoredAstMap, delombokedThenRefactoredFolder);
			fillAstMap(delombokedThenRefactoredAstMap, expected);
			fillAstMap(refactoredThenDelombokedAstMap, refactoredThenDelombokedFolder);
			checkRefactoringFailedFilesMap();
			return this;
		} catch (JavaModelException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (CoreException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	private void fillAstMap(Map<String, String> astMap, IFolder folder) throws JavaModelException, FileNotFoundException, CoreException, IOException { 		
		for (IResource resource :folder.members()) {
			if (resource instanceof IFolder) {
				fillAstMap(astMap, (IFolder) resource);
			}
			else {
				astMap.put(resource.getName(), createEcjTreePrinter(resource).getContent());
			}
		}
	}
	
	private EcjTreePrinter createEcjTreePrinter(IResource resource)
			throws JavaModelException, FileNotFoundException, IOException {
		ICompilationUnit refactorICompilationUnit = JavaCore.createCompilationUnitFrom((IFile)resource);
		
		CompilerOptions compilerOptions = createCompilerOptions();
		Parser parser = createParser(compilerOptions);
		
		org.eclipse.jdt.internal.compiler.batch.CompilationUnit refactorUnit = new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(refactorICompilationUnit.getSource().toCharArray(), resource.getName(), "UTF-8");
		CompilationResult compilationResult = new CompilationResult(refactorUnit, 0, 0, 0);
		ASTNode refactorNode = parser.parse(refactorUnit, compilationResult);
		
		EcjTreePrinter printer = new EcjTreePrinter(false);
		printer.skipProperty(CompilationUnitDeclaration.class, "$lombokAST");
		printer.skipProperty(StringLiteral.class, "lineNumber");
		printer.skipProperty(ExtendedStringLiteral.class, "lineNumber");
		printer.skipProperty(Block.class, "explicitDeclarations");
		printer.skipReferenceTracking(LocalDeclaration.class, TypeReference.class);
		printer.skipReferenceTracking(FieldDeclaration.class, TypeReference.class);
		printer.visit(refactorNode);
		
		return printer;
	}

	private CompilerOptions createCompilerOptions() {
		CompilerOptions compilerOptions = new CompilerOptions();
		compilerOptions.complianceLevel = ClassFileConstants.JDK1_6;
		compilerOptions.sourceLevel = ClassFileConstants.JDK1_6;
		compilerOptions.targetJDK = ClassFileConstants.JDK1_6;
		compilerOptions.parseLiteralExpressionsAsConstants = true;
		return compilerOptions;
	}

	private Parser createParser(CompilerOptions compilerOptions) {
		Parser parser = new Parser(new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				compilerOptions,
				new DefaultProblemFactory()
		), compilerOptions.parseLiteralExpressionsAsConstants);
		return parser;
	}

	private void checkRefactoringFailedFilesMap() {
		for (Entry<String, String> entry : failedFilesMap.entrySet()) {
			delombokedThenRefactoredAstMap.put(entry.getKey(), "Something Failed");
			refactoredThenDelombokedAstMap.put(entry.getKey(), entry.getValue());
		}
	}

	String getRefactoredThenDelomboked(String fileName) {
		return refactoredThenDelombokedAstMap.get(fileName);
	}
	
	String getDelombokedThenRefactored(String fileName) {
		return delombokedThenRefactoredAstMap.get(fileName);
	}
}
