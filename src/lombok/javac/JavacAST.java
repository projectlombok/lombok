package lombok.javac;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import lombok.core.AST;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symtab;
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
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

public class JavacAST extends AST<JCTree> {
	private final Messager messager;
	private final Name.Table nameTable;
	private final TreeMaker treeMaker;
	private final Symtab symtab;
	private final Log log;
	
	public JavacAST(Trees trees, JavacProcessingEnvironment env, JCCompilationUnit top) {
		super(top.sourcefile == null ? null : top.sourcefile.toString());
		setTop(buildCompilationUnit(top));
		this.messager = env.getMessager();
		this.log = Log.instance(env.getContext());
		this.nameTable = Name.Table.instance(env.getContext());
		this.treeMaker = TreeMaker.instance(env.getContext());
		this.symtab = Symtab.instance(env.getContext());
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
		top().traverse(visitor);
	}
	
	private void traverseChildren(JavacASTVisitor visitor, Node node) {
		for ( Node child : node.down() ) {
			child.traverse(visitor);
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
	
	public Symtab getSymbolTable() {
		return symtab;
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
	
	private Node buildLocalVar(JCVariableDecl local, Kind kind) {
		if ( alreadyHandled(local) ) return null;
		List<Node> childNodes = new ArrayList<Node>();
		for ( JCAnnotation annotation : local.mods.annotations ) addIfNotNull(childNodes, buildAnnotation(annotation));
		addIfNotNull(childNodes, buildExpression(local.init));
		return putInMap(new Node(local, childNodes, kind));
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
		for ( JCVariableDecl param : method.params ) addIfNotNull(childNodes, buildLocalVar(param, Kind.ARGUMENT));
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
		if ( statement instanceof JCVariableDecl ) return buildLocalVar((JCVariableDecl)statement, Kind.LOCAL);
		
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
		
		public void traverse(JavacASTVisitor visitor) {
			switch ( this.getKind() ) {
			case COMPILATION_UNIT:
				visitor.visitCompilationUnit(this, (JCCompilationUnit)get());
				traverseChildren(visitor, this);
				visitor.endVisitCompilationUnit(this, (JCCompilationUnit)get());
				break;
			case TYPE:
				visitor.visitType(this, (JCClassDecl)get());
				traverseChildren(visitor, this);
				visitor.endVisitType(this, (JCClassDecl)get());
				break;
			case FIELD:
				visitor.visitField(this, (JCVariableDecl)get());
				traverseChildren(visitor, this);
				visitor.endVisitField(this, (JCVariableDecl)get());
				break;
			case METHOD:
				visitor.visitMethod(this, (JCMethodDecl)get());
				traverseChildren(visitor, this);
				visitor.endVisitMethod(this, (JCMethodDecl)get());
				break;
			case INITIALIZER:
				visitor.visitInitializer(this, (JCBlock)get());
				traverseChildren(visitor, this);
				visitor.endVisitInitializer(this, (JCBlock)get());
				break;
			case ARGUMENT:
				JCMethodDecl parent = (JCMethodDecl) up().get();
				visitor.visitMethodArgument(this, (JCVariableDecl)get(), parent);
				traverseChildren(visitor, this);
				visitor.endVisitMethodArgument(this, (JCVariableDecl)get(), parent);
				break;
			case LOCAL:
				visitor.visitLocal(this, (JCVariableDecl)get());
				traverseChildren(visitor, this);
				visitor.endVisitLocal(this, (JCVariableDecl)get());
				break;
			case STATEMENT:
				visitor.visitStatement(this, (JCTree)get());
				traverseChildren(visitor, this);
				visitor.endVisitStatement(this, (JCTree)get());
				break;
			case ANNOTATION:
				switch ( up().getKind() ) {
				case TYPE:
					visitor.visitAnnotationOnType((JCClassDecl)up().get(), this, (JCAnnotation)get());
					break;
				case FIELD:
					visitor.visitAnnotationOnField((JCVariableDecl)up().get(), this, (JCAnnotation)get());
					break;
				case METHOD:
					visitor.visitAnnotationOnMethod((JCMethodDecl)up().get(), this, (JCAnnotation)get());
					break;
				case ARGUMENT:
					JCVariableDecl argument = (JCVariableDecl)up().get();
					JCMethodDecl method = (JCMethodDecl)up().up().get();
					visitor.visitAnnotationOnMethodArgument(argument, method, this, (JCAnnotation)get());
					break;
				case LOCAL:
					visitor.visitAnnotationOnLocal((JCVariableDecl)up().get(), this, (JCAnnotation)get());
					break;
				default:
					throw new AssertionError("Annotion not expected as child of a " + up().getKind());
				}
				break;
			default:
				throw new AssertionError("Unexpected kind during node traversal: " + getKind());
			}
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
		
		public Symtab getSymbolTable() {
			return symtab;
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
			printMessage(Diagnostic.Kind.ERROR, message, this, null);
		}
		
		public void addError(String message, DiagnosticPosition pos) {
			printMessage(Diagnostic.Kind.ERROR, message, null, pos);
		}
		
		public void addWarning(String message) {
			printMessage(Diagnostic.Kind.WARNING, message, this, null);
		}
		
		public void addWarning(String message, DiagnosticPosition pos) {
			printMessage(Diagnostic.Kind.WARNING, message, null, pos);
		}
	}
	
	/** Supply either a position or a node (in that case, position of the node is used) */
	private void printMessage(Diagnostic.Kind kind, String message, Node node, DiagnosticPosition pos) {
		JavaFileObject oldSource = null;
		JavaFileObject newSource = null;
		JCTree astObject = node == null ? null : node.get();
		JCCompilationUnit top = (JCCompilationUnit) top().get();
		newSource = top.sourcefile;
		if (newSource != null) {
			oldSource = log.useSource(newSource);
			if ( pos == null ) pos = astObject.pos();
		}
		try {
			switch (kind) {
			case ERROR:
				increaseErrorCount(messager);
				boolean prev = log.multipleErrors;
				log.multipleErrors = true;
				try {
					log.error(pos, "proc.messager", message);
				} finally {
					log.multipleErrors = prev;
				}
				break;
			default:
			case WARNING:
				log.warning(pos, "proc.messager", message);
				break;
			}
		} finally {
			if (oldSource != null)
				log.useSource(oldSource);
		}
	}
	
	private void increaseErrorCount(Messager messager) {
		try {
			Field f = messager.getClass().getDeclaredField("errorCount");
			f.setAccessible(true);
			if ( f.getType() == int.class ) {
				int val = ((Number)f.get(messager)).intValue();
				f.set(messager, val +1);
			}
		} catch ( Throwable t ) {
			//Very unfortunate, but in most cases it still works fine, so we'll silently swallow it.
		}
	}
	
	@Override protected Node buildStatement(Object statement) {
		return buildStatementOrExpression((JCTree) statement);
	}
}
