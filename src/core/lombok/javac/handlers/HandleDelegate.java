/*
 * Copyright © 2010 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import lombok.Delegate;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.FindTypeVarScanner;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.JavacResolution.TypeNotConvertibleException;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleDelegate implements JavacAnnotationHandler<Delegate> {
	@Override public boolean isResolutionBased() {
		return true;
	}
	
	private static final List<String> METHODS_IN_OBJECT = Collections.unmodifiableList(Arrays.asList(
			"hashCode()",
			"equals(java.lang.Object)",
			"wait()",
			"wait(long)",
			"wait(long, int)",
			"notify()",
			"notifyAll()",
			"toString()",
			"getClass()",
			"clone()",
			"finalize()"));
	
	@Override public boolean handle(AnnotationValues<Delegate> annotation, JCAnnotation ast, JavacNode annotationNode) {
		if (annotationNode.up().getKind() != Kind.FIELD) {
			// As the annotation is legal on fields only, javac itself will take care of printing an error message for this.
			return false;
		}
		
		List<Object> delegateTypes = annotation.getActualExpressions("types");
		List<Object> excludeTypes = annotation.getActualExpressions("excludes");
		JavacResolution reso = new JavacResolution(annotationNode.getContext());
		List<Type> toDelegate = new ArrayList<Type>();
		List<Type> toExclude = new ArrayList<Type>();
		
		if (delegateTypes.isEmpty()) {
			Type type = ((JCVariableDecl)annotationNode.up().get()).type;
			if (type == null) reso.resolveClassMember(annotationNode.up());
			//TODO I'm fairly sure the above line (and that entire method) does effectively bupkis!
			type = ((JCVariableDecl)annotationNode.up().get()).type;
			if (type != null) toDelegate.add(type);
		} else {
			for (Object dt : delegateTypes) {
				if (dt instanceof JCFieldAccess && ((JCFieldAccess)dt).name.toString().equals("class")) {
					Type type = ((JCFieldAccess)dt).selected.type;
					if (type == null) reso.resolveClassMember(annotationNode);
					type = ((JCFieldAccess)dt).selected.type;
					if (type != null) toDelegate.add(type);
				}
			}
		}
		
		for (Object et : excludeTypes) {
			if (et instanceof JCFieldAccess && ((JCFieldAccess)et).name.toString().equals("class")) {
				Type type = ((JCFieldAccess)et).selected.type;
				if (type == null) reso.resolveClassMember(annotationNode);
				type = ((JCFieldAccess)et).selected.type;
				if (type != null) toExclude.add(type);
			}
		}
		
		List<MethodSig> signaturesToDelegate = new ArrayList<MethodSig>();
		List<MethodSig> signaturesToExclude = new ArrayList<MethodSig>();
		Set<String> banList = new HashSet<String>();
		banList.addAll(METHODS_IN_OBJECT);
		/* To exclude all methods in the class itself, try this:
		for (Symbol member : ((JCClassDecl)typeNode.get()).sym.getEnclosedElements()) {
			if (member instanceof MethodSymbol) {
				MethodSymbol method = (MethodSymbol) member;
				banList.add(printSig((ExecutableType) method.asType(), method.name, annotationNode.getTypesUtil()));
			}
		}
		 */
		for (Type t : toExclude) {
			if (t instanceof ClassType) {
				ClassType ct = (ClassType) t;
				addMethodBindings(signaturesToExclude, ct, annotationNode, banList);
			} else {
				annotationNode.addError("@Delegate can only use concrete class types, not wildcards, arrays, type variables, or primitives.");
				return false;
			}
		}
		
		for (MethodSig sig : signaturesToExclude) {
			banList.add(printSig(sig.type, sig.name, annotationNode.getTypesUtil()));
		}
		
		for (Type t : toDelegate) {
			if (t instanceof ClassType) {
				ClassType ct = (ClassType) t;
				addMethodBindings(signaturesToDelegate, ct, annotationNode, banList);
			} else {
				annotationNode.addError("@Delegate can only use concrete class types, not wildcards, arrays, type variables, or primitives.");
				return false;
			}
		}
		
		Name delegateFieldName = annotationNode.toName(annotationNode.up().getName());
		
		for (MethodSig sig : signaturesToDelegate) generateAndAdd(sig, annotationNode, delegateFieldName);
		
		return false;
	}
	
	private void generateAndAdd(MethodSig sig, JavacNode annotation, Name delegateFieldName) {
		List<JCMethodDecl> toAdd = new ArrayList<JCMethodDecl>();
		try {
			toAdd.add(createDelegateMethod(sig, annotation, delegateFieldName));
		} catch (TypeNotConvertibleException e) {
			annotation.addError("Can't create delegate method for " + sig.name + ": " + e.getMessage());
			return;
		} catch (CantMakeDelegates e) {
			annotation.addError("There's a conflict in the names of type parameters. Fix it by renaming the following type parameters of your class: " + e.conflicted);
			return;
		}
		
		for (JCMethodDecl method : toAdd) {
			JavacHandlerUtil.injectMethod(annotation.up().up(), method);
		}
	}
	
	private static class CantMakeDelegates extends Exception {
		Set<String> conflicted;
	}
	
	/**
	 * There's a rare but problematic case if a delegate method has its own type variables, and the delegated type does too, and the method uses both.
	 * If for example the delegated type has {@code <E>}, and the method has {@code <T>}, but in our class we have a {@code <T>} at the class level, then we have two different
	 * type variables both named {@code T}. We detect this situation and error out asking the programmer to rename their type variable.
	 * 
	 * @throws CantMakeDelegates If there's a conflict. Conflict list is in ex.conflicted.
	 */
	private void checkConflictOfTypeVarNames(MethodSig sig, JavacNode annotation) throws CantMakeDelegates {
		// As first step, we check if there's a conflict between the delegate method's type vars and our own class.
		
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
		
		// We might be delegating a List<T>, and we are making method <T> toArray(). A conflict is possible.
		// But only if the toArray method also uses type vars from its class, otherwise we're only shadowing,
		// which is okay as we'll add a @SuppressWarnings.
		FindTypeVarScanner scanner = new FindTypeVarScanner();
		sig.elem.asType().accept(scanner, null);
		Set<String> names = new HashSet<String>(scanner.getTypeVariables());
		names.removeAll(usedInMethodSig);
		if (!names.isEmpty()) {
			// We have a confirmed conflict. We could dig deeper as this may still be a false alarm, but its already an exceedingly rare case.
			CantMakeDelegates cmd = new CantMakeDelegates();
			cmd.conflicted = usedInMethodSig;
			throw cmd;
		}
	}
	
	private JCMethodDecl createDelegateMethod(MethodSig sig, JavacNode annotation, Name delegateFieldName) throws TypeNotConvertibleException, CantMakeDelegates {
		/* public <T, U, ...> ReturnType methodName(ParamType1 name1, ParamType2 name2, ...) throws T1, T2, ... {
		 *      (return) delegate.<T, U>methodName(name1, name2);
		 *  }
		 */
		
		checkConflictOfTypeVarNames(sig, annotation);
		
		TreeMaker maker = annotation.getTreeMaker();
		
		com.sun.tools.javac.util.List<JCAnnotation> annotations;
		if (sig.isDeprecated) {
			annotations = com.sun.tools.javac.util.List.of(maker.Annotation(
					JavacHandlerUtil.chainDots(maker, annotation, "java", "lang", "Deprecated"),
					com.sun.tools.javac.util.List.<JCExpression>nil()));
		} else {
			annotations = com.sun.tools.javac.util.List.nil();
		}
		
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, annotations);
		JCExpression returnType = JavacResolution.typeToJCTree((Type) sig.type.getReturnType(), maker, annotation.getAst(), true);
		boolean useReturn = sig.type.getReturnType().getKind() != TypeKind.VOID;
		ListBuffer<JCVariableDecl> params = sig.type.getParameterTypes().isEmpty() ? null : new ListBuffer<JCVariableDecl>();
		ListBuffer<JCExpression> args = sig.type.getParameterTypes().isEmpty() ? null : new ListBuffer<JCExpression>();
		ListBuffer<JCExpression> thrown = sig.type.getThrownTypes().isEmpty() ? null : new ListBuffer<JCExpression>();
		ListBuffer<JCTypeParameter> typeParams = sig.type.getTypeVariables().isEmpty() ? null : new ListBuffer<JCTypeParameter>();
		ListBuffer<JCExpression> typeArgs = sig.type.getTypeVariables().isEmpty() ? null : new ListBuffer<JCExpression>();
		Types types = Types.instance(annotation.getContext());
		
		for (TypeMirror param : sig.type.getTypeVariables()) {
			Name name = ((TypeVar) param).tsym.name;
			typeParams.append(maker.TypeParameter(name, maker.Types(types.getBounds((TypeVar) param))));
			typeArgs.append(maker.Ident(name));
		}
		
		for (TypeMirror ex : sig.type.getThrownTypes()) {
			thrown.append(JavacResolution.typeToJCTree((Type) ex, maker, annotation.getAst(), true));
		}
		
		int idx = 0;
		for (TypeMirror param : sig.type.getParameterTypes()) {
			JCModifiers paramMods = maker.Modifiers(Flags.FINAL);
			String[] paramNames = sig.getParameterNames();
			Name name = annotation.toName(paramNames[idx++]);
			params.append(maker.VarDef(paramMods, name, JavacResolution.typeToJCTree((Type) param, maker, annotation.getAst(), true), null));
			args.append(maker.Ident(name));
		}
		
		JCExpression delegateFieldRef = maker.Select(maker.Ident(annotation.toName("this")), delegateFieldName);
		
		JCExpression delegateCall = maker.Apply(toList(typeArgs), maker.Select(delegateFieldRef, sig.name), toList(args));
		JCStatement body = useReturn ? maker.Return(delegateCall) : maker.Exec(delegateCall);
		JCBlock bodyBlock = maker.Block(0, com.sun.tools.javac.util.List.of(body));
		
		return maker.MethodDef(mods, sig.name, returnType, toList(typeParams), toList(params), toList(thrown), bodyBlock, null);
	}
	
	private static <T> com.sun.tools.javac.util.List<T> toList(ListBuffer<T> collection) {
		return collection == null ? com.sun.tools.javac.util.List.<T>nil() : collection.toList();
	}
	
	private void addMethodBindings(List<MethodSig> signatures, ClassType ct, JavacNode node, Set<String> banList) {
		TypeSymbol tsym = ct.asElement();
		if (tsym == null) return;
		for (Symbol member : tsym.getEnclosedElements()) {
			if (member.getKind() != ElementKind.METHOD) continue;
			if (member.isStatic()) continue;
			if (member.isConstructor()) continue;
			ExecutableElement exElem = (ExecutableElement)member;
			if (!exElem.getModifiers().contains(Modifier.PUBLIC)) continue;
			ExecutableType methodType = (ExecutableType) node.getTypesUtil().asMemberOf(ct, member);
			String sig = printSig(methodType, member.name, node.getTypesUtil());
			if (!banList.add(sig)) continue; //If add returns false, it was already in there
			boolean isDeprecated = exElem.getAnnotation(Deprecated.class) != null;
			signatures.add(new MethodSig(member.name, methodType, isDeprecated, exElem));
		}
		
		if (ct.supertype_field instanceof ClassType) addMethodBindings(signatures, (ClassType) ct.supertype_field, node, banList);
		if (ct.interfaces_field != null) for (Type iface : ct.interfaces_field) {
			if (iface instanceof ClassType) addMethodBindings(signatures, (ClassType) iface, node, banList);
		}
	}
	
	private static class MethodSig {
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
	
	private static String printSig(ExecutableType method, Name name, JavacTypes types) {
		StringBuilder sb = new StringBuilder();
		sb.append(name.toString()).append("(");
		boolean first = true;
		for (TypeMirror param : method.getParameterTypes()) {
			if (!first) sb.append(", ");
			first = false;
			sb.append(typeBindingToSignature(param, types));
		}
		return sb.append(")").toString();
	}
	
	private static String typeBindingToSignature(TypeMirror binding, JavacTypes types) {
		binding = types.erasure(binding);
		return binding.toString();
	}
}
