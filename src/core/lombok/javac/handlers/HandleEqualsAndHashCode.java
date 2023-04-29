/*
 * Copyright (C) 2009-2021 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.configuration.CallSuperType;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil;
import lombok.core.handlers.HandlerUtil.FieldAccess;
import lombok.core.handlers.InclusionExclusionUtils;
import lombok.core.handlers.InclusionExclusionUtils.Included;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.spi.Provides;

/**
 * Handles the {@code lombok.EqualsAndHashCode} annotation for javac.
 */
@Provides
public class HandleEqualsAndHashCode extends JavacAnnotationHandler<EqualsAndHashCode> {
	private static final String RESULT_NAME = "result";
	private static final String PRIME_NAME = "PRIME";
	private static final String HASH_CODE_CACHE_NAME = "$hashCodeCache";
	
	@Override public void handle(AnnotationValues<EqualsAndHashCode> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.EQUALS_AND_HASH_CODE_FLAG_USAGE, "@EqualsAndHashCode");
		
		deleteAnnotationIfNeccessary(annotationNode, EqualsAndHashCode.class);
		deleteImportFromCompilationUnit(annotationNode, CacheStrategy.class.getName());
		EqualsAndHashCode ann = annotation.getInstance();
		java.util.List<Included<JavacNode, EqualsAndHashCode.Include>> members = InclusionExclusionUtils.handleEqualsAndHashCodeMarking(annotationNode.up(), annotation, annotationNode);
		JavacNode typeNode = annotationNode.up();
		List<JCAnnotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@EqualsAndHashCode(onParam", annotationNode);
		
		Boolean callSuper = ann.callSuper();
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		
		Boolean doNotUseGettersConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.EQUALS_AND_HASH_CODE_DO_NOT_USE_GETTERS);
		boolean doNotUseGetters = annotation.isExplicit("doNotUseGetters") || doNotUseGettersConfiguration == null ? ann.doNotUseGetters() : doNotUseGettersConfiguration;
		FieldAccess fieldAccess = doNotUseGetters ? FieldAccess.PREFER_FIELD : FieldAccess.GETTER;
		
		boolean cacheHashCode = ann.cacheStrategy() == CacheStrategy.LAZY;
		
		generateMethods(typeNode, annotationNode, members, callSuper, true, cacheHashCode, fieldAccess, onParam);
	}
	
	public void generateEqualsAndHashCodeForType(JavacNode typeNode, JavacNode source) {
		if (hasAnnotation(EqualsAndHashCode.class, typeNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		Boolean doNotUseGettersConfiguration = typeNode.getAst().readConfiguration(ConfigurationKeys.EQUALS_AND_HASH_CODE_DO_NOT_USE_GETTERS);
		FieldAccess access = doNotUseGettersConfiguration == null || !doNotUseGettersConfiguration ? FieldAccess.GETTER : FieldAccess.PREFER_FIELD;
		
		java.util.List<Included<JavacNode, EqualsAndHashCode.Include>> members = InclusionExclusionUtils.handleEqualsAndHashCodeMarking(typeNode, null, null);
		
		generateMethods(typeNode, source, members, null, false, false, access, List.<JCAnnotation>nil());
	}
	
	public void generateMethods(JavacNode typeNode, JavacNode source, java.util.List<Included<JavacNode, EqualsAndHashCode.Include>> members,
		Boolean callSuper, boolean whineIfExists, boolean cacheHashCode, FieldAccess fieldAccess, List<JCAnnotation> onParam) {
		
		if (!isClass(typeNode)) {
			source.addError("@EqualsAndHashCode is only supported on a class.");
			return;
		}
		
		boolean implicitCallSuper = callSuper == null;
		if (callSuper == null) {
			try {
				callSuper = ((Boolean) EqualsAndHashCode.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {
				throw new InternalError("Lombok bug - this cannot happen - can't find callSuper field in EqualsAndHashCode annotation.");
			}
		}
		
		boolean isDirectDescendantOfObject = isDirectDescendantOfObject(typeNode);
		
		boolean isFinal = (((JCClassDecl) typeNode.get()).mods.flags & Flags.FINAL) != 0;
		boolean needsCanEqual = !isFinal || !isDirectDescendantOfObject;
		MemberExistsResult equalsExists = methodExists("equals", typeNode, 1);
		MemberExistsResult hashCodeExists = methodExists("hashCode", typeNode, 0);
		MemberExistsResult canEqualExists = methodExists("canEqual", typeNode, 1);
		switch (Collections.max(Arrays.asList(equalsExists, hashCodeExists))) {
		case EXISTS_BY_LOMBOK:
			return;
		case EXISTS_BY_USER:
			if (whineIfExists) {
				String msg = "Not generating equals and hashCode: A method with one of those names already exists. (Either both or none of these methods will be generated).";
				source.addWarning(msg);
			} else if (equalsExists == MemberExistsResult.NOT_EXISTS || hashCodeExists == MemberExistsResult.NOT_EXISTS) {
				// This means equals OR hashCode exists and not both.
				// Even though we should suppress the message about not generating these, this is such a weird and surprising situation we should ALWAYS generate a warning.
				// The user code couldn't possibly (barring really weird subclassing shenanigans) be in a shippable state anyway; the implementations of these 2 methods are
				// all inter-related and should be written by the same entity.
				String msg = String.format("Not generating %s: One of equals or hashCode exists. " +
					"You should either write both of these or none of these (in the latter case, lombok generates them).",
					equalsExists == MemberExistsResult.NOT_EXISTS ? "equals" : "hashCode");
				source.addWarning(msg);
			}
			return;
		case NOT_EXISTS:
		default:
			//fallthrough
		}
		
		if (isDirectDescendantOfObject && callSuper) {
			source.addError("Generating equals/hashCode with a supercall to java.lang.Object is pointless.");
			return;
		}
		
		if (implicitCallSuper && !isDirectDescendantOfObject) {
			CallSuperType cst = typeNode.getAst().readConfiguration(ConfigurationKeys.EQUALS_AND_HASH_CODE_CALL_SUPER);
			if (cst == null) cst = CallSuperType.WARN;
			
			switch (cst) {
			default:
			case WARN:
				source.addWarning("Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.");
				callSuper = false;
				break;
			case SKIP:
				callSuper = false;
				break;
			case CALL:
				callSuper = true;
				break;
			}
		}
		
		JCMethodDecl equalsMethod = createEquals(typeNode, members, callSuper, fieldAccess, needsCanEqual, source, onParam);
		
		injectMethod(typeNode, equalsMethod);
		
		if (needsCanEqual && canEqualExists == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl canEqualMethod = createCanEqual(typeNode, source, copyAnnotations(onParam));
			injectMethod(typeNode, canEqualMethod);
		}
		
		if (cacheHashCode){
			if (fieldExists(HASH_CODE_CACHE_NAME, typeNode) != MemberExistsResult.NOT_EXISTS) {
				String msg = String.format("Not caching the result of hashCode: A field named %s already exists.", HASH_CODE_CACHE_NAME);
				source.addWarning(msg);
				cacheHashCode = false;
			} else {
				createHashCodeCacheField(typeNode, source);
			}
		}
		
		JCMethodDecl hashCodeMethod = createHashCode(typeNode, members, callSuper, cacheHashCode, fieldAccess, source);
		injectMethod(typeNode, hashCodeMethod);
	}

	private void createHashCodeCacheField(JavacNode typeNode, JavacNode source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.PRIVATE | Flags.TRANSIENT);
		JCVariableDecl hashCodeCacheField = maker.VarDef(mods, typeNode.toName(HASH_CODE_CACHE_NAME), maker.TypeIdent(CTC_INT), null);
		injectFieldAndMarkGenerated(typeNode, hashCodeCacheField);
		recursiveSetGeneratedBy(hashCodeCacheField, source);
	}
	
	public JCMethodDecl createHashCode(JavacNode typeNode, java.util.List<Included<JavacNode, EqualsAndHashCode.Include>> members, boolean callSuper, boolean cacheHashCode, FieldAccess fieldAccess, JavacNode source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(typeNode, "Override"), List.<JCExpression>nil());
		List<JCAnnotation> annsOnMethod = List.of(overrideAnnotation);
		CheckerFrameworkVersion checkerFramework = getCheckerFrameworkVersion(typeNode);
		if (cacheHashCode && checkerFramework.generatePure()) {
			annsOnMethod = annsOnMethod.prepend(maker.Annotation(genTypeRef(typeNode, CheckerFrameworkVersion.NAME__PURE), List.<JCExpression>nil()));
		} else if (checkerFramework.generateSideEffectFree()) { 
			annsOnMethod = annsOnMethod.prepend(maker.Annotation(genTypeRef(typeNode, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil()));
		}
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, annsOnMethod);
		JCExpression returnType = maker.TypeIdent(CTC_INT);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		Name primeName = typeNode.toName(PRIME_NAME);
		Name resultName = typeNode.toName(RESULT_NAME);
		long finalFlag = JavacHandlerUtil.addFinalIfNeeded(0L, typeNode.getContext());
		
		boolean isEmpty = members.isEmpty();
		
		/* if (this.$hashCodeCache != 0) return this.$hashCodeCache; */ {
			if (cacheHashCode) {
				JCFieldAccess hashCodeCacheFieldAccess = createHashCodeCacheFieldAccess(typeNode, maker);
				JCExpression cacheNotZero = maker.Binary(CTC_NOT_EQUAL, hashCodeCacheFieldAccess, maker.Literal(CTC_INT, 0));
				hashCodeCacheFieldAccess = createHashCodeCacheFieldAccess(typeNode, maker);
				statements.append(maker.If(cacheNotZero, maker.Return(hashCodeCacheFieldAccess), null));
			}
		}
		
		/* final int PRIME = X; */ {
			if (!isEmpty) {
				statements.append(maker.VarDef(maker.Modifiers(finalFlag), primeName, maker.TypeIdent(CTC_INT), maker.Literal(HandlerUtil.primeForHashcode())));
			}
		}
		
		/* int result = ... */ {
			final JCExpression init;
			if (callSuper) {
				/* ... super.hashCode(); */
				init = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("hashCode")),
					List.<JCExpression>nil());
			} else {
				/* ... 1; */
				init = maker.Literal(1);
			}
			statements.append(maker.VarDef(maker.Modifiers(isEmpty && !cacheHashCode ? finalFlag : 0L), resultName, maker.TypeIdent(CTC_INT), init));
		}
		
		for (Included<JavacNode, EqualsAndHashCode.Include> member : members) {
			JavacNode memberNode = member.getNode();
			JCExpression fType = removeTypeUseAnnotations(getFieldType(memberNode, fieldAccess));
			boolean isMethod = memberNode.getKind() == Kind.METHOD;
			
			JCExpression fieldAccessor = isMethod ? createMethodAccessor(maker, memberNode) : createFieldAccessor(maker, memberNode, fieldAccess);
			if (fType instanceof JCPrimitiveTypeTree) {
				switch (((JCPrimitiveTypeTree) fType).getPrimitiveTypeKind()) {
				case BOOLEAN:
					/* this.fieldName ? X : Y */
					statements.append(createResultCalculation(typeNode, maker.Parens(maker.Conditional(fieldAccessor, 
						maker.Literal(HandlerUtil.primeForTrue()), maker.Literal(HandlerUtil.primeForFalse())))));
					break;
				case LONG: {
						Name dollarFieldName = memberNode.toName((isMethod ? "$$" : "$") + memberNode.getName());
						statements.append(maker.VarDef(maker.Modifiers(finalFlag), dollarFieldName, maker.TypeIdent(CTC_LONG), fieldAccessor));
						statements.append(createResultCalculation(typeNode, longToIntForHashCode(maker, maker.Ident(dollarFieldName), maker.Ident(dollarFieldName))));
					}
					break;
				case FLOAT:
					/* Float.floatToIntBits(this.fieldName) */
					statements.append(createResultCalculation(typeNode, maker.Apply(
						List.<JCExpression>nil(),
						genJavaLangTypeRef(typeNode, "Float", "floatToIntBits"),
						List.of(fieldAccessor))));
					break;
				case DOUBLE: {
						/* longToIntForHashCode(Double.doubleToLongBits(this.fieldName)) */
					Name dollarFieldName = memberNode.toName((isMethod ? "$$" : "$") + memberNode.getName());
						JCExpression init = maker.Apply(
							List.<JCExpression>nil(),
							genJavaLangTypeRef(typeNode, "Double", "doubleToLongBits"),
							List.of(fieldAccessor));
						statements.append(maker.VarDef(maker.Modifiers(finalFlag), dollarFieldName, maker.TypeIdent(CTC_LONG), init));
						statements.append(createResultCalculation(typeNode, longToIntForHashCode(maker, maker.Ident(dollarFieldName), maker.Ident(dollarFieldName))));
					}
					break;
				default:
				case BYTE:
				case SHORT:
				case INT:
				case CHAR:
					/* just the field */
					statements.append(createResultCalculation(typeNode, fieldAccessor));
					break;
				}
			} else if (fType instanceof JCArrayTypeTree) {
				JCArrayTypeTree array = (JCArrayTypeTree) fType;
				/* java.util.Arrays.deepHashCode(this.fieldName) //use just hashCode() for primitive arrays. */
				boolean multiDim = removeTypeUseAnnotations(array.elemtype) instanceof JCArrayTypeTree;
				boolean primitiveArray = removeTypeUseAnnotations(array.elemtype) instanceof JCPrimitiveTypeTree;
				boolean useDeepHC = multiDim || !primitiveArray;
				
				JCExpression hcMethod = chainDots(typeNode, "java", "util", "Arrays", useDeepHC ? "deepHashCode" : "hashCode");
				statements.append(createResultCalculation(typeNode, maker.Apply(List.<JCExpression>nil(), hcMethod, List.of(fieldAccessor))));
			} else /* objects */ {
				/* final java.lang.Object $fieldName = this.fieldName; */
				/* ($fieldName == null ? NULL_PRIME : $fieldName.hashCode()) */
				
				Name dollarFieldName = memberNode.toName((isMethod ? "$$" : "$") + memberNode.getName());
				statements.append(maker.VarDef(maker.Modifiers(finalFlag), dollarFieldName, genJavaLangTypeRef(typeNode, "Object"), fieldAccessor));
				
				JCExpression hcCall = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(dollarFieldName), typeNode.toName("hashCode")),
					List.<JCExpression>nil());
				JCExpression thisEqualsNull = maker.Binary(CTC_EQUAL, maker.Ident(dollarFieldName), maker.Literal(CTC_BOT, null));
				statements.append(createResultCalculation(typeNode, maker.Parens(maker.Conditional(thisEqualsNull, maker.Literal(HandlerUtil.primeForNull()), hcCall))));
			}
		}
		
		/* 
		 * if (result == 0) result = Integer.MIN_VALUE;
		 * this.$hashCodeCache = result;
		 * 
		 */ {
			if (cacheHashCode) {
				statements.append(maker.If(maker.Binary(CTC_EQUAL, maker.Ident(resultName), maker.Literal(CTC_INT, 0)), 
					maker.Exec(maker.Assign(maker.Ident(resultName), genJavaLangTypeRef(typeNode, "Integer", "MIN_VALUE"))), null));
				
				JCFieldAccess cacheHashCodeFieldAccess = createHashCodeCacheFieldAccess(typeNode, maker);
				statements.append(maker.Exec(maker.Assign(cacheHashCodeFieldAccess, maker.Ident(resultName))));
			}
		}
		
		/* return result; */ {
			statements.append(maker.Return(maker.Ident(resultName)));
		}
		
		JCBlock body = maker.Block(0, statements.toList());
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("hashCode"), returnType,
			List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null), source);
	}

	private JCFieldAccess createHashCodeCacheFieldAccess(JavacNode typeNode, JavacTreeMaker maker) {
		JCIdent receiver = maker.Ident(typeNode.toName("this"));
		JCFieldAccess cacheHashCodeFieldAccess = maker.Select(receiver, typeNode.toName(HASH_CODE_CACHE_NAME));
		return cacheHashCodeFieldAccess;
	}

	public JCExpressionStatement createResultCalculation(JavacNode typeNode, JCExpression expr) {
		/* result = result * PRIME + expr; */
		JavacTreeMaker maker = typeNode.getTreeMaker();
		Name resultName = typeNode.toName(RESULT_NAME);
		JCExpression mult = maker.Binary(CTC_MUL, maker.Ident(resultName), maker.Ident(typeNode.toName(PRIME_NAME)));
		JCExpression add = maker.Binary(CTC_PLUS, mult, expr);
		return maker.Exec(maker.Assign(maker.Ident(resultName), add));
	}
	
	/** The 2 references must be clones of each other. */
	public JCExpression longToIntForHashCode(JavacTreeMaker maker, JCExpression ref1, JCExpression ref2) {
		/* (int) (ref >>> 32 ^ ref) */
		JCExpression shift = maker.Binary(CTC_UNSIGNED_SHIFT_RIGHT, ref1, maker.Literal(32));
		JCExpression xorBits = maker.Binary(CTC_BITXOR, shift, ref2);
		return maker.TypeCast(maker.TypeIdent(CTC_INT), maker.Parens(xorBits));
	}
	
	public JCExpression createTypeReference(JavacNode type, boolean addWildcards) {
		java.util.List<String> list = new ArrayList<String>();
		java.util.List<Integer> genericsCount = addWildcards ? new ArrayList<Integer>() : null;
		
		list.add(type.getName());
		if (addWildcards) genericsCount.add(((JCClassDecl) type.get()).typarams.size());
		boolean staticContext = (((JCClassDecl) type.get()).getModifiers().flags & Flags.STATIC) != 0;
		JavacNode tNode = type.up();
		
		while (tNode != null && tNode.getKind() == Kind.TYPE && !tNode.getName().isEmpty()) {
			list.add(tNode.getName());
			if (addWildcards) genericsCount.add(staticContext ? 0 : ((JCClassDecl) tNode.get()).typarams.size());
			if (!staticContext) staticContext = (((JCClassDecl) tNode.get()).getModifiers().flags & Flags.STATIC) != 0;
			tNode = tNode.up();
		}
		Collections.reverse(list);
		if (addWildcards) Collections.reverse(genericsCount);
		
		JavacTreeMaker maker = type.getTreeMaker();
		
		JCExpression chain = maker.Ident(type.toName(list.get(0)));
		if (addWildcards) chain = wildcardify(maker, chain, genericsCount.get(0));
		
		for (int i = 1; i < list.size(); i++) {
			chain = maker.Select(chain, type.toName(list.get(i)));
			if (addWildcards) chain = wildcardify(maker, chain, genericsCount.get(i));
		}
		
		return chain;
	}
	
	private JCExpression wildcardify(JavacTreeMaker maker, JCExpression expr, int count) {
		if (count == 0) return expr;
		
		ListBuffer<JCExpression> wildcards = new ListBuffer<JCExpression>();
		for (int i = 0 ; i < count ; i++) {
			wildcards.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
		}
		
		return maker.TypeApply(expr, wildcards.toList());
	}
	
	public JCMethodDecl createEquals(JavacNode typeNode, java.util.List<Included<JavacNode, EqualsAndHashCode.Include>> members, boolean callSuper, FieldAccess fieldAccess, boolean needsCanEqual, JavacNode source, List<JCAnnotation> onParam) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		Name oName = typeNode.toName("o");
		Name otherName = typeNode.toName("other");
		Name thisName = typeNode.toName("this");
		
		List<JCAnnotation> annsOnParamOnMethod = List.nil();
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(typeNode, "Override"), List.<JCExpression>nil());
		List<JCAnnotation> annsOnMethod = List.of(overrideAnnotation);
		CheckerFrameworkVersion checkerFramework = getCheckerFrameworkVersion(typeNode);
		if (checkerFramework.generateSideEffectFree()) {
			annsOnMethod = annsOnMethod.prepend(maker.Annotation(genTypeRef(typeNode, CheckerFrameworkVersion.NAME__SIDE_EFFECT_FREE), List.<JCExpression>nil()));
		}
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, annsOnMethod);
		JCExpression objectType;
		if (annsOnParamOnMethod.isEmpty()) {
			objectType = genJavaLangTypeRef(typeNode, "Object");
		} else {
			objectType = chainDots(typeNode, "java", "lang", "Object");
			objectType = maker.AnnotatedType(annsOnParamOnMethod, objectType);
		}
		
		JCExpression returnType = maker.TypeIdent(CTC_BOOLEAN);
		
		long finalFlag = JavacHandlerUtil.addFinalIfNeeded(0L, typeNode.getContext());
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		JCVariableDecl param = maker.VarDef(maker.Modifiers(finalFlag | Flags.PARAMETER, onParam), oName, objectType, null);
		JavacHandlerUtil.createRelevantNullableAnnotation(typeNode, param);
		
		final List<JCVariableDecl> params = List.of(param);
		
		/* if (o == this) return true; */ {
			statements.append(maker.If(maker.Binary(CTC_EQUAL, maker.Ident(oName),
				maker.Ident(thisName)), returnBool(maker, true), null));
		}
		
		/* if (!(o instanceof Outer.Inner.MyType)) return false; */ {
			 
			JCUnary notInstanceOf = maker.Unary(CTC_NOT, maker.Parens(maker.TypeTest(maker.Ident(oName), createTypeReference(typeNode, false))));
			statements.append(maker.If(notInstanceOf, returnBool(maker, false), null));
		}
		
		/* Outer.Inner.MyType<?> other = (Outer.Inner.MyType<?>) o; */ {
			if (!members.isEmpty() || needsCanEqual) {
				final JCExpression selfType1 = createTypeReference(typeNode, true), selfType2 = createTypeReference(typeNode, true);
				
				statements.append(
					maker.VarDef(maker.Modifiers(finalFlag), otherName, selfType1, maker.TypeCast(selfType2, maker.Ident(oName))));
			}
		}
		
		/* if (!other.canEqual((java.lang.Object) this)) return false; */ {
			if (needsCanEqual) {
				List<JCExpression> exprNil = List.nil();
				JCExpression thisRef = maker.Ident(thisName);
				JCExpression castThisRef = maker.TypeCast(genJavaLangTypeRef(typeNode, "Object"), thisRef);
				JCExpression equalityCheck = maker.Apply(exprNil, 
					maker.Select(maker.Ident(otherName), typeNode.toName("canEqual")),
					List.of(castThisRef));
				statements.append(maker.If(maker.Unary(CTC_NOT, equalityCheck), returnBool(maker, false), null));
			}
		}
		
		/* if (!super.equals(o)) return false; */
		if (callSuper) {
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
				maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("equals")),
				List.<JCExpression>of(maker.Ident(oName)));
			JCUnary superNotEqual = maker.Unary(CTC_NOT, callToSuper);
			statements.append(maker.If(superNotEqual, returnBool(maker, false), null));
		}
		
		for (Included<JavacNode, EqualsAndHashCode.Include> member : members) {
			JavacNode memberNode = member.getNode();
			boolean isMethod = memberNode.getKind() == Kind.METHOD;
			
			JCExpression fType = removeTypeUseAnnotations(getFieldType(memberNode, fieldAccess));
			JCExpression thisFieldAccessor = isMethod ? createMethodAccessor(maker, memberNode) : createFieldAccessor(maker, memberNode, fieldAccess);
			JCExpression otherFieldAccessor = isMethod ? createMethodAccessor(maker, memberNode, maker.Ident(otherName)) : createFieldAccessor(maker, memberNode, fieldAccess, maker.Ident(otherName));
			if (fType instanceof JCPrimitiveTypeTree) {
				switch (((JCPrimitiveTypeTree)fType).getPrimitiveTypeKind()) {
				case FLOAT:
					/* if (Float.compare(this.fieldName, other.fieldName) != 0) return false; */
					statements.append(generateCompareFloatOrDouble(thisFieldAccessor, otherFieldAccessor, maker, typeNode, false));
					break;
				case DOUBLE:
					/* if (Double.compare(this.fieldName, other.fieldName) != 0) return false; */
					statements.append(generateCompareFloatOrDouble(thisFieldAccessor, otherFieldAccessor, maker, typeNode, true));
					break;
				default:
					/* if (this.fieldName != other.fieldName) return false; */
					statements.append(
							maker.If(maker.Binary(CTC_NOT_EQUAL, thisFieldAccessor, otherFieldAccessor), returnBool(maker, false), null));
					break;
				}
			} else if (fType instanceof JCArrayTypeTree) {
				JCArrayTypeTree array = (JCArrayTypeTree) fType;
				/* if (!java.util.Arrays.deepEquals(this.fieldName, other.fieldName)) return false; //use equals for primitive arrays. */
				boolean multiDim = removeTypeUseAnnotations(array.elemtype) instanceof JCArrayTypeTree;
				boolean primitiveArray = removeTypeUseAnnotations(array.elemtype) instanceof JCPrimitiveTypeTree;
				boolean useDeepEquals = multiDim || !primitiveArray;
				
				JCExpression eqMethod = chainDots(typeNode, "java", "util", "Arrays", useDeepEquals ? "deepEquals" : "equals");
				List<JCExpression> args = List.of(thisFieldAccessor, otherFieldAccessor);
				statements.append(maker.If(maker.Unary(CTC_NOT,
					maker.Apply(List.<JCExpression>nil(), eqMethod, args)), returnBool(maker, false), null));
			} else /* objects */ {
				/* final java.lang.Object this$fieldName = this.fieldName; */
				/* final java.lang.Object other$fieldName = other.fieldName; */
				/* if (this$fieldName == null ? other$fieldName != null : !this$fieldName.equals(other$fieldName)) return false; */
				Name thisDollarFieldName = memberNode.toName("this" + (isMethod ? "$$" : "$") + memberNode.getName());
				Name otherDollarFieldName = memberNode.toName("other" + (isMethod ? "$$" : "$") + memberNode.getName());
				
				statements.append(maker.VarDef(maker.Modifiers(finalFlag), thisDollarFieldName, genJavaLangTypeRef(typeNode, "Object"), thisFieldAccessor));
				statements.append(maker.VarDef(maker.Modifiers(finalFlag), otherDollarFieldName, genJavaLangTypeRef(typeNode, "Object"), otherFieldAccessor));

				JCExpression thisEqualsNull = maker.Binary(CTC_EQUAL, maker.Ident(thisDollarFieldName), maker.Literal(CTC_BOT, null));
				JCExpression otherNotEqualsNull = maker.Binary(CTC_NOT_EQUAL, maker.Ident(otherDollarFieldName), maker.Literal(CTC_BOT, null));
				JCExpression thisEqualsThat = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(thisDollarFieldName), typeNode.toName("equals")),
					List.<JCExpression>of(maker.Ident(otherDollarFieldName)));
				JCExpression fieldsAreNotEqual = maker.Conditional(thisEqualsNull, otherNotEqualsNull, maker.Unary(CTC_NOT, thisEqualsThat));
				statements.append(maker.If(fieldsAreNotEqual, returnBool(maker, false), null));
			}
		}
		
		/* return true; */ {
			statements.append(returnBool(maker, true));
		}
		
		JCBlock body = maker.Block(0, statements.toList());
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("equals"), returnType, List.<JCTypeParameter>nil(), params, List.<JCExpression>nil(), body, null), source);
	}

	public JCMethodDecl createCanEqual(JavacNode typeNode, JavacNode source, List<JCAnnotation> onParam) {
		/* protected boolean canEqual(final java.lang.Object other) {
		 *     return other instanceof Outer.Inner.MyType;
		 * }
		 */
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		List<JCAnnotation> annsOnMethod = List.nil();
		CheckerFrameworkVersion checkerFramework = getCheckerFrameworkVersion(typeNode);
		if (checkerFramework.generatePure()) {
			annsOnMethod = annsOnMethod.prepend(maker.Annotation(genTypeRef(typeNode, CheckerFrameworkVersion.NAME__PURE), List.<JCExpression>nil()));
		}
		JCModifiers mods = maker.Modifiers(Flags.PROTECTED, annsOnMethod);
		JCExpression returnType = maker.TypeIdent(CTC_BOOLEAN);
		Name canEqualName = typeNode.toName("canEqual");
		JCExpression objectType = genJavaLangTypeRef(typeNode, "Object");
		Name otherName = typeNode.toName("other");
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
		JCVariableDecl param = maker.VarDef(maker.Modifiers(flags, onParam), otherName, objectType, null);
		createRelevantNullableAnnotation(typeNode, param);
		List<JCVariableDecl> params = List.of(param);
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(
			maker.Return(maker.TypeTest(maker.Ident(otherName), createTypeReference(typeNode, false)))));
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, canEqualName, returnType, List.<JCTypeParameter>nil(), params, List.<JCExpression>nil(), body, null), source);
	}
	
	public JCStatement generateCompareFloatOrDouble(JCExpression thisDotField, JCExpression otherDotField,
		JavacTreeMaker maker, JavacNode node, boolean isDouble) {
		
		/* if (Float.compare(fieldName, other.fieldName) != 0) return false; */
		JCExpression clazz = genJavaLangTypeRef(node, isDouble ? "Double" : "Float");
		List<JCExpression> args = List.of(thisDotField, otherDotField);
		JCBinary compareCallEquals0 = maker.Binary(CTC_NOT_EQUAL, maker.Apply(
			List.<JCExpression>nil(), maker.Select(clazz, node.toName("compare")), args), maker.Literal(0));
		return maker.If(compareCallEquals0, returnBool(maker, false), null);
	}
	
	public JCStatement returnBool(JavacTreeMaker maker, boolean bool) {
		return maker.Return(maker.Literal(CTC_BOOLEAN, bool ? 1 : 0));
	}
}
