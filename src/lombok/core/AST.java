package lombok.core;

import static lombok.Lombok.sneakyThrow;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public abstract class AST<N> {
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
	
	protected void setTop(Node top) {
		this.top = top;
	}
	
	public abstract String getPackageDeclaration();
	
	public abstract Collection<String> getImportStatements();
	
	protected <T extends Node> T putInMap(T parent) {
		nodeMap.put(parent.get(), parent);
		identityDetector.put(parent.get(), null);
		return parent;
	}
	
	protected Map<N, Node> getNodeMap() {
		return nodeMap;
	}
	
	protected void clearState() {
		identityDetector = new IdentityHashMap<N, Void>();
		nodeMap = new IdentityHashMap<N, Node>();
	}
	
	protected boolean alreadyHandled(N node) {
		return identityDetector.containsKey(node);
	}
	
	protected void setAsHandled(N node) {
		identityDetector.put(node, null);
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Node top() {
		return top;
	}
	
	public Node get(N node) {
		return nodeMap.get(node);
	}
	
	public abstract class Node {
		protected final Kind kind;
		protected final N node;
		protected final Collection<? extends Node> children;
		protected Node parent;
		protected boolean handled;
		protected boolean isStructurallySignificant;
		
		protected Node(N node, Collection<? extends Node> children, Kind kind) {
			this.kind = kind;
			this.node = node;
			this.children = children == null ? Collections.<Node>emptyList() : children;
			for ( Node child : this.children ) child.parent = this;
			this.isStructurallySignificant = calculateIsStructurallySignificant();
		}
		
		public String getPackageDeclaration() {
			return AST.this.getPackageDeclaration();
		}
		
		public Collection<String> getImportStatements() {
			return AST.this.getImportStatements();
		}
		
		protected abstract boolean calculateIsStructurallySignificant();
		
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
		public Node up() {
			Node result = (Node)parent;
			while ( result != null && !result.isStructurallySignificant ) result = (Node)result.parent;
			return result;
		}
		
		/**
		 * Returns the direct parent node in the AST tree of this node. For example, a local variable declaration's
		 * direct parent can be e.g. an If block, but its up() Node is the Method that contains it.
		 */
		public Node directUp() {
			return parent;
		}
		
		public Collection<? extends Node> down() {
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
		
		public abstract void addError(String message);
		
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
	
	protected static class FieldAccess {
		public final Field field;
		public final int dim;
		
		FieldAccess(Field field, int dim) {
			this.field = field;
			this.dim = dim;
		}
	}
	
	private static Map<Class<?>, Collection<FieldAccess>> fieldsOfASTClasses = new HashMap<Class<?>, Collection<FieldAccess>>();
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
	
	protected abstract Collection<Class<? extends N>> getStatementTypes();
	
	protected <T extends Node> Collection<T> buildWithField(Class<T> nodeType, N statement, FieldAccess fa) {
		List<T> list = new ArrayList<T>();
		buildWithField0(nodeType, statement, fa, list);
		return list;
	}
	
	private <T extends Node> void buildWithField0(Class<T> nodeType, N child, FieldAccess fa, Collection<T> list) {
		try {
			Object o = fa.field.get(child);
			if ( o == null ) return;
			if ( fa.dim == 0 ) {
				Node node = buildStatement(o);
				if ( node != null ) list.add(nodeType.cast(node));
			} else if ( o.getClass().isArray() ) buildWithArray(nodeType, o, list, fa.dim);
			else if ( Collection.class.isInstance(o) ) buildWithCollection(nodeType, o, list, fa.dim);
		} catch ( IllegalAccessException e ) {
			sneakyThrow(e);
		}
	}
	
	private <T extends Node> void buildWithArray(Class<T> nodeType, Object array, Collection<T> list, int dim) {
		if ( dim == 1 ) for ( Object v : (Object[])array ) {
			if ( v == null ) continue;
			Node node = buildStatement(v);
			if ( node != null ) list.add(nodeType.cast(node));
		} else for ( Object v : (Object[])array ) {
			if ( v == null ) return;
			buildWithArray(nodeType, v, list, dim-1);
		}
	}
	
	private <T extends Node> void buildWithCollection(Class<T> nodeType, Object collection, Collection<T> list, int dim) {
		if ( dim == 1 ) for ( Object v : (Collection<?>)collection ) {
			if ( v == null ) continue;
			Node node = buildStatement(v);
			if ( node != null ) list.add(nodeType.cast(node));
		} else for ( Object v : (Collection<?>)collection ) {
			buildWithCollection(nodeType, v, list, dim-1);
		}
	}
	
	protected abstract Node buildStatement(Object statement);
}
