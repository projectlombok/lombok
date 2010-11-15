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

import java.lang.reflect.Field;

import lombok.eclipse.Eclipse;
import lombok.patcher.Hook;
import lombok.patcher.MethodTarget;
import lombok.patcher.ScriptManager;
import lombok.patcher.StackRequest;
import lombok.patcher.scripts.ScriptBuilder;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class PatchVal {
	
	// Creates a copy of the 'initialization' field on a LocalDeclaration if the type of the LocalDeclaration is 'val', because the completion parser will null this out,
	// which in turn stops us from inferring the intended type for 'val x = 5;'. We look at the copy.
	// Also patches local declaration to not call .resolveType() on the initializer expression if we've already done so (calling it twice causes weird errors),
	// and patches .resolve() on LocalDeclaration itself to just-in-time replace the 'val' vartype with the right one.
	
	static void addPatches(ScriptManager sm, boolean ecj) {
		final String LOCALDECLARATION_SIG = "org.eclipse.jdt.internal.compiler.ast.LocalDeclaration";
		final String FOREACHSTATEMENT_SIG = "org.eclipse.jdt.internal.compiler.ast.ForeachStatement";
		final String EXPRESSION_SIG = "org.eclipse.jdt.internal.compiler.ast.Expression";
		final String BLOCKSCOPE_SIG = "org.eclipse.jdt.internal.compiler.lookup.BlockScope";
		final String PARSER_SIG = "org.eclipse.jdt.internal.compiler.parser.Parser";
		final String TYPEBINDING_SIG = "org.eclipse.jdt.internal.compiler.lookup.TypeBinding";
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(LOCALDECLARATION_SIG, "resolve", "void", BLOCKSCOPE_SIG))
				.request(StackRequest.THIS, StackRequest.PARAM1)
				.decisionMethod(new Hook("lombok.eclipse.agent.PatchVal", "handleValForLocalDeclaration", "boolean", LOCALDECLARATION_SIG, BLOCKSCOPE_SIG))
				.build());
		
		sm.addScript(ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget(LOCALDECLARATION_SIG, "resolve", "void", BLOCKSCOPE_SIG))
				.methodToReplace(new Hook(EXPRESSION_SIG, "resolveType", TYPEBINDING_SIG, BLOCKSCOPE_SIG))
				.requestExtra(StackRequest.THIS)
				.replacementMethod(new Hook("lombok.eclipse.agent.PatchVal", "skipResolveInitializerIfAlreadyCalled2", TYPEBINDING_SIG, EXPRESSION_SIG, BLOCKSCOPE_SIG, LOCALDECLARATION_SIG))
				.build());
		
		sm.addScript(ScriptBuilder.replaceMethodCall()
				.target(new MethodTarget(FOREACHSTATEMENT_SIG, "resolve", "void", BLOCKSCOPE_SIG))
				.methodToReplace(new Hook(EXPRESSION_SIG, "resolveType", TYPEBINDING_SIG, BLOCKSCOPE_SIG))
				.replacementMethod(new Hook("lombok.eclipse.agent.PatchVal", "skipResolveInitializerIfAlreadyCalled", TYPEBINDING_SIG, EXPRESSION_SIG, BLOCKSCOPE_SIG))
				.build());
		
		sm.addScript(ScriptBuilder.exitEarly()
				.target(new MethodTarget(FOREACHSTATEMENT_SIG, "resolve", "void", BLOCKSCOPE_SIG))
				.request(StackRequest.THIS, StackRequest.PARAM1)
				.decisionMethod(new Hook("lombok.eclipse.agent.PatchVal", "handleValForForEach", "boolean", FOREACHSTATEMENT_SIG, BLOCKSCOPE_SIG))
				.build());
		
		if (!ecj) {
			sm.addScript(ScriptBuilder.addField()
					.fieldName("$initCopy")
					.fieldType("Lorg/eclipse/jdt/internal/compiler/ast/ASTNode;")
					.setPublic()
					.setTransient()
					.targetClass("org.eclipse.jdt.internal.compiler.ast.LocalDeclaration")
					.build());
			
			sm.addScript(ScriptBuilder.addField()
					.fieldName("$iterableCopy")
					.fieldType("Lorg/eclipse/jdt/internal/compiler/ast/ASTNode;")
					.setPublic()
					.setTransient()
					.targetClass("org.eclipse.jdt.internal.compiler.ast.LocalDeclaration")
					.build());
			
			sm.addScript(ScriptBuilder.wrapReturnValue()
					.target(new MethodTarget(PARSER_SIG, "consumeExitVariableWithInitialization", "void"))
					.request(StackRequest.THIS)
					.wrapMethod(new Hook("lombok.eclipse.agent.PatchVal", "copyInitializationOfLocalDeclaration", "void", PARSER_SIG))
					.build());
			
			sm.addScript(ScriptBuilder.wrapReturnValue()
					.target(new MethodTarget(PARSER_SIG, "consumeEnhancedForStatementHeader", "void"))
					.request(StackRequest.THIS)
					.wrapMethod(new Hook("lombok.eclipse.agent.PatchVal", "copyInitializationOfForEachIterable", "void", PARSER_SIG))
					.build());
		}
	}
	
	private static final class Reflection {
		private static final Field initCopyField, iterableCopyField;
		private static final Field astStackField, astPtrField;
		
		static {
			Field a = null, b = null, c = null, d = null;
			
			try {
				a = LocalDeclaration.class.getDeclaredField("$initCopy");
				b = LocalDeclaration.class.getDeclaredField("$iterableCopy");
			} catch (Throwable t) {
				//ignore - no $initCopy exists when running in ecj.
			}
			
			try {
				c = Parser.class.getDeclaredField("astStack");
				c.setAccessible(true);
				d = Parser.class.getDeclaredField("astPtr");
				d.setAccessible(true);
			} catch (Exception e) {
				// Most likely we're in ecj or some other plugin usage of the eclipse compiler. No need for this.
			}
			
			initCopyField = a;
			iterableCopyField = b;
			astStackField = c;
			astPtrField = d;
		}
	}
	
	public static void copyInitializationOfLocalDeclaration(Parser parser) {
		ASTNode[] astStack;
		int astPtr;
		try {
			astStack = (ASTNode[]) Reflection.astStackField.get(parser);
			astPtr = (Integer)Reflection.astPtrField.get(parser);
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
			if (Reflection.initCopyField != null) Reflection.initCopyField.set(variableDecl, init);
		} catch (Exception e) {
			// In ecj mode this field isn't there and we don't need the copy anyway, so, we ignore the exception.
		}
	}
	
	public static TypeBinding skipResolveInitializerIfAlreadyCalled(Expression expr, BlockScope scope) {
		if (expr.resolvedType != null) return expr.resolvedType;
		return expr.resolveType(scope);
	}
	
	public static TypeBinding skipResolveInitializerIfAlreadyCalled2(Expression expr, BlockScope scope, LocalDeclaration decl) {
		if (decl != null && LocalDeclaration.class.equals(decl.getClass()) && expr.resolvedType != null) return expr.resolvedType;
		return expr.resolveType(scope);
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
		if (init == null && Reflection.initCopyField != null) {
			try {
				init = (Expression) Reflection.initCopyField.get(local);
			} catch (Exception e) {
			}
		}
		
		if (init == null && Reflection.iterableCopyField != null) {
			try {
				init = (Expression) Reflection.iterableCopyField.get(local);
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
	public static void copyInitializationOfForEachIterable(Parser parser) {
		ASTNode[] astStack;
		int astPtr;
		try {
			astStack = (ASTNode[]) Reflection.astStackField.get(parser);
			astPtr = (Integer) Reflection.astPtrField.get(parser);
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
			if (Reflection.iterableCopyField != null) Reflection.iterableCopyField.set(foreachDecl.elementVariable, init);
		} catch (Exception e) {
			// In ecj mode this field isn't there and we don't need the copy anyway, so, we ignore the exception.
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
}
