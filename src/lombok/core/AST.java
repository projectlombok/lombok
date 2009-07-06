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
package lombok.core;

import static lombok.Lombok.sneakyThrow;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lombok wraps the AST produced by a target platform into its own AST system, mostly because both eclipse and javac
 * do not allow upward traversal (from a method to its owning type, for example).
 * 
 * @param N The common type of all AST nodes in the internal representation of the target platform.
 *   For example, JCTree for javac, and ASTNode for eclipse.
 */
public abstract class AST<N> {
	/** The kind of node represented by a given AST.Node object. */
	public enum Kind {
		COMPILATION_UNIT, TYPE, FIELD, INITIALIZER, METHOD, ANNOTATION, ARGUMENT, LOCAL, STATEMENT;
	}
	
	private Node top;
	private final String fileName;
	private Map<N, Void> identityDetector = new IdentityHashMap<N, Void>();
	private Map<N, Node> nodeMap = new IdentityHashMap<N, Node>();
	
	protected AST(String fileName) {
		this.fileName = fileName == null ? "(unknown).java" : fileName;
	}
	
	/** Set the node object that wraps the internal Compilation Unit node. */
	protected void setTop(Node top) {
		this.top = top;
	}
	
	/**
	 * Return the content of the package declaration on this AST's top (Compilation Unit) node.
	 * 
	 * Example: "java.util".
	 */
	public abstract String getPackageDeclaration();
	
	/**
	 * Return the contents of each non-static import statement on this AST's top (Compilation Unit) node.
	 * 
	 * Example: "java.util.IOException".
	 */
	public abstract Collection<String> getImportStatements();
	
	/**
	 * Puts the given node in the map so that javac/eclipse's own internal AST object can be translated to
	 * an AST.Node object. Also registers the object as visited to avoid endless loops.
	 */
	protected <T extends Node> T putInMap(T node) {
		nodeMap.put(node.get(), node);
		identityDetector.put(node.get(), null);
		return node;
	}
	
	/** Returns the node map, that can map javac/eclipse internal AST objects to AST.Node objects. */
	protected Map<N, Node> getNodeMap() {
		return nodeMap;
	}
	
	/** Clears the registry that avoids endless loops, and empties the node map. The existing node map
	 * object is left untouched, and instead a new map is created. */
	protected void clearState() {
		identityDetector = new IdentityHashMap<N, Void>();
		nodeMap = new IdentityHashMap<N, Node>();
	}
	
	/**
	 * Marks the stated node as handled (to avoid endless loops if 2 nodes refer to each other, or a node
	 * refers to itself). Will then return true if it was already set as handled before this call - in which
	 * case you should do nothing lest the AST build process loops endlessly.
	 */
	protected boolean setAndGetAsHandled(N node) {
		if ( identityDetector.containsKey(node) ) return true;
		identityDetector.put(node, null);
		return false;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	/** The AST.Node object representing the Compilation Unit. */
	public Node top() {
		return top;
	}
	
	/** Maps a javac/eclipse internal AST Node to the appropriate AST.Node object. */
	public Node get(N node) {
		return nodeMap.get(node);
	}
	
	@SuppressWarnings("unchecked")
	private Node replaceNewWithExistingOld(Map<N, Node> oldNodes, Node newNode) {
		Node oldNode = oldNodes.get(newNode.get());
		Node targetNode = oldNode == null ? newNode : oldNode;
		
		List children = new ArrayList();
		for ( Node child : newNode.children ) {
			Node oldChild = replaceNewWithExistingOld(oldNodes, child);
			children.add(oldChild);
			oldChild.parent = targetNode;
		}
		
		targetNode.children.clear();
		((List)targetNode.children).addAll(children);
		return targetNode;
	}
	
	/** An instance of this class wraps an eclipse/javac internal node object. */
	public abstract class Node {
		protected final Kind kind;
		protected final N node;
		protected final List<? extends Node> children;
		protected Node parent;
		
		/** This flag has no specified meaning; you can set and retrieve it.
		 * 
		 * In practice, for annotation nodes it means: Some AnnotationHandler finished whatever changes were required,
		 * and for all other nodes it means: This node was made by a lombok operation.
		 */
		protected boolean handled;
		
		/** structurally significant are those nodes that can be annotated in java 1.6 or are method-like toplevels,
		 * so fields, local declarations, method arguments, methods, types, the Compilation Unit itself, and initializers. */
		protected boolean isStructurallySignificant;
		
		/**
		 * Creates a new Node object that represents the provided node.
		 * 
		 * Make sure you manually set the parent correctly.
		 * 
		 * @param node The AST object in the target parser's own internal AST tree that this node object will represent.
		 * @param children A list of child nodes. Passing in null results in the children list being empty, not null.
		 * @param kind The kind of node represented by this object.
		 */
		protected Node(N node, List<? extends Node> children, Kind kind) {
			this.kind = kind;
			this.node = node;
			this.children = children == null ? new ArrayList<Node>() : children;
			for ( Node child : this.children ) child.parent = this;
			this.isStructurallySignificant = calculateIsStructurallySignificant();
		}
		
		/** {@inheritDoc} */
		@Override public String toString() {
			return String.format("NODE %s (%s) %s%s",
					kind, node == null ? "(NULL)" : node.getClass(), handled ? "[HANDLED]" : "", node == null ? "" : node);
		}
		
		/**
		 * Convenient shortcut to the owning JavacAST object's getPackageDeclaration method.
		 * 
		 * @see AST#getPackageDeclaration()
		 */
		public String getPackageDeclaration() {
			return AST.this.getPackageDeclaration();
		}
		
		/**
		 * Convenient shortcut to the owning JavacAST object's getImportStatements method.
		 * 
		 * @see AST#getImportStatements()
		 */
		public Collection<String> getImportStatements() {
			return AST.this.getImportStatements();
		}
		
		/**
		 * See {@link #isStructurallySignificant}.
		 */
		protected abstract boolean calculateIsStructurallySignificant();
		
		/**
		 * Convenient shortcut to the owning JavacAST object's getNodeFor method.
		 * 
		 * @see AST#getNodeFor()
		 */
		public Node getNodeFor(N obj) {
			return AST.this.get(obj);
		}
		
		/**
		 * @return The javac/eclipse internal AST object wrapped by this AST.Node object.
		 */
		public N get() {
			return node;
		}
		
		/**
		 * Replaces the AST node represented by this node object with the provided node. This node must
		 * have a parent, obviously, for this to work.
		 * 
		 * Also affects the underlying (eclipse/javac) AST.
		 */
		@SuppressWarnings("unchecked")
		public Node replaceWith(N newN, Kind kind) {
			Node newNode = buildTree(newN, kind);
			newNode.parent = parent;
			for ( int i = 0 ; i < parent.children.size() ; i++ ) {
				if ( parent.children.get(i) == this ) ((List)parent.children).set(i, newNode);
			}
			
			parent.replaceChildNode(get(), newN);
			return newNode;
		}
		
		/**
		 * Replaces the stated node with a new one. The old node must be a direct child of this node.
		 * 
		 * Also affects the underlying (eclipse/javac) AST.
		 */
		public void replaceChildNode(N oldN, N newN) {
			replaceStatementInNode(get(), oldN, newN);
		}
		
		public Kind getKind() {
			return kind;
		}
		
		/**
		 * Return the name of your type (simple name), method, field, or local variable. Return null if this
		 * node doesn't really have a name, such as initializers, while statements, etc.
		 */
		public abstract String getName();
		
		/** Returns the structurally significant node that encloses this one.
		 * 
		 * @see #isStructurallySignificant()
		 */
		public Node up() {
			Node result = parent;
			while ( result != null && !result.isStructurallySignificant ) result = result.parent;
			return result;
		}
		
		/**
		 * Returns the direct parent node in the AST tree of this node. For example, a local variable declaration's
		 * direct parent can be e.g. an If block, but its up() Node is the Method that contains it.
		 */
		public Node directUp() {
			return parent;
		}
		
		/**
		 * Returns all children nodes.
		 * 
		 * A copy is created, so changing the list has no effect. Also, while iterating through this list,
		 * you may add, remove, or replace children without causing ConcurrentModificationExceptions.
		 */
		public Collection<? extends Node> down() {
			return new ArrayList<Node>(children);
		}
		
		/**
		 * returns the value of the 'handled' flag.
		 * 
		 * @see #handled
		 */
		public boolean isHandled() {
			return handled;
		}
		
		/**
		 * Sets the handled flag, then returns 'this'.
		 * 
		 * @see #handled
		 */
		public Node setHandled() {
			this.handled = true;
			return this;
		}
		
		/**
		 * Convenient shortcut to the owning JavacAST object's top method.
		 * 
		 * @see AST#top()
		 */
		public Node top() {
			return top;
		}
		
		/**
		 * Convenient shortcut to the owning JavacAST object's getFileName method.
		 * 
		 * @see AST#getFileName()
		 */
		public String getFileName() {
			return fileName;
		}
		
		/**
		 * Adds the stated node as a direct child of this node.
		 * 
		 * Does not change the underlying (javac/eclipse) AST, only the wrapped view.
		 */
		@SuppressWarnings("unchecked") public Node add(N newChild, Kind kind) {
			Node n = buildTree(newChild, kind);
			if ( n == null ) return null;
			n.parent = this;
			((List)children).add(n);
			return n;
		}
		
		/**
		 * Reparses the AST node represented by this node. Any existing nodes that occupy a different space in the AST are rehomed, any
		 * nodes that no longer exist are removed, and new nodes are created.
		 * 
		 * Careful - the node you call this on must not itself have been removed or rehomed - it rebuilds <i>all children</i>.
		 */
		public void rebuild() {
			Map<N, Node> oldNodes = new IdentityHashMap<N, Node>();
			gatherAndRemoveChildren(oldNodes);
			
			Node newNode = buildTree(get(), kind);
			
			replaceNewWithExistingOld(oldNodes, newNode);
		}
		
		private void gatherAndRemoveChildren(Map<N, Node> map) {
			for ( Node child : children ) child.gatherAndRemoveChildren(map);
			identityDetector.remove(get());
			map.put(get(), this);
			children.clear();
			nodeMap.remove(get());
		}
		
		/**
		 * Removes the stated node, which must be a direct child of this node, from the AST.
		 * 
		 * Does not change the underlying (javac/eclipse) AST, only the wrapped view.
		 */
		public void removeChild(Node child) {
			children.remove(child);
		}
		
		/**
		 * Sets the handled flag on this node, and all child nodes, then returns this.
		 * 
		 * @see #handled
		 */
		public Node recursiveSetHandled() {
			this.handled = true;
			for ( Node child : children ) child.recursiveSetHandled();
			return this;
		}
		
		/** Generate a compiler error on this node. */
		public abstract void addError(String message);
		
		/** Generate a compiler warning on this node. */
		public abstract void addWarning(String message);
		
		/**
		 * Structurally significant means: LocalDeclaration, TypeDeclaration, MethodDeclaration, ConstructorDeclaration,
		 * FieldDeclaration, Initializer, and CompilationUnitDeclaration.
		 * The rest is e.g. if statements, while loops, etc.
		 */
		public boolean isStructurallySignificant() {
			return isStructurallySignificant;
		}
	}
	
	/** Build an AST.Node object for the stated internal (javac/eclipse) AST Node object. */
	protected abstract Node buildTree(N item, Kind kind);
	
	/**
	 * Represents a field that contains AST children.
	 */
	protected static class FieldAccess {
		/** The actual field. */
		public final Field field;
		/** Dimensions of the field. Works for arrays, or for java.util.collections. */
		public final int dim;
		
		FieldAccess(Field field, int dim) {
			this.field = field;
			this.dim = dim;
		}
	}
	
	private static Map<Class<?>, Collection<FieldAccess>> fieldsOfASTClasses = new HashMap<Class<?>, Collection<FieldAccess>>();
	
	/** Returns FieldAccess objects for the stated class. Each field that contains objects of the kind returned by
	 * {@link #getStatementTypes()}, either directly or inside of an array or java.util.collection (or array-of-arrays,
	 * or collection-of-collections, etcetera), is returned.
	 */
	protected Collection<FieldAccess> fieldsOf(Class<?> c) {
		Collection<FieldAccess> fields = fieldsOfASTClasses.get(c);
		if ( fields != null ) return fields;
		
		fields = new ArrayList<FieldAccess>();
		getFields(c, fields);
		fieldsOfASTClasses.put(c, fields);
		return fields;
	}
	
	private void getFields(Class<?> c, Collection<FieldAccess> fields) {
		if ( c == Object.class || c == null ) return;
		for ( Field f : c.getDeclaredFields() ) {
			if ( Modifier.isStatic(f.getModifiers()) ) continue;
			Class<?> t = f.getType();
			int dim = 0;
			
			if ( t.isArray() ) {
				while ( t.isArray() ) {
					dim++;
					t = t.getComponentType();
				}
			} else if ( Collection.class.isAssignableFrom(t) ) {
				while ( Collection.class.isAssignableFrom(t) ) {
					dim++;
					t = getComponentType(f.getGenericType());
				}
			}
			
			for ( Class<?> statementType : getStatementTypes() ) {
				if ( statementType.isAssignableFrom(t) ) {
					f.setAccessible(true);
					fields.add(new FieldAccess(f, dim));
					break;
				}
			}
		}
		getFields(c.getSuperclass(), fields);
	}
	
	private Class<?> getComponentType(Type type) {
		if ( type instanceof ParameterizedType ) {
			Type component = ((ParameterizedType)type).getActualTypeArguments()[0];
			return component instanceof Class<?> ? (Class<?>)component : Object.class;
		} else return Object.class;
	}
	
	/**
	 * The supertypes which are considered AST Node children. Usually, the Statement, and the Expression,
	 * though some platforms (such as eclipse) group these under one common supertype. */
	protected abstract Collection<Class<? extends N>> getStatementTypes();
	
	/**
	 * buildTree implementation that uses reflection to find all child nodes by way of inspecting
	 * the fields. */
	protected <T extends Node> Collection<T> buildWithField(Class<T> nodeType, N statement, FieldAccess fa) {
		List<T> list = new ArrayList<T>();
		buildWithField0(nodeType, statement, fa, list);
		return list;
	}
	
	/**
	 * Uses reflection to find the given direct child on the given statement, and replace it with a new child.
	 */
	protected boolean replaceStatementInNode(N statement, N oldN, N newN) {
		for ( FieldAccess fa : fieldsOf(statement.getClass()) ) {
			if ( replaceStatementInField(fa, statement, oldN, newN) ) return true;
		}
		
		return false;
	}
	
	private boolean replaceStatementInField(FieldAccess fa, N statement, N oldN, N newN) {
		try {
			Object o = fa.field.get(statement);
			if ( o == null ) return false;
			
			if ( o == oldN ) {
				fa.field.set(statement, newN);
				return true;
			}
			
			if ( fa.dim > 0 ) {
				if ( o.getClass().isArray() ) {
					return replaceStatementInArray(o, oldN, newN);
				} else if ( Collection.class.isInstance(o) ) {
					return replaceStatementInCollection(fa.field, statement, new ArrayList<Collection<?>>(), (Collection<?>)o, oldN, newN);
				}
			}
			
			return false;
		} catch ( IllegalAccessException e ) {
			throw sneakyThrow(e);
		}
		
	}
	
	private boolean replaceStatementInCollection(Field field, Object fieldRef, List<Collection<?>> chain, Collection<?> collection, N oldN, N newN) throws IllegalAccessException {
		if ( collection == null ) return false;
		
		int idx = -1;
		for ( Object o : collection ) {
			idx++;
			if ( o == null ) continue;
			if ( Collection.class.isInstance(o) ) {
				Collection<?> newC = (Collection<?>)o;
				List<Collection<?>> newChain = new ArrayList<Collection<?>>(chain);
				newChain.add(newC);
				if ( replaceStatementInCollection(field, fieldRef, newChain, newC, oldN, newN) ) return true;
			}
			if ( o == oldN ) {
				setElementInASTCollection(field, fieldRef, chain, collection, idx, newN);
				return true;
			}
		}
		
		return false;
	}
	
	/** Override if your AST collection does not support the set method. Javac's for example, does not. */
	@SuppressWarnings("unchecked")
	protected void setElementInASTCollection(Field field, Object fieldRef, List<Collection<?>> chain, Collection<?> collection, int idx, N newN) throws IllegalAccessException {
		if ( collection instanceof List<?> ) {
			((List)collection).set(idx, newN);
		}
	}
	
	private boolean replaceStatementInArray(Object array, N oldN, N newN) {
		if ( array == null ) return false;
		
		int len = Array.getLength(array);
		for ( int i = 0 ; i < len ; i++ ) {
			Object o = Array.get(array, i);
			if ( o == null ) continue;
			if ( o.getClass().isArray() ) {
				if ( replaceStatementInArray(o, oldN, newN) ) return true;
			} else if ( o == oldN ) {
				Array.set(array, i, newN);
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Node> void buildWithField0(Class<T> nodeType, N child, FieldAccess fa, Collection<T> list) {
		try {
			Object o = fa.field.get(child);
			if ( o == null ) return;
			if ( fa.dim == 0 ) {
				Node node = buildTree((N)o, Kind.STATEMENT);
				if ( node != null ) list.add(nodeType.cast(node));
			} else if ( o.getClass().isArray() ) buildWithArray(nodeType, o, list, fa.dim);
			else if ( Collection.class.isInstance(o) ) buildWithCollection(nodeType, o, list, fa.dim);
		} catch ( IllegalAccessException e ) {
			sneakyThrow(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Node> void buildWithArray(Class<T> nodeType, Object array, Collection<T> list, int dim) {
		if ( dim == 1 ) for ( Object v : (Object[])array ) {
			if ( v == null ) continue;
			Node node = buildTree((N)v, Kind.STATEMENT);
			if ( node != null ) list.add(nodeType.cast(node));
		} else for ( Object v : (Object[])array ) {
			if ( v == null ) return;
			buildWithArray(nodeType, v, list, dim-1);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Node> void buildWithCollection(Class<T> nodeType, Object collection, Collection<T> list, int dim) {
		if ( dim == 1 ) for ( Object v : (Collection<?>)collection ) {
			if ( v == null ) continue;
			Node node = buildTree((N)v, Kind.STATEMENT);
			if ( node != null ) list.add(nodeType.cast(node));
		} else for ( Object v : (Collection<?>)collection ) {
			buildWithCollection(nodeType, v, list, dim-1);
		}
	}
}
