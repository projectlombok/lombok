/*
 * Copyright (C) 2011-2025 The Project Lombok Authors.
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

import static lombok.javac.Javac.*;
import static lombok.javac.JavacTreeMaker.TypeTag.typeTag;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.CapturedType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.ArgumentAttr;
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
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;

import lombok.core.debug.AssertionLogger;
import lombok.permit.Permit;

public class JavacResolution {
	private final Context context;
	private final Attr attr;
	private final CompilerMessageSuppressor messageSuppressor;
	
	private static final Method isLocal;
	
	static {
		Method local = Permit.permissiveGetMethod(TypeSymbol.class, "isLocal");
		if (local == null) {
			local = Permit.permissiveGetMethod(TypeSymbol.class, "isDirectlyOrIndirectlyLocal");
		}
		isLocal = local;
	}
	
	public JavacResolution(Context context) {
		this.context = context;
		attr = Attr.instance(context);
		messageSuppressor = new CompilerMessageSuppressor(context);
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
			if (tree.sym != null) env = enter.getClassEnv(tree.sym);
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
		
		messageSuppressor.disableLoggers();
		try {
			EnvFinder finder = new EnvFinder(node.getContext());
			while (!stack.isEmpty()) stack.pop().accept(finder);
			
			TreeMirrorMaker mirrorMaker = new TreeMirrorMaker(node.getTreeMaker(), node.getContext());
			JCTree copy = mirrorMaker.copy(finder.copyAt());
			Log log = Log.instance(node.getContext());
			JavaFileObject oldFileObject = log.useSource(((JCCompilationUnit) node.top().get()).getSourceFile());
			try {
				memberEnterAndAttribute(copy, finder.get(), node.getContext());
				return mirrorMaker.getOriginalToCopyMap();
			} finally {
				log.useSource(oldFileObject);
			}
		} finally {
			messageSuppressor.enableLoggers();
		}
	}
	
	private static Field memberEnterDotEnv;
	
	private static Field getMemberEnterDotEnv() {
		if (memberEnterDotEnv != null) return memberEnterDotEnv;
		try {
			return memberEnterDotEnv = Permit.getField(MemberEnter.class, "env");
		} catch (NoSuchFieldException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Env<AttrContext> getEnvOfMemberEnter(MemberEnter memberEnter) {
		Field f = getMemberEnterDotEnv();
		try {
			return (Env<AttrContext>) f.get(memberEnter);
		} catch (Exception e) {
			return null;
		}
	}
	
	private static void setEnvOfMemberEnter(MemberEnter memberEnter, Env<AttrContext> env) {
		Field f = getMemberEnterDotEnv();
		try {
			f.set(memberEnter, env);
		} catch (Exception e) {
			return;
		}
	}
	
	private void memberEnterAndAttribute(JCTree copy, Env<AttrContext> env, Context context) {
		MemberEnter memberEnter = MemberEnter.instance(context);
		Env<AttrContext> oldEnv = getEnvOfMemberEnter(memberEnter);
		setEnvOfMemberEnter(memberEnter, env);
		try {
			copy.accept(memberEnter);
		} catch (Exception ignore) {
			// intentionally ignored; usually even if this step fails, val will work (but not for val in method local inner classes and anonymous inner classes).
			AssertionLogger.assertLog("member enter failed.", ignore);
		} finally {
			setEnvOfMemberEnter(memberEnter, oldEnv);
		}
		attrib(copy, env);
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
		
		messageSuppressor.disableLoggers();
		try {
			EnvFinder finder = new EnvFinder(node.getContext());
			while (!stack.isEmpty()) stack.pop().accept(finder);
			
			attrib(node.get(), finder.get());
		} finally {
			messageSuppressor.enableLoggers();
		}
	}
	
	private void attrib(JCTree tree, Env<AttrContext> env) {
		try {
			if (env.enclClass.type == null) {
				if (env.enclClass.sym != null) {
					env.enclClass.type = env.enclClass.sym.type;
				}
			}
			if (env.enclClass.type == null) {
				env.enclClass.type = Type.noType;
			}
		} catch (Throwable ignore) {
			// This addresses issue #1553 which involves JDK9; if it doesn't exist, we probably don't need to set it.
		}
		
		Map<?,?> cache = null;
		try {
			cache = ArgumentAttrReflect.enableTempCache(context);
			
			if (tree instanceof JCBlock) attr.attribStat(tree, env);
			else if (tree instanceof JCMethodDecl) attr.attribStat(((JCMethodDecl) tree).body, env);
			else if (tree instanceof JCVariableDecl) attr.attribStat(tree, env);
			else throw new IllegalStateException("Called with something that isn't a block, method decl, or variable decl");
		} finally {
			ArgumentAttrReflect.restoreCache(cache, context);
		}
		
	}
	
	public static class TypeNotConvertibleException extends Exception {
		public TypeNotConvertibleException(String msg) {
			super(msg);
		}
	}
	
	private static class ReflectiveAccess {
		private static Method UPPER_BOUND;
		private static Throwable initError;
		
		static {
			Method upperBound = null;
			try {
				upperBound = Permit.getMethod(Types.class, "upperBound", Type.class);
			} catch (Throwable e) {
				initError = e;
			}
			
			if (upperBound == null) try {
				upperBound = Permit.getMethod(Types.class, "wildUpperBound", Type.class);
			} catch (Throwable e) {
				initError = e;
			}
			
			UPPER_BOUND = upperBound;
		}
		
		public static Type Types_upperBound(Types types, Type type) {
			return (Type) Permit.invokeSneaky(initError, UPPER_BOUND, types, type);
		}
	}
	
	/**
	 * ArgumentAttr was added in Java 9 and caches some method arguments. Lombok should cleanup its changes after resolution.
	 */
	private static class ArgumentAttrReflect {
		private static Field ARGUMENT_TYPE_CACHE;
		
		static {
			if (Javac.getJavaCompilerVersion() >= 9) {
				try {
					ARGUMENT_TYPE_CACHE = Permit.getField(ArgumentAttr.class, "argumentTypeCache");
				} catch (Exception ignore) {}
			}
		}
		
		public static Map<?, ?> enableTempCache(Context context) {
			if (ARGUMENT_TYPE_CACHE == null) return null;
			
			ArgumentAttr argumentAttr = ArgumentAttr.instance(context);
			try {
				Map<?, ?> cache = (Map<?, ?>) Permit.get(ARGUMENT_TYPE_CACHE, argumentAttr);
				Permit.set(ARGUMENT_TYPE_CACHE, argumentAttr, new LinkedHashMap<Object, Object>(cache));
				return cache;
			} catch (Exception ignore) { }
			
			return null;
		}
		
		public static void restoreCache(Map<?, ?> cache, Context context) {
			if (ARGUMENT_TYPE_CACHE == null) return;
			
			ArgumentAttr argumentAttr = ArgumentAttr.instance(context);
			try {
				Permit.set(ARGUMENT_TYPE_CACHE, argumentAttr, cache);
			} catch (Exception ignore) { }
		}
	}
	
	public static Type ifTypeIsIterableToComponent(Type type, JavacAST ast) {
		if (type == null) return null;
		Types types = Types.instance(ast.getContext());
		Symtab syms = Symtab.instance(ast.getContext());
		Type boundType = ReflectiveAccess.Types_upperBound(types, type);
//		Type boundType = types.upperBound(type);
		Type elemTypeIfArray = types.elemtype(boundType);
		if (elemTypeIfArray != null) return elemTypeIfArray;
		
		Type base = types.asSuper(boundType, syms.iterableType.tsym);
		if (base == null) return syms.objectType;
		
		List<Type> iterableParams = base.allparams();
		return iterableParams.isEmpty() ? syms.objectType : ReflectiveAccess.Types_upperBound(types, iterableParams.head);
	}
	
	public static JCExpression typeToJCTree(Type type, JavacAST ast, boolean allowVoid) throws TypeNotConvertibleException {
		return typeToJCTree(type, ast, false, allowVoid, false);
	}
	
	public static JCExpression createJavaLangObject(JavacAST ast) {
		JavacTreeMaker maker = ast.getTreeMaker();
		JCExpression out = maker.Ident(ast.toName("java"));
		out = maker.Select(out, ast.toName("lang"));
		out = maker.Select(out, ast.toName("Object"));
		return out;
	}
	
	private static JCExpression typeToJCTree(Type type, JavacAST ast, boolean allowCompound, boolean allowVoid, boolean allowCapture) throws TypeNotConvertibleException {
		int dims = 0;
		Type type0 = type;
		while (type0 instanceof ArrayType) {
			dims++;
			type0 = ((ArrayType) type0).elemtype;
		}
		
		JCExpression result = typeToJCTree0(type0, ast, allowCompound, allowVoid, allowCapture);
		while (dims > 0) {
			result = ast.getTreeMaker().TypeArray(result);
			dims--;
		}
		return result;
	}
	
	private static Iterable<? extends Type> concat(final Type t, final Collection<? extends Type> ts) {
		if (t == null) return ts;
		
		return new Iterable<Type>() {
			@Override public Iterator<Type> iterator() {
				return new Iterator<Type>() {
					private boolean first = true;
					private Iterator<? extends Type> wrap = ts == null ? null : ts.iterator();
					@Override public boolean hasNext() {
						if (first) return true;
						if (wrap == null) return false;
						return wrap.hasNext();
					}
					
					@Override public Type next() {
						if (first) {
							first = false;
							return t;
						}
						if (wrap == null) throw new NoSuchElementException();
						return wrap.next();
					}
					
					@Override public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	private static int compare(Name a, Name b) {
		return a.toString().compareTo(b.toString());
	}
	
	private static boolean isLocalType(TypeSymbol symbol) {
		try {
			return (Boolean) Permit.invoke(isLocal, symbol);
		}
		catch (Exception e) {
			return false;
		}
	}
	
	private static JCExpression typeToJCTree0(Type type, JavacAST ast, boolean allowCompound, boolean allowVoid, boolean allowCapture) throws TypeNotConvertibleException {
		// NB: There's such a thing as maker.Type(type), but this doesn't work very well; it screws up anonymous classes, captures, and adds an extra prefix dot for some reason too.
		//  -- so we write our own take on that here.
		
		JavacTreeMaker maker = ast.getTreeMaker();
		
		if (CTC_BOT.equals(typeTag(type))) return createJavaLangObject(ast);
		if (CTC_VOID.equals(typeTag(type))) return allowVoid ? primitiveToJCTree(type.getKind(), maker) : createJavaLangObject(ast);
		if (type.isPrimitive()) return primitiveToJCTree(type.getKind(), maker);
		if (type.isErroneous()) throw new TypeNotConvertibleException("Type cannot be resolved");
		
		TypeSymbol symbol = type.asElement();
		List<Type> generics = type.getTypeArguments();
		
		JCExpression replacement = null;
		
		if (symbol == null) throw new TypeNotConvertibleException("Null or compound type");
		
		if (symbol.name.length() == 0) {
			// Anonymous inner class
			if (type instanceof ClassType) {
				Type winner = null;
				int winLevel = 0; // 100 = array, 50 = class, 20 = typevar, 15 = wildcard, 10 = interface, 1 = Object.
				Type supertype = ((ClassType) type).supertype_field;
				List<Type> ifaces = ((ClassType) type).interfaces_field;
				for (Type t : concat(supertype, ifaces)) {
					int level = 0;
					if (t instanceof ArrayType) level = 100;
					else if (t instanceof TypeVar) level = 20;
					else if (t instanceof WildcardType) level = 15;
					else if (t.isInterface()) level = 10;
					else if (isObject(t)) level = 1;
					else if (t instanceof ClassType) level = 50;
					else level = 5;
					
					if (winLevel > level) continue;
					if (winLevel < level) {
						winner = t;
						winLevel = level;
						continue;
					}
					if (compare(winner.tsym.getQualifiedName(), t.tsym.getQualifiedName()) > 0) winner = t;
				}
				if (winner == null) return createJavaLangObject(ast);
				return typeToJCTree(winner, ast, allowCompound, allowVoid, allowCapture);
			}
			throw new TypeNotConvertibleException("Anonymous inner class");
		}
		
		if (type instanceof WildcardType || type instanceof CapturedType) {
			Type lower, upper;
			if (type instanceof WildcardType) {
				upper = ((WildcardType) type).getExtendsBound();
				lower = ((WildcardType) type).getSuperBound();
			} else {
				lower = type.getLowerBound();
				upper = type.getUpperBound();
				if (allowCapture) {
					BoundKind bk = ((CapturedType) type).wildcard.kind;
					if (bk == BoundKind.UNBOUND) {
						return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
					} else if (bk == BoundKind.EXTENDS) {
						lower = null;
						upper = ((CapturedType) type).wildcard.type;
					} else if (bk == BoundKind.SUPER) {
						lower = ((CapturedType) type).wildcard.type;
						upper = null;
					}
				}
			}
			if (allowCompound) {
				if (lower == null || CTC_BOT.equals(typeTag(lower))) {
					if (upper == null || upper.toString().equals("java.lang.Object")) {
						return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
					}
					if (upper.getTypeArguments().contains(type)) {
						return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
					}
					JCExpression bound = typeToJCTree(upper, ast, false, false, true);
					if (bound instanceof JCWildcard) return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
					return maker.Wildcard(maker.TypeBoundKind(BoundKind.EXTENDS), bound);
				} else {
					JCExpression bound = typeToJCTree(lower, ast, false, false, true);
					if (bound instanceof JCWildcard) return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
					return maker.Wildcard(maker.TypeBoundKind(BoundKind.SUPER), bound);
				}
			}
			if (upper != null) {
				if (upper.getTypeArguments().contains(type)) {
					return maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null);
				}
				return typeToJCTree(upper, ast, allowCompound, allowVoid, true);
			}
			
			return createJavaLangObject(ast);
		}
		
		String qName;
		if (isLocalType(symbol)) {
			qName = symbol.getSimpleName().toString();
		} else if (symbol.type != null && symbol.type.getEnclosingType() != null && typeTag(symbol.type.getEnclosingType()).equals(typeTag("CLASS"))) {
			replacement = typeToJCTree0(type.getEnclosingType(), ast, false, false, false);
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
	
	private static boolean isObject(Type supertype) {
		return supertype.tsym.toString().equals("java.lang.Object");
	}
	
	private static JCExpression genericsToJCTreeNodes(List<Type> generics, JavacAST ast, JCExpression rawTypeNode) throws TypeNotConvertibleException {
		if (generics != null && !generics.isEmpty()) {
			ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
			for (Type t : generics) args.append(typeToJCTree(t, ast, true, false, true));
			return ast.getTreeMaker().TypeApply(rawTypeNode, args.toList());
		}
		
		return rawTypeNode;
	}
	
	private static JCExpression primitiveToJCTree(TypeKind kind, JavacTreeMaker maker) throws TypeNotConvertibleException {
		switch (kind) {
		case BYTE:
			return maker.TypeIdent(CTC_BYTE);
		case CHAR:
			return maker.TypeIdent( CTC_CHAR);
		case SHORT:
			return maker.TypeIdent(CTC_SHORT);
		case INT:
			return maker.TypeIdent(CTC_INT);
		case LONG:
			return maker.TypeIdent(CTC_LONG);
		case FLOAT:
			return maker.TypeIdent(CTC_FLOAT);
		case DOUBLE:
			return maker.TypeIdent(CTC_DOUBLE);
		case BOOLEAN:
			return maker.TypeIdent(CTC_BOOLEAN);
		case VOID:
			return maker.TypeIdent(CTC_VOID);
		case NULL:
		case NONE:
		case OTHER:
		default:
			throw new TypeNotConvertibleException("Nulltype");
		}
	}
	
	public static boolean platformHasTargetTyping() {
		return Javac.getJavaCompilerVersion() >= 8;
	}
}
