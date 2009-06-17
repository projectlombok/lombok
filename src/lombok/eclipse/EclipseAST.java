package lombok.eclipse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.core.AST;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.apt.dispatch.AptProblem;
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
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

public class EclipseAST extends AST<ASTNode> {
	@Override public String getPackageDeclaration() {
		CompilationUnitDeclaration cud = (CompilationUnitDeclaration) top().get();
		ImportReference pkg = cud.currentPackage;
		return pkg == null ? null : Eclipse.toQualifiedName(pkg.getImportName());
	}
	
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
	
	public void traverse(EclipseASTVisitor visitor) {
		Node current = top();
		visitor.visitCompilationUnit(current, (CompilationUnitDeclaration)current.get());
		traverseChildren(visitor, current);
		visitor.endVisitCompilationUnit(current, (CompilationUnitDeclaration)current.get());
	}
	
	private void traverseChildren(EclipseASTVisitor visitor, Node node) {
		for ( Node child : node.down() ) {
			ASTNode n = child.get();
			switch ( child.getKind() ) {
			case TYPE:
				visitor.visitType(child, (TypeDeclaration)n);
				traverseChildren(visitor, child);
				visitor.endVisitType(child, (TypeDeclaration)n);
				break;
			case FIELD:
				visitor.visitField(child, (FieldDeclaration)n);
				traverseChildren(visitor, child);
				visitor.endVisitField(child, (FieldDeclaration)n);
				break;
			case INITIALIZER:
				visitor.visitInitializer(child, (Initializer)n);
				traverseChildren(visitor, child);
				visitor.endVisitInitializer(child, (Initializer)n);
				break;
			case METHOD:
				if ( n instanceof Clinit ) continue;
				visitor.visitMethod(child, (AbstractMethodDeclaration)n);
				traverseChildren(visitor, child);
				visitor.endVisitMethod(child, (AbstractMethodDeclaration)n);
				break;
			case ARGUMENT:
				AbstractMethodDeclaration method = (AbstractMethodDeclaration)child.up().get();
				visitor.visitMethodArgument(child, (Argument)n, method);
				traverseChildren(visitor, child);
				visitor.endVisitMethodArgument(child, (Argument)n, method);
				break;
			case LOCAL:
				visitor.visitLocal(child, (LocalDeclaration)n);
				traverseChildren(visitor, child);
				visitor.endVisitLocal(child, (LocalDeclaration)n);
				break;
			case ANNOTATION:
				Node parent = child.up();
				switch ( parent.getKind() ) {
				case TYPE:
					visitor.visitAnnotationOnType((TypeDeclaration)parent.get(), child, (Annotation)n);
					break;
				case FIELD:
					visitor.visitAnnotationOnField((FieldDeclaration)parent.get(), child, (Annotation)n);
					break;
				case METHOD:
					visitor.visitAnnotationOnMethod((AbstractMethodDeclaration)parent.get(), child, (Annotation)n);
					break;
				case ARGUMENT:
					visitor.visitAnnotationOnMethodArgument(
							(Argument)parent.get(),
							(AbstractMethodDeclaration)parent.directUp().get(),
							child, (Annotation)n);
					break;
				case LOCAL:
					visitor.visitAnnotationOnLocal((LocalDeclaration)parent.get(), child, (Annotation)n);
					break;
				default:
					throw new AssertionError("Annotion not expected as child of a " + parent.getKind());
				}
				break;
			case STATEMENT:
				visitor.visitStatement(child, (Statement)n);
				traverseChildren(visitor, child);
				visitor.endVisitStatement(node, (Statement)n);
				break;
			default:
				throw new AssertionError("Unexpected kind during child traversal: " + child.getKind());
			}
		}
	}
	
	public boolean isCompleteParse() {
		return completeParse;
	}
	
	@Override public Node top() {
		return (Node) super.top();
	}
	
	public Node get(ASTNode node) {
		return (Node) super.get(node);
	}
	
	private class ParseProblem {
		final boolean isWarning;
		final String message;
		final Node node;
		final int sourceStart;
		final int sourceEnd;
		
		public ParseProblem(boolean isWarning, String message, Node node, int sourceStart, int sourceEnd) {
			this.isWarning = isWarning;
			this.message = message;
			this.node = node;
			this.sourceStart = sourceStart;
			this.sourceEnd = sourceEnd;
		}
		
		void addToCompilationResult() {
			addProblemToCompilationResult(getFileName(), (CompilationUnitDeclaration) top().get(),
					isWarning, message, node.get(), sourceStart, sourceEnd);
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
	
	static void addProblemToCompilationResult(String fileName, CompilationUnitDeclaration ast,
			boolean isWarning, String message, ASTNode node, int sourceStart, int sourceEnd) {
		char[] fileNameArray = fileName.toCharArray();
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
		
		CategorizedProblem ecProblem = new AptProblem(null, 
				fileNameArray, message, 0, new String[0],
				isWarning ? ProblemSeverities.Warning : ProblemSeverities.Error,
				sourceStart, sourceEnd, lineNumber, columnNumber);
		ast.compilationResult.record(ecProblem, null);
	}
	
	public final class Node extends AST<ASTNode>.Node {
		Node(ASTNode node, Collection<Node> children, Kind kind) {
			super(node, children, kind);
		}
		
		@Override public String getName() {
			final char[] n;
			if ( node instanceof TypeDeclaration ) n = ((TypeDeclaration)node).name;
			else if ( node instanceof FieldDeclaration ) n = ((FieldDeclaration)node).name;
			else if ( node instanceof AbstractMethodDeclaration ) n = ((AbstractMethodDeclaration)node).selector;
			else if ( node instanceof LocalDeclaration ) n = ((LocalDeclaration)node).name;
			else n = null;
			
			return n == null ? null : new String(n);
		}
		
		@Override public void addError(String message) {
			this.addError(message, this.get().sourceStart, this.get().sourceEnd);
		}
		
		public void addError(String message, int sourceStart, int sourceEnd) {
			addProblem(new ParseProblem(false, message, this, sourceStart, sourceEnd));
		}
		
		@Override public void addWarning(String message) {
			this.addWarning(message, this.get().sourceStart, this.get().sourceEnd);
		}
		
		public void addWarning(String message, int sourceStart, int sourceEnd) {
			addProblem(new ParseProblem(true, message, this, sourceStart, sourceEnd));
		}
		
		/** {@inheritDoc} */
		@Override public Node up() {
			return (Node) super.up();
		}
		
		@Override protected boolean calculateIsStructurallySignificant() {
			if ( node instanceof TypeDeclaration ) return true;
			if ( node instanceof AbstractMethodDeclaration ) return true;
			if ( node instanceof FieldDeclaration ) return true;
			if ( node instanceof LocalDeclaration ) return true;
			if ( node instanceof CompilationUnitDeclaration ) return true;
			return false;
		}
		
		/** {@inheritDoc} */
		public Node directUp() {
			return (Node) super.directUp();
		}
		
		/** {@inheritDoc} */
		@SuppressWarnings("unchecked")
		@Override public Collection<Node> down() {
			return (Collection<Node>) children;
		}
		
		/** {@inheritDoc} */
		@Override public Node top() {
			return (Node) super.top();
		}
		
		public boolean isCompleteParse() {
			return completeParse;
		}
	}
	
	private final CompilationUnitDeclaration compilationUnitDeclaration;
	private boolean completeParse;
	
	public EclipseAST(CompilationUnitDeclaration ast) {
		super(toFileName(ast));
		this.compilationUnitDeclaration = ast;
		setTop(buildCompilationUnit(ast));
		this.completeParse = isComplete(ast);
	}
	
	private static String toFileName(CompilationUnitDeclaration ast) {
		return ast.compilationResult.fileName == null ? null : new String(ast.compilationResult.fileName);
	}

	public void reparse() {
		propagateProblems();
		if ( completeParse ) return;
		boolean newCompleteParse = isComplete(compilationUnitDeclaration);
		if ( !newCompleteParse ) return;
		Map<ASTNode, AST<ASTNode>.Node> oldMap = getNodeMap();
		clearState();
		setTop(buildCompilationUnit(compilationUnitDeclaration));
		
		//Retain 'handled' flags.
		for ( Map.Entry<ASTNode, AST<ASTNode>.Node> e : getNodeMap().entrySet() ) {
			Node oldEntry = (Node) oldMap.get(e.getKey());
			if ( oldEntry != null && oldEntry.isHandled() ) e.getValue().setHandled();
		}
		
		this.completeParse = true;
	}
	
	private static boolean isComplete(CompilationUnitDeclaration unit) {
		return (unit.bits & ASTNode.HasAllMethodBodies) > 0;
	}
	
	private Node buildCompilationUnit(CompilationUnitDeclaration top) {
		Collection<Node> children = buildTypes(top.types);
		return putInMap(new Node(top, children, Kind.COMPILATION_UNIT));
	}
	
	private void addIfNotNull(Collection<Node> collection, Node n) {
		if ( n != null ) collection.add(n);
	}
	
	private Collection<Node> buildTypes(TypeDeclaration[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( TypeDeclaration type : children ) addIfNotNull(childNodes, buildType(type));
		return childNodes;
	}
	
	private Node buildType(TypeDeclaration type) {
		if ( alreadyHandled(type) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		childNodes.addAll(buildFields(type.fields));
		childNodes.addAll(buildTypes(type.memberTypes));
		childNodes.addAll(buildMethods(type.methods));
		childNodes.addAll(buildAnnotations(type.annotations));
		return putInMap(new Node(type, childNodes, Kind.TYPE));
	}
	
	private Collection<Node> buildFields(FieldDeclaration[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( FieldDeclaration child : children ) addIfNotNull(childNodes, buildField(child));
		return childNodes;
	}
	
	private static <T> Collection<T> singleton(T item) {
		if ( item == null ) return Collections.emptyList();
		else return Collections.singleton(item);
	}
	
	private Node buildField(FieldDeclaration field) {
		if ( field instanceof Initializer ) return buildInitializer((Initializer)field);
		if ( alreadyHandled(field) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		addIfNotNull(childNodes, buildStatement(field.initialization));
		childNodes.addAll(buildAnnotations(field.annotations));
		return putInMap(new Node(field, childNodes, Kind.FIELD));
	}
	
	private Node buildInitializer(Initializer initializer) {
		if ( alreadyHandled(initializer) ) return null;
		return putInMap(new Node(initializer, singleton(buildStatement(initializer.block)), Kind.INITIALIZER));
	}
	
	private Collection<Node> buildMethods(AbstractMethodDeclaration[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for (AbstractMethodDeclaration method : children ) addIfNotNull(childNodes, buildMethod(method));
		return childNodes;
	}
	
	private Node buildMethod(AbstractMethodDeclaration method) {
		if ( alreadyHandled(method) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		childNodes.addAll(buildArguments(method.arguments));
		childNodes.addAll(buildStatements(method.statements));
		childNodes.addAll(buildAnnotations(method.annotations));
		return putInMap(new Node(method, childNodes, Kind.METHOD));
	}
	
	//Arguments are a kind of LocalDeclaration. They can definitely contain lombok annotations, so we care about them.
	private Collection<Node> buildArguments(Argument[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( LocalDeclaration local : children ) {
			addIfNotNull(childNodes, buildLocal(local));
		}
		return childNodes;
	}
	
	private Node buildLocal(LocalDeclaration local) {
		if ( alreadyHandled(local) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		addIfNotNull(childNodes, buildStatement(local.initialization));
		childNodes.addAll(buildAnnotations(local.annotations));
		return putInMap(new Node(local, childNodes, Kind.LOCAL));
	}
	
	private Collection<Node> buildAnnotations(Annotation[] annotations) {
		if ( annotations == null ) return Collections.emptyList();
		List<Node> elements = new ArrayList<Node>();
		for ( Annotation an : annotations ) {
			if ( an == null ) continue;
			if ( alreadyHandled(an) ) continue;
			elements.add(putInMap(new Node(an, null, Kind.ANNOTATION)));
		}
		return elements;
	}
	
	private Collection<Node> buildStatements(Statement[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( Statement child  : children ) addIfNotNull(childNodes, buildStatement(child));
		return childNodes;
	}
	
	//Almost anything is a statement, so this method has a different name to avoid overloading confusion
	private Node buildStatement(Statement child) {
		if ( child == null || alreadyHandled(child) ) return null;
		if ( child instanceof TypeDeclaration ) return buildType((TypeDeclaration)child);
		
		if ( child instanceof LocalDeclaration ) return buildLocal((LocalDeclaration)child);
		
		//We drill down because LocalDeclarations and TypeDeclarations can occur anywhere, even in, say,
		//an if block, or even the expression on an assert statement!
		
		setAsHandled(child);
		return drill(child);
	}
	
	protected Node drill(Statement statement) {
		List<Node> childNodes = new ArrayList<Node>();
		for ( FieldAccess fa : fieldsOf(statement.getClass()) ) childNodes.addAll(buildWithField(Node.class, statement, fa));
		return putInMap(new Node(statement, childNodes, Kind.STATEMENT));
	}
	
	@Override protected Collection<Class<? extends ASTNode>> getStatementTypes() {
		return Collections.<Class<? extends ASTNode>>singleton(Statement.class);
	}
	
	@Override protected Node buildStatement(Object node) {
		return buildStatement((Statement)node);
	}
}
