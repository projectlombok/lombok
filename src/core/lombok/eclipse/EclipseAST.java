/*
 * Copyright (C) 2009-2010 The Project Lombok Authors.
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
package lombok.eclipse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.core.AST;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Wraps around Eclipse's internal AST view to add useful features as well as the ability to visit parents from children,
 * something Eclipse own AST system does not offer.
 */
public class EclipseAST extends AST<EclipseAST, EclipseNode, ASTNode> {
	/**
	 * Creates a new EclipseAST of the provided Compilation Unit.
	 * 
	 * @param ast The compilation unit, which serves as the top level node in the tree to be built.
	 */
	public EclipseAST(CompilationUnitDeclaration ast) {
		super(toFileName(ast), packageDeclaration(ast), imports(ast));
		this.compilationUnitDeclaration = ast;
		setTop(buildCompilationUnit(ast));
		this.completeParse = isComplete(ast);
		clearChanged();
	}
	
	private static String packageDeclaration(CompilationUnitDeclaration cud) {
		ImportReference pkg = cud.currentPackage;
		return pkg == null ? null : Eclipse.toQualifiedName(pkg.getImportName());
	}
	
	private static Collection<String> imports(CompilationUnitDeclaration cud) { 
		List<String> imports = new ArrayList<String>();
		if (cud.imports == null) return imports;
		for (ImportReference imp : cud.imports) {
			if (imp == null) continue;
			String qualifiedName = Eclipse.toQualifiedName(imp.getImportName());
			if ((imp.bits & ASTNode.OnDemand) != 0) qualifiedName += ".*";
			imports.add(qualifiedName);
		}
		return imports;
	}
	
	/**
	 * Runs through the entire AST, starting at the compilation unit, calling the provided visitor's visit methods
	 * for each node, depth first.
	 */
	public void traverse(EclipseASTVisitor visitor) {
		top().traverse(visitor);
	}
	
	void traverseChildren(EclipseASTVisitor visitor, EclipseNode node) {
		for (EclipseNode child : node.down()) {
			child.traverse(visitor);
		}
	}
	
	/**
	 * Eclipse starts off with a 'diet' parse which leaves method bodies blank, amongst other shortcuts.
	 * 
	 * For such diet parses, this method returns false, otherwise it returns true. Any lombok processor
	 * that needs the contents of methods should just do nothing (and return false so it gets another shot later!)
	 * when this is false.
	 */
	public boolean isCompleteParse() {
		return completeParse;
	}
	
	class ParseProblem {
		final boolean isWarning;
		final String message;
		final int sourceStart;
		final int sourceEnd;
		
		ParseProblem(boolean isWarning, String message, int sourceStart, int sourceEnd) {
			this.isWarning = isWarning;
			this.message = message;
			this.sourceStart = sourceStart;
			this.sourceEnd = sourceEnd;
		}
		
		void addToCompilationResult() {
			addProblemToCompilationResult((CompilationUnitDeclaration) top().get(),
					isWarning, message, sourceStart, sourceEnd);
		}
	}
	
	private void propagateProblems() {
		if (queuedProblems.isEmpty()) return;
		CompilationUnitDeclaration cud = (CompilationUnitDeclaration) top().get();
		if (cud.compilationResult == null) return;
		for (ParseProblem problem : queuedProblems) problem.addToCompilationResult();
		queuedProblems.clear();
	}
	
	private final List<ParseProblem> queuedProblems = new ArrayList<ParseProblem>();
	
	void addProblem(ParseProblem problem) {
		queuedProblems.add(problem);
		propagateProblems();
	}
	
	/**
	 * Adds a problem to the provided CompilationResult object so that it will show up
	 * in the Problems/Warnings view.
	 */
	public static void addProblemToCompilationResult(CompilationUnitDeclaration ast,
			boolean isWarning, String message, int sourceStart, int sourceEnd) {
		if (ast.compilationResult == null) return;
		char[] fileNameArray = ast.getFileName();
		if (fileNameArray == null) fileNameArray = "(unknown).java".toCharArray();
		int lineNumber = 0;
		int columnNumber = 1;
		CompilationResult result = ast.compilationResult;
		int[] lineEnds = null;
		lineNumber = sourceStart >= 0
				? Util.getLineNumber(sourceStart, lineEnds = result.getLineSeparatorPositions(), 0, lineEnds.length-1)
				: 0;
		columnNumber = sourceStart >= 0
				? Util.searchColumnNumber(result.getLineSeparatorPositions(), lineNumber,sourceStart)
				: 0;
		
		CategorizedProblem ecProblem = new LombokProblem(
				fileNameArray, message, 0, new String[0],
				isWarning ? ProblemSeverities.Warning : ProblemSeverities.Error,
				sourceStart, sourceEnd, lineNumber, columnNumber);
		ast.compilationResult.record(ecProblem, null);
	}
	
	private static class LombokProblem extends DefaultProblem {
		private static final String MARKER_ID = "org.eclipse.jdt.apt.pluggable.core.compileProblem";  //$NON-NLS-1$
		
		public LombokProblem(char[] originatingFileName, String message, int id,
				String[] stringArguments, int severity,
				int startPosition, int endPosition, int line, int column) {
			super(originatingFileName, message, id, stringArguments, severity, startPosition, endPosition, line, column);
		}
		
		@Override public int getCategoryID() {
			return CAT_UNSPECIFIED;
		}
		
		@Override public String getMarkerType() {
			return MARKER_ID;
		}
	}
	
	private final CompilationUnitDeclaration compilationUnitDeclaration;
	private boolean completeParse;
	
	private static String toFileName(CompilationUnitDeclaration ast) {
		return ast.compilationResult.fileName == null ? null : new String(ast.compilationResult.fileName);
	}
	
	/**
	 * Call this method to move an EclipseAST generated for a diet parse to rebuild itself for the full parse -
	 * with filled in method bodies and such. Also propagates problems and errors, which in diet parse
	 * mode can't be reliably added to the problems/warnings view.
	 */
	public void rebuild(boolean force) {
		propagateProblems();
		if (completeParse && !force) return;
		boolean changed = isChanged();
		boolean newCompleteParse = isComplete(compilationUnitDeclaration);
		if (!newCompleteParse && !force) return;
		
		top().rebuild();
		
		this.completeParse = newCompleteParse;
		if (!changed) clearChanged();
	}
	
	private static boolean isComplete(CompilationUnitDeclaration unit) {
		return (unit.bits & ASTNode.HasAllMethodBodies) != 0;
	}
	
	/** {@inheritDoc} */
	@Override protected EclipseNode buildTree(ASTNode node, Kind kind) {
		switch (kind) {
		case COMPILATION_UNIT:
			return buildCompilationUnit((CompilationUnitDeclaration) node);
		case TYPE:
			return buildType((TypeDeclaration) node);
		case FIELD:
			return buildField((FieldDeclaration) node);
		case INITIALIZER:
			return buildInitializer((Initializer) node);
		case METHOD:
			return buildMethod((AbstractMethodDeclaration) node);
		case ARGUMENT:
			return buildLocal((Argument) node, kind);
		case LOCAL:
			return buildLocal((LocalDeclaration) node, kind);
		case STATEMENT:
			return buildStatement((Statement) node);
		case ANNOTATION:
			return buildAnnotation((Annotation) node, false);
		default:
			throw new AssertionError("Did not expect to arrive here: " + kind);
		}
	}
	
	private EclipseNode buildCompilationUnit(CompilationUnitDeclaration top) {
		if (setAndGetAsHandled(top)) return null;
		List<EclipseNode> children = buildTypes(top.types);
		return putInMap(new EclipseNode(this, top, children, Kind.COMPILATION_UNIT));
	}
	
	private void addIfNotNull(Collection<EclipseNode> collection, EclipseNode n) {
		if (n != null) collection.add(n);
	}
	
	private List<EclipseNode> buildTypes(TypeDeclaration[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (TypeDeclaration type : children) addIfNotNull(childNodes, buildType(type));
		return childNodes;
	}
	
	private EclipseNode buildType(TypeDeclaration type) {
		if (setAndGetAsHandled(type)) return null;
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		childNodes.addAll(buildFields(type.fields));
		childNodes.addAll(buildTypes(type.memberTypes));
		childNodes.addAll(buildMethods(type.methods));
		childNodes.addAll(buildAnnotations(type.annotations, false));
		return putInMap(new EclipseNode(this, type, childNodes, Kind.TYPE));
	}
	
	private Collection<EclipseNode> buildFields(FieldDeclaration[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (FieldDeclaration child : children) addIfNotNull(childNodes, buildField(child));
		return childNodes;
	}
	
	private static <T> List<T> singleton(T item) {
		List<T> list = new ArrayList<T>();
		if (item != null) list.add(item);
		return list;
	}
	
	private EclipseNode buildField(FieldDeclaration field) {
		if (field instanceof Initializer) return buildInitializer((Initializer)field);
		if (setAndGetAsHandled(field)) return null;
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		addIfNotNull(childNodes, buildStatement(field.initialization));
		childNodes.addAll(buildAnnotations(field.annotations, true));
		return putInMap(new EclipseNode(this, field, childNodes, Kind.FIELD));
	}
	
	private EclipseNode buildInitializer(Initializer initializer) {
		if (setAndGetAsHandled(initializer)) return null;
		return putInMap(new EclipseNode(this, initializer, singleton(buildStatement(initializer.block)), Kind.INITIALIZER));
	}
	
	private Collection<EclipseNode> buildMethods(AbstractMethodDeclaration[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (AbstractMethodDeclaration method : children) addIfNotNull(childNodes, buildMethod(method));
		return childNodes;
	}
	
	private EclipseNode buildMethod(AbstractMethodDeclaration method) {
		if (setAndGetAsHandled(method)) return null;
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		childNodes.addAll(buildArguments(method.arguments));
		if (method instanceof ConstructorDeclaration) {
			ConstructorDeclaration constructor = (ConstructorDeclaration) method;
			addIfNotNull(childNodes, buildStatement(constructor.constructorCall));
		}
		childNodes.addAll(buildStatements(method.statements));
		childNodes.addAll(buildAnnotations(method.annotations, false));
		return putInMap(new EclipseNode(this, method, childNodes, Kind.METHOD));
	}
	
	//Arguments are a kind of LocalDeclaration. They can definitely contain lombok annotations, so we care about them.
	private Collection<EclipseNode> buildArguments(Argument[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (LocalDeclaration local : children) {
			addIfNotNull(childNodes, buildLocal(local, Kind.ARGUMENT));
		}
		return childNodes;
	}
	
	private EclipseNode buildLocal(LocalDeclaration local, Kind kind) {
		if (setAndGetAsHandled(local)) return null;
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		addIfNotNull(childNodes, buildStatement(local.initialization));
		childNodes.addAll(buildAnnotations(local.annotations, true));
		return putInMap(new EclipseNode(this, local, childNodes, kind));
	}
	
	private Collection<EclipseNode> buildAnnotations(Annotation[] annotations, boolean varDecl) {
		List<EclipseNode> elements = new ArrayList<EclipseNode>();
		if (annotations != null) for (Annotation an : annotations) addIfNotNull(elements, buildAnnotation(an, varDecl));
		return elements;
	}
	
	private EclipseNode buildAnnotation(Annotation annotation, boolean field) {
		if (annotation == null) return null;
		boolean handled = setAndGetAsHandled(annotation);
		if (!field && handled) {
			// @Foo int x, y; is handled in eclipse by putting the same annotation node on 2 FieldDeclarations.
			return null;
		}
		return putInMap(new EclipseNode(this, annotation, null, Kind.ANNOTATION));
	}
	
	private Collection<EclipseNode> buildStatements(Statement[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (Statement child  : children) addIfNotNull(childNodes, buildStatement(child));
		return childNodes;
	}
	
	private EclipseNode buildStatement(Statement child) {
		if (child == null) return null;
		if (child instanceof TypeDeclaration) return buildType((TypeDeclaration)child);
		
		if (child instanceof LocalDeclaration) return buildLocal((LocalDeclaration)child, Kind.LOCAL);
		
		if (setAndGetAsHandled(child)) return null;
		
		return drill(child);
	}
	
	private EclipseNode drill(Statement statement) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		for (FieldAccess fa : fieldsOf(statement.getClass())) childNodes.addAll(buildWithField(EclipseNode.class, statement, fa));
		return putInMap(new EclipseNode(this, statement, childNodes, Kind.STATEMENT));
	}
	
	/** For Eclipse, only Statement counts, as Expression is a subclass of it, even though this isn't
	 * entirely correct according to the JLS spec (only some expressions can be used as statements, not all of them). */
	@Override protected Collection<Class<? extends ASTNode>> getStatementTypes() {
		return Collections.<Class<? extends ASTNode>>singleton(Statement.class);
	}
}
