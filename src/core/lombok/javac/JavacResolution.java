/*
 * Copyright (C) 2011-2012 The Project Lombok Authors.
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.lang.model.type.TypeKind;
import javax.tools.DiagnosticListener;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.CapturedType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.WildcardType;
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
import com.sun.tools.javac.util.ListBuffer;
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
		private static final Field deferDiagnosticsField, deferredDiagnosticsField;
		private PrintWriter errWriter, warnWriter, noticeWriter;
		private Boolean dumpOnError, promptOnError;
		private DiagnosticListener<?> contextDiagnosticListener, logDiagnosticListener;
		private final Context context;
		
		// If this is true, the fields changed. Better to print weird error messages than to fail outright.
		private static final boolean dontBother;
		
		private static final ThreadLocal<Queue<?>> queueCache = new ThreadLocal<Queue<?>>();
		
		static {
			boolean z;
			Field a = null, b = null, c = null, d = null, e = null, f = null, g = null, h = null;
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
			} catch (Throwable x) {
				z = true;
			}
			
			try {
				g = Log.class.getDeclaredField("deferDiagnostics");
				h = Log.class.getDeclaredField("deferredDiagnostics");
				g.setAccessible(true);
				h.setAccessible(true);
			} catch (Throwable x) {
			}
			
			errWriterField = a;
			warnWriterField = b;
			noticeWriterField = c;
			dumpOnErrorField = d;
			promptOnErrorField = e;
			diagnosticListenerField = f;
			deferDiagnosticsField = g;
			deferredDiagnosticsField = h;
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
			
			if (deferDiagnosticsField != null) try {
				if (Boolean.TRUE.equals(deferDiagnosticsField.get(log))) {
					queueCache.set((Queue<?>) deferredDiagnosticsField.get(log));
					Queue<?> empty = new LinkedList<Object>();
					deferredDiagnosticsField.set(log, empty);
				}
			} catch (Exception e) {}
			
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
			
			if (deferDiagnosticsField != null && queueCache.get() != null) try {
				deferredDiagnosticsField.set(log, queueCache.get());
				queueCache.set(null);
			} catch (Exception e) {}
		}
	}
	
	/*
	 * We need to dig down to the level of the method or field declaration or (static) initializer block, then attribute that entire method/field/block using
	 * the appropriate environment. So, we start from the top and walk down the node tree until we hit that method/field/block and stop there, recording both
	 * the environment object (`env`) and the exact tree node (`copyAt`) at which to begin the attr process.
	 */
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
			// The commented out stuff requires reflection tricks to avoid leaving lint unset which causes NPEs during attrib. So, we use the other one, much less code.
//			env = enter.classEnv((JCClassDecl) tree, env);
//			try {
//				Field f = env.info.getClass().getDeclaredField("lint");
//				f.setAccessible(true);
//				Constructor<?> c = Lint.class.getDeclaredConstructor(Lint.class);
//				c.setAccessible(true);
//				f.set(env.info, c.newInstance(lint));
//			} catch (Exception e) {
//				throw Lombok.sneakyThrow(e);
//			}
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
	
	public Map<JCTree, JCTree> resolveMethodMember(JavacNode node) {
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
			
			TreeMirrorMaker mirrorMaker = new TreeMirrorMaker(node.getTreeMaker());
			JCTree copy = mirrorMaker.copy(finder.copyAt());
			
			attrib(copy, finder.get());
			return mirrorMaker.getOriginalToCopyMap();
		} finally {
			logDisabler.enableLoggers();
		}
	}
	
	public void resolveClassMember(JavacNode node) {
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
			
			attrib(node.get(), finder.get());
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
	
	public static JCExpression typeToJCTree(Type type, JavacAST ast, boolean allowVoid) throws TypeNotConvertibleException {
		return typeToJCTree(type, ast, false, allowVoid);
	}
	
	public static JCExpression createJavaLangObject(JavacAST ast) {
		TreeMaker maker = ast.getTreeMaker();
		JCExpression out = maker.Ident(ast.toName("java"));
		out = maker.Select(out, ast.toName("lang"));
		out = maker.Select(out, ast.toName("Object"));
		return out;
	}
	
	private static JCExpression typeToJCTree(Type type, JavacAST ast, boolean allowCompound, boolean allowVoid) throws TypeNotConvertibleException {
		int dims = 0;
		Type type0 = type;
		while (type0 instanceof ArrayType) {
			dims++;
			type0 = ((ArrayType)type0).elemtype;
		}
		
		JCExpression result = typeToJCTree0(type0, ast, allowCompound, allowVoid);
		while (dims > 0) {
			result = ast.getTreeMaker().TypeArray(result);
			dims--;
		}
		return result;
	}
	
	private static JCExpression typeToJCTree0(Type type, JavacAST ast, boolean allowCompound, boolean allowVoid) throws TypeNotConvertibleException {
		// NB: There's such a thing as maker.Type(type), but this doesn't work very well; it screws up anonymous classes, captures, and adds an extra prefix dot for some reason too.
		//  -- so we write our own take on that here.
		
		TreeMaker maker = ast.getTreeMaker();
		
		if (type.tag == Javac.getCtcInt(TypeTags.class, "BOT")) return createJavaLangObject(ast);
		if (type.tag == Javac.getCtcInt(TypeTags.class, "VOID")) return allowVoid ? primitiveToJCTree(type.getKind(), maker) : createJavaLangObject(ast);
		if (type.isPrimitive()) return primitiveToJCTree(type.getKind(), maker);
		if (type.isErroneous()) throw new TypeNotConvertibleException("Type cannot be resolved");
		
		TypeSymbol symbol = type.asElement();
		List<Type> generics = type.getTypeArguments();
		
		JCExpression replacement = null;
		
		if (symbol == null) throw new TypeNotConvertibleException("Null or compound type");
		
		if (symbol.name.length() == 0) {
			// Anonymous inner class
			if (type instanceof ClassType) {
				List<Type> ifaces = ((ClassType)type).interfaces_field;
				Type supertype = ((ClassType)type).supertype_field;
				if (ifaces != null && ifaces.length() == 1) {
					return typeToJCTree(ifaces.get(0), ast, allowCompound, allowVoid);
				}
				if (supertype != null) return typeToJCTree(supertype, ast, allowCompound, allowVoid);
			}
			throw new TypeNotConvertibleException("Anonymous inner class");
		}
		
		if (type instanceof CapturedType || type instanceof WildcardType) {
			Type lower, upper;
			if (type instanceof WildcardType) {
				upper = ((WildcardType)type).getExtendsBound();
				lower = ((WildcardType)type).getSuperBound();
			} else {
				lower = type.getLowerBound();
				upper = type.getUpperBound();
			}
			if (allowCompound) {
				if (lower == null || lower.tag == Javac.getCtcInt(TypeTags.class, "BOT")) {
					if (upper == null || upper.toString().equals("java.lang.Object")) {
						return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
					}
					if (upper.getTypeArguments().contains(type)) {
						return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
					}
					return maker.Wildcard(maker.TypeBoundKind(BoundKind.EXTENDS), typeToJCTree(upper, ast, false, false));
				} else {
					return maker.Wildcard(maker.TypeBoundKind(BoundKind.SUPER), typeToJCTree(lower, ast, false, false));
				}
			}
			if (upper != null) {
				if (upper.getTypeArguments().contains(type)) {
					return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
				}
				return typeToJCTree(upper, ast, allowCompound, allowVoid);
			}
			
			return createJavaLangObject(ast);
		}
		
		String qName;
		if (symbol.isLocal()) {
			qName = symbol.getSimpleName().toString();
		} else if (symbol.type != null && symbol.type.getEnclosingType() != null && symbol.type.getEnclosingType().tag == TypeTags.CLASS) {
			replacement = typeToJCTree0(type.getEnclosingType(), ast, false, false);
			qName = symbol.getSimpleName().toString();
		} else {
			qName = symbol.getQualifiedName().toString();
		}
		
		if (qName.isEmpty()) throw new TypeNotConvertibleException("unknown type");
		if (qName.startsWith("<")) throw new TypeNotConvertibleException(qName);
		String[] baseNames = qName.split("\\.");
		int i = 0;
		
		if (replacement == null) {
			replacement = maker.Ident(ast.toName(baseNames[0]));
			i = 1;
		}
		for (; i < baseNames.length; i++) {
			replacement = maker.Select(replacement, ast.toName(baseNames[i]));
		}
		
		return genericsToJCTreeNodes(generics, ast, replacement);
	}
	
	private static JCExpression genericsToJCTreeNodes(List<Type> generics, JavacAST ast, JCExpression rawTypeNode) throws TypeNotConvertibleException {
		if (generics != null && !generics.isEmpty()) {
			ListBuffer<JCExpression> args = ListBuffer.lb();
			for (Type t : generics) args.append(typeToJCTree(t, ast, true, false));
			return ast.getTreeMaker().TypeApply(rawTypeNode, args.toList());
		}
		
		return rawTypeNode;
	}
	
	private static JCExpression primitiveToJCTree(TypeKind kind, TreeMaker maker) throws TypeNotConvertibleException {
		switch (kind) {
		case BYTE:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "BYTE"));
		case CHAR:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "CHAR"));
		case SHORT:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "SHORT"));
		case INT:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "INT"));
		case LONG:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "LONG"));
		case FLOAT:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "FLOAT"));
		case DOUBLE:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "DOUBLE"));
		case BOOLEAN:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "BOOLEAN"));
		case VOID:
			return maker.TypeIdent(Javac.getCtcInt(TypeTags.class, "VOID"));
		case NULL:
		case NONE:
		case OTHER:
		default:
			throw new TypeNotConvertibleException("Nulltype");
		}
	}
}
