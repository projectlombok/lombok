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
package lombok.eclipse.agent;

import static lombok.eclipse.Eclipse.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAST;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.TransformEclipseAST;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.StackRequest;
import lombok.patcher.scripts.ScriptBuilder;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class PatchDelegate {
	static void addPatches(ScriptManager sm, boolean ecj) {
		final String CLASSSCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.ClassScope";
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(CLASSSCOPE_SIG, "buildFieldsAndMethods", "void"))
				.request(StackRequest.THIS)
				.decisionMethod(new Hook(PatchDelegate.class.getName(), "handleDelegateForType", "boolean", CLASSSCOPE_SIG))
				.build());
	}
	
	public static boolean handleDelegateForType(ClassScope scope) {
		TypeDeclaration decl = scope.referenceContext;
		if (decl == null) return false;
		
		CompilationUnitDeclaration cud = null;
		EclipseAST astNode = null;
		
		if (decl.fields != null) for (FieldDeclaration field : decl.fields) {
			if (field.annotations == null) continue;
			for (Annotation ann : field.annotations) {
				if (ann.type == null) continue;
				TypeBinding tb = ann.type.resolveType(decl.initializerScope);
				if (!charArrayEquals("lombok", tb.qualifiedPackageName())) continue;
				if (!charArrayEquals("Delegate", tb.qualifiedSourceName())) continue;
				
				if (cud == null) {
					cud = scope.compilationUnitScope().referenceContext;
					astNode = TransformEclipseAST.getAST(cud);
				}
				
				List<ClassLiteralAccess> rawTypes = new ArrayList<ClassLiteralAccess>();
				for (MemberValuePair pair : ann.memberValuePairs()) {
					if (pair.name == null || charArrayEquals("value", pair.name)) {
						if (pair.value instanceof ArrayInitializer) {
							for (Expression expr : ((ArrayInitializer)pair.value).expressions) {
								if (expr instanceof ClassLiteralAccess) rawTypes.add((ClassLiteralAccess) expr);
							}
						}
						if (pair.value instanceof ClassLiteralAccess) {
							rawTypes.add((ClassLiteralAccess) pair.value);
						}
					}
				}
				
				List<MethodBinding> methodsToDelegate = new ArrayList<MethodBinding>();
				
				if (rawTypes.isEmpty()) {
					addAllMethodBindings(methodsToDelegate, field.type.resolveType(decl.initializerScope));
				} else {
					for (ClassLiteralAccess cla : rawTypes) {
						addAllMethodBindings(methodsToDelegate, cla.type.resolveType(decl.initializerScope));
					}
				}
				
				removeExistingMethods(methodsToDelegate, decl, scope);
				
				generateDelegateMethods(astNode.get(decl), methodsToDelegate, field.name, ann);
			}
		}
		
		return false;
	}
	
	private static void removeExistingMethods(List<MethodBinding> list, TypeDeclaration decl, ClassScope scope) {
		for (AbstractMethodDeclaration methodDecl : decl.methods) {
			if (!(methodDecl instanceof MethodDeclaration)) continue;
			MethodDeclaration md = (MethodDeclaration) methodDecl;
			char[] name = md.selector;
			TypeBinding[] args = md.arguments == null ? new TypeBinding[0] : new TypeBinding[md.arguments.length];
			for (int i = 0; i < args.length; i++) {
				TypeReference clone = Eclipse.copyType(md.arguments[i].type, md.arguments[i]);
				args[i] = clone.resolveType(scope).erasure();
			}
			Iterator<MethodBinding> it = list.iterator();
			methods:
			while (it.hasNext()) {
				MethodBinding mb = it.next();
				if (!Arrays.equals(mb.selector, name)) continue;
				int paramLen = mb.parameters == null ? 0 : mb.parameters.length;
				if (paramLen != args.length) continue;
				// TODO check if varargs are copied or otherwise handled correctly.
				for (int i = 0; i < paramLen; i++) {
					if (!mb.parameters[i].erasure().isEquivalentTo(args[i])) continue methods;
				}
				it.remove(); // Method already exists in this class - don't create a delegating implementation.
			}
		}
	}
	
	private static void generateDelegateMethods(EclipseNode typeNode, List<MethodBinding> methods, char[] delegate, ASTNode source) {
		CompilationUnitDeclaration top = (CompilationUnitDeclaration) typeNode.top().get();
		for (MethodBinding binding : methods) {
			MethodDeclaration method = generateDelegateMethod(delegate, binding, top.compilationResult, source);
			EclipseHandlerUtil.injectMethod(typeNode, method);
		}
	}
	
	private static boolean hasDeprecatedAnnotation(MethodBinding binding) {
		AnnotationBinding[] annotations = binding.getAnnotations();
		if (annotations != null) for (AnnotationBinding ann : annotations) {
			ReferenceBinding annType = ann.getAnnotationType();
			char[] pkg = annType.qualifiedPackageName();
			char[] src = annType.qualifiedSourceName();
			
			if (charArrayEquals("java.lang", pkg) && charArrayEquals("Deprecated", src)) return true;
		}
		
		return false;
	}
	
	private static MethodDeclaration generateDelegateMethod(char[] name, MethodBinding binding, CompilationResult compilationResult, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		
		MethodDeclaration method = new MethodDeclaration(compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.sourceStart = pS; method.sourceEnd = pE;
		method.modifiers = ClassFileConstants.AccPublic;
		
		method.returnType = Eclipse.makeType(binding.returnType, source, false);
		boolean isDeprecated = hasDeprecatedAnnotation(binding);
		
		method.selector = binding.selector;
		
		if (binding.thrownExceptions != null && binding.thrownExceptions.length > 0) {
			method.thrownExceptions = new TypeReference[binding.thrownExceptions.length];
			for (int i = 0; i < method.thrownExceptions.length; i++) {
				method.thrownExceptions[i] = Eclipse.makeType(binding.thrownExceptions[i], source, false);
			}
		}
		
		MessageSend call = new MessageSend();
		call.sourceStart = pS; call.sourceEnd = pE;
		Eclipse.setGeneratedBy(call, source);
		FieldReference fieldRef = new FieldReference(name, pos(source));
		fieldRef.receiver = new ThisReference(pS, pE);
		Eclipse.setGeneratedBy(fieldRef, source);
		Eclipse.setGeneratedBy(fieldRef.receiver, source);
		call.receiver = fieldRef;
		call.selector = binding.selector;
		
		if (binding.typeVariables != null && binding.typeVariables.length > 0) {
			method.typeParameters = new TypeParameter[binding.typeVariables.length];
			call.typeArguments = new TypeReference[binding.typeVariables.length];
			for (int i = 0; i < method.typeParameters.length; i++) {
				method.typeParameters[i] = new TypeParameter();
				method.typeParameters[i].sourceStart = pS; method.typeParameters[i].sourceEnd = pE;
				Eclipse.setGeneratedBy(method.typeParameters[i], source);
				method.typeParameters[i].name = binding.typeVariables[i].sourceName;
				call.typeArguments[i] = new SingleTypeReference(binding.typeVariables[i].sourceName, pos(source));
				Eclipse.setGeneratedBy(call.typeArguments[i], source);
				ReferenceBinding super1 = binding.typeVariables[i].superclass;
				ReferenceBinding[] super2 = binding.typeVariables[i].superInterfaces;
				if (super2 == null) super2 = new ReferenceBinding[0];
				if (super1 != null || super2.length > 0) {
					int offset = super1 == null ? 0 : 1;
					method.typeParameters[i].bounds = new TypeReference[super2.length + offset - 1];
					if (super1 != null) method.typeParameters[i].type = Eclipse.makeType(super1, source, false);
					else method.typeParameters[i].type = Eclipse.makeType(super2[0], source, false);
					int ctr = 0;
					for (int j = (super1 == null) ? 1 : 0; j < super2.length; j++) {
						method.typeParameters[i].bounds[ctr] = Eclipse.makeType(super2[j], source, false);
						method.typeParameters[i].bounds[ctr++].bits |= ASTNode.IsSuperType;
					}
				}
			}
		}
		
		if (isDeprecated) {
			QualifiedTypeReference qtr = new QualifiedTypeReference(new char[][] {
					{'j', 'a', 'v', 'a'}, {'l', 'a', 'n', 'g'}, {'D', 'e', 'p', 'r', 'e', 'c', 'a', 't', 'e', 'd'}}, poss(source, 3));
			Eclipse.setGeneratedBy(qtr, source);
			MarkerAnnotation ann = new MarkerAnnotation(qtr, pS);
			method.annotations = new Annotation[] {ann};
		}
		
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		if (binding.parameters != null && binding.parameters.length > 0) {
			method.arguments = new Argument[binding.parameters.length];
			call.arguments = new Expression[method.arguments.length];
			for (int i = 0; i < method.arguments.length; i++) {
				AbstractMethodDeclaration sourceElem = binding.sourceMethod();
				char[] argName;
				if (sourceElem == null) argName = ("arg" + i).toCharArray();
				else {
					argName = sourceElem.arguments[i].name;
				}
				method.arguments[i] = new Argument(
						argName, pos(source),
						Eclipse.makeType(binding.parameters[i], source, false),
						ClassFileConstants.AccFinal);
				Eclipse.setGeneratedBy(method.arguments[i], source);
				call.arguments[i] = new SingleNameReference(argName, pos(source));
				Eclipse.setGeneratedBy(call.arguments[i], source);
			}
		}
		
		Statement body;
		if (method.returnType instanceof SingleTypeReference && ((SingleTypeReference)method.returnType).token == TypeConstants.VOID) {
			body = call;
		} else {
			body = new ReturnStatement(call, source.sourceStart, source.sourceEnd);
			Eclipse.setGeneratedBy(body, source);
		}
		
		method.statements = new Statement[] {body};
		return method;
	}
	
	private static final class Reflection {
		public static final Method classScopeBuildMethodsMethod;
		
		static {
			Method m = null;
			try {
				m = ClassScope.class.getDeclaredMethod("buildMethods");
				m.setAccessible(true);
			} catch (Exception e) {
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
			}
			
			classScopeBuildMethodsMethod = m;
		}
	}
	
	private static void addAllMethodBindings(List<MethodBinding> list, TypeBinding binding) {
		Set<String> banList = new HashSet<String>();
		banList.addAll(METHODS_IN_OBJECT);
		addAllMethodBindings(list, binding, banList);
	}
	
	private static void addAllMethodBindings(List<MethodBinding> list, TypeBinding binding, Set<String> banList) {
		if (binding == null) return;
		if (binding instanceof MemberTypeBinding) {
			ClassScope cs = ((SourceTypeBinding)binding).scope;
			if (cs != null) {
				try {
					Reflection.classScopeBuildMethodsMethod.invoke(cs);
				} catch (Exception e) {
					// See 'Reflection' class for why we ignore this exception.
				}
			}
		}
		
		if (binding instanceof ReferenceBinding) {
			ReferenceBinding rb = (ReferenceBinding) binding;
			for (MethodBinding mb : rb.availableMethods()) {
				String sig = printSig(mb);
				if (mb.isStatic()) continue;
				if (mb.isBridge()) continue;
				if (mb.isConstructor()) continue;
				if (mb.isDefaultAbstract()) continue;
				if (!mb.isPublic()) continue;
				if (mb.isSynthetic()) continue;
				if (!banList.add(sig)) continue; // If add returns false, it was already in there.
				list.add(mb);
			}
			addAllMethodBindings(list, rb.superclass(), banList);
			ReferenceBinding[] interfaces = rb.superInterfaces();
			if (interfaces != null) {
				for (ReferenceBinding iface : interfaces) addAllMethodBindings(list, iface, banList);
			}
		}
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
	
	private static String printSig(MethodBinding binding) {
		StringBuilder signature = new StringBuilder();
		
		signature.append(binding.selector);
		signature.append("(");
		boolean first = true;
		if (binding.parameters != null) for (TypeBinding param : binding.parameters) {
			if (!first) signature.append(", ");
			first = false;
			signature.append(typeBindingToSignature(param));
		}
		signature.append(")");
		
		return signature.toString();
	}
	
	private static String typeBindingToSignature(TypeBinding binding) {
		binding = binding.erasure();
		if (binding != null && binding.isBaseType()) {
			return new String (binding.sourceName());
		} else if (binding instanceof ReferenceBinding) {
			String pkg = binding.qualifiedPackageName() == null ? "" : new String(binding.qualifiedPackageName());
			String qsn = binding.qualifiedSourceName() == null ? "" : new String(binding.qualifiedSourceName());
			return pkg.isEmpty() ? qsn : (pkg + "." + qsn);
		} else if (binding instanceof ArrayBinding) {
			StringBuilder out = new StringBuilder();
			out.append(typeBindingToSignature(binding.leafComponentType()));
			for (int i = 0; i < binding.dimensions(); i++) out.append("[]");
			return out.toString();
		}
		
		return "";
	}
	
	private static boolean charArrayEquals(String s, char[] c) {
		if (s == null) return c == null;
		if (c == null) return false;
		
		if (s.length() != c.length) return false;
		for (int i = 0; i < s.length(); i++) if (s.charAt(i) != c[i]) return false;
		return true;
		
		
	}
}
