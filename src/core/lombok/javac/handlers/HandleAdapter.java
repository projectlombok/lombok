/*
 * Copyright (C) 2009-2024 The Project Lombok Authors.
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
package lombok.javac.handlers;

import static com.sun.tools.javac.code.Flags.*;
import static lombok.core.handlers.HandlerUtil.handleExperimentalFlagUsage;
import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.experimental.Adapter;
import lombok.javac.FindTypeVarScanner;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.JavacResolution.TypeNotConvertibleException;
import lombok.javac.JavacTreeMaker;
import lombok.javac.ResolutionResetNeeded;
import lombok.permit.Permit;
import lombok.spi.Provides;

/**
 * Handles the {@code lombok.experimental.Adapter} annotation for javac.
 */
@Provides
@HandlerPriority(HandleAdapter.HANDLE_ADAPTER_PRIORITY)
@ResolutionResetNeeded
public class HandleAdapter extends JavacAnnotationHandler<Adapter> {
	
	public static final int HANDLE_ADAPTER_PRIORITY = 65536;
	private static final com.sun.tools.javac.util.List<JCExpression> EMPTY_LIST = com.sun.tools.javac.util.List.<JCExpression>nil();

	public static final java.util.Map<String, Object> DEFAULT_VALUE_MAP;
	public static final java.util.Map<String, String> DEFAULT_COLLECTIONS_METHOD;
	static {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("boolean", false);
		m.put("byte", 0);
		m.put("char", (char)0x0);
		m.put("character", (char)0x0);
		m.put("double", 0d);
		m.put("float", 0f);
		m.put("int", 0);
		m.put("integer", 0);
		m.put("long", 0L);
		m.put("short", 0);
		DEFAULT_VALUE_MAP = Collections.unmodifiableMap(m);
	}
	static {
		Map<String, String> m = new HashMap<String, String>();
		m.put("java.util.Collection", "emptyList");
		m.put("java.util.List", "emptyList");
		m.put("java.util.Set", "emptySet");
		m.put("java.util.NavigableSet", "emptyNavigableSet");
		m.put("java.util.SortedSet", "emptySortedSet");
		m.put("java.util.Map", "emptyMap");
		m.put("java.util.NavigableMap", "emptyNavigableMap");
		m.put("java.util.SortedMap", "emptySortedMap");
		m.put("java.util.Iterator", "emptyIterator");
		m.put("java.util.ListIterator", "emptyListIterator");
		DEFAULT_COLLECTIONS_METHOD = Collections.unmodifiableMap(m);
	}
	
	private static JCExpression createLiteral(JavacNode node, Object value) {
		return node.getTreeMaker().Literal(value);
	}

	private static JCExpression callCollectionsMethod(JavacNode node, String methodName) {
		return node.getTreeMaker().Apply(EMPTY_LIST, chainDots(node, "java", "util", "Collections", methodName), EMPTY_LIST);
	}
	
	@Override
	public void handle(AnnotationValues<Adapter> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.ADAPTER_FLAG_USAGE, "@Adapter");
		
		deleteAnnotationIfNeccessary(annotationNode, Adapter.class);
		
		JavacNode typeNode = annotationNode.up();
		
		if (!isClass(typeNode)) {
			annotationNode.addError("@Data is only supported on a class.");
			return;
		}
		JCClassDecl clazz = (JCClassDecl) typeNode.get();
		
		JavacResolution reso = new JavacResolution(annotationNode.getContext());
		
		GeneratorOptions options = GeneratorOptions.from(annotation.getInstance());
		List<Object> overrideTypes = annotation.getActualExpressions("of");
		List<Type> toOverride = new ArrayList<Type>();
		
		if (overrideTypes.isEmpty()) {
			com.sun.tools.javac.util.List<JCExpression> implementingInterfaces = clazz.implementing;
			for (JCTree.JCExpression item : implementingInterfaces) {
				if (item instanceof JCTree.JCIdent) {
					Type interfaceType = ((JCTree.JCIdent)item).type;
					toOverride.add(interfaceType);
				}
			}
				
		} else {
			for (Object dt : overrideTypes) {
				if (dt instanceof JCFieldAccess && ((JCFieldAccess)dt).name.toString().equals("class")) {
					Type type = ((JCFieldAccess)dt).selected.type;
					if (type == null) reso.resolveClassMember(annotationNode);
					type = ((JCFieldAccess)dt).selected.type;
					if (type != null) toOverride.add(type);
				}
			}
		}
		
		List<MethodSig> signaturesToOverride = new ArrayList<MethodSig>();
		Set<String> banList = new HashSet<String>();
		
		// Add already implemented methods to ban list
		for (Symbol m : ((JCClassDecl)typeNode.get()).sym.getEnclosedElements()) {
			if (m instanceof MethodSymbol) {
				banList.add(printSig((ExecutableType) m.asType(), m.name, annotationNode.getTypesUtil()));
			}
		}
		
		for (Type t : toOverride) {
			Type unannotatedType = Unannotated.unannotatedType(t);
			if (unannotatedType instanceof ClassType) {
				ClassType ct = (ClassType) unannotatedType;
				addMethodBindings(signaturesToOverride, ct, annotationNode.getTypesUtil(), banList);
			} else {
				annotationNode.addError("@Adapter can only use interfaces");
				return;
			}
		}
		
		for (MethodSig sig : signaturesToOverride) generateAndAdd(sig, annotationNode, options);
	}
	
	public void generateAndAdd(MethodSig sig, JavacNode annotation, GeneratorOptions options) {
		List<JCMethodDecl> toAdd = new ArrayList<JCMethodDecl>();
		try {
			toAdd.add(createOverriddenMethod(sig, annotation, options));
		} catch (TypeNotConvertibleException e) {
			annotation.addError("Can't create overridden method for " + sig.name + ": " + e.getMessage());
			return;
		} catch (CantMakeOverrides e) {
			annotation.addError("There's a conflict in the names of type parameters. Fix it by renaming the following type parameters of your class: " + e.conflicted);
			return;
		}
		
		for (JCMethodDecl method : toAdd) {
			injectMethod(annotation.up(), method);
		}
	}
	
	public static class CantMakeOverrides extends Exception {
		Set<String> conflicted;
	}
	
	/**
	 * There's a rare but problematic case if an overridden method has its own type variables, and the overriding type does too, and the method uses both.
	 * If for example the overriding type has {@code <E>}, and the method has {@code <T>}, but in our class we have a {@code <T>} at the class level, then we have two different
	 * type variables both named {@code T}. We detect this situation and error out asking the programmer to rename their type variable.
	 * 
	 * @throws CantMakeOverrides If there's a conflict. Conflict list is in ex.conflicted.
	 */
	public void checkConflictOfTypeVarNames(MethodSig sig, JavacNode annotation) throws CantMakeOverrides {
		// As first step, we check if there's a conflict between the overridden method's type vars and our own class.
		
		if (sig.elem.getTypeParameters().isEmpty()) return;
		Set<String> usedInOurType = new HashSet<String>();
		
		JavacNode enclosingType = annotation;
		while (enclosingType != null) {
			if (enclosingType.getKind() == Kind.TYPE) {
				List<JCTypeParameter> typarams = ((JCClassDecl)enclosingType.get()).typarams;
				if (typarams != null) for (JCTypeParameter param : typarams) {
					if (param.name != null) usedInOurType.add(param.name.toString());
				}
			}
			enclosingType = enclosingType.up();
		}
		
		Set<String> usedInMethodSig = new HashSet<String>();
		for (TypeParameterElement param : sig.elem.getTypeParameters()) {
			usedInMethodSig.add(param.getSimpleName().toString());
		}
		
		usedInMethodSig.retainAll(usedInOurType);
		if (usedInMethodSig.isEmpty()) return;
		
		// We might be overriding a List<T>, and we are making method <T> toArray(). A conflict is possible.
		// But only if the toArray method also uses type vars from its class, otherwise we're only shadowing,
		// which is okay as we'll add a @SuppressWarnings.
		FindTypeVarScanner scanner = new FindTypeVarScanner();
		sig.elem.asType().accept(scanner, null);
		Set<String> names = new HashSet<String>(scanner.getTypeVariables());
		names.removeAll(usedInMethodSig);
		if (!names.isEmpty()) {
			// We have a confirmed conflict. We could dig deeper as this may still be a false alarm, but its already an exceedingly rare case.
			CantMakeOverrides cmd = new CantMakeOverrides();
			cmd.conflicted = usedInMethodSig;
			throw cmd;
		}
	}
	
	public JCMethodDecl createOverriddenMethod(MethodSig sig, JavacNode annotation, GeneratorOptions options) throws TypeNotConvertibleException, CantMakeOverrides {
		/* 
		 * public <T, U, ...> ReturnType methodName(ParamType1 name1, ParamType2 name2, ...) throws T1, T2, ... {
		 *      return null; // or return default value based upon the return type
		 * }
		 *  
		 *  or
		 * public <T, U, ...> ReturnType methodName(ParamType1 name1, ParamType2 name2, ...) throws T1, T2, ... {
		 *      throw new UnsupportedOperationException(); // or the exception specified by the annotation attribute
		 * }
		 */
		
		checkConflictOfTypeVarNames(sig, annotation);
		
		JavacTreeMaker maker = annotation.getTreeMaker();
		
		com.sun.tools.javac.util.List<JCAnnotation> annotations = com.sun.tools.javac.util.List.of(maker.Annotation(
			genJavaLangTypeRef(annotation, "Override"),
			EMPTY_LIST));
		if (sig.isDeprecated) {
			annotations = annotations.append(maker.Annotation(
					genJavaLangTypeRef(annotation, "Deprecated"),
					EMPTY_LIST));
		}
		
		JCModifiers mods = maker.Modifiers(PUBLIC, annotations);
		JCExpression returnType = JavacResolution.typeToJCTree((Type) sig.type.getReturnType(), annotation.getAst(), true);
		boolean useReturn = sig.type.getReturnType().getKind() != TypeKind.VOID;
		boolean throwError = !options.silentMode;
		ListBuffer<JCVariableDecl> params = sig.type.getParameterTypes().isEmpty() ? null : new ListBuffer<JCVariableDecl>();
		ListBuffer<JCExpression> args = sig.type.getParameterTypes().isEmpty() ? null : new ListBuffer<JCExpression>();
		ListBuffer<JCExpression> thrown = sig.type.getThrownTypes().isEmpty() ? null : new ListBuffer<JCExpression>();
		ListBuffer<JCTypeParameter> typeParams = sig.type.getTypeVariables().isEmpty() ? null : new ListBuffer<JCTypeParameter>();
		ListBuffer<JCExpression> typeArgs = sig.type.getTypeVariables().isEmpty() ? null : new ListBuffer<JCExpression>();
		Types types = Types.instance(annotation.getContext());
		
		for (TypeMirror param : sig.type.getTypeVariables()) {
			Name name = ((TypeVar) param).tsym.name;
			
			ListBuffer<JCExpression> bounds = new ListBuffer<JCExpression>();
			for (Type type : types.getBounds((TypeVar) param)) {
				bounds.append(JavacResolution.typeToJCTree(type, annotation.getAst(), true));
			}
			
			typeParams.append(maker.TypeParameter(name, bounds.toList()));
			typeArgs.append(maker.Ident(name));
		}
		
		if (!options.suppressThrows) {
			for (TypeMirror ex : sig.type.getThrownTypes()) {
				thrown.append(JavacResolution.typeToJCTree((Type) ex, annotation.getAst(), true));
			}
		}
		
		int idx = 0;
		String[] paramNames = sig.getParameterNames();
		boolean varargs = sig.elem.isVarArgs();
		for (TypeMirror param : sig.type.getParameterTypes()) {
			long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, annotation.getContext());
			JCModifiers paramMods = maker.Modifiers(flags);
			Name name = annotation.toName(paramNames[idx++]);
			if (varargs && idx == paramNames.length) {
				paramMods.flags |= VARARGS;
			}
			params.append(maker.VarDef(paramMods, name, JavacResolution.typeToJCTree((Type) param, annotation.getAst(), true), null));
			args.append(maker.Ident(name));
		}
		
		com.sun.tools.javac.util.List<JCStatement> bodyExpressions = com.sun.tools.javac.util.List.nil();
		if (throwError) {
			JCExpression exType = genTypeRef(annotation, options.exceptionClass.getName());
			com.sun.tools.javac.util.List<JCExpression> parameters = EMPTY_LIST;
			if (options.exceptionMsg != null && !options.exceptionMsg.isEmpty()) {
				JCLiteral message = maker.Literal(options.exceptionMsg);
				parameters = parameters.append(message);
			}
			JCExpression exception = maker.NewClass(null, EMPTY_LIST, exType, 
				parameters, null);
			JCStatement body = maker.Throw(exception);
			bodyExpressions = bodyExpressions.append(body);
		} else if (useReturn) {
			// create null or default value to be returned
			JCExpression returnValue = findExpressionForReturnType(sig, annotation);
			JCStatement body = maker.Return(returnValue);
			bodyExpressions = bodyExpressions.append(body);
		}
		JCBlock bodyBlock = maker.Block(0, bodyExpressions);
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, sig.name, returnType, toList(typeParams), toList(params), toList(thrown), bodyBlock, null), annotation);
	}

	private JCExpression findExpressionForReturnType(MethodSig sig, JavacNode annotation) {
		String typeString = sig.type.getReturnType().toString()
			.replace("java.lang.", "")
			.replaceFirst("<.*>", "");
		Object defaultValue = DEFAULT_VALUE_MAP.get(typeString.toLowerCase());
		if (defaultValue != null) {
			return createLiteral(annotation, defaultValue);
		}
		if ("string".equals(typeString)) {
			return annotation.getTreeMaker().Literal(CTC_BOT, null);
		}
		// some array -> create an empty array
		if (typeString.endsWith("[]")) {
			return annotation.getTreeMaker().NewArray(
				chainDotsString(annotation.up(), sig.type.getReturnType().toString().replace("[]", "")),
				com.sun.tools.javac.util.List.<JCExpression>of(annotation.getTreeMaker().Literal(CTC_INT, 0)), null);
		}
		// some collection interface -> use Collections.empty... 
		String collectionsMethod = DEFAULT_COLLECTIONS_METHOD.get(typeString);
		if (collectionsMethod != null) {
			return callCollectionsMethod(annotation, collectionsMethod);
		}
		// Optional<T> -> Optional.empty()
		if ("java.util.Optional".equals(typeString)) {
			return annotation.getTreeMaker().Apply(EMPTY_LIST, chainDots(annotation, "java", "util", "Optional", "empty"), EMPTY_LIST);
		}
		// if it's a non-abstract class that extends AbstractMap or AbstractCollection, then create a new instance of it
		if (isMapOrCollectionClass(sig.type.getReturnType())) {
			return annotation.getTreeMaker().NewClass(null, EMPTY_LIST, 
				chainDotsString(annotation, sig.type.getReturnType().toString().replaceFirst("<.*>", "")),
				EMPTY_LIST, null);
		}
		return annotation.getTreeMaker().Literal(CTC_BOT, null);
	}
	
	private boolean isMapOrCollectionClass(TypeMirror typeMirror) {
		// check if given type is non-abstract class that extends AbstractMap or AbstractCollection
		ClassSymbol csym = getClassSymbol(typeMirror);
		if (csym != null && csym.getModifiers().contains(Modifier.ABSTRACT))
			return false;
		while (csym != null) {
			String className = csym.getQualifiedName().toString();
			if ("java.util.AbstractCollection".equals(className) || "java.util.AbstractMap".equals(className)) {
				return true;
			}
			csym = getClassSymbol(csym.getSuperclass());
		}
		return false;
	}

	private ClassSymbol getClassSymbol(TypeMirror typeMirror) {
		if (!(typeMirror instanceof ClassType))
			return null;
		TypeSymbol tsym = ((ClassType) typeMirror).tsym;
		ClassSymbol csym = (ClassSymbol)tsym;
		return csym;
	}

	public static <T> com.sun.tools.javac.util.List<T> toList(ListBuffer<T> collection) {
		return collection == null ? com.sun.tools.javac.util.List.<T>nil() : collection.toList();
	}
	
	public void addMethodBindings(List<MethodSig> signatures, ClassType ct, JavacTypes types, Set<String> banList)  {
		if ("java.lang.Object".equals(ct.toString())) return;
		TypeSymbol tsym = ct.asElement();
		if (tsym == null) return;
		
		for (Symbol member : tsym.getEnclosedElements()) {
			if (member.getKind() != ElementKind.METHOD) continue;
			if (member.isStatic()) continue;
			if (member.isConstructor()) continue;
			MethodSymbol method = (MethodSymbol)member;
			if (method.isDefault()) continue;
			if (!method.getModifiers().contains(Modifier.ABSTRACT)) continue;
			ExecutableElement exElem = (ExecutableElement)member;
			if (!exElem.getModifiers().contains(Modifier.PUBLIC)) continue;
			ExecutableType methodType = (ExecutableType) types.asMemberOf(ct, member);
			String sig = printSig(methodType, member.name, types);
			if (!banList.add(sig)) continue; //If add returns false, it was already in there
			boolean isDeprecated = (member.flags() & DEPRECATED) != 0;
			signatures.add(new MethodSig(member.name, methodType, isDeprecated, exElem));
		}

		for (Type type : types.directSupertypes(ct)) {
			if (type instanceof ClassType) {
				addMethodBindings(signatures, (ClassType) type, types, banList);
			}
		}
	}
	
	public static class MethodSig {
		final Name name;
		final ExecutableType type;
		final boolean isDeprecated;
		final ExecutableElement elem;
		
		MethodSig(Name name, ExecutableType type, boolean isDeprecated, ExecutableElement elem) {
			this.name = name;
			this.type = type;
			this.isDeprecated = isDeprecated;
			this.elem = elem;
		}
		
		String[] getParameterNames() {
			List<? extends VariableElement> paramList = elem.getParameters();
			String[] paramNames = new String[paramList.size()];
			for (int i = 0; i < paramNames.length; i++) {
				paramNames[i] = paramList.get(i).getSimpleName().toString();
			}
			return paramNames;
		}
		
		@Override public String toString() {
			return (isDeprecated ? "@Deprecated " : "") + name + " " + type;
		}
	}
	
	public static String printSig(ExecutableType method, Name name, JavacTypes types) {
		StringBuilder sb = new StringBuilder();
		sb.append(name.toString()).append("(");
		String sep = "";
		for (TypeMirror param : method.getParameterTypes()) {
			sb.append(sep);
			sb.append(typeBindingToSignature(param, types));
			sep = ", ";
		}
		return sb.append(")").toString();
	}
	
	public static String typeBindingToSignature(TypeMirror binding, JavacTypes types) {
		binding = types.erasure(binding);
		return binding.toString();
	}
	
	private static class Unannotated {
		private static final Method unannotated;
		
		static {
			Method m = null;
			try {
				m = Permit.getMethod(Type.class, "unannotatedType");
			} catch (Exception e) {/* ignore */}
			unannotated = m;
		}
		
		static Type unannotatedType(Type t) {
			if (unannotated == null) return t;
			try {
				return (Type) Permit.invoke(unannotated, t);
			} catch (Exception e) {
				return t;
			}
		}
	}
	
	private static class GeneratorOptions {
		private Class<? extends RuntimeException> exceptionClass;
		private String exceptionMsg;
		private boolean suppressThrows;
		private boolean silentMode;
		public static GeneratorOptions from(Adapter annotation) {
			GeneratorOptions ret = new GeneratorOptions();
			ret.exceptionClass = annotation.throwException();
			ret.exceptionMsg = annotation.message();
			ret.suppressThrows = annotation.suppressThrows();
			ret.silentMode = annotation.silent();
			return ret;
		}
	}
}
