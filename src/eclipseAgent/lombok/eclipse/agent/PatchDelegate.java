package lombok.eclipse.agent;

import static lombok.eclipse.Eclipse.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.eclipse.Eclipse;
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
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
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
		
		if (decl.fields != null) for (FieldDeclaration field : decl.fields) {
			if (field.annotations == null) continue;
			for (Annotation ann : field.annotations) {
				if (ann.type == null) continue;
				TypeBinding tb = ann.type.resolveType(decl.initializerScope);
				if (!charArrayEquals("lombok", tb.qualifiedPackageName())) continue;
				if (!charArrayEquals("Delegate", tb.qualifiedSourceName())) continue;
				
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
				
				generateDelegateMethods(decl, methodsToDelegate, field.name, ann);
			}
		}
		
		return false;
	}
	
	private static void generateDelegateMethods(TypeDeclaration type, List<MethodBinding> methods, char[] delegate, ASTNode source) {
		for (MethodBinding binding : methods) {
			MethodDeclaration method = generateDelegateMethod(delegate, binding, type.compilationResult, source);
			if (type.methods == null) {
				type.methods = new AbstractMethodDeclaration[1];
				type.methods[0] = method;
			} else {
				int insertionPoint;
				for (insertionPoint = 0; insertionPoint < type.methods.length; insertionPoint++) {
					AbstractMethodDeclaration current = type.methods[insertionPoint];
					if (current instanceof Clinit) continue;
					if (Eclipse.isGenerated(current)) continue;
					break;
				}
				AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[type.methods.length + 1];
				System.arraycopy(type.methods, 0, newArray, 0, insertionPoint);
				if (insertionPoint <= type.methods.length) {
					System.arraycopy(type.methods, insertionPoint, newArray, insertionPoint + 1, type.methods.length - insertionPoint);
				}
				
				newArray[insertionPoint] = method;
				type.methods = newArray;
			}
		}
	}
	
	private static MethodDeclaration generateDelegateMethod(char[] name, MethodBinding binding, CompilationResult compilationResult, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		
		MethodDeclaration method = new MethodDeclaration(compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.sourceStart = pS; method.sourceEnd = pE;
		method.modifiers = ClassFileConstants.AccPublic;
		method.returnType = Eclipse.makeType(binding.returnType, source, false);
		method.annotations = EclipseHandlerUtil.createSuppressWarningsAll(source, null);
		if (binding.parameters != null && binding.parameters.length > 0) {
			method.arguments = new Argument[binding.parameters.length];
			for (int i = 0; i < method.arguments.length; i++) {
				String argName = "$p" + i;
				method.arguments[i] = new Argument(
						argName.toCharArray(), pos(source),
						Eclipse.makeType(binding.parameters[i], source, false),
						ClassFileConstants.AccFinal);
				Eclipse.setGeneratedBy(method.arguments[i], source);
			}
		}
		
		method.selector = binding.selector;
		if (binding.thrownExceptions != null && binding.thrownExceptions.length > 0) {
			method.thrownExceptions = new TypeReference[binding.thrownExceptions.length];
			for (int i = 0; i < method.thrownExceptions.length; i++) {
				method.thrownExceptions[i] = Eclipse.makeType(binding.thrownExceptions[i], source, false);
			}
		}
		
		if (binding.typeVariables != null && binding.typeVariables.length > 0) {
			method.typeParameters = new TypeParameter[binding.typeVariables.length];
			for (int i = 0; i < method.typeParameters.length; i++) {
				method.typeParameters[i] = new TypeParameter();
				method.typeParameters[i].sourceStart = pS; method.typeParameters[i].sourceEnd = pE;
				Eclipse.setGeneratedBy(method.typeParameters[i], source);
				method.typeParameters[i].name = binding.typeVariables[i].sourceName;
				ReferenceBinding super1 = binding.typeVariables[i].superclass;
				System.out.println("super1: " + super1);
				ReferenceBinding[] super2 = binding.typeVariables[i].superInterfaces;
				if (super2 == null) super2 = new ReferenceBinding[0];
				System.out.println("super2: " + Arrays.asList(super2));
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
		
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		FieldReference fieldRef = new FieldReference(name, pos(source));
		fieldRef.receiver = new ThisReference(pS, pE);
		Eclipse.setGeneratedBy(fieldRef, source);
		Eclipse.setGeneratedBy(fieldRef.receiver, source);
		MessageSend call = new MessageSend();
		call.sourceStart = pS; call.sourceEnd = pE;
		Eclipse.setGeneratedBy(call, source);
		call.receiver = fieldRef;
		call.selector = binding.selector;
		if (method.arguments != null) {
			call.arguments = new Expression[method.arguments.length];
			for (int i = 0; i < method.arguments.length; i++) {
				call.arguments[i] = new SingleNameReference(("$p" + i).toCharArray(), pos(source));
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
				if (mb.isStatic()) continue;
				if (mb.isBridge()) continue;
				if (mb.isConstructor()) continue;
				if (mb.isDefaultAbstract()) continue;
				if (!mb.isPublic()) continue;
				if (mb.isSynthetic()) continue;
				if (METHODS_IN_OBJECT.contains(printSig(mb))) continue;
				list.add(mb);
			}
			addAllMethodBindings(list, rb.superclass());
			ReferenceBinding[] interfaces = rb.superInterfaces();
			if (interfaces != null) {
				for (ReferenceBinding iface : interfaces) addAllMethodBindings(list, iface);
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
			signature.append(simpleTypeBindingToString(param));
		}
		signature.append(")");
		
		return signature.toString();
	}
	
	private static String simpleTypeBindingToString(TypeBinding binding) {
		binding = binding.erasure();
		if (binding != null && binding.isBaseType()) {
			return new String (binding.sourceName());
		} else if (binding instanceof ReferenceBinding) {
			String pkg = binding.qualifiedPackageName() == null ? "" : new String(binding.qualifiedPackageName());
			String qsn = binding.qualifiedSourceName() == null ? "" : new String(binding.qualifiedSourceName());
			return pkg.isEmpty() ? qsn : (pkg + "." + qsn);
		} else if (binding instanceof ArrayBinding) {
			StringBuilder out = new StringBuilder();
			out.append(binding.leafComponentType());
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
