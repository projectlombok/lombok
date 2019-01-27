/*
 * Copyright (C) 2009-2019 The Project Lombok Authors.
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.core.configuration.ConfigurationKey;
import lombok.core.debug.HistogramTracker;
import lombok.permit.Permit;

/**
 * Lombok wraps the AST produced by a target platform into its own AST system, mostly because both Eclipse and javac
 * do not allow upward traversal (from a method to its owning type, for example).
 * 
 * @param A Self-type.
 * @param L type of all LombokNodes.
 * @param N The common type of all AST nodes in the internal representation of the target platform.
 *          For example, JCTree for javac, and ASTNode for Eclipse.
 */
public abstract class AST<A extends AST<A, L, N>, L extends LombokNode<A, L, N>, N> {
	/** The kind of node represented by a given AST.Node object. */
	public enum Kind {
		COMPILATION_UNIT, TYPE, FIELD, INITIALIZER, METHOD, ANNOTATION, ARGUMENT, LOCAL, STATEMENT, TYPE_USE;
	}
	
	private L top;
	private final String fileName;
	private final String packageDeclaration;
	private final ImportList imports;
	Map<N, N> identityDetector = new IdentityHashMap<N, N>();
	private Map<N, L> nodeMap = new IdentityHashMap<N, L>();
	private boolean changed = false;
	
	// The supertypes which are considered AST Node children. Usually, the Statement, and the Expression,
	// though some platforms (such as Eclipse) group these under one common supertype.
	private final Collection<Class<? extends N>> statementTypes;
	
	private static final HistogramTracker configTracker = System.getProperty("lombok.timeConfig") == null ? null : new HistogramTracker("lombok.config");
	
	protected AST(String fileName, String packageDeclaration, ImportList imports, Collection<Class<? extends N>> statementTypes) {
		this.fileName = fileName == null ? "(unknown).java" : fileName;
		this.packageDeclaration = packageDeclaration;
		this.imports = imports;
		this.statementTypes = statementTypes;
	}
	
	/**
	 * Attempts to find the absolute path (in URI form) to the source file represented by this AST.
	 * 
	 * May return {@code null} if this cannot be done. We don't yet know under which conditions this will happen.
	 */
	public abstract URI getAbsoluteFileLocation();
	
	public void setChanged() {
		this.changed = true;
	}
	
	protected void clearChanged() {
		this.changed = false;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	/** Set the node object that wraps the internal Compilation Unit node. */
	protected void setTop(L top) {
		this.top = top;
	}
	
	/**
	 * Return the content of the package declaration on this AST's top (Compilation Unit) node.
	 * 
	 * Example: "java.util".
	 */
	public final String getPackageDeclaration() {
		return packageDeclaration;
	}
	
	/**
	 * Return the contents of each non-static import statement on this AST's top (Compilation Unit) node.
	 * 
	 * Example: "java.util.IOException".
	 */
	public final ImportList getImportList() {
		return imports;
	}
	
	/**
	 * Puts the given node in the map so that javac/Eclipse's own internal AST object can be translated to
	 * an AST.Node object. Also registers the object as visited to avoid endless loops.
	 */
	protected L putInMap(L node) {
		nodeMap.put(node.get(), node);
		identityDetector.put(node.get(), node.get());
		return node;
	}
	
	/** Returns the node map, that can map javac/Eclipse internal AST objects to AST.Node objects. */
	protected Map<N, L> getNodeMap() {
		return nodeMap;
	}
	
	/** Clears the registry that avoids endless loops, and empties the node map. The existing node map
	 * object is left untouched, and instead a new map is created. */
	protected void clearState() {
		identityDetector = new IdentityHashMap<N, N>();
		nodeMap = new IdentityHashMap<N, L>();
	}
	
	/**
	 * Marks the stated node as handled (to avoid endless loops if 2 nodes refer to each other, or a node
	 * refers to itself). Will then return true if it was already set as handled before this call - in which
	 * case you should do nothing lest the AST build process loops endlessly.
	 */
	protected boolean setAndGetAsHandled(N node) {
		return identityDetector.put(node, node) != null;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	/** The AST.Node object representing the Compilation Unit. */
	public L top() {
		return top;
	}
	
	/** Maps a javac/Eclipse internal AST Node to the appropriate AST.Node object. */
	public L get(N node) {
		return nodeMap.get(node);
	}
	
	/**
	 * Returns the JLS spec version that the compiler uses to parse and compile this AST.
	 * For example, if -source 1.6 is on the command line, this will return {@code 6}.
	 */
	public int getSourceVersion() {
		return 6;
	}
	
	/**
	 * Returns the latest version of the java language specification supported by the host compiler.
	 * For example, if compiling with javac v1.7, this returns {@code 7}.
	 * 
	 * NB: Even if -source (lower than maximum) is specified, this method still returns the maximum supported number.
	 */
	public int getLatestJavaSpecSupported() {
		return 6;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	L replaceNewWithExistingOld(Map<N, L> oldNodes, L newNode) {
		L oldNode = oldNodes.get(newNode.get());
		L targetNode = oldNode == null ? newNode : oldNode;
		
		List children = new ArrayList();
		for (L child : newNode.children) {
			L oldChild = replaceNewWithExistingOld(oldNodes, child);
			children.add(oldChild);
			oldChild.parent = targetNode;
		}
		
		targetNode.children = LombokImmutableList.copyOf(children);
		
		return targetNode;
	}
	
	/** Build an AST.Node object for the stated internal (javac/Eclipse) AST Node object. */
	protected abstract L buildTree(N item, Kind kind);
	
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
	
	private static final ConcurrentMap<Class<?>, FieldAccess[]> fieldsOfASTClasses = new ConcurrentHashMap<Class<?>, FieldAccess[]>();
	
	/** Returns FieldAccess objects for the stated class. Each field that contains objects of the kind returned by
	 * {@link #getStatementTypes()}, either directly or inside of an array or java.util.collection (or array-of-arrays,
	 * or collection-of-collections, et cetera), is returned.
	 */
	protected FieldAccess[] fieldsOf(Class<?> c) {
		FieldAccess[] fields = fieldsOfASTClasses.get(c);
		if (fields != null) return fields;
		
		List<FieldAccess> fieldList = new ArrayList<FieldAccess>();
		getFields(c, fieldList);
		fieldsOfASTClasses.putIfAbsent(c, fieldList.toArray(new FieldAccess[0]));
		return fieldsOfASTClasses.get(c);
	}
	
	private void getFields(Class<?> c, Collection<FieldAccess> fields) {
		if (c == Object.class || c == null) return;
		for (Field f : c.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers())) continue;
			Class<?> t = f.getType();
			int dim = 0;
			
			if (t.isArray()) {
				while (t.isArray()) {
					dim++;
					t = t.getComponentType();
				}
			} else {
				while (Collection.class.isAssignableFrom(t)) {
					dim++;
					t = getComponentType(f.getGenericType());
				}
			}
			
			if (shouldDrill(c, t, f.getName())) {
				Permit.setAccessible(f);
				fields.add(new FieldAccess(f, dim));
			}
		}
		
		getFields(c.getSuperclass(), fields);
	}
	
	private Class<?> getComponentType(Type type) {
		if (type instanceof ParameterizedType) {
			Type component = ((ParameterizedType) type).getActualTypeArguments()[0];
			return component instanceof Class<?> ? (Class<?>) component : Object.class;
		}
		return Object.class;
	}
	
	private boolean shouldDrill(Class<?> parentType, Class<?> childType, String fieldName) {
		for (Class<?> statementType : statementTypes) {
			if (statementType.isAssignableFrom(childType)) return true;
		}
		
		return false;
	}
	
	/**
	 * buildTree implementation that uses reflection to find all child nodes by way of inspecting
	 * the fields. */
	protected Collection<L> buildWithField(Class<L> nodeType, N statement, FieldAccess fa) {
		List<L> list = new ArrayList<L>();
		buildWithField0(nodeType, statement, fa, list);
		return list;
	}
	
	/**
	 * Uses reflection to find the given direct child on the given statement, and replace it with a new child.
	 */
	protected boolean replaceStatementInNode(N statement, N oldN, N newN) {
		for (FieldAccess fa : fieldsOf(statement.getClass())) {
			if (replaceStatementInField(fa, statement, oldN, newN)) return true;
		}
		
		return false;
	}
	
	private boolean replaceStatementInField(FieldAccess fa, N statement, N oldN, N newN) {
		try {
			Object o = fa.field.get(statement);
			if (o == null) return false;
			
			if (o == oldN) {
				fa.field.set(statement, newN);
				return true;
			}
			
			if (fa.dim > 0) {
				if (o.getClass().isArray()) {
					return replaceStatementInArray(o, oldN, newN);
				} else if (Collection.class.isInstance(o)) {
					return replaceStatementInCollection(fa.field, statement, new ArrayList<Collection<?>>(), (Collection<?>)o, oldN, newN);
				}
			}
			
			return false;
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
		
	}
	
	private boolean replaceStatementInCollection(Field field, Object fieldRef, List<Collection<?>> chain, Collection<?> collection, N oldN, N newN) throws IllegalAccessException {
		if (collection == null) return false;
		
		int idx = -1;
		for (Object o : collection) {
			idx++;
			if (o == null) continue;
			if (Collection.class.isInstance(o)) {
				Collection<?> newC = (Collection<?>) o;
				List<Collection<?>> newChain = new ArrayList<Collection<?>>(chain);
				newChain.add(newC);
				if (replaceStatementInCollection(field, fieldRef, newChain, newC, oldN, newN)) return true;
			}
			if (o == oldN) {
				setElementInASTCollection(field, fieldRef, chain, collection, idx, newN);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Override if your AST collection does not support the set method. Javac's for example, does not.
	 * 
	 * @param field The field that contains the array or list of AST nodes.
	 * @param fieldRef The object that you can supply to the field's {@code get} method.
	 * @param chain If the collection is immutable, you need to update the pointer to the collection in each element in the chain.
	 * 
	 * @throws IllegalAccessException This exception won't happen, but we allow you to throw it so you can avoid having to catch it.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void setElementInASTCollection(Field field, Object fieldRef, List<Collection<?>> chain, Collection<?> collection, int idx, N newN) throws IllegalAccessException {
		if (collection instanceof List<?>) {
			((List) collection).set(idx, newN);
		}
	}
	
	private boolean replaceStatementInArray(Object array, N oldN, N newN) {
		if (array == null) return false;
		
		int len = Array.getLength(array);
		for (int i = 0; i < len; i++) {
			Object o = Array.get(array, i);
			if (o == null) continue;
			if (o.getClass().isArray()) {
				if (replaceStatementInArray(o, oldN, newN)) return true;
			} else if (o == oldN) {
				Array.set(array, i, newN);
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private void buildWithField0(Class<L> nodeType, N child, FieldAccess fa, Collection<L> list) {
		try {
			Object o = fa.field.get(child);
			if (o == null) return;
			if (fa.dim == 0) {
				L node = buildTree((N) o, Kind.STATEMENT);
				if (node != null) list.add(nodeType.cast(node));
			} else if (o.getClass().isArray()) {
				buildWithArray(nodeType, o, list, fa.dim);
			} else if (Collection.class.isInstance(o)) {
				buildWithCollection(nodeType, o, list, fa.dim);
			}
		} catch (IllegalAccessException e) {
			throw sneakyThrow(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void buildWithArray(Class<L> nodeType, Object array, Collection<L> list, int dim) {
		if (dim == 1) {
			for (Object v : (Object[]) array) {
				if (v == null) continue;
				L node = buildTree((N)v, Kind.STATEMENT);
				if (node != null) list.add(nodeType.cast(node));
			}
		} else for (Object v : (Object[]) array) {
			if (v == null) return;
			buildWithArray(nodeType, v, list, dim -1);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void buildWithCollection(Class<L> nodeType, Object collection, Collection<L> list, int dim) {
		if (dim == 1) {
			for (Object v : (Collection<?>) collection) {
				if (v == null) continue;
				L node = buildTree((N) v, Kind.STATEMENT);
				if (node != null) list.add(nodeType.cast(node));
			}
		} else for (Object v : (Collection<?>) collection) {
			buildWithCollection(nodeType, v, list, dim - 1);
		}
	}
	
	public final <T> T readConfiguration(ConfigurationKey<T> key) {
		long start = configTracker == null ? 0L : configTracker.start();
		try {
			return LombokConfiguration.read(key, this);
		} finally {
			if (configTracker != null) configTracker.end(start);
		}
	}
}
