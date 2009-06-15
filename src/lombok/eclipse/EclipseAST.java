package lombok.eclipse;

import static lombok.Lombok.sneakyThrow;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.apt.dispatch.AptProblem;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

public class EclipseAST {
	public void traverse(EclipseASTVisitor visitor) {
		Node current = top();
		visitor.visitCompilationUnit(current, (CompilationUnitDeclaration)current.node);
		traverseChildren(visitor, current);
		visitor.endVisitCompilationUnit(current, (CompilationUnitDeclaration)current.node);
	}
	
	private void traverseChildren(EclipseASTVisitor visitor, Node node) {
		for ( Node child : node.children ) {
			ASTNode n = child.node;
			if ( n instanceof TypeDeclaration ) {
				visitor.visitType(child, (TypeDeclaration)n);
				traverseChildren(visitor, child);
				visitor.endVisitType(child, (TypeDeclaration)n);
			} else if ( n instanceof Initializer ) {
				visitor.visitInitializer(child, (Initializer)n);
				traverseChildren(visitor, child);
				visitor.endVisitInitializer(child, (Initializer)n);
			} else if ( n instanceof FieldDeclaration ) {
				visitor.visitField(child, (FieldDeclaration)n);
				traverseChildren(visitor, child);
				visitor.endVisitField(child, (FieldDeclaration)n);
			} else if ( n instanceof AbstractMethodDeclaration ) {
				if ( n instanceof Clinit ) continue;
				visitor.visitMethod(child, (AbstractMethodDeclaration)n);
				traverseChildren(visitor, child);
				visitor.endVisitMethod(child, (AbstractMethodDeclaration)n);
			} else if ( n instanceof LocalDeclaration ) {
				visitor.visitLocal(child, (LocalDeclaration)n);
				traverseChildren(visitor, child);
				visitor.endVisitLocal(child, (LocalDeclaration)n);
			} else if ( n instanceof Statement ) {
				visitor.visitStatement(child, (Statement)n);
				traverseChildren(visitor, child);
				visitor.endVisitStatement(node, (Statement)n);
			} else throw new AssertionError("Can't be reached");
		}
	}
	
	public boolean isCompleteParse() {
		return completeParse;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Node top() {
		return top;
	}
	
	public Node get(ASTNode node) {
		return nodeMap.get(node);
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
			addProblemToCompilationResult(getFileName(), (CompilationUnitDeclaration) top().getEclipseNode(),
					isWarning, message, node.getEclipseNode(), sourceStart, sourceEnd);
		}
	}
	
	public void propagateProblems() {
		if ( queuedProblems.isEmpty() ) return;
		CompilationUnitDeclaration cud = (CompilationUnitDeclaration) top().getEclipseNode();
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
	
	public final class Node {
		final ASTNode node;
		Node parent;
		final Collection<Node> children;
		boolean handled;
		private final boolean isStructurallySignificant;
		
		Node(ASTNode node, Collection<Node> children) {
			this.node = node;
			this.children = children == null ? Collections.<Node>emptyList() : children;
			this.isStructurallySignificant = calculateIsStructurallySignificant();
		}
		
		public void addError(String message) {
			this.addError(message, this.getEclipseNode().sourceStart, this.getEclipseNode().sourceEnd);
		}
		
		public void addError(String message, int sourceStart, int sourceEnd) {
			addProblem(new ParseProblem(false, message, this, sourceStart, sourceEnd));
		}
		
		public void addWarning(String message) {
			this.addWarning(message, this.getEclipseNode().sourceStart, this.getEclipseNode().sourceEnd);
		}
		
		public void addWarning(String message, int sourceStart, int sourceEnd) {
			addProblem(new ParseProblem(true, message, this, sourceStart, sourceEnd));
		}
		
		public ASTNode getEclipseNode() {
			return node;
		}
		
		/** Returns the structurally significant node that encloses this one.
		 * 
		 * @see #isStructurallySignificant()
		 */
		public Node up() {
			Node result = parent;
			while ( result != null && !result.isStructurallySignificant() ) result = result.parent;
			return result;
		}
		
		/**
		 * Structurally significant means: LocalDeclaration, TypeDeclaration, MethodDeclaration, ConstructorDeclaration,
		 * FieldDeclaration, Initializer, and CompilationUnitDeclaration.
		 * The rest is e.g. if statements, while loops, etc.
		 */
		public boolean isStructurallySignificant() {
			return isStructurallySignificant;
		}
		
		private boolean calculateIsStructurallySignificant() {
			if ( node instanceof TypeDeclaration ) return true;
			if ( node instanceof AbstractMethodDeclaration ) return true;
			if ( node instanceof FieldDeclaration ) return true;
			if ( node instanceof LocalDeclaration ) return true;
			if ( node instanceof CompilationUnitDeclaration ) return true;
			return false;
		}
		
		/**
		 * Returns the direct parent node in the AST tree of this node. For example, a local variable declaration's
		 * direct parent can be e.g. an If block, but its up() Node is the Method that contains it.
		 */
		public Node directUp() {
			return parent;
		}
		
		public Collection<Node> down() {
			return children;
		}
		
		public boolean isHandled() {
			return handled;
		}
		
		public Node setHandled() {
			this.handled = true;
			return this;
		}
		
		public Node top() {
			return top;
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public boolean isCompleteParse() {
			return completeParse;
		}
	}
	
	private final Map<ASTNode, Void> identityDetector = new IdentityHashMap<ASTNode, Void>();
	private Map<ASTNode, Node> nodeMap = new HashMap<ASTNode, Node>();
	private final CompilationUnitDeclaration compilationUnitDeclaration;
	private final String fileName;
	private Node top;
	private boolean completeParse;
	
	public EclipseAST(CompilationUnitDeclaration ast) {
		this.compilationUnitDeclaration = ast;
		this.fileName = ast.compilationResult.fileName == null ? "(unknown).java" : new String(ast.compilationResult.fileName);
		this.top = buildTree(ast);
		this.completeParse = isComplete(ast);
	}
	
	public void reparse() {
		propagateProblems();
		if ( completeParse ) return;
		boolean newCompleteParse = isComplete(compilationUnitDeclaration);
		if ( !newCompleteParse ) return;
		Map<ASTNode, Node> oldMap = nodeMap;
		nodeMap = new HashMap<ASTNode, Node>();
		this.top = buildTree(compilationUnitDeclaration);
		
		//Retain 'handled' flags.
		for ( Map.Entry<ASTNode, Node> e : nodeMap.entrySet() ) {
			Node oldEntry = oldMap.get(e.getKey());
			if ( oldEntry != null && oldEntry.handled ) e.getValue().handled = true;
		}
		
		this.completeParse = true;
	}
	
	private static boolean isComplete(CompilationUnitDeclaration unit) {
		return (unit.bits & ASTNode.HasAllMethodBodies) > 0;
	}
	
	private Node putInMap(Node parent) {
		for ( Node child : parent.children ) child.parent = parent;
		nodeMap.put(parent.node, parent);
		identityDetector.put(parent.node, null);
		return parent;
	}
	
	private Node buildTree(CompilationUnitDeclaration top) {
		identityDetector.clear();
		Collection<Node> children = buildTree(top.types);
		return putInMap(new Node(top, children));
	}
	
	private void addIfNotNull(Collection<Node> collection, Node n) {
		if ( n != null ) collection.add(n);
	}
	
	private Collection<Node> buildTree(TypeDeclaration[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( TypeDeclaration type : children ) addIfNotNull(childNodes, buildTree(type));
		return childNodes;
	}
	
	private Node buildTree(TypeDeclaration type) {
		if ( identityDetector.containsKey(type) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		childNodes.addAll(buildTree(type.fields));
		childNodes.addAll(buildTree(type.memberTypes));
		childNodes.addAll(buildTree(type.methods));
		return putInMap(new Node(type, childNodes));
	}
	
	private Collection<Node> buildTree(FieldDeclaration[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( FieldDeclaration child : children ) addIfNotNull(childNodes, buildTree(child));
		return childNodes;
	}
	
	private static <T> Collection<T> singleton(T item) {
		if ( item == null ) return Collections.emptyList();
		else return Collections.singleton(item);
	}
	
	private Node buildTree(FieldDeclaration field) {
		if ( field instanceof Initializer ) return buildTree((Initializer)field);
		if ( identityDetector.containsKey(field) ) return null;
		return putInMap(new Node(field, singleton(buildWithStatement(field.initialization))));
	}
	
	private Node buildTree(Initializer initializer) {
		if ( identityDetector.containsKey(initializer) ) return null;
		return putInMap(new Node(initializer, singleton(buildWithStatement(initializer.block))));
	}
	
	private Collection<Node> buildTree(AbstractMethodDeclaration[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for (AbstractMethodDeclaration method : children ) addIfNotNull(childNodes, buildTree(method));
		return childNodes;
	}
	
	private Node buildTree(AbstractMethodDeclaration method) {
		if ( identityDetector.containsKey(method) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		childNodes.addAll(buildTree(method.arguments));
		childNodes.addAll(buildTree(method.statements));
		return putInMap(new Node(method, childNodes));
	}
	
	//Arguments are a kind of LocalDeclaration. They can definitely contain lombok annotations, so we care about them.
	private Collection<Node> buildTree(Argument[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( LocalDeclaration local : children ) {
			addIfNotNull(childNodes, buildTree(local));
		}
		return childNodes;
	}
	
	private Node buildTree(LocalDeclaration local) {
		if ( identityDetector.containsKey(local) ) return null;
		return putInMap(new Node(local, singleton(buildWithStatement(local.initialization))));
	}
	
	private Collection<Node> buildTree(Statement[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( Statement child  : children ) addIfNotNull(childNodes, buildWithStatement(child));
		return childNodes;
	}
	
	//Almost anything is a statement, so this method has a different name to avoid overloading confusion
	private Node buildWithStatement(Statement child) {
		if ( child == null || identityDetector.containsKey(child) ) return null;
		if ( child instanceof TypeDeclaration ) return buildTree((TypeDeclaration)child);
		
		if ( child instanceof LocalDeclaration ) return buildTree((LocalDeclaration)child);
		
		//We drill down because LocalDeclarations and TypeDeclarations can occur anywhere, even in, say,
		//an if block, or even the expression on an assert statement!
		
		return drill(child);
	}
	
	private Node drill(Statement statement) {
		List<Node> childNodes = new ArrayList<Node>();
		for ( FieldAccess fa : fieldsOf(statement.getClass()) ) childNodes.addAll(buildWithField(statement, fa));
		return new Node(statement, childNodes);
	}
	
	private static class FieldAccess {
		final Field field;
		final int dim;
		
		FieldAccess(Field field, int dim) {
			this.field = field;
			this.dim = dim;
		}
	}
	
	private static Map<Class<?>, Collection<FieldAccess>> fieldsOfASTClasses = new HashMap<Class<?>, Collection<FieldAccess>>();
	private Collection<FieldAccess> fieldsOf(Class<?> c) {
		Collection<FieldAccess> fields = fieldsOfASTClasses.get(c);
		if ( fields != null ) return fields;
		
		fields = new ArrayList<FieldAccess>();
		getFields(c, fields);
		fieldsOfASTClasses.put(c, fields);
		return fields;
	}
	
	private void getFields(Class<?> c, Collection<FieldAccess> fields) {
		if ( c == ASTNode.class || c == null ) return;
		for ( Field f : c.getDeclaredFields() ) {
			if ( Modifier.isStatic(f.getModifiers()) ) continue;
			Class<?> t = f.getType();
			int dim = 0;
			while ( t.isArray() ) {
				dim++;
				t = t.getComponentType();
			}
			if ( Statement.class.isAssignableFrom(t) ) {
				f.setAccessible(true);
				fields.add(new FieldAccess(f, dim));
			}
		}
		getFields(c.getSuperclass(), fields);
	}
	
	private Collection<Node> buildWithField(Statement statement, FieldAccess fa) {
		List<Node> list = new ArrayList<Node>();
		buildWithField(statement, fa, list);
		return list;
	}
	
	private void buildWithField(Statement child, FieldAccess fa, Collection<Node> list) {
		try {
			Object o = fa.field.get(child);
			if ( fa.dim == 0 ) addIfNotNull(list, buildWithStatement((Statement)o));
			else buildWithArray(o, list, fa.dim);
		} catch ( IllegalAccessException e ) {
			sneakyThrow(e);
		}
	}
	
	private void buildWithArray(Object array, Collection<Node> list, int dim) {
		if ( array == null ) return;
		if ( dim == 1 ) for ( Object v : (Object[])array ) {
			addIfNotNull(list, buildWithStatement((Statement)v));
		} else for ( Object v : (Object[])array ) {
			buildWithArray(v, list, dim-1);
		}
	}
}
