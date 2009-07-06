/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
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
 * Wraps around eclipse's internal AST view to add useful features as well as the ability to visit parents from children,
 * something eclipse's own AST system does not offer.
 */
public class EclipseAST extends AST<ASTNode> {
	/**
	 * Creates a new EclipseAST of the provided Compilation Unit.
	 * 
	 * @param ast The compilation unit, which serves as the top level node in the tree to be built.
	 */
	public EclipseAST(CompilationUnitDeclaration ast) {
		super(toFileName(ast));
		this.compilationUnitDeclaration = ast;
		setTop(buildCompilationUnit(ast));
		this.completeParse = isComplete(ast);
	}
	
	/** {@inheritDoc} */
	@Override public String getPackageDeclaration() {
		CompilationUnitDeclaration cud = (CompilationUnitDeclaration) top().get();
		ImportReference pkg = cud.currentPackage;
		return pkg == null ? null : Eclipse.toQualifiedName(pkg.getImportName());
	}
	
	/** {@inheritDoc} */
	@Override public Collection<String> getImportStatements() {
		List<String> imports = new ArrayList<String>();
		CompilationUnitDeclaration cud = (CompilationUnitDeclaration) top().get();
		if ( cud.imports == null ) return imports;
		for ( ImportReference imp : cud.imports ) {
			if ( imp == null ) continue;
			imports.add(Eclipse.toQualifiedName(imp.getImportName()));
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
	
	private void traverseChildren(EclipseASTVisitor visitor, Node node) {
		for ( Node child : node.down() ) {
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
	
	/** {@inheritDoc} */
	@Override public Node top() {
		return (Node) super.top();
	}
	
	/** {@inheritDoc} */
	@Override public Node get(ASTNode node) {
		return (Node) super.get(node);
	}
	
	private class ParseProblem {
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
		if ( queuedProblems.isEmpty() ) return;
		CompilationUnitDeclaration cud = (CompilationUnitDeclaration) top().get();
		if ( cud.compilationResult == null ) return;
		for ( ParseProblem problem : queuedProblems ) problem.addToCompilationResult();
		queuedProblems.clear();
	}
	
	private final List<ParseProblem> queuedProblems = new ArrayList<ParseProblem>();
	
	private void addProblem(ParseProblem problem) {
		queuedProblems.add(problem);
		propagateProblems();
	}
	
	/**
	 * Adds a problem to the provided CompilationResult object so that it will show up
	 * in the Problems/Warnings view.
	 */
	static void addProblemToCompilationResult(CompilationUnitDeclaration ast,
			boolean isWarning, String message, int sourceStart, int sourceEnd) {
		if ( ast.compilationResult == null ) return;
		char[] fileNameArray = ast.getFileName();
		if ( fileNameArray == null ) fileNameArray = "(unknown).java".toCharArray();
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
	
	/**
	 * Eclipse specific version of the AST.Node class.
	 */
	public final class Node extends AST<ASTNode>.Node {
		/**
		 * See the {@link AST.Node} constructor for information.
		 */
		Node(ASTNode node, List<Node> children, Kind kind) {
			super(node, children, kind);
		}
		
		/**
		 * Visits this node and all child nodes depth-first, calling the provided visitor's visit methods.
		 */
		public void traverse(EclipseASTVisitor visitor) {
			switch ( getKind() ) {
			case COMPILATION_UNIT:
				visitor.visitCompilationUnit(this, (CompilationUnitDeclaration)get());
				traverseChildren(visitor, this);
				visitor.endVisitCompilationUnit(this, (CompilationUnitDeclaration)get());
				break;
			case TYPE:
				visitor.visitType(this, (TypeDeclaration)get());
				traverseChildren(visitor, this);
				visitor.endVisitType(this, (TypeDeclaration)get());
				break;
			case FIELD:
				visitor.visitField(this, (FieldDeclaration)get());
				traverseChildren(visitor, this);
				visitor.endVisitField(this, (FieldDeclaration)get());
				break;
			case INITIALIZER:
				visitor.visitInitializer(this, (Initializer)get());
				traverseChildren(visitor, this);
				visitor.endVisitInitializer(this, (Initializer)get());
				break;
			case METHOD:
				if ( get() instanceof Clinit ) return;
				visitor.visitMethod(this, (AbstractMethodDeclaration)get());
				traverseChildren(visitor, this);
				visitor.endVisitMethod(this, (AbstractMethodDeclaration)get());
				break;
			case ARGUMENT:
				AbstractMethodDeclaration method = (AbstractMethodDeclaration)up().get();
				visitor.visitMethodArgument(this, (Argument)get(), method);
				traverseChildren(visitor, this);
				visitor.endVisitMethodArgument(this, (Argument)get(), method);
				break;
			case LOCAL:
				visitor.visitLocal(this, (LocalDeclaration)get());
				traverseChildren(visitor, this);
				visitor.endVisitLocal(this, (LocalDeclaration)get());
				break;
			case ANNOTATION:
				switch ( up().getKind() ) {
				case TYPE:
					visitor.visitAnnotationOnType((TypeDeclaration)up().get(), this, (Annotation)get());
					break;
				case FIELD:
					visitor.visitAnnotationOnField((FieldDeclaration)up().get(), this, (Annotation)get());
					break;
				case METHOD:
					visitor.visitAnnotationOnMethod((AbstractMethodDeclaration)up().get(), this, (Annotation)get());
					break;
				case ARGUMENT:
					visitor.visitAnnotationOnMethodArgument(
							(Argument)parent.get(),
							(AbstractMethodDeclaration)parent.directUp().get(),
							this, (Annotation)get());
					break;
				case LOCAL:
					visitor.visitAnnotationOnLocal((LocalDeclaration)parent.get(), this, (Annotation)get());
					break;
				default:
					throw new AssertionError("Annotion not expected as child of a " + up().getKind());
				}
				break;
			case STATEMENT:
				visitor.visitStatement(this, (Statement)get());
				traverseChildren(visitor, this);
				visitor.endVisitStatement(this, (Statement)get());
				break;
			default:
				throw new AssertionError("Unexpected kind during node traversal: " + getKind());
			}
		}
		
		/** {@inheritDoc} */
		@Override public String getName() {
			final char[] n;
			if ( node instanceof TypeDeclaration ) n = ((TypeDeclaration)node).name;
			else if ( node instanceof FieldDeclaration ) n = ((FieldDeclaration)node).name;
			else if ( node instanceof AbstractMethodDeclaration ) n = ((AbstractMethodDeclaration)node).selector;
			else if ( node instanceof LocalDeclaration ) n = ((LocalDeclaration)node).name;
			else n = null;
			
			return n == null ? null : new String(n);
		}
		
		/** {@inheritDoc} */
		@Override public void addError(String message) {
			this.addError(message, this.get().sourceStart, this.get().sourceEnd);
		}
		
		/** Generate a compiler error that shows the wavy underline from-to the stated character positions. */
		public void addError(String message, int sourceStart, int sourceEnd) {
			addProblem(new ParseProblem(false, message, sourceStart, sourceEnd));
		}
		
		/** {@inheritDoc} */
		@Override public void addWarning(String message) {
			this.addWarning(message, this.get().sourceStart, this.get().sourceEnd);
		}
		
		/** Generate a compiler warning that shows the wavy underline from-to the stated character positions. */
		public void addWarning(String message, int sourceStart, int sourceEnd) {
			addProblem(new ParseProblem(true, message, sourceStart, sourceEnd));
		}
		
		/** {@inheritDoc} */
		@Override public Node up() {
			return (Node) super.up();
		}
		
		/** {@inheritDoc} */
		@Override protected boolean calculateIsStructurallySignificant() {
			if ( node instanceof TypeDeclaration ) return true;
			if ( node instanceof AbstractMethodDeclaration ) return true;
			if ( node instanceof FieldDeclaration ) return true;
			if ( node instanceof LocalDeclaration ) return true;
			if ( node instanceof CompilationUnitDeclaration ) return true;
			return false;
		}
		
		/** {@inheritDoc} */
		@Override public Node getNodeFor(ASTNode obj) {
			return (Node) super.getNodeFor(obj);
		}
		
		/** {@inheritDoc} */
		public Node directUp() {
			return (Node) super.directUp();
		}
		
		/** {@inheritDoc} */
		@SuppressWarnings("unchecked")
		@Override public Collection<Node> down() {
			return (Collection<Node>) super.down();
		}
		
		/** {@inheritDoc} */
		@Override public Node top() {
			return (Node) super.top();
		}
		
		/**
		 * Convenient shortcut to the owning EclipseAST object's isCompleteParse method.
		 * 
		 * @see JavacAST#isCompleteParse()
		 */
		public boolean isCompleteParse() {
			return completeParse;
		}
	}
	
	private final CompilationUnitDeclaration compilationUnitDeclaration;
	private boolean completeParse;
	
	private static String toFileName(CompilationUnitDeclaration ast) {
		return ast.compilationResult.fileName == null ? null : new String(ast.compilationResult.fileName);
	}
	
	/**
	 * Call to move an EclipseAST generated for a diet parse to rebuild itself for the full parse -
	 * with filled in method bodies and such. Also propagates problems and errors, which in diet parse
	 * mode can't be reliably added to the problems/warnings view.
	 */
	public void reparse() {
		propagateProblems();
		if ( completeParse ) return;
		boolean newCompleteParse = isComplete(compilationUnitDeclaration);
		if ( !newCompleteParse ) return;
		
		top().rebuild();
		
		this.completeParse = true;
	}
	
	private static boolean isComplete(CompilationUnitDeclaration unit) {
		return (unit.bits & ASTNode.HasAllMethodBodies) != 0;
	}
	
	/** {@inheritDoc} */
	@Override protected Node buildTree(ASTNode node, Kind kind) {
		switch ( kind ) {
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
			return buildAnnotation((Annotation) node);
		default:
			throw new AssertionError("Did not expect to arrive here: " + kind);
		}
	}
	
	private Node buildCompilationUnit(CompilationUnitDeclaration top) {
		if ( setAndGetAsHandled(top) ) return null;
		List<Node> children = buildTypes(top.types);
		return putInMap(new Node(top, children, Kind.COMPILATION_UNIT));
	}
	
	private void addIfNotNull(Collection<Node> collection, Node n) {
		if ( n != null ) collection.add(n);
	}
	
	private List<Node> buildTypes(TypeDeclaration[] children) {
		List<Node> childNodes = new ArrayList<Node>();
		if ( children != null ) for ( TypeDeclaration type : children ) addIfNotNull(childNodes, buildType(type));
		return childNodes;
	}
	
	private Node buildType(TypeDeclaration type) {
		if ( setAndGetAsHandled(type) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		childNodes.addAll(buildFields(type.fields));
		childNodes.addAll(buildTypes(type.memberTypes));
		childNodes.addAll(buildMethods(type.methods));
		childNodes.addAll(buildAnnotations(type.annotations));
		return putInMap(new Node(type, childNodes, Kind.TYPE));
	}
	
	private Collection<Node> buildFields(FieldDeclaration[] children) {
		List<Node> childNodes = new ArrayList<Node>();
		if ( children != null ) for ( FieldDeclaration child : children ) addIfNotNull(childNodes, buildField(child));
		return childNodes;
	}
	
	private static <T> List<T> singleton(T item) {
		List<T> list = new ArrayList<T>();
		if ( item != null ) list.add(item);
		return list;
	}
	
	private Node buildField(FieldDeclaration field) {
		if ( field instanceof Initializer ) return buildInitializer((Initializer)field);
		if ( setAndGetAsHandled(field) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		addIfNotNull(childNodes, buildStatement(field.initialization));
		childNodes.addAll(buildAnnotations(field.annotations));
		return putInMap(new Node(field, childNodes, Kind.FIELD));
	}
	
	private Node buildInitializer(Initializer initializer) {
		if ( setAndGetAsHandled(initializer) ) return null;
		return putInMap(new Node(initializer, singleton(buildStatement(initializer.block)), Kind.INITIALIZER));
	}
	
	private Collection<Node> buildMethods(AbstractMethodDeclaration[] children) {
		List<Node> childNodes = new ArrayList<Node>();
		if ( children != null ) for (AbstractMethodDeclaration method : children ) addIfNotNull(childNodes, buildMethod(method));
		return childNodes;
	}
	
	private Node buildMethod(AbstractMethodDeclaration method) {
		if ( setAndGetAsHandled(method) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		childNodes.addAll(buildArguments(method.arguments));
		childNodes.addAll(buildStatements(method.statements));
		childNodes.addAll(buildAnnotations(method.annotations));
		return putInMap(new Node(method, childNodes, Kind.METHOD));
	}
	
	//Arguments are a kind of LocalDeclaration. They can definitely contain lombok annotations, so we care about them.
	private Collection<Node> buildArguments(Argument[] children) {
		List<Node> childNodes = new ArrayList<Node>();
		if ( children != null ) for ( LocalDeclaration local : children ) {
			addIfNotNull(childNodes, buildLocal(local, Kind.ARGUMENT));
		}
		return childNodes;
	}
	
	private Node buildLocal(LocalDeclaration local, Kind kind) {
		if ( setAndGetAsHandled(local) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		addIfNotNull(childNodes, buildStatement(local.initialization));
		childNodes.addAll(buildAnnotations(local.annotations));
		return putInMap(new Node(local, childNodes, kind));
	}
	
	private Collection<Node> buildAnnotations(Annotation[] annotations) {
		List<Node> elements = new ArrayList<Node>();
		if ( annotations != null ) for ( Annotation an : annotations ) addIfNotNull(elements, buildAnnotation(an));
		return elements;
	}
	
	private Node buildAnnotation(Annotation annotation) {
		if ( annotation == null ) return null;
		if ( setAndGetAsHandled(annotation) ) return null;
		return putInMap(new Node(annotation, null, Kind.ANNOTATION));
	}
	
	private Collection<Node> buildStatements(Statement[] children) {
		List<Node> childNodes = new ArrayList<Node>();
		if ( children != null ) for ( Statement child  : children ) addIfNotNull(childNodes, buildStatement(child));
		return childNodes;
	}
	
	private Node buildStatement(Statement child) {
		if ( child == null ) return null;
		if ( child instanceof TypeDeclaration ) return buildType((TypeDeclaration)child);
		
		if ( child instanceof LocalDeclaration ) return buildLocal((LocalDeclaration)child, Kind.LOCAL);
		
		if ( setAndGetAsHandled(child) ) return null;
		
		return drill(child);
	}
	
	private Node drill(Statement statement) {
		List<Node> childNodes = new ArrayList<Node>();
		for ( FieldAccess fa : fieldsOf(statement.getClass()) ) childNodes.addAll(buildWithField(Node.class, statement, fa));
		return putInMap(new Node(statement, childNodes, Kind.STATEMENT));
	}
	
	/** For eclipse, only Statement counts, as Expression is a subclass of it, eventhough this isn't
	 * entirely correct according to the JLS spec (only some expressions can be used as statements, not all of them). */
	@Override protected Collection<Class<? extends ASTNode>> getStatementTypes() {
		return Collections.<Class<? extends ASTNode>>singleton(Statement.class);
	}
}
