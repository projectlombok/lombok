package lombok.eclipse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.core.AST;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
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
		top().traverse(visitor);
	}
	
	private void traverseChildren(EclipseASTVisitor visitor, Node node) {
		for ( Node child : node.down() ) {
			child.traverse(visitor);
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
		final int sourceStart;
		final int sourceEnd;
		
		public ParseProblem(boolean isWarning, String message, int sourceStart, int sourceEnd) {
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
	
	public final class Node extends AST<ASTNode>.Node {
		Node(ASTNode node, Collection<Node> children, Kind kind) {
			super(node, children, kind);
		}
		
		public void rebuild() {
			super.rebuild();
			System.out.println("REBUILD COMPLETE");
			AbstractMethodDeclaration me = (AbstractMethodDeclaration) get();
			for ( Statement outer : me.statements ) {
				System.out.println("OUTER: "+ outer);
				if ( outer instanceof TryStatement ) {
					TryStatement ts = (TryStatement)outer;
					Block tb = ((TryStatement) outer).tryBlock;
					for ( Statement inner : tb.statements ) {
						System.out.println("INNER: " + inner);
					}
				}
			}
			System.out.println("/REBUILD COMPLETE");
		}
		
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
			addProblem(new ParseProblem(false, message, sourceStart, sourceEnd));
		}
		
		@Override public void addWarning(String message) {
			this.addWarning(message, this.get().sourceStart, this.get().sourceEnd);
		}
		
		public void addWarning(String message, int sourceStart, int sourceEnd) {
			addProblem(new ParseProblem(true, message, sourceStart, sourceEnd));
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
		return (unit.bits & ASTNode.HasAllMethodBodies) != 0;
	}
	
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
			addIfNotNull(childNodes, buildLocal(local, Kind.ARGUMENT));
		}
		return childNodes;
	}
	
	private Node buildLocal(LocalDeclaration local, Kind kind) {
		if ( alreadyHandled(local) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		addIfNotNull(childNodes, buildStatement(local.initialization));
		childNodes.addAll(buildAnnotations(local.annotations));
		return putInMap(new Node(local, childNodes, kind));
	}
	
	private Collection<Node> buildAnnotations(Annotation[] annotations) {
		if ( annotations == null ) return Collections.emptyList();
		List<Node> elements = new ArrayList<Node>();
		for ( Annotation an : annotations ) addIfNotNull(elements, buildAnnotation(an));
		return elements;
	}
	
	private Node buildAnnotation(Annotation annotation) {
		if ( annotation == null ) return null;
		if ( alreadyHandled(annotation) ) return null;
		return putInMap(new Node(annotation, null, Kind.ANNOTATION));
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
		
		if ( child instanceof LocalDeclaration ) return buildLocal((LocalDeclaration)child, Kind.LOCAL);
		
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
}
