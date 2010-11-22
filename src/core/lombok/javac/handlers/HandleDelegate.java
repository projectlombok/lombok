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
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import lombok.Delegate;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.JavacResolution.TypeNotConvertibleException;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
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
		if (annotationNode.up().getKind() != Kind.FIELD) return false; // TODO error
		
		List<Object> delegateTypes = annotation.getActualExpressions("value");
		JavacResolution reso = new JavacResolution(annotationNode.getContext());
		List<Type> resolved = new ArrayList<Type>();
		
		if (delegateTypes.isEmpty()) {
			Type type = ((JCVariableDecl)annotationNode.up().get()).type;
			if (type == null) reso.resolveClassMember(annotationNode.up());
			//TODO I'm fairly sure the above line (and that entire method) does effectively bupkis!
			type = ((JCVariableDecl)annotationNode.up().get()).type;
			if (type != null) resolved.add(type);
		} else {
			for (Object dt : delegateTypes) {
				if (dt instanceof JCFieldAccess && ((JCFieldAccess)dt).name.toString().equals("class")) {
					Type type = ((JCFieldAccess)dt).selected.type;
					if (type == null) reso.resolveClassMember(annotationNode);
					type = ((JCFieldAccess)dt).selected.type;
					if (type != null) resolved.add(type);
				}
			}
		}
		
		List<MethodSig> signatures = new ArrayList<MethodSig>();
		Set<String> banList = new HashSet<String>();
		for (Type t : resolved) {
			banList.addAll(METHODS_IN_OBJECT);
			JavacNode typeNode = annotationNode.up().up();
			for (Symbol member : ((JCClassDecl)typeNode.get()).sym.getEnclosedElements()) {
				if (member instanceof MethodSymbol) {
					MethodSymbol method = (MethodSymbol) member;
					banList.add(printSig((ExecutableType) method.asType(), method.name, annotationNode.getTypesUtil()));
				}
			}
			if (t instanceof ClassType) {
				ClassType ct = (ClassType) t;
				addMethodBindings(signatures, ct, annotationNode, banList);
			} else {
				annotationNode.addError("@Delegate can only use concrete class types, not wildcards, arrays, type variables, or primitives.");
				return false;
			}
		}
		
		Name delegateFieldName = annotationNode.toName(annotationNode.up().getName());
		
		for (MethodSig sig : signatures) generateAndAdd(sig, annotationNode, delegateFieldName);
		
		return false;
	}
	
	private void generateAndAdd(MethodSig sig, JavacNode annotation, Name delegateFieldName) {
		try {
			JCMethodDecl method = createDelegateMethod(sig, annotation, delegateFieldName);
			JavacHandlerUtil.injectMethod(annotation.up().up(), method);
		} catch (TypeNotConvertibleException e) {
			annotation.addError("Can't create delegate method for " + sig.name + ": " + e.getMessage());
		}
	}
	
	private JCMethodDecl createDelegateMethod(MethodSig sig, JavacNode annotation, Name delegateFieldName) throws TypeNotConvertibleException {
		/** public <P1, P2, ...> ReturnType methodName(ParamType1 $p1, ParamType2 $p2, ...) throws T1, T2, ... {
		 *      (return) delegate.<P1, P2>methodName($p1, $p2);
		 *  }
		 */
		
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
		
		int nameCounter = 0;
		for (TypeMirror param : sig.type.getTypeVariables()) {
			Name name = annotation.toName("P" + (nameCounter++));
			typeParams.append(maker.TypeParameter(name, maker.Types(types.getBounds((TypeVar) param))));
			typeArgs.append(maker.Ident(name));
		}
		
		for (TypeMirror ex : sig.type.getThrownTypes()) {
			thrown.append(JavacResolution.typeToJCTree((Type) ex, maker, annotation.getAst(), true));
		}
		
		nameCounter = 0;
		for (TypeMirror param : sig.type.getParameterTypes()) {
			Name name = annotation.toName("$a" + (nameCounter++));
			JCModifiers paramMods = maker.Modifiers(Flags.FINAL);
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
			signatures.add(new MethodSig(member.name, methodType, isDeprecated));
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
		
		MethodSig(Name name, ExecutableType type, boolean isDeprecated) {
			this.name = name;
			this.type = type;
			this.isDeprecated = isDeprecated;
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
