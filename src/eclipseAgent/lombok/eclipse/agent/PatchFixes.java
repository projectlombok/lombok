/*
 * Copyright © 2010 Reinier Zwitserloot and Roel Spilker.
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompiler;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
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
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class PatchFixes {
	public static int fixRetrieveStartingCatchPosition(int in) {
		return in;
	}
	
	public static final int ALREADY_PROCESSED_FLAG = 0x800000;	//Bit 24
	
	public static boolean checkBit24(Object node) throws Exception {
		int bits = (Integer)(node.getClass().getField("bits").get(node));
		return (bits & ALREADY_PROCESSED_FLAG) != 0;
	}
	
	/**
	 * XXX LIVE DEBUG
	 * 
	 * Once in a blue moon eclipse throws a NullPointerException while editing a file. Can't reproduce it while running eclipse in a debugger,
	 * but at least this way we patch the problem to be a bit more specific in the error that should then appear.
	 */
	public static boolean debugPrintStateOfScope(Object in) throws Exception {
		/* this.scope.enclosingSourceType().sourceName */
		Object scope = in.getClass().getField("scope").get(in);
		String msg = null;
		if (scope == null) msg = "scope itself is null";
		else {
			Object sourceTypeBinding = scope.getClass().getMethod("enclosingSourceType").invoke(scope);
			if (sourceTypeBinding == null) msg = "scope.enclosingSourceType() is null";
		}
		
		if (msg != null) throw new NullPointerException(msg);
		return false;
	}
	
	public static boolean skipRewritingGeneratedNodes(org.eclipse.jdt.core.dom.ASTNode node) throws Exception {
		return ((Boolean)node.getClass().getField("$isGenerated").get(node)).booleanValue();
	}
	
	public static void setIsGeneratedFlag(org.eclipse.jdt.core.dom.ASTNode domNode,
			org.eclipse.jdt.internal.compiler.ast.ASTNode internalNode) throws Exception {
		if (internalNode == null || domNode == null) return;
		boolean isGenerated = internalNode.getClass().getField("$generatedBy").get(internalNode) != null;
		if (isGenerated) {
			domNode.getClass().getField("$isGenerated").set(domNode, true);
			domNode.setFlags(domNode.getFlags() & ~org.eclipse.jdt.core.dom.ASTNode.ORIGINAL);
		}
	}
	
	public static void setIsGeneratedFlagForSimpleName(SimpleName name, Object internalNode) throws Exception {
		if (internalNode instanceof org.eclipse.jdt.internal.compiler.ast.ASTNode) {
			if (internalNode.getClass().getField("$generatedBy").get(internalNode) != null) {
				name.getClass().getField("$isGenerated").set(name, true);
			}
		}
	}
	
	public static IMethod[] removeGeneratedMethods(IMethod[] methods) throws Exception {
		List<IMethod> result = new ArrayList<IMethod>();
		for (IMethod m : methods) {
			if (m.getNameRange().getLength() > 0) result.add(m);
		}
		return result.size() == methods.length ? methods : result.toArray(new IMethod[0]);
	}
	
	public static SimpleName[] removeGeneratedSimpleNames(SimpleName[] in) throws Exception {
		Field f = SimpleName.class.getField("$isGenerated");
		
		int count = 0;
		for (int i = 0; i < in.length; i++) {
			if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) count++;
		}
		if (count == in.length) return in;
		SimpleName[] newSimpleNames = new SimpleName[count];
		count = 0;
		for (int i = 0; i < in.length; i++) {
			if (in[i] == null || !((Boolean)f.get(in[i])).booleanValue()) newSimpleNames[count++] = in[i];
		}
		return newSimpleNames;
	}
	
	public static byte[] runPostCompiler(byte[] bytes, String fileName) {
		byte[] transformed = PostCompiler.applyTransformations(bytes, fileName, DiagnosticsReceiver.CONSOLE);
		return transformed == null ? bytes : transformed;
	}
	
	public static OutputStream runPostCompiler(OutputStream out) throws IOException {
		return PostCompiler.wrapOutputStream(out, "TEST", DiagnosticsReceiver.CONSOLE);
	}
	
	public static BufferedOutputStream runPostCompiler(BufferedOutputStream out, String path, String name) throws IOException {
		String fileName = path + "/" + name;
		return new BufferedOutputStream(PostCompiler.wrapOutputStream(out, fileName, DiagnosticsReceiver.CONSOLE));
	}
	
	private static Field astStackField, astPtrField;
	
	static {
		try {
			astStackField = Parser.class.getDeclaredField("astStack");
			astStackField.setAccessible(true);
			astPtrField = Parser.class.getDeclaredField("astPtr");
			astPtrField.setAccessible(true);
		} catch (Exception e) {
			// Most likely we're in ecj or some other plugin usage of the eclipse compiler. No need for this.
		}
	}
	
	public static void copyInitializationOfForEachIterable(Parser parser) {
		ASTNode[] astStack;
		int astPtr;
		try {
			astStack = (ASTNode[]) astStackField.get(parser);
			astPtr = (Integer)astPtrField.get(parser);
		} catch (Exception e) {
			// Most likely we're in ecj or some other plugin usage of the eclipse compiler. No need for this.
			return;
		}
		
		ForeachStatement foreachDecl = (ForeachStatement) astStack[astPtr];
		ASTNode init = foreachDecl.collection;
		if (init == null) return;
		if (foreachDecl.elementVariable != null && foreachDecl.elementVariable.type instanceof SingleTypeReference) {
			SingleTypeReference ref = (SingleTypeReference) foreachDecl.elementVariable.type;
			if (ref.token == null || ref.token.length != 3 || ref.token[0] != 'v' || ref.token[1] != 'a' || ref.token[2] != 'l') return;
		} else return;
		
		try {
			if (iterableCopyField != null) iterableCopyField.set(foreachDecl.elementVariable, init);
		} catch (Exception e) {
			// In ecj mode this field isn't there and we don't need the copy anyway, so, we ignore the exception.
		}
	}
	
	public static void copyInitializationOfLocalDeclarationForVal(Parser parser) {
		ASTNode[] astStack;
		int astPtr;
		try {
			astStack = (ASTNode[]) astStackField.get(parser);
			astPtr = (Integer)astPtrField.get(parser);
		} catch (Exception e) {
			// Most likely we're in ecj or some other plugin usage of the eclipse compiler. No need for this.
			return;
		}
		AbstractVariableDeclaration variableDecl = (AbstractVariableDeclaration) astStack[astPtr];
		if (!(variableDecl instanceof LocalDeclaration)) return;
		ASTNode init = variableDecl.initialization;
		if (init == null) return;
		if (variableDecl.type instanceof SingleTypeReference) {
			SingleTypeReference ref = (SingleTypeReference) variableDecl.type;
			if (ref.token == null || ref.token.length != 3 || ref.token[0] != 'v' || ref.token[1] != 'a' || ref.token[2] != 'l') return;
		} else return;
		
		try {
			if (initCopyField != null) initCopyField.set(variableDecl, init);
		} catch (Exception e) {
			// In ecj mode this field isn't there and we don't need the copy anyway, so, we ignore the exception.
		}
	}
	
	private static Field initCopyField, iterableCopyField;
	
	static {
		try {
			initCopyField = LocalDeclaration.class.getDeclaredField("$initCopy");
			iterableCopyField = LocalDeclaration.class.getDeclaredField("$iterableCopy");
		} catch (Throwable t) {
			 //ignore - no $initCopy exists when running in ecj.
		}
	}
	
	public static boolean handleValForForEach(ForeachStatement forEach, BlockScope scope) {
		if (forEach.elementVariable != null && forEach.elementVariable.type instanceof SingleTypeReference) {
			char[] token = ((SingleTypeReference)forEach.elementVariable.type).token;
			if (token == null || token.length != 3) return false;
			else if (token[0] != 'v' || token[1] != 'a' || token[2] != 'l') return false;
		} else return false;
		
		TypeBinding component = getForEachComponentType(forEach.collection, scope);
		TypeReference replacement = Eclipse.makeType(component, forEach.elementVariable.type, false);
		
		forEach.elementVariable.modifiers |= ClassFileConstants.AccFinal;
		forEach.elementVariable.type = replacement != null ? replacement :
				new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, Eclipse.poss(forEach.elementVariable.type, 3));
		
		return false;
	}

	private static TypeBinding getForEachComponentType(Expression collection, BlockScope scope) {
		if (collection != null) {
			TypeBinding resolved = collection.resolveType(scope);
			if (resolved.isArrayType()) {
				resolved = ((ArrayBinding) resolved).elementsType();
				return resolved;
			} else if (resolved instanceof ReferenceBinding) {
				ReferenceBinding iterableType = ((ReferenceBinding)resolved).findSuperTypeOriginatingFrom(TypeIds.T_JavaLangIterable, false);
				
				TypeBinding[] arguments = null;
				if (iterableType != null) switch (iterableType.kind()) {
					case Binding.GENERIC_TYPE : // for (T t : Iterable<T>) - in case used inside Iterable itself
						arguments = iterableType.typeVariables();
						break;
					case Binding.PARAMETERIZED_TYPE : // for(E e : Iterable<E>)
						arguments = ((ParameterizedTypeBinding)iterableType).arguments;
						break;
				}
				
				if (arguments != null && arguments.length == 1) {
					return arguments[0];
				}
			}
		}
		
		return null;
	}
	
	public static boolean handleValForLocalDeclaration(LocalDeclaration local, BlockScope scope) {
		if (local == null || !LocalDeclaration.class.equals(local.getClass())) return false;
		boolean decomponent = false;
		
		if (local.type instanceof SingleTypeReference) {
			char[] token = ((SingleTypeReference)local.type).token;
			if (token == null || token.length != 3) return false;
			else if (token[0] != 'v' || token[1] != 'a' || token[2] != 'l') return false;
		} else return false;
		
		Expression init = local.initialization;
		if (init == null && initCopyField != null) {
			try {
				init = (Expression) initCopyField.get(local);
			} catch (Exception e) {
			}
		}
		
		if (init == null && iterableCopyField != null) {
			try {
				init = (Expression) iterableCopyField.get(local);
				decomponent = true;
			} catch (Exception e) {
			}
		}
		
		TypeReference replacement = null;
		if (init != null && decomponent) {
		}
		
		if (init != null) {
			TypeBinding resolved = decomponent ? getForEachComponentType(init, scope) : init.resolveType(scope);
			if (resolved != null) {
				replacement = Eclipse.makeType(resolved, local.type, false);
			}
		}
		
		local.modifiers |= ClassFileConstants.AccFinal;
		local.type = replacement != null ? replacement : new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, Eclipse.poss(local.type, 3));
		
		return false;
	}
	
	public static TypeBinding skipResolveInitializerIfAlreadyCalled(Expression expr, BlockScope scope) {
		if (expr.resolvedType != null) return expr.resolvedType;
		return expr.resolveType(scope);
	}
	
	public static TypeBinding skipResolveInitializerIfAlreadyCalled2(Expression expr, BlockScope scope, LocalDeclaration decl) {
		if (decl != null && LocalDeclaration.class.equals(decl.getClass()) && expr.resolvedType != null) return expr.resolvedType;
		return expr.resolveType(scope);
	}
	
	public static boolean handleDelegateForType(TypeDeclaration decl) {
		if (decl.scope == null) return false;
		if (decl.fields == null) return false;
		
		for (FieldDeclaration field : decl.fields) {
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
				
				System.out.println("About to generate the following methods, all delegating to: this." + new String(field.name));
				for (MethodBinding mb : methodsToDelegate) {
					System.out.println(mb);
				}
				System.out.println("-----------");
				
				generateDelegateMethods(decl, methodsToDelegate, field.name, ann);
			}
		}
		
		return false;
	}
	
	private static final Method methodScopeCreateMethodMethod;
	private static final Field sourceTypeBindingMethodsField;
	
	static {
		Method m = null;
		Field f = null;
		Exception ex = null;
		
		try {
			m = MethodScope.class.getDeclaredMethod("createMethod", AbstractMethodDeclaration.class);
			m.setAccessible(true);
			f = SourceTypeBinding.class.getDeclaredField("methods");
			f.setAccessible(true);
		} catch (Exception e) {
			ex = e;
		}
		
		methodScopeCreateMethodMethod = m;
		sourceTypeBindingMethodsField = f;
		if (ex != null) throw new RuntimeException(ex);
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
				MethodScope methodScope = new MethodScope(type.scope, method, false);
				
				try {
					MethodBinding methodBinding = (MethodBinding) methodScopeCreateMethodMethod.invoke(methodScope, method);
					System.out.println("SCOPE NOW: " + method.scope);
					
					method.resolve(type.scope);
					System.out.println("Bind now: " + methodBinding.returnType);
					
					MethodBinding[] existing = (MethodBinding[]) sourceTypeBindingMethodsField.get(type.binding);
					if (existing == null) existing = new MethodBinding[] {methodBinding};
					else {
						MethodBinding[] copy = new MethodBinding[existing.length + 1];
						System.arraycopy(existing, 0, copy, 0, existing.length);
						copy[existing.length] = methodBinding;
					}
					sourceTypeBindingMethodsField.set(type.binding, existing);
					System.out.println("Added method binding: " + methodBinding);
					System.out.println(method);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private static MethodDeclaration generateDelegateMethod(char[] name, MethodBinding binding, CompilationResult compilationResult, ASTNode source) {
		MethodDeclaration method = new MethodDeclaration(compilationResult);
		Eclipse.setGeneratedBy(method, source);
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
			}
		}
		method.selector = binding.selector;
		if (binding.thrownExceptions != null && binding.thrownExceptions.length > 0) {
			method.thrownExceptions = new TypeReference[binding.thrownExceptions.length];
			for (int i = 0; i < method.thrownExceptions.length; i++) {
				method.thrownExceptions[i] = Eclipse.makeType(binding.thrownExceptions[i], source, false);
			}
		}
		
		method.typeParameters = null; // TODO think about this
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		FieldReference fieldRef = new FieldReference(name, pos(source));
		fieldRef.receiver = new ThisReference(source.sourceStart, source.sourceEnd);
		MessageSend call = new MessageSend();
		call.receiver = fieldRef;
		call.selector = binding.selector;
		if (method.arguments != null) {
			call.arguments = new Expression[method.arguments.length];
			for (int i = 0; i < method.arguments.length; i++) {
				call.arguments[i] = new SingleNameReference(("$p" + i).toCharArray(), pos(source));
			}
		}
		
		Statement body;
		if (method.returnType instanceof SingleTypeReference && ((SingleTypeReference)method.returnType).token == TypeConstants.VOID) {
			body = call;
		} else {
			body = new ReturnStatement(call, source.sourceStart, source.sourceEnd);
		}
		
		method.statements = new Statement[] {body};
		// TODO add Eclipse.setGeneratedBy everywhere.
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}
	
	private static void addAllMethodBindings(List<MethodBinding> list, TypeBinding binding) {
		if (binding instanceof ReferenceBinding) {
			for (MethodBinding mb : ((ReferenceBinding)binding).availableMethods()) {
				if (mb.isStatic()) continue;
				if (mb.isBridge()) continue;
				if (mb.isConstructor()) continue;
				if (mb.isDefaultAbstract()) continue;
				if (!mb.isPublic()) continue;
				if (mb.isSynthetic()) continue;
				list.add(mb);
			}
		}
	}
	
	private static boolean charArrayEquals(String s, char[] c) {
		if (s == null) return c == null;
		if (c == null) return false;
		
		if (s.length() != c.length) return false;
		for (int i = 0; i < s.length(); i++) if (s.charAt(i) != c[i]) return false;
		return true;
		
		
	}
}
