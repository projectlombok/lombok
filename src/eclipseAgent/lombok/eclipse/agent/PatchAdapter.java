/*
 * Copyright (C) 2010-2024 The Project Lombok Authors.
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
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DeltaProcessor;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaElementDelta;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceMethodInfo;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;

import lombok.core.AST.Kind;
import lombok.eclipse.EcjAugments.EclipseAugments;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAST;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.TransformEclipseAST;
import lombok.eclipse.handlers.SetGeneratedByVisitor;
import lombok.patcher.Symbols;
import lombok.permit.Permit;

public class PatchAdapter {

	private static final char[][] JAVA_UTIL_COLLECTIONS = {
		{'j', 'a', 'v', 'a'}, {'u', 't', 'i', 'l'}, {'C', 'o', 'l', 'l', 'e', 'c', 't', 'i', 'o', 'n', 's'}
	};
	private static final char[][] JAVA_UTIL_OPTIONAL = {
		{'j', 'a', 'v', 'a'}, {'u', 't', 'i', 'l'}, {'O', 'p', 't', 'i', 'o', 'n', 'a', 'l'}
	};

	private static boolean eclipseAvailable = true;
	static {
		try {
			CompilationUnit.class.getName();
		} catch (Throwable t) {
			eclipseAvailable = false;
		}
	}

	public static boolean handleAdapterForType(ClassScope scope) {
		if (TransformEclipseAST.disableLombok) return false;
		
		CompilationUnitDeclaration cud = scope.compilationUnitScope().referenceContext;
		if (scope == scope.compilationUnitScope().topLevelTypes[0].scope) {
			if (eclipseAvailable) {
				EclipseOnlyMethods.cleanupAdapterMethods(cud);
			}
		}
		
		Annotation annotation = findAdapterAnnotation(scope.referenceContext);
		if (annotation == null) return false;
		
		GeneratorOptions options = GeneratorOptions.from(annotation);
		
		List<BindingTuple> methodsToOverride = fillMethodBindings(cud, scope, annotation, options);

		if (!methodsToOverride.isEmpty()) {
			TypeDeclaration decl = scope.referenceContext;
			EclipseAST eclipseAst = TransformEclipseAST.getAST(cud, true);
			generateOverriddenMethods(eclipseAst.get(decl), scope, methodsToOverride, options);
			
			EclipseOnlyMethods.notifyAdapterMethodsAdded(cud);
		}

		return false;
	}
	
	/**
	 * Returns a string containing the signature of a method that appears (erased) at least twice in the list.
	 * If no duplicates are present, {@code null} is returned.
	 */
	private static String containsDuplicates(List<BindingTuple> tuples) {
		Set<String> sigs = new HashSet<String>();
		for (BindingTuple tuple : tuples) {
			if (!sigs.add(printSig(tuple.parameterized))) return printSig(tuple.parameterized);
		}
		
		return null;
	}
	
	private static List<BindingTuple> fillMethodBindings(CompilationUnitDeclaration cud, ClassScope scope,  
		Annotation annotation, GeneratorOptions options) {
		List<BindingTuple> methodsToOverride = new LinkedList<BindingTuple>();
		TypeDeclaration decl = scope.referenceContext;
		
		Set<String> banList = findAlreadyImplementedMethods(decl);
		List<BindingTuple> methodsToExclude = new ArrayList<BindingTuple>();
		for (BindingTuple excluded : methodsToExclude) banList.add(printSig(excluded.parameterized));

		List<BindingTuple> methodsToOverrideForThisAnn = new ArrayList<BindingTuple>();
		if (!options.overrideTypes.isEmpty()) {
			for (ClassLiteralAccess ofType : options.overrideTypes) {
				addAllMethodBindings(methodsToOverrideForThisAnn, ofType.type.resolveType(decl.initializerScope), banList);
			}
		} else if (decl.superInterfaces != null) {
			for (TypeReference ofType : decl.superInterfaces) {
				addAllMethodBindings(methodsToOverrideForThisAnn, ofType.resolveType(decl.initializerScope), banList);
			}
		}
		
		String dupe = containsDuplicates(methodsToOverrideForThisAnn);
		if (dupe != null) {
			EclipseAST eclipseAst = TransformEclipseAST.getAST(cud, true);
			eclipseAst.get(annotation).addError("The method '" + dupe + "' is being implemented by more than one specified type.");
		} else {
			methodsToOverride.addAll(methodsToOverrideForThisAnn);
		}
		return methodsToOverride;
	}
	
	private static Annotation findAdapterAnnotation(TypeDeclaration decl) {
		if (decl != null && decl.annotations != null) {
			for (Annotation ann : decl.annotations) {
				if (isAdapter(ann, decl)) return ann;
			}
		}
		return null;
	}
	
	private static boolean isAdapter(Annotation ann, TypeDeclaration decl) {
		if (ann.type == null) return false;
		if (!charArrayEquals("Adapter", ann.type.getLastToken())) return false;
		
		TypeBinding tb = ann.type.resolveType(decl.initializerScope);
		if (tb == null) return false;
		if (!charArrayEquals("lombok", tb.qualifiedPackageName()) && !charArrayEquals("lombok.experimental", tb.qualifiedPackageName())) return false;
		if (!charArrayEquals("Adapter", tb.qualifiedSourceName())) return false;
		return true;
	}
	
	private static void generateOverriddenMethods(EclipseNode typeNode, ClassScope scope, List<BindingTuple> methods, GeneratorOptions options) {
		CompilationUnitDeclaration top = (CompilationUnitDeclaration) typeNode.top().get();
		List<MethodDeclaration> addedMethods = new ArrayList<MethodDeclaration>();
		for (BindingTuple pair : methods) {
			MethodDeclaration method = createOverriddenMethod(typeNode, scope, pair, top.compilationResult, options);
			if (method != null) {
				SetGeneratedByVisitor visitor = new SetGeneratedByVisitor(typeNode.get());
				method.traverse(visitor, ((TypeDeclaration)typeNode.get()).scope);
				injectMethod(typeNode, method);
				addedMethods.add(method);
			}
		}
		if (eclipseAvailable) {
			EclipseOnlyMethods.collectGeneratedAdapterMethods(top, typeNode, addedMethods);
		}
	}
	
	private static void checkConflictOfTypeVarNames(BindingTuple binding, EclipseNode typeNode) throws CantMakeAdapter {
		TypeVariableBinding[] typeVars = binding.parameterized.typeVariables();
		if (typeVars == null || typeVars.length == 0) return;
		
		Set<String> usedInOurType = new HashSet<String>();
		EclipseNode enclosingType = typeNode;
		while (enclosingType != null) {
			if (enclosingType.getKind() == Kind.TYPE) {
				TypeParameter[] typeParameters = ((TypeDeclaration)enclosingType.get()).typeParameters;
				if (typeParameters != null) {
					for (TypeParameter param : typeParameters) {
						if (param.name != null) usedInOurType.add(new String(param.name));
					}
				}
			}
			enclosingType = enclosingType.up();
		}
		
		Set<String> usedInMethodSig = new HashSet<String>();
		for (TypeVariableBinding var : typeVars) {
			char[] sourceName = var.sourceName();
			if (sourceName != null) usedInMethodSig.add(new String(sourceName));
		}
		
		usedInMethodSig.retainAll(usedInOurType);
		if (usedInMethodSig.isEmpty()) return;
		
		// We might be overriding a List<T>, and we are making method <T> toArray(). A conflict is possible.
		// But only if the toArray method also uses type vars from its class, otherwise we're only shadowing,
		// which is okay as we'll add a @SuppressWarnings.

		// is this relevant for the Adapter? even if an inner method is repeating an outer type parameter
		// that is causing a warning that the inner one is hiding the other (shadowing)
		
		PatchDelegate.TypeVarFinder finder = new PatchDelegate.TypeVarFinder();
		finder.visitRaw(binding.base);
		
		Set<String> names = new HashSet<String>(finder.getTypeVariables());
		names.removeAll(usedInMethodSig);
		if (!names.isEmpty()) {
			// We have a confirmed conflict. We could dig deeper as this may still be a false alarm, but its already an exceedingly rare case.
			CantMakeAdapter cmd = new CantMakeAdapter();
			cmd.conflicted = usedInMethodSig;
			throw cmd;
		}
	}
	
	private static class CantMakeAdapter extends Exception {
		public Set<String> conflicted;
	}
	
	private static MethodDeclaration createOverriddenMethod(EclipseNode typeNode, ClassScope scope, BindingTuple pair, 
		CompilationResult compilationResult, GeneratorOptions options) {
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
		
		boolean isVarargs = (pair.base.modifiers & ClassFileConstants.AccVarargs) != 0;
		
		try {
			checkConflictOfTypeVarNames(pair, typeNode);
		} catch (CantMakeAdapter e) {
			typeNode.addError("There's a conflict in the names of type parameters. Fix it by renaming the following type parameters of your class: " + e.conflicted);
			return null;
		}
		
		ASTNode source = typeNode.get();
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		
		MethodBinding binding = pair.parameterized;
		MethodDeclaration method = new MethodDeclaration(compilationResult);
		setGeneratedBy(method, source);
		method.sourceStart = pS; method.sourceEnd = pE;
		method.modifiers = ClassFileConstants.AccPublic;
		
		method.returnType = makeType(binding.returnType, source, false);
		boolean isDeprecated = binding.isDeprecated();
		
		method.selector = binding.selector;
		
		if (!options.suppressThrows && binding.thrownExceptions != null && binding.thrownExceptions.length > 0) {
			method.thrownExceptions = new TypeReference[binding.thrownExceptions.length];
			for (int i = 0; i < method.thrownExceptions.length; i++) {
				method.thrownExceptions[i] = makeType(binding.thrownExceptions[i], source, false);
			}
		}
		
		if (binding.typeVariables != null && binding.typeVariables.length > 0) {
			method.typeParameters = new TypeParameter[binding.typeVariables.length];
			for (int i = 0; i < method.typeParameters.length; i++) {
				method.typeParameters[i] = new TypeParameter();
				method.typeParameters[i].sourceStart = pS; method.typeParameters[i].sourceEnd = pE;
				setGeneratedBy(method.typeParameters[i], source);
				method.typeParameters[i].name = binding.typeVariables[i].sourceName;
				ReferenceBinding super1 = binding.typeVariables[i].superclass;
				ReferenceBinding[] super2 = binding.typeVariables[i].superInterfaces;
				if (super2 == null) super2 = new ReferenceBinding[0];
				if (super1 != null || super2.length > 0) {
					int offset = super1 == null ? 0 : 1;
					method.typeParameters[i].bounds = new TypeReference[super2.length + offset - 1];
					if (super1 != null) method.typeParameters[i].type = makeType(super1, source, false);
					else method.typeParameters[i].type = makeType(super2[0], source, false);
					int ctr = 0;
					for (int j = (super1 == null) ? 1 : 0; j < super2.length; j++) {
						method.typeParameters[i].bounds[ctr] = makeType(super2[j], source, false);
						method.typeParameters[i].bounds[ctr++].bits |= ASTNode.IsSuperType;
					}
				}
			}
		}
		
		if (isDeprecated) {
			method.annotations = new Annotation[] { generateDeprecatedAnnotation(source) };
		}
		
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		if (binding.parameters != null && binding.parameters.length > 0) {
			method.arguments = new Argument[binding.parameters.length];
			for (int i = 0; i < method.arguments.length; i++) {
				AbstractMethodDeclaration sourceElem;
				try {
					sourceElem = pair.base.sourceMethod();
				} catch (Exception e) {
					sourceElem = null;
				}
				char[] argName;
				if (sourceElem == null) argName = ("arg" + i).toCharArray();
				else {
					argName = sourceElem.arguments[i].name;
				}
				method.arguments[i] = new Argument(
						argName, pos(source),
						makeType(binding.parameters[i], source, false),
						ClassFileConstants.AccFinal);
				setGeneratedBy(method.arguments[i], source);
			}
			if (isVarargs) {
				method.arguments[method.arguments.length - 1].type.bits |= ASTNode.IsVarArgs;
			}
		}
		
		Statement body;
		// determine default value for silentMode
		if (options.silentMode) {
			if (method.returnType instanceof SingleTypeReference && ((SingleTypeReference)method.returnType).token == TypeConstants.VOID) {
				body = new EmptyStatement(source.sourceStart, source.sourceEnd);
			} else if (method.returnType instanceof QualifiedTypeReference && Eclipse.nameEquals(((QualifiedTypeReference)method.returnType).getTypeName(), new String(binding.declaringClass.readableName(false)))) {
				// if returnType is same as the interface, then return 'this' (most likely it's some kind of fluent API)
				body = new ReturnStatement(new ThisReference(source.sourceStart, source.sourceEnd), source.sourceStart, source.sourceEnd);
			} else {
				// determine default value to be returned
				Expression returnValue = findExpressionForReturnType(method.returnType, scope, typeNode);
				body = new ReturnStatement(returnValue, source.sourceStart, source.sourceEnd);
			}
		} else { // create a throwExpression for the specified or default exception type with or without message
			String exceptionTypeStr;
			ClassLiteralAccess exceptionClass = options.exceptionClass;
			if (exceptionClass == null) {
				exceptionTypeStr = UnsupportedOperationException.class.getCanonicalName();
			} else {
				exceptionTypeStr = getNonGenericQualifiedName(exceptionClass.type.resolveType(scope));
					// options.exceptionClass.type.resolveType(method.scope);
			}
			int partCount = 1;
			for (int i = 0; i < exceptionTypeStr.length(); i++) if (exceptionTypeStr.charAt(i) == '.') partCount++;
			long[] ps = new long[partCount];
			Arrays.fill(ps, 0L);
			AllocationExpression exception = new AllocationExpression();
			setGeneratedBy(exception, source);
			exception.type = new QualifiedTypeReference(fromQualifiedName(exceptionTypeStr), ps);
			setGeneratedBy(exception.type, source);
			if (options.exceptionMsg != null && !options.exceptionMsg.isEmpty()) {
				StringLiteral message = new StringLiteral(options.exceptionMsg.toCharArray(), pS, pE, 0);
				setGeneratedBy(message, source);
				exception.arguments = new Expression[] {message};
			}
			
			body = new ThrowStatement(exception, pS, pE);
			
			setGeneratedBy(body, source);
		}
		
		method.statements = new Statement[] {body};
		return method;
	}

	private static String getNonGenericQualifiedName(TypeBinding typeBinding) {
		return new String(typeBinding.readableName()).replaceFirst("<.*>", "");
	}
	
	private static Expression findExpressionForReturnType(TypeReference returnType, ClassScope scope, EclipseNode typeNode) {
		ASTNode source = typeNode.get();
		int pS = source.sourceStart, pE = source.sourceEnd;
		// create null or default value to be returned
		String typeString = returnType.toString()
			.replaceFirst("<.*>", "");
		Expression defaultPrimitiveValue = DEFAULT_VALUE_MAP.get(typeString);
		if (defaultPrimitiveValue != null) {
			defaultPrimitiveValue.sourceStart = pS;
			defaultPrimitiveValue.sourceEnd = pE;
			if (defaultPrimitiveValue instanceof CastExpression) {
				((CastExpression)defaultPrimitiveValue).expression.sourceStart = pS;
				((CastExpression)defaultPrimitiveValue).expression.sourceEnd = pE;
			}
			return defaultPrimitiveValue;
		}
		// some array -> create an empty array
		if (returnType instanceof ArrayTypeReference || typeString.endsWith("[]")) {
			ArrayAllocationExpression ret = new ArrayAllocationExpression();
			ret.dimensions = new Expression[] { IntLiteral.buildIntLiteral(new char[] {'0'}, pS, pE)};
			ret.sourceStart = pS;
			ret.sourceEnd = pE;
			String typeName = returnType.toString().replace("[]", "");
			ret.type = getQualifiedTypeReference(typeName);
			return ret;
		}
		// some collection interface -> use Collections.empty... 
		String collectionsMethod = DEFAULT_COLLECTIONS_METHOD.get(typeString);
		if (collectionsMethod != null) {
			MessageSend invoke = new MessageSend();
			invoke.receiver = new QualifiedNameReference(JAVA_UTIL_COLLECTIONS, new long[]{0L}, pS, pE);
			invoke.selector = collectionsMethod.toCharArray();
			return invoke;
		}
		// Optional<T> -> Optional.empty()
		if ("java.util.Optional".equals(typeString)) {
			MessageSend invoke = new MessageSend();
			invoke.receiver = new QualifiedNameReference(JAVA_UTIL_OPTIONAL, new long[]{0L}, pS, pE);
			invoke.selector = "empty".toCharArray();
			return invoke;
		}
		// if it's a non-abstract class that extends AbstractMap or AbstractCollection, then create a new instance of it
		if (isMapOrCollectionClass(returnType.resolveType(scope))) {
			QualifiedAllocationExpression ret = new QualifiedAllocationExpression();
			ret.sourceStart = pS;
			ret.sourceEnd = pE;
			ret.type = new QualifiedTypeReference(fromQualifiedName(returnType.toString().replaceFirst("<.*>", "")), new long[] {0L});
			// should we keep the type arguments for java6?
			return ret;
		}
		
		return new NullLiteral(pS, pE);
	}

	private static QualifiedTypeReference getQualifiedTypeReference(String typeName) {
		return new QualifiedTypeReference(fromQualifiedName(typeName), new long[] {0L});
	}

	private static boolean isMapOrCollectionClass(TypeBinding typeBinding) {
		// check if given type is non-abstract class that extends AbstractMap or AbstractCollection
		if (typeBinding == null) return false;
		ReferenceBinding actualType = typeBinding.actualType();
		if (actualType != null && actualType.isAbstract())
			return false;
		while (actualType != null) {
			String className = getNonGenericQualifiedName(actualType);
			if ("java.util.AbstractCollection".equals(className) || "java.util.AbstractMap".equals(className)) {
				return true;
			}
			actualType = actualType.superclass();
		}
		return false;
	}

	public static Object[] addGeneratedAdapterMethods(Object[] returnValue, Object javaElement) {
		if (Symbols.hasSymbol("lombok.skipadapters")) return returnValue;
		if (!eclipseAvailable) return returnValue;
		
		return EclipseOnlyMethods.addGeneratedAdapterMethodsToChildren(returnValue, javaElement);
	}
	
	public static Object returnElementInfo(Object adapterSourceMethod) {
		Field field = Permit.permissiveGetField(adapterSourceMethod.getClass(), "sourceMethodInfo");
		return Permit.permissiveReadField(Object.class, field, adapterSourceMethod);
	}
	
	public static boolean isAdapterSourceMethod(Object sourceMethod) {
		return sourceMethod.getClass().getName().equals("lombok.eclipse.agent.PatchAdapter$EclipseOnlyMethods$AdapterSourceMethod");
	}
	
	public static class EclipseOnlyMethods {
		private static void cleanupAdapterMethods(CompilationUnitDeclaration cud) {
			CompilationUnit compilationUnit = getCompilationUnit(cud);
			if (compilationUnit != null) {
				EclipseAugments.CompilationUnit_adapterMethods.clear(compilationUnit);
			}
		}

		public static void collectGeneratedAdapterMethods(CompilationUnitDeclaration top, EclipseNode typeNode, List<MethodDeclaration> addedMethods) {
			String qualifiedName = new String(CharOperation.concatWith(getQualifiedInnerName(typeNode.up(), typeNode.getName().toCharArray()), '$'));
			SourceType sourceType = getSourceType(top, qualifiedName);
			List<SourceMethod> generatedMethods = getGeneratedMethods(sourceType);
			if (generatedMethods == null) return;
			
			for (MethodDeclaration md : addedMethods) {
				generatedMethods.add(AdapterSourceMethod.forMethodDeclaration(sourceType, md));
			}
		}
		
		public static Object[] addGeneratedAdapterMethodsToChildren(Object[] returnValue, Object javaElement) {
			List<SourceMethod> adapterMethods = getGeneratedMethods((SourceType) ((SourceTypeElementInfo) javaElement).getHandle());
			if (adapterMethods != null && !adapterMethods.isEmpty()) {
				return concat((IJavaElement[]) returnValue, adapterMethods.toArray(new IJavaElement[0]), IJavaElement.class);
			}
			return returnValue;
		}
		
		private static void notifyAdapterMethodsAdded(CompilationUnitDeclaration cud) {
			CompilationUnit compilationUnit = getCompilationUnit(cud);
			if (compilationUnit != null) {
				DeltaProcessor deltaProcessor = JavaModelManager.getJavaModelManager().getDeltaProcessor();
				deltaProcessor.fire(new JavaElementDelta(compilationUnit), ElementChangedEvent.POST_CHANGE);
			}
		}
		
		private static CompilationUnit getCompilationUnit(Object iCompilationUnit) {
			if (iCompilationUnit instanceof CompilationUnit) {
				CompilationUnit compilationUnit = (CompilationUnit) iCompilationUnit;
				return compilationUnit.originalFromClone();
			}
			return null;
		}
		
		private static CompilationUnit getCompilationUnit(CompilationUnitDeclaration cud) {
			return getCompilationUnit(cud.compilationResult.compilationUnit);
		}
		
		private static final class AdapterSourceMethod extends SourceMethod {
			private AdapterSourceMethodInfo sourceMethodInfo;
	
			private static AdapterSourceMethod forMethodDeclaration(JavaElement parent, MethodDeclaration method) {
				Argument[] arguments = method.arguments != null ? method.arguments : new Argument[0];
				String[] parameterTypes = new String[arguments.length];
				for (int i = 0; i < arguments.length; i++) {
					parameterTypes[i] = Signature.createTypeSignature(CharOperation.concatWith(arguments[i].type.getParameterizedTypeName(), '.'), false);
				}
				return new AdapterSourceMethod(parent, new String(method.selector), parameterTypes, method);
			}
			
			private AdapterSourceMethod(JavaElement parent, String name, String[] parameterTypes, MethodDeclaration md) {
				super(parent, name, parameterTypes);
				sourceMethodInfo = new AdapterSourceMethodInfo(this, md);
			}
			
			@Override public Object getElementInfo() throws JavaModelException {
				return sourceMethodInfo;
			}
			
			/**
			 * Disable refactoring for adapter methods
			 */
			@Override public boolean isReadOnly() {
				return true;
			}
			
			/**
			 * This is required to prevent duplicate entries in the outline
			 */
			@Override public boolean equals(Object o) {
				return this == o;
			}
			
			public static final class AdapterSourceMethodInfo extends SourceMethodInfo {
				AdapterSourceMethodInfo(AdapterSourceMethod adapterSourceMethod, MethodDeclaration md) {
					int pS = md.sourceStart;
					int pE = md.sourceEnd;
					
					Argument[] methodArguments = md.arguments != null ? md.arguments : new Argument[0];
					char[][] argumentNames = new char[methodArguments.length][];
					arguments = new ILocalVariable[methodArguments.length];
					for (int i = 0; i < methodArguments.length; i++) {
						Argument argument = methodArguments[i];
						argumentNames[i] = argument.name;
						arguments[i] = new LocalVariable(adapterSourceMethod, new String(argument.name), pS, pE, pS, pS, adapterSourceMethod.getParameterTypes()[i], argument.annotations, argument.modifiers, true);
					}
					setArgumentNames(argumentNames);
					
					setSourceRangeStart(pS);
					setSourceRangeEnd(pE);
					setNameSourceStart(pS);
					setNameSourceEnd(pE);
					
					setExceptionTypeNames(CharOperation.NO_CHAR_CHAR);
					setReturnType(md.returnType == null ? new char[]{'v', 'o','i', 'd'} : CharOperation.concatWith(md.returnType.getParameterizedTypeName(), '.'));
					setFlags(md.modifiers);
				}
			}
		}
		
		private static List<SourceMethod> getGeneratedMethods(SourceType sourceType) {
			if (sourceType != null) {
				CompilationUnit compilationUnit = getCompilationUnit(sourceType.getCompilationUnit());
				if (compilationUnit != null) {
					ConcurrentMap<String, List<SourceMethod>> map = EclipseAugments.CompilationUnit_adapterMethods.setIfAbsent(compilationUnit, new ConcurrentHashMap<String, List<SourceMethod>>());
					List<SourceMethod> newList = new ArrayList<SourceMethod>();
					List<SourceMethod> oldList = map.putIfAbsent(sourceType.getTypeQualifiedName(), newList);
					return oldList != null ? oldList : newList;
				}
			}
			return null;
		}
		
		private static SourceType getSourceType(CompilationUnitDeclaration cud, String typeName) {
			CompilationUnit compilationUnit = getCompilationUnit(cud);
			if (compilationUnit != null) {
				try {
					for (IType type : compilationUnit.getAllTypes()) {
						if (type instanceof SourceType && type.getTypeQualifiedName().equals(typeName)) {
							return (SourceType) type;
						}
					}
				} catch (JavaModelException e) {
					// Ignore
				}
			}
			return null;
		}
	}
	
	private static final class Reflection {
		public static final Method classScopeBuildFieldsAndMethodsMethod;
		public static final Throwable initCause;
		static {
			Method m = null;
			Throwable c = null;
			try {
				m = Permit.getMethod(ClassScope.class, "buildFieldsAndMethods");
			} catch (Throwable t) {
				c = t;
				// That's problematic, but as long as no local classes are used we don't actually need it.
				// Better fail on local classes than crash altogether.
			}
			
			classScopeBuildFieldsAndMethodsMethod = m;
			initCause = c;
		}
	}
	
	private static void addAllMethodBindings(List<BindingTuple> list, TypeBinding binding, Set<String> banList)  {
		if (binding instanceof SourceTypeBinding) {
			ClassScope scope = ((SourceTypeBinding) binding).scope;
			if (scope != null) scope.environment().globalOptions.storeAnnotations = true;
		}
		if (binding == null) return;
		
		TypeBinding inner;
		
		if (binding instanceof ParameterizedTypeBinding) {
			inner = ((ParameterizedTypeBinding) binding).genericType();
		} else {
			inner = binding;
		}
		
		if (inner instanceof SourceTypeBinding) {
			ClassScope cs = ((SourceTypeBinding)inner).scope;
			if (cs != null) {
				try {
					Permit.invoke(Reflection.initCause, Reflection.classScopeBuildFieldsAndMethodsMethod, cs);
				} catch (Exception e) {
					// See 'Reflection' class for why we ignore this exception.
				}
			}
		}
		
		if (!(binding instanceof ReferenceBinding)) return;
		
		ReferenceBinding rb = (ReferenceBinding) binding;
		
		MethodBinding[] parameterizedSigs = rb.availableMethods();
		MethodBinding[] baseSigs = parameterizedSigs;
		if (binding instanceof ParameterizedTypeBinding) {
			baseSigs = ((ParameterizedTypeBinding)binding).genericType().availableMethods();
			if (baseSigs.length != parameterizedSigs.length) {
				// The last known state of eclipse source says this can't happen, so we rely on it,
				// but if this invariant is broken, better to go with 'arg0' naming instead of crashing.
				baseSigs = parameterizedSigs;
			}
		}
		for (int i = 0; i < parameterizedSigs.length; i++) {
			MethodBinding mb = parameterizedSigs[i];
			String sig = printSig(mb);
			if (mb.isStatic()) continue;
			if (mb.isBridge()) continue;
			if (mb.isConstructor()) continue;
			if (!mb.isAbstract()) continue; // only the abstract methods are relevant for the Adapter
			if (mb.isDefaultAbstract()) continue;
			if (!mb.isPublic()) continue;
			if (mb.isSynthetic()) continue;
			if (!banList.add(sig)) continue; // If add returns false, it was already in there.
			BindingTuple pair = new BindingTuple(mb, baseSigs[i]);
			list.add(pair);
		}
		addAllMethodBindings(list, rb.superclass(), banList);
		ReferenceBinding[] interfaces = rb.superInterfaces();
		if (interfaces != null) {
			for (ReferenceBinding iface : interfaces) addAllMethodBindings(list, iface, banList);
		}
	}
	
	private static Set<String> findAlreadyImplementedMethods(TypeDeclaration decl) {
		Set<String> sigs = new HashSet<String>();
		for (AbstractMethodDeclaration md : decl.methods) {
			if (md.isStatic()) continue;
			if ((md.modifiers & ClassFileConstants.AccBridge) != 0) continue;
			if (md.isConstructor()) continue;
			if ((md.modifiers & ExtraCompilerModifiers.AccDefaultAbstract) != 0) continue;
			if ((md.modifiers & ClassFileConstants.AccPublic) == 0) continue;
			if ((md.modifiers & ClassFileConstants.AccSynthetic) != 0) continue;
			
			sigs.add(printSig(md, decl.scope));
		}
		return sigs;
	}
	
	private static final class BindingTuple {
		BindingTuple(MethodBinding parameterized, MethodBinding base) {
			this.parameterized = parameterized;
			this.base = base;
		}
		
		final MethodBinding parameterized, base;
		
		@Override public String toString() {
			return String.format("{param: %s, base: %s}", parameterized == null ? "(null)" : printSig(parameterized), base == null ? "(null)" : printSig(base));
		}
	}
	
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
	
	private static String printSig(AbstractMethodDeclaration md, ClassScope scope) {
		StringBuilder signature = new StringBuilder();
		
		signature.append(md.selector);
		signature.append("(");
		boolean first = true;
		if (md.arguments != null) {
			TypeParameter[] typeParameters = md.typeParameters();
			Map<String, TypeParameter> typeParametersMap = new HashMap<String, TypeParameter>();
			if (typeParameters != null) {
				for (TypeParameter typeParameter : typeParameters) {
					typeParametersMap.put(new String(typeParameter.name), typeParameter);
				}
			}
			
			for (Argument argument : md.arguments) {
				TypeBinding typeBinding = makeTypeBinding(argument.type, typeParametersMap, scope);
				
				if (!first) signature.append(", ");
				first = false;
				signature.append(typeBindingToSignature(typeBinding));
			}
		}
		signature.append(")");
		
		return signature.toString();
	}
	
	private static TypeBinding makeTypeBinding(TypeReference typeReference, Map<String, TypeParameter> typeParametersMap, ClassScope scope) {
		char[][] typeName = typeReference.getTypeName();
		String typeNameString = Eclipse.toQualifiedName(typeName);
		
		TypeParameter typeParameter = typeParametersMap.get(typeNameString);
		if (typeParameter != null) {
			if (typeParameter.type != null) {
				typeName = typeParameter.type.getTypeName();
			} else {
				typeName = TypeConstants.JAVA_LANG_OBJECT;
			}
		}
		
		TypeBinding typeBinding = scope.getType(typeName, typeName.length);
		if (typeReference.dimensions() > 0) {
			typeBinding = scope.createArrayType(typeBinding, typeReference.dimensions());
		}
		return typeBinding;
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
	
	private static class GeneratorOptions {
		private List<ClassLiteralAccess> overrideTypes;
		private ClassLiteralAccess exceptionClass;
		private String exceptionMsg;
		private boolean suppressThrows;
		private boolean silentMode;
		public static GeneratorOptions from(Annotation annotation) {
			GeneratorOptions ret = new GeneratorOptions();
			ret.overrideTypes = getTypes(annotation, "of");
			List<ClassLiteralAccess> throwExceptions = getTypes(annotation, "throwException");
			ret.exceptionClass = throwExceptions.isEmpty() ? null : throwExceptions.get(0);
			ret.exceptionMsg = getStringValue(annotation, "message");
			ret.suppressThrows = getBooleanValue(annotation, "suppressThrows");
			ret.silentMode = getBooleanValue(annotation, "silent");
			return ret;
		}

		private static List<ClassLiteralAccess> getTypes(Annotation ann, String name) {
			List<ClassLiteralAccess> rawTypes = new ArrayList<ClassLiteralAccess>();
			for (MemberValuePair pair : ann.memberValuePairs()) {
				if (charArrayEquals(name, pair.name)) {
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
			return rawTypes;
		}

		private static String getStringValue(Annotation ann, String name) {
			for (MemberValuePair pair : ann.memberValuePairs()) {
				if (charArrayEquals(name, pair.name)) {
					if (pair.value instanceof StringLiteral) {
						if (((StringLiteral)pair.value).constant != null)
							return ((StringLiteral)pair.value).constant.stringValue();
						return new String(((StringLiteral)pair.value).source());
					}
				}
			}
			return "";
		}

		private static boolean getBooleanValue(Annotation ann, String name) {
			for (MemberValuePair pair : ann.memberValuePairs()) {
				if (charArrayEquals(name, pair.name)) {
					return pair.value instanceof TrueLiteral;
				}
			}
			return false;
		}

	}

	// how to share this configuration with HandleAdapter to reduce duplication?
	public static final java.util.Map<String, Expression> DEFAULT_VALUE_MAP;
	static {
		Map<String, Expression> m = new HashMap<String, Expression>();
		m.put("boolean", new CastExpression(new FalseLiteral(0, 0), new SingleTypeReference(TypeConstants.BOOLEAN, 0L)));
		m.put("java.lang.Boolean", new FalseLiteral(0, 0));
		m.put("byte", new CastExpression(IntLiteral.buildIntLiteral("0".toCharArray(), 0, 0), new SingleTypeReference(TypeConstants.BYTE, 0L)));
		m.put("java.lang.Byte", new CastExpression(IntLiteral.buildIntLiteral("0".toCharArray(), 0, 0), new SingleTypeReference(TypeConstants.BYTE, 0L)));
		m.put("char", new CharLiteral("'\0'".toCharArray(), 0, 0));
		m.put("java.lang.Character", new CharLiteral("'\0'".toCharArray(), 0, 0));
		m.put("double", new CastExpression(new DoubleLiteral("0D".toCharArray(), 0, 0), new SingleTypeReference(TypeConstants.DOUBLE, 0L)));
		m.put("java.lang.Double", new DoubleLiteral("0D".toCharArray(), 0, 0));
		m.put("float", new CastExpression(new FloatLiteral("0F".toCharArray(), 0, 0), new SingleTypeReference(TypeConstants.FLOAT, 0L)));
		m.put("java.lang.Float", new FloatLiteral("0F".toCharArray(), 0, 0));
		m.put("int", IntLiteral.buildIntLiteral("0".toCharArray(), 0, 0));
		m.put("java.lang.Integer", IntLiteral.buildIntLiteral("0".toCharArray(), 0, 0));
		m.put("long", new CastExpression(LongLiteral.buildLongLiteral("0L".toCharArray(), 0, 0), new SingleTypeReference(TypeConstants.LONG, 0L)));
		m.put("java.lang.Long", LongLiteral.buildLongLiteral("0L".toCharArray(), 0, 0));
		m.put("short", new CastExpression(IntLiteral.buildIntLiteral("0".toCharArray(), 0, 0), new SingleTypeReference(TypeConstants.SHORT, 0L)));
		m.put("java.lang.Short", new CastExpression(IntLiteral.buildIntLiteral("0".toCharArray(), 0, 0), new SingleTypeReference(TypeConstants.SHORT, 0L)));
		m.put("java.lang.String", new NullLiteral(0, 0));
		DEFAULT_VALUE_MAP = Collections.unmodifiableMap(m);
	}
	public static final java.util.Map<String, String> DEFAULT_COLLECTIONS_METHOD;
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

}
