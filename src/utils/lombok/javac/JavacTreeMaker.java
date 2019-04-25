/*
 * Copyright (C) 2013-2018 The Project Lombok Authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCSkip;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import lombok.permit.Permit;

public class JavacTreeMaker {
	private final TreeMaker tm;
	
	public JavacTreeMaker(TreeMaker tm) {
		this.tm = tm;
	}
	
	public TreeMaker getUnderlyingTreeMaker() {
		return tm;
	}
	
	public JavacTreeMaker at(int pos) {
		tm.at(pos);
		return this;
	}
	
	private static class MethodId<J> {
		private final Class<?> owner;
		private final String name;
		private final Class<J> returnType;
		private final Class<?>[] paramTypes;
		
		MethodId(Class<?> owner, String name, Class<J> returnType, Class<?>... types) {
			this.owner = owner;
			this.name = name;
			this.paramTypes = types;
			this.returnType = returnType;
		}
		
		@Override public String toString() {
			StringBuilder out = new StringBuilder();
			out.append(returnType.getName()).append(" ").append(owner.getName()).append(".").append(name).append("(");
			boolean f = true;
			for (Class<?> p : paramTypes) {
				if (f) f = false;
				else out.append(", ");
				out.append(p.getName());
			}
			return out.append(")").toString();
		}
	}
	
	private static class SchroedingerType {
		final Object value;
		
		private SchroedingerType(Object value) {
			this.value = value;
		}
		
		@Override public int hashCode() {
			return value == null ? -1 : value.hashCode();
		}
		
		@Override public boolean equals(Object obj) {
			if (obj instanceof SchroedingerType) {
				Object other = ((SchroedingerType) obj).value;
				return value == null ? other == null : value.equals(other);
			}
			return false;
		}
		
		static Object getFieldCached(ConcurrentMap<String, Object> cache, String className, String fieldName) {
			Object value = cache.get(fieldName);
			if (value != null) return value;
			try {
				value = Permit.getField(Class.forName(className), fieldName).get(null);
			} catch (NoSuchFieldException e) {
				throw Javac.sneakyThrow(e);
			} catch (IllegalAccessException e) {
				throw Javac.sneakyThrow(e);
			} catch (ClassNotFoundException e) {
				throw Javac.sneakyThrow(e);
			}
			
			cache.putIfAbsent(fieldName, value);
			return value;
		}
		
		private static Field NOSUCHFIELDEX_MARKER;
		static {
			try {
				NOSUCHFIELDEX_MARKER = Permit.getField(SchroedingerType.class, "NOSUCHFIELDEX_MARKER");
			} catch (NoSuchFieldException e) {
				throw Javac.sneakyThrow(e);
			}
		}
		
		static Object getFieldCached(ConcurrentMap<Class<?>, Field> cache, Object ref, String fieldName) throws NoSuchFieldException {
			Class<?> c = ref.getClass();
			Field field = cache.get(c);
			if (field == null) {
				try {
					field = Permit.getField(c, fieldName);
				} catch (NoSuchFieldException e) {
					cache.putIfAbsent(c, NOSUCHFIELDEX_MARKER);
					throw Javac.sneakyThrow(e);
				}
				Permit.setAccessible(field);
				Field old = cache.putIfAbsent(c, field);
				if (old != null) field = old;
			}
			
			if (field == NOSUCHFIELDEX_MARKER) throw new NoSuchFieldException(fieldName);
			try {
				return field.get(ref);
			} catch (IllegalAccessException e) {
				throw Javac.sneakyThrow(e);
			}
		}
	}
	
	public static class TypeTag extends SchroedingerType {
		private static final ConcurrentMap<String, Object> TYPE_TAG_CACHE = new ConcurrentHashMap<String, Object>();
		private static final ConcurrentMap<Class<?>, Field> FIELD_CACHE = new ConcurrentHashMap<Class<?>, Field>();
		private static final Method TYPE_TYPETAG_METHOD;
		
		static {
			Method m = null;
			try {
				m = Permit.getMethod(Type.class, "getTag");
			} catch (NoSuchMethodException e) {}
			TYPE_TYPETAG_METHOD = m;
		}
		
		private TypeTag(Object value) {
			super(value);
		}
		
		public static TypeTag typeTag(JCTree o) {
			try {
				return new TypeTag(getFieldCached(FIELD_CACHE, o, "typetag"));
			} catch (NoSuchFieldException e) {
				throw Javac.sneakyThrow(e);
			}
		}
		
		public static TypeTag typeTag(Type t) {
			if (t == null) return Javac.CTC_VOID;
			try {
				return new TypeTag(getFieldCached(FIELD_CACHE, t, "tag"));
			} catch (NoSuchFieldException e) {
				if (TYPE_TYPETAG_METHOD == null) throw new IllegalStateException("Type " + t.getClass() + " has neither 'tag' nor getTag()");
				try {
					return new TypeTag(TYPE_TYPETAG_METHOD.invoke(t));
				} catch (IllegalAccessException ex) {
					throw Javac.sneakyThrow(ex);
				} catch (InvocationTargetException ex) {
					throw Javac.sneakyThrow(ex.getCause());
				}
			}
		}
		
		public static TypeTag typeTag(String identifier) {
			return new TypeTag(getFieldCached(TYPE_TAG_CACHE, Javac.getJavaCompilerVersion() < 8 ? "com.sun.tools.javac.code.TypeTags" : "com.sun.tools.javac.code.TypeTag", identifier));
		}
	}
	
	public static class TreeTag extends SchroedingerType {
		private static final ConcurrentMap<String, Object> TREE_TAG_CACHE = new ConcurrentHashMap<String, Object>();
		private static final Field TAG_FIELD;
		private static final Method TAG_METHOD;
		private static final MethodId<Integer> OP_PREC = MethodId(TreeInfo.class, "opPrec", int.class, TreeTag.class);
		
		static {
			Method m = null;
			try {
				m = Permit.getMethod(JCTree.class, "getTag");
			} catch (NoSuchMethodException e) {}
			
			if (m != null) {
				TAG_FIELD = null;
				TAG_METHOD = m;
			} else {
				Field f = null;
				try {
					f = Permit.getField(JCTree.class, "tag");
				} catch (NoSuchFieldException e) {}
				TAG_FIELD = f;
				TAG_METHOD = null;
			}
		}
		
		private TreeTag(Object value) {
			super(value);
		}
		
		public static TreeTag treeTag(JCTree o) {
			try {
				if (TAG_METHOD != null) return new TreeTag(TAG_METHOD.invoke(o));
				else return new TreeTag(TAG_FIELD.get(o));
			} catch (InvocationTargetException e) {
				throw Javac.sneakyThrow(e.getCause());
			} catch (IllegalAccessException e) {
				throw Javac.sneakyThrow(e);
			}
		}
		
		public static TreeTag treeTag(String identifier) {
			return new TreeTag(getFieldCached(TREE_TAG_CACHE, Javac.getJavaCompilerVersion() < 8 ? "com.sun.tools.javac.tree.JCTree" : "com.sun.tools.javac.tree.JCTree$Tag", identifier));
		}
		
		public int getOperatorPrecedenceLevel() {
			return invokeAny(null, OP_PREC, value);
		}
		
		public boolean isPrefixUnaryOp() {
			return Javac.CTC_NEG.equals(this) || Javac.CTC_POS.equals(this) || Javac.CTC_NOT.equals(this) || Javac.CTC_COMPL.equals(this) || Javac.CTC_PREDEC.equals(this) || Javac.CTC_PREINC.equals(this);
		}
	}
	
	static <J> MethodId<J> MethodId(Class<?> owner, String name, Class<J> returnType, Class<?>... types) {
		return new MethodId<J>(owner, name, returnType, types);
	}
	
	/**
	 * Creates a new method ID based on the name of the method to invoke, the return type of that method, and the types of the parameters.
	 * 
	 * A method matches if the return type matches, and for each parameter the following holds:
	 * 
	 * Either (A) the type listed here is the same as, or a subtype of, the type of the method in javac's TreeMaker, or
	 *  (B) the type listed here is a subtype of SchroedingerType.
	 */
	static <J> MethodId<J> MethodId(String name, Class<J> returnType, Class<?>... types) {
		return new MethodId<J>(TreeMaker.class, name, returnType, types);
	}
	
	/**
	 * Creates a new method ID based on the name of a method in this class, assuming the name of the method to invoke in TreeMaker has the same name,
	 * the same return type, and the same parameters (under the same rules as the other MethodId method).
	 */
	static <J> MethodId<J> MethodId(String name) {
		for (Method m : JavacTreeMaker.class.getDeclaredMethods()) {
			if (m.getName().equals(name)) {
				@SuppressWarnings("unchecked") Class<J> r = (Class<J>) m.getReturnType();
				Class<?>[] p = m.getParameterTypes();
				return new MethodId<J>(TreeMaker.class, name, r, p);
			}
		}
		
		throw new InternalError("Not found: " + name);
	}
	
	private static final Object METHOD_NOT_FOUND = new Object[0];
	private static final Object METHOD_MULTIPLE_FOUND = new Object[0];
	private static final ConcurrentHashMap<MethodId<?>, Object> METHOD_CACHE = new ConcurrentHashMap<MethodId<?>, Object>();
	private <J> J invoke(MethodId<J> m, Object... args) {
		return invokeAny(tm, m, args);
	}
	
	@SuppressWarnings("unchecked") private static <J> J invokeAny(Object owner, MethodId<J> m, Object... args) {
		Method method = getFromCache(m);
		try {
			if (m.returnType.isPrimitive()) {
				Object res = method.invoke(owner, args);
				String sn = res.getClass().getSimpleName().toLowerCase();
				if (!sn.startsWith(m.returnType.getSimpleName())) throw new ClassCastException(res.getClass() + " to " + m.returnType);
				return (J) res;
			}
			return m.returnType.cast(method.invoke(owner, args));
		} catch (InvocationTargetException e) {
			throw Javac.sneakyThrow(e.getCause());
		} catch (IllegalAccessException e) {
			throw Javac.sneakyThrow(e);
		} catch (IllegalArgumentException e) {
			System.err.println(method);
			throw Javac.sneakyThrow(e);
		}
	}
	
	private static boolean tryResolve(MethodId<?> m) {
		Object s = METHOD_CACHE.get(m);
		if (s == null) s = addToCache(m);
		if (s instanceof Method) return true;
		return false;
	}
	
	private static Method getFromCache(MethodId<?> m) {
		Object s = METHOD_CACHE.get(m);
		if (s == null) s = addToCache(m);
		if (s == METHOD_MULTIPLE_FOUND) throw new IllegalStateException("Lombok TreeMaker frontend issue: multiple matches when looking for method: " + m);
		if (s == METHOD_NOT_FOUND) throw new IllegalStateException("Lombok TreeMaker frontend issue: no match when looking for method: " + m);
		return (Method) s;
	}
	
	private static Object addToCache(MethodId<?> m) {
		Method found = null;
		
		outer:
		for (Method method : m.owner.getDeclaredMethods()) {
			if (!m.name.equals(method.getName())) continue;
			Class<?>[] t = method.getParameterTypes();
			if (t.length != m.paramTypes.length) continue;
			for (int i = 0; i < t.length; i++) {
				if (Symbol.class.isAssignableFrom(t[i])) continue outer;
				if (!SchroedingerType.class.isAssignableFrom(m.paramTypes[i])) {
					if (t[i].isPrimitive()) {
						if (t[i] != m.paramTypes[i]) continue outer;
					} else {
						if (!t[i].isAssignableFrom(m.paramTypes[i])) continue outer;
					}
				}
			}
			if (found == null) found = method;
			else {
				METHOD_CACHE.putIfAbsent(m, METHOD_MULTIPLE_FOUND);
				return METHOD_MULTIPLE_FOUND;
			}
		}
		if (found == null) {
			METHOD_CACHE.putIfAbsent(m, METHOD_NOT_FOUND);
			return METHOD_NOT_FOUND;
		}
		Permit.setAccessible(found);
		Object marker = METHOD_CACHE.putIfAbsent(m, found);
		if (marker == null) return found;
		return marker;
	}
	
	//javac versions: 6-8
	private static final MethodId<JCCompilationUnit> TopLevel = MethodId("TopLevel");
	public JCCompilationUnit TopLevel(List<JCAnnotation> packageAnnotations, JCExpression pid, List<JCTree> defs) {
		return invoke(TopLevel, packageAnnotations, pid, defs);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCImport> Import = MethodId("Import");
	public JCImport Import(JCTree qualid, boolean staticImport) {
		return invoke(Import, qualid, staticImport);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCClassDecl> ClassDef = MethodId("ClassDef");
	public JCClassDecl ClassDef(JCModifiers mods, Name name, List<JCTypeParameter> typarams, JCExpression extending, List<JCExpression> implementing, List<JCTree> defs) {
		return invoke(ClassDef, mods, name, typarams, extending, implementing, defs);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCMethodDecl> MethodDef = MethodId("MethodDef", JCMethodDecl.class, JCModifiers.class, Name.class, JCExpression.class, List.class, List.class, List.class, JCBlock.class, JCExpression.class);
	public JCMethodDecl MethodDef(JCModifiers mods, Name name, JCExpression resType, List<JCTypeParameter> typarams, List<JCVariableDecl> params, List<JCExpression> thrown, JCBlock body, JCExpression defaultValue) {
		return invoke(MethodDef, mods, name, resType, typarams, params, thrown, body, defaultValue);
	}
	
	//javac versions: 8
	private static final MethodId<JCMethodDecl> MethodDefWithRecvParam = MethodId("MethodDef", JCMethodDecl.class, JCModifiers.class, Name.class, JCExpression.class, List.class, JCVariableDecl.class, List.class, List.class, JCBlock.class, JCExpression.class);
	public JCMethodDecl MethodDef(JCModifiers mods, Name name, JCExpression resType, List<JCTypeParameter> typarams, JCVariableDecl recvparam, List<JCVariableDecl> params, List<JCExpression> thrown, JCBlock body, JCExpression defaultValue) {
		return invoke(MethodDefWithRecvParam, mods, name, resType, recvparam, typarams, params, thrown, body, defaultValue);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCVariableDecl> VarDef = MethodId("VarDef");
	public JCVariableDecl VarDef(JCModifiers mods, Name name, JCExpression vartype, JCExpression init) {
		JCVariableDecl varDef = invoke(VarDef, mods, name, vartype, init);
		// We use 'position of the type is -1' as indicator in delombok that the original node was written using JDK10's 'var' feature, because javac desugars 'var' to the real type and doesn't leave any markers other than the
		// node position to indicate that it did so. Unfortunately, that means vardecls we generate look like 'var' to delombok. Adjust the position to avoid this.
		if (varDef.vartype != null && varDef.vartype.pos == -1) varDef.vartype.pos = 0;
		return varDef;
	}
	
	//javac versions: 8
	private static final MethodId<JCVariableDecl> ReceiverVarDef = MethodId("ReceiverVarDef");
	public JCVariableDecl ReceiverVarDef(JCModifiers mods, JCExpression name, JCExpression vartype) {
		return invoke(ReceiverVarDef, mods, name, vartype);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCSkip> Skip = MethodId("Skip");
	public JCSkip Skip() {
		return invoke(Skip);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCBlock> Block = MethodId("Block");
	public JCBlock Block(long flags, List<JCStatement> stats) {
		return invoke(Block, flags, stats);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCDoWhileLoop> DoLoop = MethodId("DoLoop");
	public JCDoWhileLoop DoLoop(JCStatement body, JCExpression cond) {
		return invoke(DoLoop, body, cond);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCWhileLoop> WhileLoop = MethodId("WhileLoop");
	public JCWhileLoop WhileLoop(JCExpression cond, JCStatement body) {
		return invoke(WhileLoop, cond, body);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCForLoop> ForLoop = MethodId("ForLoop");
	public JCForLoop ForLoop(List<JCStatement> init, JCExpression cond, List<JCExpressionStatement> step, JCStatement body) {
		return invoke(ForLoop, init, cond, step, body);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCEnhancedForLoop> ForeachLoop = MethodId("ForeachLoop");
	public JCEnhancedForLoop ForeachLoop(JCVariableDecl var, JCExpression expr, JCStatement body) {
		return invoke(ForeachLoop, var, expr, body);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCLabeledStatement> Labelled = MethodId("Labelled");
	public JCLabeledStatement Labelled(Name label, JCStatement body) {
		return invoke(Labelled, label, body);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCSwitch> Switch = MethodId("Switch");
	public JCSwitch Switch(JCExpression selector, List<JCCase> cases) {
		return invoke(Switch, selector, cases);
	}
	
	//javac versions: 6-11
	private static final MethodId<JCCase> Case11 = MethodId("Case", JCCase.class, JCExpression.class, com.sun.tools.javac.util.List.class);
	//javac version: 12+
	public static class Case12 {
		private static final Class<?> CASE_KIND_CLASS = classForName(TreeMaker.class, "com.sun.source.tree.CaseTree$CaseKind");
		static final MethodId<JCCase> Case12 = MethodId("Case", JCCase.class, CASE_KIND_CLASS, com.sun.tools.javac.util.List.class, com.sun.tools.javac.util.List.class, JCTree.class);
		static final Object CASE_KIND_STATEMENT = CASE_KIND_CLASS.getEnumConstants()[0];
	}
	
	static Class<?> classForName(Class<?> context, String name) {
		try {
			return context.getClassLoader().loadClass(name);
		} catch (ClassNotFoundException e) {
			Error x = new NoClassDefFoundError(e.getMessage());
			x.setStackTrace(e.getStackTrace());
			throw x;
		}
	}
	
	public JCCase Case(JCExpression pat, List<JCStatement> stats) {
		if (tryResolve(Case11)) return invoke(Case11, pat, stats);
		return invoke(Case12.Case12, Case12.CASE_KIND_STATEMENT, pat == null ? com.sun.tools.javac.util.List.nil() : com.sun.tools.javac.util.List.of(pat), stats, null);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCSynchronized> Synchronized = MethodId("Synchronized");
	public JCSynchronized Synchronized(JCExpression lock, JCBlock body) {
		return invoke(Synchronized, lock, body);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCTry> Try = MethodId("Try", JCTry.class, JCBlock.class, List.class, JCBlock.class);
	public JCTry Try(JCBlock body, List<JCCatch> catchers, JCBlock finalizer) {
		return invoke(Try, body, catchers, finalizer);
	}
	
	//javac versions: 7-8
	private static final MethodId<JCTry> TryWithResources = MethodId("Try", JCTry.class, List.class, JCBlock.class, List.class, JCBlock.class);
	public JCTry Try(List<JCTree> resources, JCBlock body, List<JCCatch> catchers, JCBlock finalizer) {
		return invoke(TryWithResources, resources, body, catchers, finalizer);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCCatch> Catch = MethodId("Catch");
	public JCCatch Catch(JCVariableDecl param, JCBlock body) {
		return invoke(Catch, param, body);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCConditional> Conditional = MethodId("Conditional");
	public JCConditional Conditional(JCExpression cond, JCExpression thenpart, JCExpression elsepart) {
		return invoke(Conditional, cond, thenpart, elsepart);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCIf> If = MethodId("If");
	public JCIf If(JCExpression cond, JCStatement thenpart, JCStatement elsepart) {
		return invoke(If, cond, thenpart, elsepart);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCExpressionStatement> Exec = MethodId("Exec");
	public JCExpressionStatement Exec(JCExpression expr) {
		return invoke(Exec, expr);
	}
	
	//javac version: 6-11
	private static final MethodId<JCBreak> Break11 = MethodId("Break", JCBreak.class, Name.class);
	//javac version: 12+
	private static final MethodId<JCBreak> Break12 = MethodId("Break", JCBreak.class, JCExpression.class);
	
	public JCBreak Break(Name label) {
		if (tryResolve(Break11)) return invoke(Break11, label);
		return invoke(Break12, label != null ? Ident(label) : null);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCContinue> Continue = MethodId("Continue");
	public JCContinue Continue(Name label) {
		return invoke(Continue, label);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCReturn> Return = MethodId("Return");
	public JCReturn Return(JCExpression expr) {
		return invoke(Return, expr);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCThrow> Throw = MethodId("Throw");
	public JCThrow Throw(JCExpression expr) {
		return invoke(Throw, expr);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCAssert> Assert = MethodId("Assert");
	public JCAssert Assert(JCExpression cond, JCExpression detail) {
		return invoke(Assert, cond, detail);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCMethodInvocation> Apply = MethodId("Apply");
	public JCMethodInvocation Apply(List<JCExpression> typeargs, JCExpression fn, List<JCExpression> args) {
		return invoke(Apply, typeargs, fn, args);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCNewClass> NewClass = MethodId("NewClass");
	public JCNewClass NewClass(JCExpression encl, List<JCExpression> typeargs, JCExpression clazz, List<JCExpression> args, JCClassDecl def) {
		return invoke(NewClass, encl, typeargs, clazz, args, def);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCNewArray> NewArray = MethodId("NewArray");
	public JCNewArray NewArray(JCExpression elemtype, List<JCExpression> dims, List<JCExpression> elems) {
		return invoke(NewArray, elemtype, dims, elems);
	}
	
	//javac versions: 8
//	private static final MethodId<JCLambda> Lambda = MethodId("Lambda");
//	public JCLambda Lambda(List<JCVariableDecl> params, JCTree body) {
//		return invoke(Lambda, params, body);
//	}
	
	//javac versions: 6-8
	private static final MethodId<JCParens> Parens = MethodId("Parens");
	public JCParens Parens(JCExpression expr) {
		return invoke(Parens, expr);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCAssign> Assign = MethodId("Assign");
	public JCAssign Assign(JCExpression lhs, JCExpression rhs) {
		return invoke(Assign, lhs, rhs);
	}
	
	//javac versions: 6-8
	//opcode = [6-7] int  [8] JCTree.Tag
	private static final MethodId<JCAssignOp> Assignop = MethodId("Assignop");
	public JCAssignOp Assignop(TreeTag opcode, JCTree lhs, JCTree rhs) {
		return invoke(Assignop, opcode.value, lhs, rhs);
	}
	
	//javac versions: 6-8
	//opcode = [6-7] int  [8] JCTree.Tag
	private static final MethodId<JCUnary> Unary = MethodId("Unary");
	public JCUnary Unary(TreeTag opcode, JCExpression arg) {
		return invoke(Unary, opcode.value, arg);
	}
	
	//javac versions: 6-8
	//opcode = [6-7] int  [8] JCTree.Tag
	private static final MethodId<JCBinary> Binary = MethodId("Binary");
	public JCBinary Binary(TreeTag opcode, JCExpression lhs, JCExpression rhs) {
		return invoke(Binary, opcode.value, lhs, rhs);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCTypeCast> TypeCast = MethodId("TypeCast");
	public JCTypeCast TypeCast(JCTree expr, JCExpression type) {
		return invoke(TypeCast, expr, type);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCInstanceOf> TypeTest = MethodId("TypeTest");
	public JCInstanceOf TypeTest(JCExpression expr, JCTree clazz) {
		return invoke(TypeTest, expr, clazz);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCArrayAccess> Indexed = MethodId("Indexed");
	public JCArrayAccess Indexed(JCExpression indexed, JCExpression index) {
		return invoke(Indexed, indexed, index);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCFieldAccess> Select = MethodId("Select");
	public JCFieldAccess Select(JCExpression selected, Name selector) {
		return invoke(Select, selected, selector);
	}
	
	//javac versions: 8
//	private static final MethodId<JCMemberReference> Reference = MethodId("Reference");
//	public JCMemberReference Reference(JCMemberReference.ReferenceMode mode, Name name, JCExpression expr, List<JCExpression> typeargs) {
//		return invoke(Reference, mode, name, expr, typeargs);
//	}
	
	//javac versions: 6-8
	private static final MethodId<JCIdent> Ident = MethodId("Ident", JCIdent.class, Name.class);
	public JCIdent Ident(Name idname) {
		return invoke(Ident, idname);
	}
	
	//javac versions: 6-8
	//tag = [6-7] int  [8] TypeTag
	private static final MethodId<JCLiteral> Literal = MethodId("Literal", JCLiteral.class, TypeTag.class, Object.class);
	public JCLiteral Literal(TypeTag tag, Object value) {
		return invoke(Literal, tag.value, value);
	}
	
	//javac versions: 6-8
	//typetag = [6-7] int  [8] TypeTag
	private static final MethodId<JCPrimitiveTypeTree> TypeIdent = MethodId("TypeIdent");
	public JCPrimitiveTypeTree TypeIdent(TypeTag typetag) {
		return invoke(TypeIdent, typetag.value);
	}
	//javac versions: 6-8
	private static final MethodId<JCArrayTypeTree> TypeArray = MethodId("TypeArray");
	public JCArrayTypeTree TypeArray(JCExpression elemtype) {
		return invoke(TypeArray, elemtype);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCTypeApply> TypeApply = MethodId("TypeApply");
	public JCTypeApply TypeApply(JCExpression clazz, List<JCExpression> arguments) {
		return invoke(TypeApply, clazz, arguments);
	}
	
	//javac versions: 7-8
//	private static final MethodId<JCTypeUnion> TypeUnion = MethodId("TypeUnion");
//	public JCTypeUnion TypeUnion(List<JCExpression> components) {
//		return invoke(TypeUnion, compoonents);
//	}

	//javac versions: 8
//	private static final MethodId<JCTypeIntersection> TypeIntersection = MethodId("TypeIntersection");
//	public JCTypeIntersection TypeIntersection(List<JCExpression> components) {
//		return invoke(TypeIntersection, components);
//	}
	
	//javac versions: 6-8
	private static final MethodId<JCTypeParameter> TypeParameter = MethodId("TypeParameter", JCTypeParameter.class, Name.class, List.class);
	public JCTypeParameter TypeParameter(Name name, List<JCExpression> bounds) {
		return invoke(TypeParameter, name, bounds);
	}
	
	//javac versions: 8
	private static final MethodId<JCTypeParameter> TypeParameterWithAnnos = MethodId("TypeParameter", JCTypeParameter.class, Name.class, List.class, List.class);
	public JCTypeParameter TypeParameter(Name name, List<JCExpression> bounds, List<JCAnnotation> annos) {
		return invoke(TypeParameterWithAnnos, name, bounds, annos);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCWildcard> Wildcard = MethodId("Wildcard");
	public JCWildcard Wildcard(TypeBoundKind kind, JCTree type) {
		return invoke(Wildcard, kind, type);
	}
	
	//javac versions: 6-8
	private static final MethodId<TypeBoundKind> TypeBoundKind = MethodId("TypeBoundKind");
	public TypeBoundKind TypeBoundKind(BoundKind kind) {
		return invoke(TypeBoundKind, kind);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCAnnotation> Annotation = MethodId("Annotation", JCAnnotation.class, JCTree.class, List.class);
	public JCAnnotation Annotation(JCTree annotationType, List<JCExpression> args) {
		return invoke(Annotation, annotationType, args);
	}
	
	//javac versions: 8
	private static final MethodId<JCAnnotation> TypeAnnotation = MethodId("TypeAnnotation", JCAnnotation.class, JCTree.class, List.class);
	public JCAnnotation TypeAnnotation(JCTree annotationType, List<JCExpression> args) {
		return invoke(TypeAnnotation, annotationType, args);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCModifiers> ModifiersWithAnnotations = MethodId("Modifiers", JCModifiers.class, long.class, List.class);
	public JCModifiers Modifiers(long flags, List<JCAnnotation> annotations) {
		return invoke(ModifiersWithAnnotations, flags, annotations);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCModifiers> Modifiers = MethodId("Modifiers", JCModifiers.class, long.class);
	public JCModifiers Modifiers(long flags) {
		return invoke(Modifiers, flags);
	}
	
	//javac versions: 8
//	private static final MethodId<JCAnnotatedType> AnnotatedType = MethodId("AnnotatedType");
//	public JCAnnotatedType AnnotatedType(List<JCAnnotation> annotations, JCExpression underlyingType) {
//		return invoke(AnnotatedType, annotations, underlyingType);
//	}
	
	//javac versions: 6-8
	private static final MethodId<JCErroneous> Erroneous = MethodId("Erroneous", JCErroneous.class);
	public JCErroneous Erroneous() {
		return invoke(Erroneous);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCErroneous> ErroneousWithErrs = MethodId("Erroneous", JCErroneous.class, List.class);
	public JCErroneous Erroneous(List<? extends JCTree> errs) {
		return invoke(ErroneousWithErrs, errs);
	}
	
	//javac versions: 6-8
	private static final MethodId<LetExpr> LetExpr = MethodId("LetExpr", LetExpr.class, List.class, JCTree.class);
	public LetExpr LetExpr(List<JCVariableDecl> defs, JCTree expr) {
		return invoke(LetExpr, defs, expr);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCClassDecl> AnonymousClassDef = MethodId("AnonymousClassDef");
	public JCClassDecl AnonymousClassDef(JCModifiers mods, List<JCTree> defs) {
		return invoke(AnonymousClassDef, mods, defs);
	}
	
	//javac versions: 6-8
	private static final MethodId<LetExpr> LetExprSingle = MethodId("LetExpr", LetExpr.class, JCVariableDecl.class, JCTree.class);
	public LetExpr LetExpr(JCVariableDecl def, JCTree expr) {
		return invoke(LetExprSingle, def, expr);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCIdent> IdentVarDecl = MethodId("Ident", JCIdent.class, JCVariableDecl.class);
	public JCExpression Ident(JCVariableDecl param) {
		return invoke(IdentVarDecl, param);
	}
	
	//javac versions: 6-8
	private static final MethodId<List<JCExpression>> Idents = MethodId("Idents");
	public List<JCExpression> Idents(List<JCVariableDecl> params) {
		return invoke(Idents, params);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCMethodInvocation> App2 = MethodId("App", JCMethodInvocation.class, JCExpression.class, List.class);
	public JCMethodInvocation App(JCExpression meth, List<JCExpression> args) {
		return invoke(App2, meth, args);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCMethodInvocation> App1 = MethodId("App", JCMethodInvocation.class, JCExpression.class);
	public JCMethodInvocation App(JCExpression meth) {
		return invoke(App1, meth);
	}
	
	//javac versions: 6-8
	private static final MethodId<List<JCAnnotation>> Annotations = MethodId("Annotations");
	public List<JCAnnotation> Annotations(List<Attribute.Compound> attributes) {
		return invoke(Annotations, attributes);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCLiteral> LiteralWithValue = MethodId("Literal", JCLiteral.class, Object.class);
	public JCLiteral Literal(Object value) {
		return invoke(LiteralWithValue, value);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCAnnotation> AnnotationWithAttributeOnly = MethodId("Annotation", JCAnnotation.class, Attribute.class);
	public JCAnnotation Annotation(Attribute a) {
		return invoke(AnnotationWithAttributeOnly, a);
	}
	
	//javac versions: 8
	private static final MethodId<JCAnnotation> TypeAnnotationWithAttributeOnly = MethodId("TypeAnnotation", JCAnnotation.class, Attribute.class);
	public JCAnnotation TypeAnnotation(Attribute a) {
		return invoke(TypeAnnotationWithAttributeOnly, a);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCStatement> Call = MethodId("Call");
	public JCStatement Call(JCExpression apply) {
		return invoke(Call, apply);
	}
	
	//javac versions: 6-8
	private static final MethodId<JCExpression> Type = MethodId("Type");
	public JCExpression Type(Type type) {
		return invoke(Type, type);
	}
}