/*
 * Copyright (C) 2009-2018 The Project Lombok Authors.
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
package lombok.eclipse;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.Lombok;
import lombok.core.AST;
import lombok.core.LombokImmutableList;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import lombok.permit.Permit;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/**
 * Wraps around Eclipse's internal AST view to add useful features as well as the ability to visit parents from children,
 * something Eclipse own AST system does not offer.
 */
public class EclipseAST extends AST<EclipseAST, EclipseNode, ASTNode> {
	/**
	 * Creates a new EclipseAST of the provided Compilation Unit.
	 * 
	 * @param ast The compilation unit, which serves as the top level node in the tree to be built.
	 */
	public EclipseAST(CompilationUnitDeclaration ast) {
		super(toFileName(ast), packageDeclaration(ast), new EclipseImportList(ast), statementTypes());
		this.compilationUnitDeclaration = ast;
		setTop(buildCompilationUnit(ast));
		this.completeParse = isComplete(ast);
		clearChanged();
	}
	
	private static volatile boolean skipEclipseWorkspaceBasedFileResolver = false;
	private static final URI NOT_CALCULATED_MARKER = URI.create("https://projectlombok.org/not/calculated");
	private URI memoizedAbsoluteFileLocation = NOT_CALCULATED_MARKER;
	
	public static URI getAbsoluteFileLocation(CompilationUnitDeclaration ast) {
		return getAbsoluteFileLocation0(ast);
	}
	
	public URI getAbsoluteFileLocation() {
		if (memoizedAbsoluteFileLocation != NOT_CALCULATED_MARKER) return memoizedAbsoluteFileLocation;
		
		memoizedAbsoluteFileLocation = getAbsoluteFileLocation0(this.compilationUnitDeclaration);
		return memoizedAbsoluteFileLocation;
	}
	
	/** This is the call, but we wrapped it to memoize this. */
	private static URI getAbsoluteFileLocation0(CompilationUnitDeclaration ast) {
		String fileName = toFileName(ast);
		if (fileName != null && (fileName.startsWith("file:") || fileName.startsWith("sourcecontrol:"))) {
			// Some exotic build systems get real fancy with filenames. Known culprits:
			// The 'jazz' source control system _probably_ (not confirmed yet) uses sourcecontrol://jazz: urls.
			// GWT puts file:/D:/etc/etc/etc/Foo.java in here.
			return URI.create(fileName);
		}
		
		// state of the research in this:
		// * We need an abstraction of a 'directory level'. This abstraction needs 'read()' which returns a string (content of lombok.config) and 'getParent()'.
		// * sometimes, cud.compilationResult.compilationUnit is an 'openable', you can chase this down to end up with a path, you can jigger this into being the sibling 'lombok.config', and then use:
		// 				InputStream in = ResourcesPlugin.getWorkspace().getRoot().getFile(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(x)).getFullPath()).getContents(true);
		// to read out this data. Our theory is that this will work even with very crazy virtual filesystems such as sourcecontrol://jazz/blabla.
		// * With jazz and other creative file backed systems, is there even a 'project root' concept? Surely there won't be a 'workspace root' concept so how do we abstract the idea that, from jazz://whatever/projectroot, the parent is c:\myWorkspace?
		// * Check the .getAlternateAbsolutePath() impl which has the research done so far.
		// * VIRTUAL FILES: Sometimes virtual files are created; their location tends to be /FileName.java which cannot be resolved. Optimally speaking we should find the 'source' of the virtual code and use IT for determining lombok.config, but that may not be feasible. If not, can we get at project or at least workspace?
		// * Either way there are sufficiently many WTF situations, that in case of error, as painful as this is, we should just carry on and not apply lombok.config, though at least if we don't recognize the scenario we should write a log file imploring the user to send us a bunch of feedback on the situation.
		// Relevant issues: Comment 2 on #683, all of #682
		if (!skipEclipseWorkspaceBasedFileResolver) {
//			if (Boolean.FALSE) throw new IllegalArgumentException("Here's the alt strat result: " + getAlternativeAbsolutePathDEBUG());
			try {
				/*if (fileName.startsWith("/") && fileName.indexOf('/', 1) > -1) */
				try {
					return EclipseWorkspaceBasedFileResolver.resolve(fileName);
				} catch (IllegalArgumentException e) {
					EclipseHandlerUtil.warning("Finding 'lombok.config' file failed for '" + fileName + "'", e);
//					String msg = e.getMessage();
//					if (msg != null && msg.startsWith("Path must include project and resource name")) {
//						// We shouldn't throw an exception at all, but we can't reproduce this so we need help from our users to figure this out.
//						// Let's bother them with an error that slows their eclipse down to a crawl and makes it unusable.
//						throw new IllegalArgumentException("Path resolution for lombok.config failed. Path: " + fileName + " -- package of this class: " + this.getPackageDeclaration());
//					} else throw e;
				}
			} catch (NoClassDefFoundError e) {
				skipEclipseWorkspaceBasedFileResolver = true;
			}
		}
		
		// Our fancy workspace based source file to absolute disk location algorithm only works in a fully fledged eclipse.
		// This fallback works when using 'ecj', which has a much simpler project/path system. For example, no 'linked' resources.
		
		try {
			return new File(fileName).getAbsoluteFile().toURI();
		} catch (Exception e) {
			// This is a temporary workaround while we try and gather all the various exotic shenanigans where lombok.config resolution is not going to work!
			return null;
		}
	}
	
//	/** This is ongoing research for issues with lombok.config resolution. */
//	@SuppressWarnings("unused") private String getAlternativeAbsolutePathDEBUG() {
//		try {
//			ICompilationUnit cu = this.compilationUnitDeclaration.compilationResult.compilationUnit;
//			
//			if (cu instanceof Openable) {
//				String x = ((Openable) cu).getResource().getFullPath().makeAbsolute().toString();
//				int lastLoc = x.lastIndexOf('/');
//				x = x.substring(0, lastLoc + 1) + "lombok.config";
//				URI lombokConfigLoc = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(x)).getLocationURI();
//				InputStream in = ResourcesPlugin.getWorkspace().getRoot().getFile(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(x)).getFullPath()).getContents(true);
//				byte[] b = new byte[100000];
//				int p = 0;
//				while (true) {
//					int r = in.read(b, p, b.length - p);
//					if (r == -1) break;
//					p += r;
//				}
//				in.close();
//				return "(Contents of lombok.config: " + new String(b, 0, p, "UTF-8");
//
////				return "(alt strategy result C: '" + ((Openable) cu).getResource().getFullPath().makeAbsolute().toString() + "'): resolved: " + EclipseWorkspaceBasedFileResolver.resolve(((Openable) cu).getResource().getFullPath().makeAbsolute().toString());
//			}
//			if (cu instanceof SourceFile) {
//				String cuFileName = new String(((SourceFile) cu).getFileName());
//				String cuIFilePath = ((SourceFile) cu).resource.getFullPath().toString();
//				return "(alt strategy result A: \"" + cuFileName + "\" B: \"" + cuIFilePath + "\")";
//			}
//			return "(alt strategy failed: cu isn't a SourceFile or Openable but a " + cu.getClass() + ")";
//		} catch (Exception e) {
//			return "(alt strategy failed: " + e + ")";
//		}
//	}
	
	private static class EclipseWorkspaceBasedFileResolver {
		public static URI resolve(String path) {
			/* eclipse issue: When creating snippets, for example to calculate 'find callers', refactor scripts, save actions, etc,
			 * eclipse creates a psuedo-file whose path is simply "/SimpleName.java", which cannot be turned back into a real location.
			 * What we really need to do is find out which file is the source of this script job and use its directory instead. For now,
			 * we just go with all defaults; these operations are often not sensitive to proper lomboking or aren't even lomboked at all.
			 * 
			 * Reliable way to reproduce this (Kepler, possibly with JDK8 beta support):
			 * * Have a method, called once by some code in another class.
			 * * Refactor it with the 'change method signature' refactor script, and add a parameter and hit 'ok'.
			 */
			if (path == null || path.indexOf('/', 1) == -1) {
				return null;
			}
			try {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path)).getLocationURI();
			} catch (Exception e) {
				// One of the exceptions that can occur is IllegalStateException (during getWorkspace())
				// if you try to run this while eclipse is shutting down.
				return null;
			}
		}
	}
	
	private static String packageDeclaration(CompilationUnitDeclaration cud) {
		ImportReference pkg = cud.currentPackage;
		return pkg == null ? null : Eclipse.toQualifiedName(pkg.getImportName());
	}
	
	@Override public int getSourceVersion() {
		long sl = compilationUnitDeclaration.problemReporter.options.sourceLevel;
		long cl = compilationUnitDeclaration.problemReporter.options.complianceLevel;
		sl >>= 16;
		cl >>= 16;
		if (sl == 0) sl = cl;
		if (cl == 0) cl = sl;
		return Math.min((int)(sl - 44), (int)(cl - 44));
	}
	
	@Override public int getLatestJavaSpecSupported() {
		return Eclipse.getEcjCompilerVersion();
	}
	
	/**
	 * Runs through the entire AST, starting at the compilation unit, calling the provided visitor's visit methods
	 * for each node, depth first.
	 */
	public void traverse(EclipseASTVisitor visitor) {
		top().traverse(visitor);
	}
	
	void traverseChildren(EclipseASTVisitor visitor, EclipseNode node) {
		LombokImmutableList<EclipseNode> children = node.down();
		int len = children.size();
		for (int i = 0; i < len; i++) {
			children.get(i).traverse(visitor);
		}
	}
	
	/**
	 * Eclipse starts off with a 'diet' parse which leaves method bodies blank, amongst other shortcuts.
	 * 
	 * For such diet parses, this method returns false, otherwise it returns true. Any lombok processor
	 * that needs the contents of methods should just do nothing (and return false so it gets another shot later!)
	 * when this is false.
	 */
	public boolean isCompleteParse() {
		return completeParse;
	}
	
	class ParseProblem {
		final boolean isWarning;
		final String message;
		final int sourceStart;
		final int sourceEnd;
		
		ParseProblem(boolean isWarning, String message, int sourceStart, int sourceEnd) {
			this.isWarning = isWarning;
			this.message = message;
			this.sourceStart = sourceStart;
			this.sourceEnd = sourceEnd;
		}
		
		void addToCompilationResult() {
			CompilationUnitDeclaration cud = (CompilationUnitDeclaration) top().get();
			addProblemToCompilationResult(cud.getFileName(), cud.compilationResult,
					isWarning, message, sourceStart, sourceEnd);
		}
	}
	
	private void propagateProblems() {
		if (queuedProblems.isEmpty()) return;
		CompilationUnitDeclaration cud = (CompilationUnitDeclaration) top().get();
		if (cud.compilationResult == null) return;
		for (ParseProblem problem : queuedProblems) problem.addToCompilationResult();
		queuedProblems.clear();
	}
	
	private final List<ParseProblem> queuedProblems = new ArrayList<ParseProblem>();
	
	void addProblem(ParseProblem problem) {
		queuedProblems.add(problem);
		propagateProblems();
	}
	
	/**
	 * Adds a problem to the provided CompilationResult object so that it will show up
	 * in the Problems/Warnings view.
	 */
	public static void addProblemToCompilationResult(char[] fileNameArray, CompilationResult result,
			boolean isWarning, String message, int sourceStart, int sourceEnd) {
		
		Permit.invokeSneaky(EcjReflectionCheck.problemAddProblemToCompilationResult, EcjReflectionCheck.addProblemToCompilationResult, null, fileNameArray, result, isWarning, message, sourceStart, sourceEnd);
	}
	
	public static Annotation[] getTopLevelTypeReferenceAnnotations(TypeReference tr) {
		Method m = EcjReflectionCheck.typeReferenceGetAnnotationsOnDimensions;
		if (m == null) return null;
		Annotation[][] annss = null;
		try {
			annss = (Annotation[][]) Permit.invoke(m, tr);
			if (annss != null) return annss[0];
		} catch (Throwable ignore) {}
		
		try {
			Field f = EcjReflectionCheck.typeReferenceAnnotations;
			if (f == null) return null;
			annss = (Annotation[][]) Permit.get(f, tr);
			if (annss == null) return null;
			return annss[annss.length - 1];
		} catch (Throwable t) {
			return null;
		}
	}
	
	private final CompilationUnitDeclaration compilationUnitDeclaration;
	private boolean completeParse;
	
	private static String toFileName(CompilationUnitDeclaration ast) {
		return ast.compilationResult.fileName == null ? null : new String(ast.compilationResult.fileName);
	}
	
	/**
	 * Call this method to move an EclipseAST generated for a diet parse to rebuild itself for the full parse -
	 * with filled in method bodies and such. Also propagates problems and errors, which in diet parse
	 * mode can't be reliably added to the problems/warnings view.
	 */
	public void rebuild(boolean force) {
		propagateProblems();
		if (completeParse && !force) return;
		boolean changed = isChanged();
		boolean newCompleteParse = isComplete(compilationUnitDeclaration);
		if (!newCompleteParse && !force) return;
		
		top().rebuild();
		
		this.completeParse = newCompleteParse;
		if (!changed) clearChanged();
	}
	
	public static boolean isComplete(CompilationUnitDeclaration unit) {
		return (unit.bits & ASTNode.HasAllMethodBodies) != 0;
	}
	
	/** {@inheritDoc} */
	@Override protected EclipseNode buildTree(ASTNode node, Kind kind) {
		switch (kind) {
		case COMPILATION_UNIT:
			return buildCompilationUnit((CompilationUnitDeclaration) node);
		case TYPE:
			return buildType((TypeDeclaration) node);
		case FIELD:
			return buildField((FieldDeclaration) node);
		case INITIALIZER:
			return buildInitializer((Initializer) node);
		case METHOD:
			return buildMethod((AbstractMethodDeclaration) node);
		case ARGUMENT:
			return buildLocal((Argument) node, kind);
		case LOCAL:
			return buildLocal((LocalDeclaration) node, kind);
		case STATEMENT:
			return buildStatement((Statement) node);
		case ANNOTATION:
			return buildAnnotation((Annotation) node, false);
		case TYPE_USE:
			return buildTypeUse((TypeReference) node);
		default:
			throw new AssertionError("Did not expect to arrive here: " + kind);
		}
	}
	
	private EclipseNode buildCompilationUnit(CompilationUnitDeclaration top) {
		if (setAndGetAsHandled(top)) return null;
		List<EclipseNode> children = buildTypes(top.types);
		return putInMap(new EclipseNode(this, top, children, Kind.COMPILATION_UNIT));
	}
	
	private void addIfNotNull(Collection<EclipseNode> collection, EclipseNode n) {
		if (n != null) collection.add(n);
	}
	
	private List<EclipseNode> buildTypes(TypeDeclaration[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (TypeDeclaration type : children) addIfNotNull(childNodes, buildType(type));
		return childNodes;
	}
	
	private EclipseNode buildType(TypeDeclaration type) {
		if (setAndGetAsHandled(type)) return null;
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		childNodes.addAll(buildFields(type.fields));
		childNodes.addAll(buildTypes(type.memberTypes));
		childNodes.addAll(buildMethods(type.methods));
		childNodes.addAll(buildAnnotations(type.annotations, false));
		return putInMap(new EclipseNode(this, type, childNodes, Kind.TYPE));
	}
	
	private Collection<EclipseNode> buildFields(FieldDeclaration[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (FieldDeclaration child : children) addIfNotNull(childNodes, buildField(child));
		return childNodes;
	}
	
	private static <T> List<T> singleton(T item) {
		List<T> list = new ArrayList<T>();
		if (item != null) list.add(item);
		return list;
	}
	
	private EclipseNode buildField(FieldDeclaration field) {
		if (field instanceof Initializer) return buildInitializer((Initializer)field);
		if (setAndGetAsHandled(field)) return null;
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		addIfNotNull(childNodes, buildTypeUse(field.type));
		addIfNotNull(childNodes, buildStatement(field.initialization));
		childNodes.addAll(buildAnnotations(field.annotations, true));
		return putInMap(new EclipseNode(this, field, childNodes, Kind.FIELD));
	}
	
	private EclipseNode buildInitializer(Initializer initializer) {
		if (setAndGetAsHandled(initializer)) return null;
		return putInMap(new EclipseNode(this, initializer, singleton(buildStatement(initializer.block)), Kind.INITIALIZER));
	}
	
	private Collection<EclipseNode> buildMethods(AbstractMethodDeclaration[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (AbstractMethodDeclaration method : children) addIfNotNull(childNodes, buildMethod(method));
		return childNodes;
	}
	
	private EclipseNode buildMethod(AbstractMethodDeclaration method) {
		if (setAndGetAsHandled(method)) return null;
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		childNodes.addAll(buildArguments(method.arguments));
		if (method instanceof ConstructorDeclaration) {
			ConstructorDeclaration constructor = (ConstructorDeclaration) method;
			addIfNotNull(childNodes, buildStatement(constructor.constructorCall));
		}
		childNodes.addAll(buildStatements(method.statements));
		childNodes.addAll(buildAnnotations(method.annotations, false));
		return putInMap(new EclipseNode(this, method, childNodes, Kind.METHOD));
	}
	
	//Arguments are a kind of LocalDeclaration. They can definitely contain lombok annotations, so we care about them.
	private Collection<EclipseNode> buildArguments(Argument[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (LocalDeclaration local : children) {
			addIfNotNull(childNodes, buildLocal(local, Kind.ARGUMENT));
		}
		return childNodes;
	}
	
	private EclipseNode buildLocal(LocalDeclaration local, Kind kind) {
		if (setAndGetAsHandled(local)) return null;
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		addIfNotNull(childNodes, buildTypeUse(local.type));
		addIfNotNull(childNodes, buildStatement(local.initialization));
		childNodes.addAll(buildAnnotations(local.annotations, true));
		return putInMap(new EclipseNode(this, local, childNodes, kind));
	}
	
	private EclipseNode buildTypeUse(TypeReference tr) {
		if (setAndGetAsHandled(tr)) return null;
		if (tr == null) return null;
		
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		Annotation[] anns = getTopLevelTypeReferenceAnnotations(tr);
		if (anns != null) for (Annotation ann : anns) addIfNotNull(childNodes, buildAnnotation(ann, false));
		
		if (tr instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference pqtr = (ParameterizedQualifiedTypeReference) tr;
			int len = pqtr.tokens.length;
			for (int i = 0; i < len; i++) {
				TypeReference[] typeArgs = pqtr.typeArguments[i];
				if (typeArgs != null) for (TypeReference tArg : typeArgs) addIfNotNull(childNodes, buildTypeUse(tArg));
			}
		} else if (tr instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference pstr = (ParameterizedSingleTypeReference) tr;
			if (pstr.typeArguments != null) for (TypeReference tArg : pstr.typeArguments) {
				addIfNotNull(childNodes, buildTypeUse(tArg));
			}
		} else if (tr instanceof Wildcard) {
			TypeReference bound = ((Wildcard) tr).bound;
			if (bound != null) addIfNotNull(childNodes, buildTypeUse(bound));
		}
		
		return putInMap(new EclipseNode(this, tr, childNodes, Kind.TYPE_USE));
	}
	
	private Collection<EclipseNode> buildAnnotations(Annotation[] annotations, boolean varDecl) {
		List<EclipseNode> elements = new ArrayList<EclipseNode>();
		if (annotations != null) for (Annotation an : annotations) addIfNotNull(elements, buildAnnotation(an, varDecl));
		return elements;
	}
	
	private EclipseNode buildAnnotation(Annotation annotation, boolean field) {
		if (annotation == null) return null;
		boolean handled = setAndGetAsHandled(annotation);
		if (!field && handled) {
			// @Foo int x, y; is handled in eclipse by putting the same annotation node on 2 FieldDeclarations.
			return null;
		}
		return putInMap(new EclipseNode(this, annotation, null, Kind.ANNOTATION));
	}
	
	private Collection<EclipseNode> buildStatements(Statement[] children) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		if (children != null) for (Statement child  : children) addIfNotNull(childNodes, buildStatement(child));
		return childNodes;
	}
	
	private EclipseNode buildStatement(Statement child) {
		if (child == null) return null;
		if (child instanceof TypeDeclaration) return buildType((TypeDeclaration) child);
		
		if (child instanceof LocalDeclaration) return buildLocal((LocalDeclaration) child, Kind.LOCAL);
		
		if (setAndGetAsHandled(child)) return null;
		
		return drill(child);
	}
	
	private EclipseNode drill(Statement statement) {
		List<EclipseNode> childNodes = new ArrayList<EclipseNode>();
		for (FieldAccess fa : fieldsOf(statement.getClass())) childNodes.addAll(buildWithField(EclipseNode.class, statement, fa));
		return putInMap(new EclipseNode(this, statement, childNodes, Kind.STATEMENT));
	}
	
	/* For Eclipse, only Statement counts, as Expression is a subclass of it, even though this isn't
	 * entirely correct according to the JLS spec (only some expressions can be used as statements, not all of them). */
	private static Collection<Class<? extends ASTNode>> statementTypes() {
		return Collections.<Class<? extends ASTNode>>singleton(Statement.class);
	}
	
	private static class EcjReflectionCheck {
		private static final String COMPILATIONRESULT_TYPE = "org.eclipse.jdt.internal.compiler.CompilationResult";
		
		public static final Method addProblemToCompilationResult;
		public static final Throwable problemAddProblemToCompilationResult;
		public static final Method typeReferenceGetAnnotationsOnDimensions;
		public static final Field typeReferenceAnnotations;
		static {
			Throwable problem_ = null;
			Method m1 = null, m2;
			Field f;
			try {
				m1 = Permit.getMethod(EclipseAstProblemView.class, "addProblemToCompilationResult", char[].class, Class.forName(COMPILATIONRESULT_TYPE), boolean.class, String.class, int.class, int.class);
			} catch (Throwable t) {
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
				problem_ = t;
			}
			try {
				m2 = Permit.getMethod(TypeReference.class, "getAnnotationsOnDimensions");
			} catch (Throwable t) {
				m2 = null;
			}
			try {
				f = Permit.getField(TypeReference.class, "annotations");
			} catch (Throwable t) {
				f = null;
			}
			addProblemToCompilationResult = m1;
			problemAddProblemToCompilationResult = problem_;
			typeReferenceGetAnnotationsOnDimensions = m2;
			typeReferenceAnnotations = f;
		}
	}
}
