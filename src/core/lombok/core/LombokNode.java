/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import lombok.core.AST.Kind;

/**
 * An instance of this class wraps an Eclipse/javac internal node object.
 * 
 * @param A Type of our owning AST.
 * @param L self-type.
 * @param N The common type of all AST nodes in the internal representation of the target platform.
 *          For example, JCTree for javac, and ASTNode for Eclipse.
 */
public abstract class LombokNode<A extends AST<A, L, N>, L extends LombokNode<A, L, N>, N> implements DiagnosticsReceiver {
	protected final A ast;
	protected final Kind kind;
	protected final N node;
	protected final List<L> children;
	protected L parent;
	
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
	 * @param ast The owning AST - this node is part of this AST's tree of nodes.
	 * @param node The AST object in the target parser's own internal AST tree that this node object will represent.
	 * @param children A list of child nodes. Passing in null results in the children list being empty, not null.
	 * @param kind The kind of node represented by this object.
	 */
	@SuppressWarnings("unchecked")
	protected LombokNode(A ast, N node, List<L> children, Kind kind) {
		this.ast = ast;
		this.kind = kind;
		this.node = node;
		this.children = children == null ? new ArrayList<L>() : children;
		for (L child : this.children) {
			child.parent = (L) this;
			if (!child.isStructurallySignificant)
				child.isStructurallySignificant = calculateIsStructurallySignificant(node);
		}
		this.isStructurallySignificant = calculateIsStructurallySignificant(null);
	}
	
	public A getAst() {
		return ast;
	}
	
	/** {@inheritDoc} */
	@Override public String toString() {
		return String.format("NODE %s (%s) %s%s",
				kind, node == null ? "(NULL)" : node.getClass(), handled ? "[HANDLED]" : "", node == null ? "" : node);
	}
	
	/**
	 * Convenient shortcut to the owning ast object's {@code getPackageDeclaration} method.
	 * 
	 * @see AST#getPackageDeclaration()
	 */
	public String getPackageDeclaration() {
		return ast.getPackageDeclaration();
	}
	
	/**
	 * Convenient shortcut to the owning ast object's {@code getImportStatements} method.
	 * 
	 * @see AST#getImportStatements()
	 */
	public Collection<String> getImportStatements() {
		return ast.getImportStatements();
	}
	
	/**
	 * See {@link #isStructurallySignificant}.
	 */
	protected abstract boolean calculateIsStructurallySignificant(N parent);
	
	/**
	 * Convenient shortcut to the owning ast object's get method.
	 * 
	 * @see AST#get(Object)
	 */
	public L getNodeFor(N obj) {
		return ast.get(obj);
	}
	
	/**
	 * @return The javac/Eclipse internal AST object wrapped by this LombokNode object.
	 */
	public N get() {
		return node;
	}
	
	/**
	 * Replaces the AST node represented by this node object with the provided node. This node must
	 * have a parent, obviously, for this to work.
	 * 
	 * Also affects the underlying (Eclipse/javac) AST.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public L replaceWith(N newN, Kind newNodeKind) {
		ast.setChanged();
		L newNode = ast.buildTree(newN, newNodeKind);
		newNode.parent = parent;
		for (int i = 0; i < parent.children.size(); i++) {
			if (parent.children.get(i) == this) ((List)parent.children).set(i, newNode);
		}
		
		parent.replaceChildNode(get(), newN);
		return newNode;
	}
	
	/**
	 * Replaces the stated node with a new one. The old node must be a direct child of this node.
	 * 
	 * Also affects the underlying (Eclipse/javac) AST.
	 */
	public void replaceChildNode(N oldN, N newN) {
		ast.setChanged();
		ast.replaceStatementInNode(get(), oldN, newN);
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
	public L up() {
		L result = parent;
		while (result != null && !result.isStructurallySignificant) result = result.parent;
		return result;
	}
	
	/**
	 * {@code @Foo int x, y;} is stored in both javac and ecj as 2 FieldDeclarations, both with the same annotation as child.
	 * The normal {@code up()} method can't handle having multiple parents, but this one can.
	 */
	public Collection<L> upFromAnnotationToFields() {
		if (getKind() != Kind.ANNOTATION) return Collections.emptyList();
		L field = up();
		if (field == null || field.getKind() != Kind.FIELD) return Collections.emptyList();
		L type = field.up();
		if (type == null || type.getKind() != Kind.TYPE) return Collections.emptyList();
		
		List<L> fields = new ArrayList<L>();
		for (L potentialField : type.down()) {
			if (potentialField.getKind() != Kind.FIELD) continue;
			if (fieldContainsAnnotation(potentialField.get(), get())) fields.add(potentialField);
		}
		
		return fields;
	}
	
	/**
	 * Return {@code true} if the annotation is attached to the field.
	 */
	protected abstract boolean fieldContainsAnnotation(N field, N annotation);
	
	/**
	 * Returns the direct parent node in the AST tree of this node. For example, a local variable declaration's
	 * direct parent can be e.g. an If block, but its {@code up()} {@code LombokNode} is the {@code Method} that contains it.
	 */
	public L directUp() {
		return parent;
	}
	
	/**
	 * Returns all children nodes.
	 * 
	 * A copy is created, so changing the list has no effect. Also, while iterating through this list,
	 * you may add, remove, or replace children without causing {@code ConcurrentModificationException}s.
	 */
	public Collection<L> down() {
		return new ArrayList<L>(children);
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
	 * Sets the handled flag, then returns itself for chaining.
	 * 
	 * @see #handled
	 */
	@SuppressWarnings("unchecked")
	public L setHandled() {
		this.handled = true;
		return (L)this;
	}
	
	/**
	 * Convenient shortcut to the owning ast object's top method.
	 * 
	 * @see AST#top()
	 */
	public L top() {
		return ast.top();
	}
	
	/**
	 * Convenient shortcut to the owning ast object's getFileName method.
	 * 
	 * @see AST#getFileName()
	 */
	public String getFileName() {
		return ast.getFileName();
	}
	
	/**
	 * Adds the stated node as a direct child of this node.
	 * 
	 * Does not change the underlying (javac/Eclipse) AST, only the wrapped view.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public L add(N newChild, Kind newChildKind) {
		ast.setChanged();
		L n = ast.buildTree(newChild, newChildKind);
		if (n == null) return null;
		n.parent = (L) this;
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
		Map<N, L> oldNodes = new IdentityHashMap<N, L>();
		gatherAndRemoveChildren(oldNodes);
		
		L newNode = ast.buildTree(get(), kind);
		
		ast.setChanged();
		
		ast.replaceNewWithExistingOld(oldNodes, newNode);
	}
	
	@SuppressWarnings("unchecked")
	private void gatherAndRemoveChildren(Map<N, L> map) {
		for (L child : children) child.gatherAndRemoveChildren(map);
		ast.identityDetector.remove(get());
		map.put(get(), (L) this);
		children.clear();
		ast.getNodeMap().remove(get());
	}
	
	/**
	 * Removes the stated node, which must be a direct child of this node, from the AST.
	 * 
	 * Does not change the underlying (javac/Eclipse) AST, only the wrapped view.
	 */
	public void removeChild(L child) {
		ast.setChanged();
		children.remove(child);
	}
	
	/**
	 * Sets the handled flag on this node, and all child nodes, then returns itself, for chaining.
	 * 
	 * @see #handled
	 */
	@SuppressWarnings("unchecked")
	public L recursiveSetHandled() {
		this.handled = true;
		for (L child : children) child.recursiveSetHandled();
		return (L) this;
	}
	
	/**
	 * Structurally significant means: LocalDeclaration, TypeDeclaration, MethodDeclaration, ConstructorDeclaration,
	 * FieldDeclaration, Initializer, and CompilationUnitDeclaration.
	 * The rest is e.g. if statements, while loops, etc.
	 */
	public boolean isStructurallySignificant() {
		return isStructurallySignificant;
	}
}
