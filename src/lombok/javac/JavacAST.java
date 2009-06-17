package lombok.javac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Messager;

import lombok.core.AST;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Name;

public class JavacAST extends AST<JCTree> {
	private final Trees trees;
	private final JavacProcessingEnvironment env;
	private final Messager messager;
	private final Name.Table nameTable;
	private final TreeMaker treeMaker;
	
	public JavacAST(Trees trees, JavacProcessingEnvironment env, JCCompilationUnit top) {
		super(top.sourcefile == null ? null : top.sourcefile.toString());
		setTop(buildCompilationUnit(top));
		this.trees = trees;
		this.env = env;
		this.messager = env.getMessager();
		this.nameTable = Name.Table.instance(env.getContext());
		this.treeMaker = TreeMaker.instance(env.getContext());
	}
	
	@Override public String getPackageDeclaration() {
		JCCompilationUnit unit = (JCCompilationUnit)top().get();
		return unit.pid instanceof JCFieldAccess ? unit.pid.toString() : null;
	}
	
	@Override public Collection<String> getImportStatements() {
		List<String> imports = new ArrayList<String>();
		JCCompilationUnit unit = (JCCompilationUnit)top().get();
		for ( JCTree def : unit.defs ) {
			if ( def instanceof JCImport ) {
				imports.add(((JCImport)def).qualid.toString());
			}
		}
		
		return imports;
	}
	
	public void traverse(JavacASTVisitor visitor) {
		Node current = top();
		visitor.visitCompilationUnit(current, (JCCompilationUnit)current.get());
		traverseChildren(visitor, current);
		visitor.endVisitCompilationUnit(current, (JCCompilationUnit)current.get());
	}
	
	private void traverseChildren(JavacASTVisitor visitor, Node node) {
		for ( Node child : node.down() ) {
			JCTree n = child.get();
			
			switch ( child.getKind() ) {
			case TYPE:
				visitor.visitType(child, (JCClassDecl)n);
				traverseChildren(visitor, child);
				visitor.endVisitType(child, (JCClassDecl)n);
				break;
			case FIELD:
				visitor.visitField(child, (JCVariableDecl)n);
				traverseChildren(visitor, child);
				visitor.endVisitField(child, (JCVariableDecl)n);
				break;
			case METHOD:
				visitor.visitMethod(child, (JCMethodDecl)n);
				traverseChildren(visitor, child);
				visitor.endVisitMethod(child, (JCMethodDecl)n);
				break;
			case INITIALIZER:
				visitor.visitInitializer(child, (JCBlock)n);
				traverseChildren(visitor, child);
				visitor.endVisitInitializer(child, (JCBlock)n);
				break;
			case ARGUMENT:
				JCMethodDecl parent = (JCMethodDecl) child.up().get();
				visitor.visitMethodArgument(child, (JCVariableDecl)n, parent);
				traverseChildren(visitor, child);
				visitor.endVisitMethodArgument(child, (JCVariableDecl)n, parent);
				break;
			case LOCAL:
				visitor.visitLocal(child, (JCVariableDecl)n);
				traverseChildren(visitor, child);
				visitor.endVisitLocal(child, (JCVariableDecl)n);
				break;
			case STATEMENT:
				visitor.visitStatement(child, (JCTree)n);
				traverseChildren(visitor, child);
				visitor.endVisitStatement(node, (JCTree)n);
				break;
			case ANNOTATION:
				switch ( child.up().getKind() ) {
				case TYPE:
					visitor.visitAnnotationOnType((JCClassDecl)child.up().get(), child, (JCAnnotation)n);
					break;
				case FIELD:
					visitor.visitAnnotationOnField((JCVariableDecl)child.up().get(), child, (JCAnnotation)n);
					break;
				case METHOD:
					visitor.visitAnnotationOnMethod((JCMethodDecl)child.up().get(), child, (JCAnnotation)n);
					break;
				case ARGUMENT:
					JCVariableDecl argument = (JCVariableDecl)child.up().get();
					JCMethodDecl method = (JCMethodDecl)child.up().up().get();
					visitor.visitAnnotationOnMethodArgument(argument, method, child, (JCAnnotation)n);
					break;
				case LOCAL:
					visitor.visitAnnotationOnLocal((JCVariableDecl)child.up().get(), child, (JCAnnotation)n);
					break;
				default:
					throw new AssertionError("Can't be reached");
				}
				break;
			default:
				throw new AssertionError("Can't be reached: " + child.getKind());
			}
		}
	}
	
	@Override public Node top() {
		return (Node) super.top();
	}
	
	@Override public Node get(JCTree astNode) {
		return (Node) super.get(astNode);
	}
	
	public Name toName(String name) {
		return nameTable.fromString(name);
	}
	
	public TreeMaker getTreeMaker() {
		return treeMaker;
	}
	
	private Node buildCompilationUnit(JCCompilationUnit top) {
		List<Node> childNodes = new ArrayList<Node>();
		for ( JCTree s : top.defs ) {
			if ( s instanceof JCClassDecl ) {
				addIfNotNull(childNodes, buildType((JCClassDecl)s));
			} // else they are import statements, which we don't care about. Or Skip objects, whatever those are.
		}
		
		return new Node(top, childNodes, Kind.COMPILATION_UNIT);
	}
	
	private Node buildType(JCClassDecl type) {
		if ( alreadyHandled(type) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		
		for ( JCTree def : type.defs ) {
			for ( JCAnnotation annotation : type.mods.annotations ) addIfNotNull(childNodes, buildAnnotation(annotation));
			/* A def can be:
			 *   JCClassDecl for inner types
			 *   JCMethodDecl for constructors and methods
			 *   JCVariableDecl for fields
			 *   JCBlock for (static) initializers
			 */
			if ( def instanceof JCMethodDecl ) addIfNotNull(childNodes, buildMethod((JCMethodDecl)def));
			else if ( def instanceof JCClassDecl ) addIfNotNull(childNodes, buildType((JCClassDecl)def));
			else if ( def instanceof JCVariableDecl ) addIfNotNull(childNodes, buildField((JCVariableDecl)def));
			else if ( def instanceof JCBlock ) addIfNotNull(childNodes, buildInitializer((JCBlock)def));
		}
		
		return putInMap(new Node(type, childNodes, Kind.TYPE));
	}
	
	private Node buildField(JCVariableDecl field) {
		if ( alreadyHandled(field) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		for ( JCAnnotation annotation : field.mods.annotations ) addIfNotNull(childNodes, buildAnnotation(annotation));
		addIfNotNull(childNodes, buildExpression(field.init));
		return putInMap(new Node(field, childNodes, Kind.FIELD));
	}
	
	private Node buildLocalVar(JCVariableDecl local) {
		if ( alreadyHandled(local) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		for ( JCAnnotation annotation : local.mods.annotations ) addIfNotNull(childNodes, buildAnnotation(annotation));
		addIfNotNull(childNodes, buildExpression(local.init));
		return putInMap(new Node(local, childNodes, Kind.LOCAL));
	}
	
	private Node buildInitializer(JCBlock initializer) {
		if ( alreadyHandled(initializer) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		for ( JCStatement statement: initializer.stats ) addIfNotNull(childNodes, buildStatement(statement));
		return putInMap(new Node(initializer, childNodes, Kind.INITIALIZER));
	}
	
	private Node buildMethod(JCMethodDecl method) {
		if ( alreadyHandled(method) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		for ( JCAnnotation annotation : method.mods.annotations ) addIfNotNull(childNodes, buildAnnotation(annotation));
		for ( JCVariableDecl param : method.params ) addIfNotNull(childNodes, buildLocalVar(param));
		if ( method.body != null && method.body.stats != null )
			for ( JCStatement statement : method.body.stats ) addIfNotNull(childNodes, buildStatement(statement));
		return putInMap(new Node(method, childNodes, Kind.METHOD));
	}
	
	private Node buildAnnotation(JCAnnotation annotation) {
		if ( alreadyHandled(annotation) ) return null;
		return putInMap(new Node(annotation, null, Kind.ANNOTATION));
	}
	
	private Node buildExpression(JCExpression expression) {
		return buildStatementOrExpression(expression);
	}
	
	private Node buildStatement(JCStatement statement) {
		return buildStatementOrExpression(statement);
	}
	
	private Node buildStatementOrExpression(JCTree statement) {
		if ( statement == null || alreadyHandled(statement) ) return null;
		if ( statement instanceof JCAnnotation ) return null;
		if ( statement instanceof JCClassDecl ) return buildType((JCClassDecl)statement);
		if ( statement instanceof JCVariableDecl ) return buildLocalVar((JCVariableDecl)statement);
		
		//We drill down because LocalDeclarations and TypeDeclarations can occur anywhere, even in, say,
		//an if block, or even the expression on an assert statement!
		
		setAsHandled(statement);
		return drill(statement);
	}
	
	private Node drill(JCTree statement) {
		List<Node> childNodes = new ArrayList<Node>();
		for ( FieldAccess fa : fieldsOf(statement.getClass()) ) childNodes.addAll(buildWithField(Node.class, statement, fa));
		return putInMap(new Node(statement, childNodes, Kind.STATEMENT));
	}
	
	protected Collection<Class<? extends JCTree>> getStatementTypes() {
		Collection<Class<? extends JCTree>> collection = new ArrayList<Class<? extends JCTree>>(2);
		collection.add(JCStatement.class);
		collection.add(JCExpression.class);
		return collection;
	}
	
	private static void addIfNotNull(Collection<Node> nodes, Node node) {
		if ( node != null ) nodes.add(node);
	}
	
	public class Node extends AST<JCTree>.Node {
		public Node(JCTree node, Collection<Node> children, Kind kind) {
			super(node, children, kind);
		}
		
		@Override public String getName() {
			final Name n;
			
			if ( node instanceof JCClassDecl ) n = ((JCClassDecl)node).name;
			else if ( node instanceof JCMethodDecl ) n = ((JCMethodDecl)node).name;
			else if ( node instanceof JCVariableDecl ) n = ((JCVariableDecl)node).name;
			else n = null;
			
			return n == null ? null : n.toString();
		}
		
		@Override protected boolean calculateIsStructurallySignificant() {
			if ( node instanceof JCClassDecl ) return true;
			if ( node instanceof JCMethodDecl ) return true;
			if ( node instanceof JCVariableDecl ) return true;
			if ( node instanceof JCCompilationUnit ) return true;
			return false;
		}
		
		public TreeMaker getTreeMaker() {
			return treeMaker;
		}
		
		public Name toName(String name) {
			return JavacAST.this.toName(name);
		}
		
		/** {@inheritDoc} */
		@Override public Node directUp() {
			return (Node) super.directUp();
		}
		
		/** {@inheritDoc} */
		@Override public Node up() {
			return (Node) super.up();
		}
		
		/** {@inheritDoc} */
		@Override public Node top() {
			return (Node) super.top();
		}
		
		/** {@inheritDoc} */
		@SuppressWarnings("unchecked")
		@Override public Collection<Node> down() {
			return (Collection<Node>) children;
		}
		
		public void addError(String message) {
			System.err.println("ERR: " + message);
			//TODO
		}
		
		public void addWarning(String message) {
			System.err.println("WARN: " + message);
			//TODO
		}
	}
	
	@Override protected Node buildStatement(Object statement) {
		return buildStatementOrExpression((JCTree) statement);
	}
}
