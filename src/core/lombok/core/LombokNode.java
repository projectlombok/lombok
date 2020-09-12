/*
 * Copyright (C) 2009-2020 The Project Lombok Authors.
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

import java.lang.annotation.Annotation;
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
	protected final Kind kind;
	protected final N node;
	protected LombokImmutableList<L> children;
	protected L parent;
	
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
	protected LombokNode(N node, List<L> children, Kind kind) {
		this.kind = kind;
		this.node = node;
		this.children = children != null ? LombokImmutableList.copyOf(children) : LombokImmutableList.<L>of();
		for (L child : this.children) {
			child.parent = (L) this;
			if (!child.isStructurallySignificant)
				child.isStructurallySignificant = calculateIsStructurallySignificant(node);
		}
		this.isStructurallySignificant = calculateIsStructurallySignificant(null);
	}
	
	public abstract A getAst();
	
	/** {@inheritDoc} */
	@Override public String toString() {
		return String.format("NODE %s (%s) %s",
				kind, node == null ? "(NULL)" : node.getClass(), node == null ? "" : node);
	}
	
	/**
	 * Convenient shortcut to the owning ast object's {@code getPackageDeclaration} method.
	 * 
	 * @see AST#getPackageDeclaration()
	 */
	public String getPackageDeclaration() {
		return getAst().getPackageDeclaration();
	}
	
	/**
	 * Convenient shortcut to the owning ast object's {@code getImportList} method.
	 * 
	 * @see AST#getImportList()
	 */
	public ImportList getImportList() {
		return getAst().getImportList();
	}
	
	/**
	 * Convenient shortcut to the owning ast object's {@code getImportListAsTypeResolver} method.
	 * 
	 * @see AST#getImportListAsTypeResolver()
	 */
	public TypeResolver getImportListAsTypeResolver() {
		return getAst().getImportListAsTypeResolver();
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
		return getAst().get(obj);
	}
	
	/**
	 * @return The javac/Eclipse internal AST object wrapped by this LombokNode object.
	 */
	public N get() {
		return node;
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
			for (L child : potentialField.down()) {
				if (child.getKind() != Kind.ANNOTATION) continue;
				if (child.get() == get()) fields.add(potentialField);
			}
		}
		
		return fields;
	}
	
	/**
	 * Returns the direct parent node in the AST tree of this node. For example, a local variable declaration's
	 * direct parent can be e.g. an If block, but its {@code up()} {@code LombokNode} is the {@code Method} that contains it.
	 */
	public L directUp() {
		return parent;
	}
	
	/**
	 * Returns all children nodes.
	 */
	public LombokImmutableList<L> down() {
		return children;
	}
	
	/**
	 * Convenient shortcut to the owning ast object's getLatestJavaSpecSupported method.
	 * 
	 * @see AST#getLatestJavaSpecSupported()
	 */
	public int getLatestJavaSpecSupported() {
		return getAst().getLatestJavaSpecSupported();
	}
	
	/**
	 * Convenient shortcut to the owning ast object's getSourceVersion method.
	 * 
	 * @see AST#getSourceVersion()
	 */
	public int getSourceVersion() {
		return getAst().getSourceVersion();
	}
	
	/**
	 * Convenient shortcut to the owning ast object's top method.
	 * 
	 * @see AST#top()
	 */
	public L top() {
		return getAst().top();
	}
	
	/**
	 * Convenient shortcut to the owning ast object's getFileName method.
	 * 
	 * @see AST#getFileName()
	 */
	public String getFileName() {
		return getAst().getFileName();
	}
	
	/**
	 * Adds the stated node as a direct child of this node.
	 * 
	 * Does not change the underlying (javac/Eclipse) AST, only the wrapped view.
	 */
	@SuppressWarnings({"unchecked"})
	public L add(N newChild, Kind newChildKind) {
		getAst().setChanged();
		L n = getAst().buildTree(newChild, newChildKind);
		if (n == null) return null;
		n.parent = (L) this;
		children = children.append(n);
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
		
		L newNode = getAst().buildTree(get(), kind);
		
		getAst().setChanged();
		
		getAst().replaceNewWithExistingOld(oldNodes, newNode);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void gatherAndRemoveChildren(Map<N, L> map) {
		for (LombokNode child : children) child.gatherAndRemoveChildren(map);
		getAst().identityDetector.remove(get());
		map.put(get(), (L) this);
		children = LombokImmutableList.of();
		getAst().getNodeMap().remove(get());
	}
	
	/**
	 * Removes the stated node, which must be a direct child of this node, from the AST.
	 * 
	 * Does not change the underlying (javac/Eclipse) AST, only the wrapped view.
	 */
	public void removeChild(L child) {
		getAst().setChanged();
		children = children.removeElement(child);
	}
	
	/**
	 * Structurally significant means: LocalDeclaration, TypeDeclaration, MethodDeclaration, ConstructorDeclaration,
	 * FieldDeclaration, Initializer, and CompilationUnitDeclaration.
	 * The rest is e.g. if statements, while loops, etc.
	 */
	public boolean isStructurallySignificant() {
		return isStructurallySignificant;
	}
	
	public abstract boolean hasAnnotation(Class<? extends Annotation> type);
	public abstract <Z extends Annotation> AnnotationValues<Z> findAnnotation(Class<Z> type);
	
	public abstract boolean isStatic();
	public abstract boolean isFinal();
	public abstract boolean isTransient();
	public abstract boolean isPrimitive();
	public abstract boolean isEnumMember();
	public abstract boolean isEnumType();
	
	/**
	 * The 'type' of the field or method, or {@code null} if this node is neither.
	 * 
	 * The type is as it is written in the code (no resolution), includes array dimensions, 
	 * but not necessarily generics.
	 * 
	 * The main purpose of this method is to verify this type against a list of known types,
	 * like primitives or primitive wrappers.
	 * 
	 * @return The 'type' of the field or method, or {@code null} if this node is neither.
	 */
	public abstract String fieldOrMethodBaseType();
	
	public abstract int countMethodParameters();
	
	public abstract int getStartPos();
}
