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
	
	public final class Node {
		final ASTNode node;
		Node parent;
		final Collection<Node> children;
		boolean handled;
		
		Node(ASTNode node, Collection<Node> children) {
			this.node = node;
			this.children = children == null ? Collections.<Node>emptyList() : children;
		}
		
		public ASTNode getEclipseNode() {
			return node;
		}
		
		public Node up() {
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
	
	private Node buildTree(FieldDeclaration field) {
		if ( field instanceof Initializer ) return buildTree((Initializer)field);
		if ( identityDetector.containsKey(field) ) return null;
		return putInMap(new Node(field, buildWithStatement(field.initialization)));
	}
	
	private Node buildTree(Initializer initializer) {
		if ( identityDetector.containsKey(initializer) ) return null;
		return putInMap(new Node(initializer, buildWithStatement(initializer.block)));
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
			if ( !identityDetector.containsKey(local) ) {
				addIfNotNull(childNodes, buildTree(local));
				childNodes.addAll(buildWithStatement(local.initialization));
			}
		}
		return childNodes;
	}
	
	private Node buildTree(LocalDeclaration local) {
		if ( identityDetector.containsKey(local) ) return null;
		return putInMap(new Node(local, null));
	}
	
	private Collection<Node> buildTree(Statement[] children) {
		if ( children == null ) return Collections.emptyList();
		List<Node> childNodes = new ArrayList<Node>();
		for ( Statement child  : children ) childNodes.addAll(buildWithStatement(child));
		return childNodes;
	}
	
	//Almost anything is a statement, so this method has a different name to avoid overloading confusion
	private Collection<Node> buildWithStatement(Statement child) {
		if ( child == null || identityDetector.containsKey(child) ) return Collections.emptyList();
		if ( child instanceof TypeDeclaration ) {
			Node n = buildTree((TypeDeclaration)child);
			return n == null ? Collections.<Node>emptyList() : Collections.singleton(n);
		}
		
		if ( child instanceof LocalDeclaration ) {
			List<Node> childNodes = new ArrayList<Node>();
			addIfNotNull(childNodes, buildTree((LocalDeclaration)child));
			identityDetector.put(child, null);
			childNodes.addAll(buildWithStatement(((LocalDeclaration)child).initialization));
			return childNodes;
		}
		//We drill down because LocalDeclarations and TypeDeclarations can occur anywhere, even in, say,
		//an if block, or even the expression on an assert statement!
		
		identityDetector.put(child, null);
		return drill(child);
	}
	
	private Collection<Node> drill(Statement child) {
		List<Node> childNodes = new ArrayList<Node>();
		for ( FieldAccess fa : fieldsOf(child.getClass()) ) childNodes.addAll(buildWithField(child, fa));
		return childNodes;
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
	
	private Collection<Node> buildWithField(Statement child, FieldAccess fa) {
		if ( Modifier.isStatic(fa.field.getModifiers()) ) return Collections.emptyList();
		List<Node> list = new ArrayList<Node>();
		buildWithField(child, fa, list);
		return list;
	}
	
	private void buildWithField(Statement child, FieldAccess fa, Collection<Node> list) {
		try {
			Object o = fa.field.get(child);
			if ( fa.dim == 0 ) list.addAll(buildWithStatement((Statement)o));
			else buildWithArray(o, list, fa.dim);
		} catch ( IllegalAccessException e ) {
			sneakyThrow(e);
		}
	}
	
	private void buildWithArray(Object array, Collection<Node> list, int dim) {
		if ( array == null ) return;
		if ( dim == 1 ) for ( Object v : (Object[])array ) {
			list.addAll(buildWithStatement((Statement)v));
		} else for ( Object v : (Object[])array ) {
			buildWithArray(v, list, dim-1);
		}
	}
}
