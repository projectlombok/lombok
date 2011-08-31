package lombokRefactorings.refactorings.eclipse;

import lombokRefactorings.refactorings.IRefactoringType;
import lombokRefactorings.refactorings.RefactoringNotFoundException;
import lombokRefactorings.refactorings.eclipse.extractRefactorings.*;
import lombokRefactorings.refactorings.eclipse.inlineRefactorings.*;
import lombokRefactorings.refactorings.eclipse.moveRefactorings.*;
import lombokRefactorings.refactorings.eclipse.quickFix.OverrideMethodsQuickFixType;
import lombokRefactorings.refactorings.eclipse.quickFix.SurroundWithTryCatchType;
import lombokRefactorings.refactorings.eclipse.renameRefactorings.*;
import lombokRefactorings.refactorings.eclipse.sourceActions.*;

/** 
 * Aims to list all refactorings, source actions and quick fixes which are available in eclipse, and provides methods to return corresponding objects which have a run() method.
 *
 */
public enum EclipseRefactorings {

	// Extract Refactorings
	EXTRACT_METHOD_REFACTORING("ExtractMethod", new ExtractMethodType()), EXTRACT_CLASS_REFACTORING(
			"ExtractClass", new ExtractClassType()), EXTRACT_SUPERCLASS_REFACTORING(
			"ExtractSuperClass", new ExtractSuperClassType()), EXTRACT_CONSTANT_REFACTORING(
			"ExtractConstant", new ExtractConstantType()), EXTRACT_INTERFACE_REFACTORING(
			"ExtractInterface", new ExtractInterfaceType()), EXTRACT_LOCAL_VARIABLE_REFACTORING(
			"ExtractLocalVariable", new ExtractLocalVariableType()),

	// Inline Refactorings
	INLINE_LOCAL_VARIABLE_REFACTORING("InlineLocalVariable",
			new InlineLocalVariableType()), INLINE_FIELD_REFACTORING(
			"InlineField", new InlineFieldType()), INLINE_METHOD_REFACTORING(
			"InlineMethod", new InlineMethodType()),

	// Rename Refactorings
	RENAME_FIELD_REFACTORING("RenameField", new RenameFieldType()), RENAME_LOCAL_VARIABLE_REFACTORING(
			"RenameLocalVariable", new RenameLocalVariableType()), RENAME_NONVIRTUALMETHOD_REFACTORING(
			"RenameNonVirtualMethod", new RenameNonVirtualMethodType()), RENAME_VIRTUALMETHOD_REFACTORING(
			"RenameVirtualMethod", new RenameVirtualMethodType()), RENAME_PACKAGE_REFACTORING(
			"RenamePackage", new RenamePackageType()), RENAME_JAVAPROJECT_REFACTORING(
			"RenameJavaProject", new RenameJavaProjectType()), RENAME_CLASS_REFACTORING(
			"RenameClass", new RenameClassType()),
	 RENAME_SOURCE_FOLDER_REFACTORING("RenameSourceFolder", new
	 RenameSourceFolderType()),

	// Move Refactorings
	GENERIC_MOVE_REFACTORING(
			"GenericMove", new GenericMoveType()), MOVE_INSTANCE_METHOD_REFACTORING(
			"MoveInstanceMethod", new MoveInstanceMethodType()), MOVE_STATIC_ELEMENTS_REFACTORING(
			"MoveStaticElements", new MoveStaticElementsType()),

	// Other Refactorings
			
	//Quick Fixes
	SURROUND_WITH_TRY_CATCH("SurroundWithTryCatch", new SurroundWithTryCatchType()),
	OVERRIDE_METHODS_QUICK_FIX("OverrideMethodsQuickFix", new OverrideMethodsQuickFixType()),
	
	//Source Actions
	ORGANIZE_IMPORTS("OrganizeImports", new OrganizeImportsType()), 
	TOGGLE_COMMENT("ToggleComment", new ToggleCommentType()),
	CREATE_UNRESOLVED_METHOD("CreateUnresolvedMethod", new CreateUnresolvedMethodType()),
	OVERRIDE_METHODS_SOURCE_ACTION("OverrideMethodsSourceAction", new OverrideMethodsSourceActionType()),
	;
	

	private final String refactorName;
	private final IRefactoringType refactoring;

	EclipseRefactorings(String refactorName, IRefactoringType refactoring) {
		this.refactorName = refactorName;
		this.refactoring = refactoring;
	}

	/**
	 * Method to get a refactoring type by name
	 * 
	 * @param refactoringName
	 * @return
	 * @throws NoRefactoringFoundException
	 */
	public static EclipseRefactorings getByName(String refactoringName) throws RefactoringNotFoundException {
		for (EclipseRefactorings refactoring : values()) {
			// " == "+refactoring.getRefactorName()+"?");
			if (refactoring.getRefactorName().equalsIgnoreCase(refactoringName)) {

				return refactoring;
			}
		}
		RefactoringNotFoundException e = new RefactoringNotFoundException(refactoringName);
		if (refactoringName.equalsIgnoreCase("RenameMethod")) {
			e.addMessage("Rename method actually consists of two refactorings, use one of the following: "
							+ "RenameVirtualMethod for extendable methods (so nonfinal and nonprivate method) and "
							+ "RenameNonVirtualMethod for non-extendable methods (so final or private method). ");
		}
		throw e;

	}

	public String getRefactorName() {
		return refactorName;
	}

	public IRefactoringType getRefactoringType() {
		return refactoring;
	}

}
