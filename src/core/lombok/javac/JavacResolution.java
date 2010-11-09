package lombok.javac;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Map;

import javax.lang.model.type.TypeKind;
import javax.tools.DiagnosticListener;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.CapturedType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;

public class JavacResolution {
	private final Attr attr;
	private final LogDisabler logDisabler;
	
	public JavacResolution(Context context) {
		attr = Attr.instance(context);
		logDisabler = new LogDisabler(context);
	}
	/**
	 * During resolution, the resolver will emit resolution errors, but without appropriate file names and line numbers. If these resolution errors stick around
	 * then they will be generated AGAIN, this time with proper names and line numbers, at the end. Therefore, we want to suppress the logger.
	 */
	private static final class LogDisabler {
		private final Log log;
		private static final Field errWriterField, warnWriterField, noticeWriterField, dumpOnErrorField, promptOnErrorField, diagnosticListenerField;
		private PrintWriter errWriter, warnWriter, noticeWriter;
		private Boolean dumpOnError, promptOnError;
		private DiagnosticListener<?> contextDiagnosticListener, logDiagnosticListener;
		private final Context context;
		
		// If this is true, the fields changed. Better to print weird error messages than to fail outright.
		private static final boolean dontBother;
		
		static {
			boolean z;
			Field a = null, b = null, c = null, d = null, e = null, f = null;
			try {
				a = Log.class.getDeclaredField("errWriter");
				b = Log.class.getDeclaredField("warnWriter");
				c = Log.class.getDeclaredField("noticeWriter");
				d = Log.class.getDeclaredField("dumpOnError");
				e = Log.class.getDeclaredField("promptOnError");
				f = Log.class.getDeclaredField("diagListener");
				z = false;
				a.setAccessible(true);
				b.setAccessible(true);
				c.setAccessible(true);
				d.setAccessible(true);
				e.setAccessible(true);
				f.setAccessible(true);
			} catch (Exception x) {
				z = true;
			}
			
			errWriterField = a;
			warnWriterField = b;
			noticeWriterField = c;
			dumpOnErrorField = d;
			promptOnErrorField = e;
			diagnosticListenerField = f;
			dontBother = z;
		}
		
		LogDisabler(Context context) {
			this.log = Log.instance(context);
			this.context = context;
		}
		
		boolean disableLoggers() {
			contextDiagnosticListener = context.get(DiagnosticListener.class);
			context.put(DiagnosticListener.class, (DiagnosticListener<?>) null);
			if (dontBother) return false;
			boolean dontBotherInstance = false;
			
			PrintWriter dummyWriter = new PrintWriter(new OutputStream() {
				@Override public void write(int b) throws IOException {
					// Do nothing on purpose
				}
			});
			
			if (!dontBotherInstance) try {
				errWriter = (PrintWriter) errWriterField.get(log);
				errWriterField.set(log, dummyWriter);
			} catch (Exception e) {
				dontBotherInstance = true;
			}
			
			if (!dontBotherInstance) try {
				warnWriter = (PrintWriter) warnWriterField.get(log);
				warnWriterField.set(log, dummyWriter);
			} catch (Exception e) {
				dontBotherInstance = true;
			}
			
			if (!dontBotherInstance) try {
				noticeWriter = (PrintWriter) noticeWriterField.get(log);
				noticeWriterField.set(log, dummyWriter);
			} catch (Exception e) {
				dontBotherInstance = true;
			}
			
			if (!dontBotherInstance) try {
				dumpOnError = (Boolean) dumpOnErrorField.get(log);
				dumpOnErrorField.set(log, false);
			} catch (Exception e) {
				dontBotherInstance = true;
			}
			
			if (!dontBotherInstance) try {
				promptOnError = (Boolean) promptOnErrorField.get(log);
				promptOnErrorField.set(log, false);
			} catch (Exception e) {
				dontBotherInstance = true;
			}
			
			if (!dontBotherInstance) try {
				logDiagnosticListener = (DiagnosticListener<?>) diagnosticListenerField.get(log);
				diagnosticListenerField.set(log, null);
			} catch (Exception e) {
				dontBotherInstance = true;
			}
			
			if (dontBotherInstance) enableLoggers();
			return !dontBotherInstance;
		}
		
		void enableLoggers() {
			if (contextDiagnosticListener != null) {
				context.put(DiagnosticListener.class, contextDiagnosticListener);
				contextDiagnosticListener = null;
			}
			if (errWriter != null) try {
				errWriterField.set(log, errWriter);
				errWriter = null;
			} catch (Exception e) {}
			
			if (warnWriter != null) try {
				warnWriterField.set(log, warnWriter);
				warnWriter = null;
			} catch (Exception e) {}
			
			if (noticeWriter != null) try {
				noticeWriterField.set(log, noticeWriter);
				noticeWriter = null;
			} catch (Exception e) {}
			
			if (dumpOnError != null) try {
				dumpOnErrorField.set(log, dumpOnError);
				dumpOnError = null;
			} catch (Exception e) {}
			
			if (promptOnError != null) try {
				promptOnErrorField.set(log, promptOnError);
				promptOnError = null;
			} catch (Exception e) {}
			
			if (logDiagnosticListener != null) try {
				diagnosticListenerField.set(log, logDiagnosticListener);
				logDiagnosticListener = null;
			} catch (Exception e) {}
		}
	}
	
	private static final class EnvFinder extends JCTree.Visitor {
		private Env<AttrContext> env = null;
		private Enter enter;
		private MemberEnter memberEnter;
		private JCTree copyAt = null;
		
		EnvFinder(Context context) {
			this.enter = Enter.instance(context);
			this.memberEnter = MemberEnter.instance(context);
		}
		
		Env<AttrContext> get() {
			return env;
		}
		
		JCTree copyAt() {
			return copyAt;
		}
		
		@Override public void visitTopLevel(JCCompilationUnit tree) {
			if (copyAt != null) return;
			env = enter.getTopLevelEnv(tree);
		}
		
		@Override public void visitClassDef(JCClassDecl tree) {
			if (copyAt != null) return;
			// The commented out one leaves the 'lint' field unset, which causes NPEs during attrib. So, we use the other one.
			//env = enter.classEnv((JCClassDecl) tree, env);
			env = enter.getClassEnv(tree.sym);
		}
		
		@Override public void visitMethodDef(JCMethodDecl tree) {
			if (copyAt != null) return;
			env = memberEnter.getMethodEnv(tree, env);
			copyAt = tree;
		}
		
		public void visitVarDef(JCVariableDecl tree) {
			if (copyAt != null) return;
			env = memberEnter.getInitEnv(tree, env);
			copyAt = tree;
		}
		
		@Override public void visitBlock(JCBlock tree) {
			if (copyAt != null) return;
			copyAt = tree;
		}
		
		@Override public void visitTree(JCTree that) {
		}
	}
	
	public Map<JCTree, JCTree> resolve(JavacNode node) {
		ArrayDeque<JCTree> stack = new ArrayDeque<JCTree>();
		
		{
			JavacNode n = node;
			while (n != null) {
				stack.push(n.get());
				n = n.up();
			}
		}
		
		logDisabler.disableLoggers();
		try {
			EnvFinder finder = new EnvFinder(node.getContext());
			while (!stack.isEmpty()) stack.pop().accept(finder);
			
			TreeMirrorMaker mirrorMaker = new TreeMirrorMaker(node);
			JCTree copy = mirrorMaker.copy(finder.copyAt());
			
			attrib(copy, finder.get());
			return mirrorMaker.getOriginalToCopyMap();
		} finally {
			logDisabler.enableLoggers();
		}
	}
	
	private void attrib(JCTree tree, Env<AttrContext> env) {
		if (tree instanceof JCBlock) attr.attribStat(tree, env);
		else if (tree instanceof JCMethodDecl) attr.attribStat(((JCMethodDecl)tree).body, env);
		else if (tree instanceof JCVariableDecl) attr.attribStat(tree, env);
		else throw new IllegalStateException("Called with something that isn't a block, method decl, or variable decl");
	}
	
	public static class TypeNotConvertibleException extends Exception {
		public TypeNotConvertibleException(String msg) {
			super(msg);
		}
	}
	
	public static Type ifTypeIsIterableToComponent(Type type, JavacAST ast) {
		Types types = Types.instance(ast.getContext());
		Symtab syms = Symtab.instance(ast.getContext());
		Type boundType = types.upperBound(type);
		Type elemTypeIfArray = types.elemtype(boundType);
		if (elemTypeIfArray != null) return elemTypeIfArray;
		
		Type base = types.asSuper(boundType, syms.iterableType.tsym);
		if (base == null) return syms.objectType;
		
		List<Type> iterableParams = base.allparams();
		return iterableParams.isEmpty() ? syms.objectType : types.upperBound(iterableParams.head);
	}
	
	public static JCExpression typeToJCTree(Type type, TreeMaker maker, JavacAST ast) throws TypeNotConvertibleException {
		return typeToJCTree(type, maker, ast, false);
	}
	
	public static JCExpression createJavaLangObject(TreeMaker maker, JavacAST ast) {
		JCExpression out = maker.Ident(ast.toName("java"));
		out = maker.Select(out, ast.toName("lang"));
		out = maker.Select(out, ast.toName("Object"));
		return out;
	}
	
	private static JCExpression typeToJCTree(Type type, TreeMaker maker, JavacAST ast, boolean allowCompound) throws TypeNotConvertibleException {
		int dims = 0;
		Type type0 = type;
		while (type0 instanceof ArrayType) {
			dims++;
			type0 = ((ArrayType)type0).elemtype;
		}
		
		JCExpression result = typeToJCTree0(type0, maker, ast, allowCompound);
		while (dims > 0) {
			result = maker.TypeArray(result);
			dims--;
		}
		return result;
	}
	
	private static JCExpression typeToJCTree0(Type type, TreeMaker maker, JavacAST ast, boolean allowCompound) throws TypeNotConvertibleException {
		// NB: There's such a thing as maker.Type(type), but this doesn't work very well; it screws up anonymous classes, captures, and adds an extra prefix dot for some reason too.
		//  -- so we write our own take on that here.
		
		if (type.isPrimitive()) return primitiveToJCTree(type.getKind(), maker);
		if (type.isErroneous()) throw new TypeNotConvertibleException("Type cannot be resolved");
		
		TypeSymbol symbol = type.asElement();
		List<Type> generics = type.getTypeArguments();
		
		JCExpression replacement = null;
		
		if (symbol == null) throw new TypeNotConvertibleException("Null or compound type");
		
		if (symbol.name.len == 0) {
			// Anonymous inner class
			if (type instanceof ClassType) {
				List<Type> ifaces = ((ClassType)type).interfaces_field;
				Type supertype = ((ClassType)type).supertype_field;
				if (ifaces != null && ifaces.length() == 1) {
					return typeToJCTree(ifaces.get(0), maker, ast, allowCompound);
				}
				if (supertype != null) return typeToJCTree(supertype, maker, ast, allowCompound);
			}
			throw new TypeNotConvertibleException("Anonymous inner class");
		}
		
		if (type instanceof CapturedType) {
			if (allowCompound) {
				if (type.getLowerBound() == null || type.getLowerBound().tag == TypeTags.BOT) {
					if (type.getUpperBound().toString().equals("java.lang.Object")) {
						return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
					}
					return maker.Wildcard(maker.TypeBoundKind(BoundKind.EXTENDS), typeToJCTree(type.getUpperBound(), maker, ast, false));
				} else {
					return maker.Wildcard(maker.TypeBoundKind(BoundKind.SUPER), typeToJCTree(type.getLowerBound(), maker, ast, false));
				}
			}
			if (type.getUpperBound() != null) {
				return typeToJCTree(type.getUpperBound(), maker, ast, allowCompound);
			}
			
			return createJavaLangObject(maker, ast);
		}
		
		String qName = symbol.getQualifiedName().toString();
		if (qName.isEmpty()) throw new TypeNotConvertibleException("unknown type");
		if (qName.startsWith("<")) throw new TypeNotConvertibleException(qName);
		String[] baseNames = symbol.getQualifiedName().toString().split("\\.");
		replacement = maker.Ident(ast.toName(baseNames[0]));
		for (int i = 1; i < baseNames.length; i++) {
			replacement = maker.Select(replacement, ast.toName(baseNames[i]));
		}
		
		if (generics != null && !generics.isEmpty()) {
			List<JCExpression> args = List.nil();
			for (Type t : generics) args = args.append(typeToJCTree(t, maker, ast, true));
			replacement = maker.TypeApply(replacement, args);
		}
		
		return replacement;
	}
	
	private static JCExpression primitiveToJCTree(TypeKind kind, TreeMaker maker) throws TypeNotConvertibleException {
		switch (kind) {
		case BYTE:
			return maker.TypeIdent(TypeTags.BYTE);
		case CHAR:
			return maker.TypeIdent(TypeTags.CHAR);
		case SHORT:
			return maker.TypeIdent(TypeTags.SHORT);
		case INT:
			return maker.TypeIdent(TypeTags.INT);
		case LONG:
			return maker.TypeIdent(TypeTags.LONG);
		case FLOAT:
			return maker.TypeIdent(TypeTags.FLOAT);
		case DOUBLE:
			return maker.TypeIdent(TypeTags.DOUBLE);
		case BOOLEAN:
			return maker.TypeIdent(TypeTags.BOOLEAN);
		case VOID:
			return maker.TypeIdent(TypeTags.VOID);
		case NULL:
		case NONE:
		case OTHER:
		default:
			throw new TypeNotConvertibleException("Nulltype");
		}
	}
}
