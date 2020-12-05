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
package lombok.javac;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.util.JCDiagnostic;
import lombok.core.AST;
import lombok.core.CleanupRegistry;
import lombok.core.CleanupTask;
import lombok.permit.Permit;

import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;

/**
 * Wraps around javac's internal AST view to add useful features as well as the ability to visit parents from children,
 * something javac's own AST system does not offer.
 */
public class JavacAST extends AST<JavacAST, JavacNode, JCTree> {
	private final CleanupRegistry cleanup;
	private final JavacElements elements;
	private final JavacTreeMaker treeMaker;
	private final Symtab symtab;
	private final JavacTypes javacTypes;
	private final Log log;
	private final ErrorLog errorLogger;
	private final Context context;
	private static final URI NOT_CALCULATED_MARKER = URI.create("https://projectlombok.org/not/calculated");
	private URI memoizedAbsoluteFileLocation = NOT_CALCULATED_MARKER;
	
	/**
	 * Creates a new JavacAST of the provided Compilation Unit.
	 * 
	 * @param messager A Messager for warning and error reporting.
	 * @param context A Context object for interfacing with the compiler.
	 * @param top The compilation unit, which serves as the top level node in the tree to be built.
	 */
	public JavacAST(Messager messager, Context context, JCCompilationUnit top, CleanupRegistry cleanup) {
		super(sourceName(top), PackageName.getPackageName(top), new JavacImportList(top), statementTypes());
		setTop(buildCompilationUnit(top));
		this.context = context;
		this.log = Log.instance(context);
		this.errorLogger = ErrorLog.create(messager, log);
		this.elements = JavacElements.instance(context);
		this.treeMaker = new JavacTreeMaker(TreeMaker.instance(context));
		this.symtab = Symtab.instance(context);
		this.javacTypes = JavacTypes.instance(context);
		this.cleanup = cleanup;
		clearChanged();
	}
	
	@Override public URI getAbsoluteFileLocation() {
		if (memoizedAbsoluteFileLocation == NOT_CALCULATED_MARKER) {
			memoizedAbsoluteFileLocation = getAbsoluteFileLocation((JCCompilationUnit) top().get());
		}
		return memoizedAbsoluteFileLocation;
	}
	
	private static Class<?> wrappedFileObjectClass, sbtJavaFileObjectClass, sbtMappedVirtualFileClass, sbtOptionClass;
	private static Field wrappedFileObjectField, sbtJavaFileObjectField, sbtMappedVirtualFilePathField, sbtMappedVirtualFileRootsField, sbtOptionField;
	private static Method sbtMapGetMethod;
	
	public static URI getAbsoluteFileLocation(JCCompilationUnit cu) {
		try {
			URI uri = cu.sourcefile.toUri();
			String fn = uri.toString();
			if (fn.startsWith("file:")) return uri;
			URI sbtUri = tryGetSbtFile(cu.sourcefile);
			if (sbtUri != null) return sbtUri;
			return uri;
		} catch (Exception e) {
			return null;
		}
	}
	
	private static URI tryGetSbtFile(JavaFileObject sourcefile) {
		try {
			return tryGetSbtFile_(sourcefile);
		} catch (Exception e) {
			return null;
		}
	}
	
	private static URI tryGetSbtFile_(JavaFileObject sourcefile) throws Exception {
		Class<?> c = sourcefile.getClass();
		String cn;
		
		if (wrappedFileObjectClass == null) {
			if (!c.getName().equals("com.sun.tools.javac.api.ClientCodeWrapper$WrappedJavaFileObject")) return null;
			wrappedFileObjectClass = c;
		}
		if (c != wrappedFileObjectClass) return null;
		
		if (wrappedFileObjectField == null) wrappedFileObjectField = Permit.permissiveGetField(wrappedFileObjectClass.getSuperclass(), "clientFileObject");
		if (wrappedFileObjectField == null) return null;
		Object fileObject = wrappedFileObjectField.get(sourcefile);
		c = fileObject.getClass();
		
		if (sbtJavaFileObjectClass == null) {
			cn = c.getName();
			if (!cn.startsWith("sbt.") || !cn.endsWith("JavaFileObject")) return null;
			sbtJavaFileObjectClass = c;
		}
		if (sbtJavaFileObjectClass != c) return null;
		if (sbtJavaFileObjectField == null) sbtJavaFileObjectField = Permit.permissiveGetField(sbtJavaFileObjectClass, "underlying");
		if (sbtJavaFileObjectField == null) return null;
		
		Object mappedVirtualFile = sbtJavaFileObjectField.get(fileObject);
		c = mappedVirtualFile.getClass();
		
		if (sbtMappedVirtualFileClass == null) {
			cn = c.getName();
			if (!cn.startsWith("sbt.") || !cn.endsWith("MappedVirtualFile")) return null;
			sbtMappedVirtualFileClass = c;
		}
		if (sbtMappedVirtualFilePathField == null) sbtMappedVirtualFilePathField = Permit.permissiveGetField(sbtMappedVirtualFileClass, "encodedPath");
		if (sbtMappedVirtualFilePathField == null) return null;
		if (sbtMappedVirtualFileRootsField == null) sbtMappedVirtualFileRootsField = Permit.permissiveGetField(sbtMappedVirtualFileClass, "rootPathsMap");
		if (sbtMappedVirtualFileRootsField == null) return null;
		
		String encodedPath = (String) sbtMappedVirtualFilePathField.get(mappedVirtualFile);
		if (!encodedPath.startsWith("${")) {
			File maybeAbsoluteFile = new File(encodedPath);
			if (maybeAbsoluteFile.exists()) {
				return maybeAbsoluteFile.toURI();
			} else {
				return null;
			}
		}
		int idx = encodedPath.indexOf('}');
		if (idx == -1) return null;
		String base = encodedPath.substring(2, idx);
		Object roots = sbtMappedVirtualFileRootsField.get(mappedVirtualFile);
		if (sbtMapGetMethod == null) sbtMapGetMethod = Permit.getMethod(roots.getClass(), "get", Object.class);
		if (sbtMapGetMethod == null) return null;
		
		Object option = sbtMapGetMethod.invoke(roots, base);
		c = option.getClass();
		if (sbtOptionClass == null) {
			if (c.getName().equals("scala.Some")) sbtOptionClass = c;
		}
		if (c != sbtOptionClass) return null;
		if (sbtOptionField == null) sbtOptionField = Permit.permissiveGetField(sbtOptionClass, "value");
		if (sbtOptionField == null) return null;
		
		Object path = sbtOptionField.get(option);
		return new File(path.toString() + encodedPath.substring(idx + 1)).toURI();
	}
	
	private static String sourceName(JCCompilationUnit cu) {
		return cu.sourcefile == null ? null : cu.sourcefile.toString();
	}
	
	public Context getContext() {
		return context;
	}
	
	/**
	 * Runs through the entire AST, starting at the compilation unit, calling the provided visitor's visit methods
	 * for each node, depth first.
	 */
	public void traverse(JavacASTVisitor visitor) {
		top().traverse(visitor);
	}
	
	void traverseChildren(JavacASTVisitor visitor, JavacNode node) {
		for (JavacNode child : node.down()) child.traverse(visitor);
	}
	
	@Override public int getSourceVersion() {
		try {
			String nm = Source.instance(context).name();
			int underscoreIdx = nm.indexOf('_');
			if (underscoreIdx > -1) return Integer.parseInt(nm.substring(underscoreIdx + 1));
			// assume java9+
			return Integer.parseInt(nm);
		} catch (Exception ignore) {}
		return 6;
	}
	
	@Override public int getLatestJavaSpecSupported() {
		return Javac.getJavaCompilerVersion();
	}
	
	public void cleanupTask(String key, JCTree target, CleanupTask task) {
		cleanup.registerTask(key, target, task);
	}
	
	/** @return A Name object generated for the proper name table belonging to this AST. */
	public Name toName(String name) {
		return elements.getName(name);
	}
	
	/** @return A TreeMaker instance that you can use to create new AST nodes. */
	public JavacTreeMaker getTreeMaker() {
		treeMaker.at(-1);
		return treeMaker;
	}
	
	/** @return The symbol table used by this AST for symbols. */
	public Symtab getSymbolTable() {
		return symtab;
	}
	
	/**
	 * @return The implementation of {@link javax.lang.model.util.Types} of javac. Contains a few extra methods beyond
	 * the ones listed in the official annotation API interface. */
	public JavacTypes getTypesUtil() {
		return javacTypes;
	}
	
	/** {@inheritDoc} */
	@Override protected JavacNode buildTree(JCTree node, Kind kind) {
		switch (kind) {
		case COMPILATION_UNIT:
			return buildCompilationUnit((JCCompilationUnit) node);
		case TYPE:
			return buildType((JCClassDecl) node);
		case FIELD:
			return buildField((JCVariableDecl) node);
		case INITIALIZER:
			return buildInitializer((JCBlock) node);
		case METHOD:
			return buildMethod((JCMethodDecl) node);
		case ARGUMENT:
			return buildLocalVar((JCVariableDecl) node, kind);
		case LOCAL:
			return buildLocalVar((JCVariableDecl) node, kind);
		case STATEMENT:
			return buildStatementOrExpression(node);
		case ANNOTATION:
			return buildAnnotation((JCAnnotation) node, false);
		case TYPE_USE:
			return buildTypeUse(node);
		default:
			throw new AssertionError("Did not expect: " + kind);
		}
	}
	
	private JavacNode buildCompilationUnit(JCCompilationUnit top) {
		List<JavacNode> childNodes = new ArrayList<JavacNode>();
		for (JCTree s : top.defs) {
			if (s instanceof JCClassDecl) {
				addIfNotNull(childNodes, buildType((JCClassDecl) s));
			} // else they are import statements, which we don't care about. Or Skip objects, whatever those are.
		}
		
		return new JavacNode(this, top, childNodes, Kind.COMPILATION_UNIT);
	}
	
	private JavacNode buildType(JCClassDecl type) {
		if (setAndGetAsHandled(type)) return null;
		List<JavacNode> childNodes = new ArrayList<JavacNode>();
		
		for (JCAnnotation annotation : type.mods.annotations) addIfNotNull(childNodes, buildAnnotation(annotation, false));
		for (JCTree def : type.defs) {
			/* A def can be:
			 *   JCClassDecl for inner types
			 *   JCMethodDecl for constructors and methods
			 *   JCVariableDecl for fields
			 *   JCBlock for (static) initializers
			 */
			if (def instanceof JCMethodDecl) addIfNotNull(childNodes, buildMethod((JCMethodDecl) def));
			else if (def instanceof JCClassDecl) addIfNotNull(childNodes, buildType((JCClassDecl) def));
			else if (def instanceof JCVariableDecl) addIfNotNull(childNodes, buildField((JCVariableDecl) def));
			else if (def instanceof JCBlock) addIfNotNull(childNodes, buildInitializer((JCBlock) def));
		}
		
		return putInMap(new JavacNode(this, type, childNodes, Kind.TYPE));
	}
	
	private JavacNode buildField(JCVariableDecl field) {
		if (setAndGetAsHandled(field)) return null;
		List<JavacNode> childNodes = new ArrayList<JavacNode>();
		for (JCAnnotation annotation : field.mods.annotations) addIfNotNull(childNodes, buildAnnotation(annotation, true));
		addIfNotNull(childNodes, buildTypeUse(field.vartype));
		addIfNotNull(childNodes, buildExpression(field.init));
		return putInMap(new JavacNode(this, field, childNodes, Kind.FIELD));
	}
	
	private JavacNode buildLocalVar(JCVariableDecl local, Kind kind) {
		if (setAndGetAsHandled(local)) return null;
		List<JavacNode> childNodes = new ArrayList<JavacNode>();
		for (JCAnnotation annotation : local.mods.annotations) addIfNotNull(childNodes, buildAnnotation(annotation, true));
		addIfNotNull(childNodes, buildTypeUse(local.vartype));
		addIfNotNull(childNodes, buildExpression(local.init));
		return putInMap(new JavacNode(this, local, childNodes, kind));
	}
	
	private JavacNode buildTypeUse(JCTree typeUse) {
		if (setAndGetAsHandled(typeUse)) return null;
		
		if (typeUse == null) return null;
		
		if (typeUse.getClass().getName().equals("com.sun.tools.javac.tree.JCTree$JCAnnotatedType")) {
			initJcAnnotatedType(typeUse.getClass());
			Collection<?> anns = Permit.permissiveReadField(Collection.class, JCANNOTATEDTYPE_ANNOTATIONS, typeUse);
			JCExpression underlying = Permit.permissiveReadField(JCExpression.class, JCANNOTATEDTYPE_UNDERLYINGTYPE, typeUse);
			
			List<JavacNode> childNodes = new ArrayList<JavacNode>();
			if (anns != null) for (Object annotation : anns) if (annotation instanceof JCAnnotation) addIfNotNull(childNodes, buildAnnotation((JCAnnotation) annotation, true));
			addIfNotNull(childNodes, buildTypeUse(underlying));
			return putInMap(new JavacNode(this, typeUse, childNodes, Kind.TYPE_USE));
		}
		
		if (typeUse instanceof JCWildcard) {
			JCTree inner = ((JCWildcard) typeUse).inner;
			List<JavacNode> childNodes = inner == null ? Collections.<JavacNode>emptyList() : new ArrayList<JavacNode>();
			if (inner != null) addIfNotNull(childNodes, buildTypeUse(inner));
			return putInMap(new JavacNode(this, typeUse, childNodes, Kind.TYPE_USE));
		}
		
		if (typeUse instanceof JCArrayTypeTree) {
			JCTree inner = ((JCArrayTypeTree) typeUse).elemtype;
			List<JavacNode> childNodes = inner == null ? Collections.<JavacNode>emptyList() : new ArrayList<JavacNode>();
			if (inner != null) addIfNotNull(childNodes, buildTypeUse(inner));
			return putInMap(new JavacNode(this, typeUse, childNodes, Kind.TYPE_USE));
		}
		
		if (typeUse instanceof JCFieldAccess) {
			JCTree inner = ((JCFieldAccess) typeUse).selected;
			List<JavacNode> childNodes = inner == null ? Collections.<JavacNode>emptyList() : new ArrayList<JavacNode>();
			if (inner != null) addIfNotNull(childNodes, buildTypeUse(inner));
			return putInMap(new JavacNode(this, typeUse, childNodes, Kind.TYPE_USE));
		}
		
		if (typeUse instanceof JCIdent) {
			return putInMap(new JavacNode(this, typeUse, Collections.<JavacNode>emptyList(), Kind.TYPE_USE));
		}
		
		return null;
	}
	
	private static boolean JCTRY_RESOURCES_FIELD_INITIALIZED = false;
	private static Field JCTRY_RESOURCES_FIELD;
	
	@SuppressWarnings("unchecked")
	private static List<JCTree> getResourcesForTryNode(JCTry tryNode) {
		if (!JCTRY_RESOURCES_FIELD_INITIALIZED) {
			JCTRY_RESOURCES_FIELD = Permit.permissiveGetField(JCTry.class, "resources");
			JCTRY_RESOURCES_FIELD_INITIALIZED = true;
		}
		
		if (JCTRY_RESOURCES_FIELD == null) return Collections.emptyList();
		Object rv = null;
		try {
			rv = JCTRY_RESOURCES_FIELD.get(tryNode);
		} catch (Exception ignore) {}
		
		if (rv instanceof List) return (List<JCTree>) rv;
		return Collections.emptyList();
	}
	
	private static boolean JCANNOTATEDTYPE_FIELDS_INITIALIZED = false;
	private static Field JCANNOTATEDTYPE_ANNOTATIONS, JCANNOTATEDTYPE_UNDERLYINGTYPE;
	private static void initJcAnnotatedType(Class<?> context) {
		if (JCANNOTATEDTYPE_FIELDS_INITIALIZED) return;
		JCANNOTATEDTYPE_ANNOTATIONS = Permit.permissiveGetField(context, "annotations");
		JCANNOTATEDTYPE_UNDERLYINGTYPE = Permit.permissiveGetField(context, "underlyingType");
		JCANNOTATEDTYPE_FIELDS_INITIALIZED = true;
	}
	
	private JavacNode buildTry(JCTry tryNode) {
		if (setAndGetAsHandled(tryNode)) return null;
		List<JavacNode> childNodes = new ArrayList<JavacNode>();
		for (JCTree varDecl : getResourcesForTryNode(tryNode)) {
			if (varDecl instanceof JCVariableDecl) {
				addIfNotNull(childNodes, buildLocalVar((JCVariableDecl) varDecl, Kind.LOCAL));
			}
		}
		addIfNotNull(childNodes, buildStatement(tryNode.body));
		for (JCCatch jcc : tryNode.catchers) addIfNotNull(childNodes, buildTree(jcc, Kind.STATEMENT));
		addIfNotNull(childNodes, buildStatement(tryNode.finalizer));
		return putInMap(new JavacNode(this, tryNode, childNodes, Kind.STATEMENT));
	}
	
	private JavacNode buildInitializer(JCBlock initializer) {
		if (setAndGetAsHandled(initializer)) return null;
		List<JavacNode> childNodes = new ArrayList<JavacNode>();
		for (JCStatement statement: initializer.stats) addIfNotNull(childNodes, buildStatement(statement));
		return putInMap(new JavacNode(this, initializer, childNodes, Kind.INITIALIZER));
	}
	
	private JavacNode buildMethod(JCMethodDecl method) {
		if (setAndGetAsHandled(method)) return null;
		List<JavacNode> childNodes = new ArrayList<JavacNode>();
		for (JCAnnotation annotation : method.mods.annotations) addIfNotNull(childNodes, buildAnnotation(annotation, false));
		for (JCVariableDecl param : method.params) addIfNotNull(childNodes, buildLocalVar(param, Kind.ARGUMENT));
		if (method.body != null && method.body.stats != null) {
			for (JCStatement statement : method.body.stats) addIfNotNull(childNodes, buildStatement(statement));
		}
		return putInMap(new JavacNode(this, method, childNodes, Kind.METHOD));
	}
	
	private JavacNode buildAnnotation(JCAnnotation annotation, boolean varDecl) {
		boolean handled = setAndGetAsHandled(annotation);
		if (!varDecl && handled) {
			// @Foo int x, y; is handled in javac by putting the same annotation node on 2 JCVariableDecls.
			return null;
		}
		return putInMap(new JavacNode(this, annotation, null, Kind.ANNOTATION));
	}
	
	private JavacNode buildExpression(JCExpression expression) {
		return buildStatementOrExpression(expression);
	}
	
	private JavacNode buildStatement(JCStatement statement) {
		return buildStatementOrExpression(statement);
	}
	
	private JavacNode buildStatementOrExpression(JCTree statement) {
		if (statement == null) return null;
		if (statement instanceof JCAnnotation) return null;
		if (statement instanceof JCClassDecl) return buildType((JCClassDecl) statement);
		if (statement instanceof JCVariableDecl) return buildLocalVar((JCVariableDecl) statement, Kind.LOCAL);
		if (statement instanceof JCTry) return buildTry((JCTry) statement);
		if (statement.getClass().getName().equals("com.sun.tools.javac.tree.JCTree$JCLambda")) return buildLambda(statement);
		if (setAndGetAsHandled(statement)) return null;
		
		return drill(statement);
	}
	
	private JavacNode buildLambda(JCTree jcTree) {
		return buildStatementOrExpression(getBody(jcTree));
	}
	
	private JCTree getBody(JCTree jcTree) {
		return (JCTree) Permit.invokeSneaky(getBodyMethod(jcTree.getClass()), jcTree);
	}
	
	private final static ConcurrentMap<Class<?>, Method> getBodyMethods = new ConcurrentHashMap<Class<?>, Method>();
	
	private Method getBodyMethod(Class<?> c) {
		Method m = getBodyMethods.get(c);
		if (m != null) {
			return m;
		}
		try {
			m = Permit.getMethod(c, "getBody");
		} catch (NoSuchMethodException e) {
			throw Javac.sneakyThrow(e);
		}
		getBodyMethods.putIfAbsent(c, m);
		return getBodyMethods.get(c);
	}
	
	private JavacNode drill(JCTree statement) {
		try {
			List<JavacNode> childNodes = new ArrayList<JavacNode>();
			for (FieldAccess fa : fieldsOf(statement.getClass())) childNodes.addAll(buildWithField(JavacNode.class, statement, fa));
			return putInMap(new JavacNode(this, statement, childNodes, Kind.STATEMENT));
		} catch (OutOfMemoryError oome) {
			String msg = oome.getMessage();
			if (msg == null) msg = "(no original message)";
			OutOfMemoryError newError = new OutOfMemoryError(getFileName() + "@pos" + statement.getPreferredPosition() + ": " + msg);
			// We could try to set the stack trace of the new exception to the same one as the old exception, but this costs memory,
			// and we're already in an extremely fragile situation in regards to remaining heap space, so let's not do that.
			throw newError;
		}
	}
	
	/* For javac, both JCExpression and JCStatement are considered as valid children types. */
	private static Collection<Class<? extends JCTree>> statementTypes() {
		Collection<Class<? extends JCTree>> collection = new ArrayList<Class<? extends JCTree>>(3);
		collection.add(JCStatement.class);
		collection.add(JCExpression.class);
		collection.add(JCCatch.class);
		return collection;
	}
	
	private static void addIfNotNull(Collection<JavacNode> nodes, JavacNode node) {
		if (node != null) nodes.add(node);
	}
	
	/**
	 * Attempts to remove any compiler errors generated by java whose reporting position is located anywhere between the start and end of the supplied node.
	 */
	void removeDeferredErrors(JavacNode node) {
		DiagnosticPosition pos = node.get().pos();
		JCCompilationUnit top = (JCCompilationUnit) top().get();
		removeFromDeferredDiagnostics(pos.getStartPosition(), Javac.getEndPosition(pos, top));
	}
	
	/** Supply either a position or a node (in that case, position of the node is used) */
	void printMessage(Diagnostic.Kind kind, String message, JavacNode node, DiagnosticPosition pos, boolean attemptToRemoveErrorsInRange) {
		JavaFileObject oldSource = null;
		JavaFileObject newSource = null;
		JCTree astObject = node == null ? null : node.get();
		JCCompilationUnit top = (JCCompilationUnit) top().get();
		newSource = top.sourcefile;
		if (newSource != null) {
			oldSource = log.useSource(newSource);
			if (pos == null) pos = astObject.pos();
		}
		if (pos != null && node != null && attemptToRemoveErrorsInRange) {
			removeFromDeferredDiagnostics(pos.getStartPosition(), node.getEndPosition(pos));
		}
		try {
			switch (kind) {
			case ERROR:
				errorLogger.error(pos, message);
				break;
			case MANDATORY_WARNING:
				errorLogger.mandatoryWarning(pos, message);
				break;
			case WARNING:
				errorLogger.warning(pos, message);
				break;
			default:
			case NOTE:
				errorLogger.note(pos, message);
				break;
			}
		} finally {
			if (newSource != null) log.useSource(oldSource);
		}
	}

	public void removeFromDeferredDiagnostics(int startPos, int endPos) {
		JCCompilationUnit self = (JCCompilationUnit) top().get();
		new CompilerMessageSuppressor(getContext()).removeAllBetween(self.sourcefile, startPos, endPos);
	}
	
	/** {@inheritDoc} */
	@Override protected void setElementInASTCollection(Field field, Object refField, List<Collection<?>> chain, Collection<?> collection, int idx, JCTree newN) throws IllegalAccessException {
		com.sun.tools.javac.util.List<?> list = setElementInConsList(chain, collection, ((List<?>)collection).get(idx), newN);
		field.set(refField, list);
	}
	
	private com.sun.tools.javac.util.List<?> setElementInConsList(List<Collection<?>> chain, Collection<?> current, Object oldO, Object newO) {
		com.sun.tools.javac.util.List<?> oldL = (com.sun.tools.javac.util.List<?>) current;
		com.sun.tools.javac.util.List<?> newL = replaceInConsList(oldL, oldO, newO);
		if (chain.isEmpty()) return newL;
		List<Collection<?>> reducedChain = new ArrayList<Collection<?>>(chain);
		Collection<?> newCurrent = reducedChain.remove(reducedChain.size() -1);
		return setElementInConsList(reducedChain, newCurrent, oldL, newL);
	}
	
	private com.sun.tools.javac.util.List<?> replaceInConsList(com.sun.tools.javac.util.List<?> oldL, Object oldO, Object newO) {
		boolean repl = false;
		Object[] a = oldL.toArray();
		for (int i = 0; i < a.length; i++) {
			if (a[i] == oldO) {
				a[i] = newO;
				repl = true;
			}
		}
		
		if (repl) return com.sun.tools.javac.util.List.<Object>from(a);
		return oldL;
	}
	
	abstract static class ErrorLog {
		final Log log;
		private final Messager messager;
		private final Field errorCount;
		private final Field warningCount;
		
		private ErrorLog(Log log, Messager messager, Field errorCount, Field warningCount) {
			this.log = log;
			this.messager = messager;
			this.errorCount = errorCount;
			this.warningCount = warningCount;
		}

		final void error(DiagnosticPosition pos, String message) {
			increment(errorCount);
			error1(pos, message);
		}

		final void warning(DiagnosticPosition pos, String message) {
			increment(warningCount);
			warning1(pos, message);
		}

		final void mandatoryWarning(DiagnosticPosition pos, String message) {
			increment(warningCount);
			mandatoryWarning1(pos, message);
		}

		abstract void error1(DiagnosticPosition pos, String message);
		abstract void warning1(DiagnosticPosition pos, String message);
		abstract void mandatoryWarning1(DiagnosticPosition pos, String message);
		abstract void note(DiagnosticPosition pos, String message);
		
		private void increment(Field field) {
			if (field == null) return;
			try {
				int val = ((Number)field.get(messager)).intValue();
				field.set(messager, val +1);
			} catch (Throwable t) {
				//Very unfortunate, but in most cases it still works fine, so we'll silently swallow it.
			}
		}
		
		static ErrorLog create(Messager messager, Log log) {
			Field errorCount; try {
				errorCount = Permit.getField(messager.getClass(), "errorCount");
			} catch (Throwable t) {
				errorCount = null;
			}
			boolean hasMultipleErrors = false;
			for (Field field : log.getClass().getFields()) {
				if (field.getName().equals("multipleErrors")) {
					hasMultipleErrors = true;
					break;
				}
			}
			if (hasMultipleErrors) return new JdkBefore9(log, messager, errorCount);
			
			Field warningCount; try {
				warningCount = Permit.getField(messager.getClass(), "warningCount");
			} catch (Throwable t) {
				warningCount = null;
			}
			
			return new Jdk9Plus(log, messager, errorCount, warningCount);
		}
	}
	
	static class JdkBefore9 extends ErrorLog {
		private JdkBefore9(Log log, Messager messager, Field errorCount) {
			super(log, messager, errorCount, null);
		}

		@Override void error1(DiagnosticPosition pos, String message) {
			boolean prev = log.multipleErrors;
			log.multipleErrors = true;
			try {
				log.error(pos, "proc.messager", message);
			} finally {
				log.multipleErrors = prev;
			}
		}

		@Override void warning1(DiagnosticPosition pos, String message) {
			log.warning(pos, "proc.messager", message);
		}

		@Override void mandatoryWarning1(DiagnosticPosition pos, String message) {
			log.mandatoryWarning(pos, "proc.messager", message);
		}

		@Override void note(DiagnosticPosition pos, String message) {
			log.note(pos, "proc.messager", message);
		}
	}
	
	static class Jdk9Plus extends ErrorLog {
		private static final String PROC_MESSAGER = "proc.messager";
		private Object multiple;
		private Method errorMethod, warningMethod, mandatoryWarningMethod, noteMethod;
		private Method errorKey, warningKey, noteKey;
		private JCDiagnostic.Factory diags;
		
		private Jdk9Plus(Log log, Messager messager, Field errorCount, Field warningCount) {
			super(log, messager, errorCount, warningCount);

			try {
				final String jcd = "com.sun.tools.javac.util.JCDiagnostic";
				Class<?> df = Class.forName(jcd + "$DiagnosticFlag");
				for (Object constant : df.getEnumConstants()) {
					if (constant.toString().equals("MULTIPLE")) this.multiple = constant;
				}

				Class<?> errorCls = Class.forName(jcd + "$Error");
				Class<?> warningCls = Class.forName(jcd + "$Warning");
				Class<?> noteCls = Class.forName(jcd + "$Note");
				
				Class<?> lc = log.getClass();
				this.errorMethod = Permit.getMethod(lc, "error", df, DiagnosticPosition.class, errorCls);
				this.warningMethod = Permit.getMethod(lc, "warning", DiagnosticPosition.class, warningCls);
				this.mandatoryWarningMethod = Permit.getMethod(lc, "mandatoryWarning", DiagnosticPosition.class, warningCls);
				this.noteMethod = Permit.getMethod(lc, "note", DiagnosticPosition.class, noteCls);

				Field diagsField = Permit.getField(lc.getSuperclass(), "diags");
				this.diags = (JCDiagnostic.Factory) diagsField.get(log);

				Class<?> dc = this.diags.getClass();
				this.errorKey = Permit.getMethod(dc, "errorKey", String.class, Object[].class);
				this.warningKey = Permit.getMethod(dc, "warningKey", String.class, Object[].class);
				this.noteKey = Permit.getMethod(dc, "noteKey", String.class, Object[].class);
			} catch (Throwable t) {
				//t.printStackTrace();
			}
		}
		
		@Override void error1(DiagnosticPosition pos, String message) {
			Object error = Permit.invokeSneaky(this.errorKey, diags, PROC_MESSAGER, new Object[] { message });
			if (error != null) Permit.invokeSneaky(errorMethod, log, multiple, pos, error);
		}

		@Override
		void warning1(DiagnosticPosition pos, String message) {
			Object warning = Permit.invokeSneaky(this.warningKey, diags, PROC_MESSAGER, new Object[] { message });
			if (warning != null) Permit.invokeSneaky(warningMethod, log, pos, warning);
		}

		@Override
		void mandatoryWarning1(DiagnosticPosition pos, String message) {
			Object warning = Permit.invokeSneaky(this.warningKey, diags, PROC_MESSAGER, new Object[] { message });
			if (warning != null) Permit.invokeSneaky(mandatoryWarningMethod, log, pos, warning);
		}

		@Override
		void note(DiagnosticPosition pos, String message) {
			Object note = Permit.invokeSneaky(this.noteKey, diags, PROC_MESSAGER, new Object[] { message });
			if (note != null) Permit.invokeSneaky(noteMethod, log, pos, note);
		}
	}
}
