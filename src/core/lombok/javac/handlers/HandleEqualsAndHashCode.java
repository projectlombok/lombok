/*
 * Copyright (C) 2009-2013 The Project Lombok Authors.
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

import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import lombok.EqualsAndHashCode;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
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

/**
 * Handles the {@code lombok.EqualsAndHashCode} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleEqualsAndHashCode extends JavacAnnotationHandler<EqualsAndHashCode> {
	private static final String RESULT_NAME = "result";
	private static final String PRIME_NAME = "PRIME";
	
	private void checkForBogusFieldNames(JavacNode type, AnnotationValues<EqualsAndHashCode> annotation) {
		if (annotation.isExplicit("exclude")) {
			for (int i : createListOfNonExistentFields(List.from(annotation.getInstance().exclude()), type, true, true)) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		if (annotation.isExplicit("of")) {
			for (int i : createListOfNonExistentFields(List.from(annotation.getInstance().of()), type, false, false)) {
				annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}
	
	@Override public void handle(AnnotationValues<EqualsAndHashCode> annotation, JCAnnotation ast, JavacNode annotationNode) {
		deleteAnnotationIfNeccessary(annotationNode, EqualsAndHashCode.class);
		EqualsAndHashCode ann = annotation.getInstance();
		List<String> excludes = List.from(ann.exclude());
		List<String> includes = List.from(ann.of());
		JavacNode typeNode = annotationNode.up();
		
		checkForBogusFieldNames(typeNode, annotation);
		
		Boolean callSuper = ann.callSuper();
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		if (!annotation.isExplicit("exclude")) excludes = null;
		if (!annotation.isExplicit("of")) includes = null;
		
		if (excludes != null && includes != null) {
			excludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		
		FieldAccess fieldAccess = ann.doNotUseGetters() ? FieldAccess.PREFER_FIELD : FieldAccess.GETTER;
		
		generateMethods(typeNode, annotationNode, excludes, includes, callSuper, true, fieldAccess);
	}
	
	public void generateEqualsAndHashCodeForType(JavacNode typeNode, JavacNode source) {
		if (hasAnnotation(EqualsAndHashCode.class, typeNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}
		
		generateMethods(typeNode, source, null, null, null, false, FieldAccess.GETTER);
	}
	
	public void generateMethods(JavacNode typeNode, JavacNode source, List<String> excludes, List<String> includes,
			Boolean callSuper, boolean whineIfExists, FieldAccess fieldAccess) {
		boolean notAClass = true;
		if (typeNode.get() instanceof JCClassDecl) {
			long flags = ((JCClassDecl)typeNode.get()).mods.flags;
			notAClass = (flags & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		}
		
		if (notAClass) {
			source.addError("@EqualsAndHashCode is only supported on a class.");
			return;
		}
		
		boolean isDirectDescendantOfObject = true;
		boolean implicitCallSuper = callSuper == null;
		if (callSuper == null) {
			try {
				callSuper = ((Boolean)EqualsAndHashCode.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {
				throw new InternalError("Lombok bug - this cannot happen - can't find callSuper field in EqualsAndHashCode annotation.");
			}
		}
		
		JCTree extending = Javac.getExtendsClause((JCClassDecl)typeNode.get());
		if (extending != null) {
			String p = extending.toString();
			isDirectDescendantOfObject = p.equals("Object") || p.equals("java.lang.Object");
		}
		
		if (isDirectDescendantOfObject && callSuper) {
			source.addError("Generating equals/hashCode with a supercall to java.lang.Object is pointless.");
			return;
		}
		
		if (!isDirectDescendantOfObject && !callSuper && implicitCallSuper) {
			source.addWarning("Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.");
		}
		
		ListBuffer<JavacNode> nodesForEquality = new ListBuffer<JavacNode>();
		if (includes != null) {
			for (JavacNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
				if (includes.contains(fieldDecl.name.toString())) nodesForEquality.append(child);
			}
		} else {
			for (JavacNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
				//Skip static fields.
				if ((fieldDecl.mods.flags & Flags.STATIC) != 0) continue;
				//Skip transient fields.
				if ((fieldDecl.mods.flags & Flags.TRANSIENT) != 0) continue;
				//Skip excluded fields.
				if (excludes != null && excludes.contains(fieldDecl.name.toString())) continue;
				//Skip fields that start with $
				if (fieldDecl.name.toString().startsWith("$")) continue;
				nodesForEquality.append(child);
			}
		}
		
		boolean isFinal = (((JCClassDecl)typeNode.get()).mods.flags & Flags.FINAL) != 0;
		boolean needsCanEqual = !isFinal || !isDirectDescendantOfObject;
		MemberExistsResult equalsExists = methodExists("equals", typeNode, 1);
		MemberExistsResult hashCodeExists = methodExists("hashCode", typeNode, 0);
		MemberExistsResult canEqualExists = methodExists("canEqual", typeNode, 1);
		switch (Collections.max(Arrays.asList(equalsExists, hashCodeExists, canEqualExists))) {
		case EXISTS_BY_LOMBOK:
			return;
		case EXISTS_BY_USER:
			if (whineIfExists) {
				String msg = String.format("Not generating equals%s: A method with one of those names already exists. (Either all or none of these methods will be generated).", needsCanEqual ? ", hashCode and canEquals" : " and hashCode");
				source.addWarning(msg);
			} else if (equalsExists == MemberExistsResult.NOT_EXISTS || hashCodeExists == MemberExistsResult.NOT_EXISTS) {
				// This means equals OR hashCode exists and not both (or neither, but canEqual is there).
				// Even though we should suppress the message about not generating these, this is such a weird and surprising situation we should ALWAYS generate a warning.
				// The user code couldn't possibly (barring really weird subclassing shenanigans) be in a shippable state anyway; the implementations of these 3 methods are
				// all inter-related and should be written by the same entity.
				String msg = String.format("Not generating %s: One of equals, hashCode, and canEqual exists. " +
						"You should either write all of these or none of these (in the latter case, lombok generates them).",
						equalsExists == MemberExistsResult.NOT_EXISTS && hashCodeExists == MemberExistsResult.NOT_EXISTS ? "equals and hashCode" :
						equalsExists == MemberExistsResult.NOT_EXISTS ? "equals" : "hashCode");
				source.addWarning(msg);
			}
			return;
		case NOT_EXISTS:
		default:
			//fallthrough
		}
		
		JCMethodDecl equalsMethod = createEquals(typeNode, nodesForEquality.toList(), callSuper, fieldAccess, needsCanEqual, source.get());
		injectMethod(typeNode, equalsMethod);
		
		if (needsCanEqual) {
			JCMethodDecl canEqualMethod = createCanEqual(typeNode, source.get());
			injectMethod(typeNode, canEqualMethod);
		}
		
		JCMethodDecl hashCodeMethod = createHashCode(typeNode, nodesForEquality.toList(), callSuper, fieldAccess, source.get());
		injectMethod(typeNode, hashCodeMethod);
	}
	
	private JCMethodDecl createHashCode(JavacNode typeNode, List<JavacNode> fields, boolean callSuper, FieldAccess fieldAccess, JCTree source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(typeNode, "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression returnType = maker.TypeIdent(CTC_INT);
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		Name primeName = typeNode.toName(PRIME_NAME);
		Name resultName = typeNode.toName(RESULT_NAME);
		long finalFlag = JavacHandlerUtil.addFinalIfNeeded(0L, typeNode.getContext());
		
		/* final int PRIME = 31; */ {
			if (!fields.isEmpty() || callSuper) {
				statements.append(maker.VarDef(maker.Modifiers(finalFlag), primeName, maker.TypeIdent(CTC_INT), maker.Literal(31)));
			}
		}
		
		/* int result = 1; */ {
			statements.append(maker.VarDef(maker.Modifiers(0), resultName, maker.TypeIdent(CTC_INT), maker.Literal(1)));
		}
		
		if (callSuper) {
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("hashCode")),
					List.<JCExpression>nil());
			statements.append(createResultCalculation(typeNode, callToSuper));
		}
		
		Name dollar = typeNode.toName("$");
		for (JavacNode fieldNode : fields) {
			JCExpression fType = getFieldType(fieldNode, fieldAccess);
			JCExpression fieldAccessor = createFieldAccessor(maker, fieldNode, fieldAccess);
			if (fType instanceof JCPrimitiveTypeTree) {
				switch (((JCPrimitiveTypeTree)fType).getPrimitiveTypeKind()) {
				case BOOLEAN:
					/* this.fieldName ? 1231 : 1237 */
					statements.append(createResultCalculation(typeNode, maker.Conditional(fieldAccessor, maker.Literal(1231), maker.Literal(1237))));
					break;
				case LONG: {
						Name dollarFieldName = dollar.append(((JCVariableDecl)fieldNode.get()).name);
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
						Name dollarFieldName = dollar.append(((JCVariableDecl)fieldNode.get()).name);
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
				/* java.util.Arrays.deepHashCode(this.fieldName) //use just hashCode() for primitive arrays. */
				boolean multiDim = ((JCArrayTypeTree)fType).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)fType).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepHC = multiDim || !primitiveArray;
				
				JCExpression hcMethod = chainDots(typeNode, "java", "util", "Arrays", useDeepHC ? "deepHashCode" : "hashCode");
				statements.append(createResultCalculation(typeNode, maker.Apply(List.<JCExpression>nil(), hcMethod, List.of(fieldAccessor))));
			} else /* objects */ {
				/* final java.lang.Object $fieldName = this.fieldName; */
				/* $fieldName == null ? 0 : $fieldName.hashCode() */
				
				Name dollarFieldName = dollar.append(((JCVariableDecl)fieldNode.get()).name);
				statements.append(maker.VarDef(maker.Modifiers(finalFlag), dollarFieldName, genJavaLangTypeRef(typeNode, "Object"), fieldAccessor));
				
				JCExpression hcCall = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(dollarFieldName), typeNode.toName("hashCode")),
						List.<JCExpression>nil());
				JCExpression thisEqualsNull = maker.Binary(CTC_EQUAL, maker.Ident(dollarFieldName), maker.Literal(CTC_BOT, null));
				statements.append(createResultCalculation(typeNode, maker.Conditional(thisEqualsNull, maker.Literal(0), hcCall)));
			}
		}
		
		/* return result; */ {
			statements.append(maker.Return(maker.Ident(resultName)));
		}
		
		JCBlock body = maker.Block(0, statements.toList());
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("hashCode"), returnType,
				List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null), source, typeNode.getContext());
	}

	private JCExpressionStatement createResultCalculation(JavacNode typeNode, JCExpression expr) {
		/* result = result * PRIME + (expr); */
		JavacTreeMaker maker = typeNode.getTreeMaker();
		Name resultName = typeNode.toName(RESULT_NAME);
		JCExpression mult = maker.Binary(CTC_MUL, maker.Ident(resultName), maker.Ident(typeNode.toName(PRIME_NAME)));
		JCExpression add = maker.Binary(CTC_PLUS, mult, expr);
		return maker.Exec(maker.Assign(maker.Ident(resultName), add));
	}
	
	/** The 2 references must be clones of each other. */
	private JCExpression longToIntForHashCode(JavacTreeMaker maker, JCExpression ref1, JCExpression ref2) {
		/* (int)(ref >>> 32 ^ ref) */
		JCExpression shift = maker.Binary(CTC_UNSIGNED_SHIFT_RIGHT, ref1, maker.Literal(32));
		JCExpression xorBits = maker.Binary(CTC_BITXOR, shift, ref2);
		return maker.TypeCast(maker.TypeIdent(CTC_INT), xorBits);
	}
	
	private JCExpression createTypeReference(JavacNode type) {
		java.util.List<String> list = new ArrayList<String>();
		list.add(type.getName());
		JavacNode tNode = type.up();
		while (tNode != null && tNode.getKind() == Kind.TYPE) {
			list.add(tNode.getName());
			tNode = tNode.up();
		}
		Collections.reverse(list);
		
		JavacTreeMaker maker = type.getTreeMaker();
		JCExpression chain = maker.Ident(type.toName(list.get(0)));
		
		for (int i = 1; i < list.size(); i++) {
			chain = maker.Select(chain, type.toName(list.get(i)));
		}
		
		return chain;
	}
	
	private JCMethodDecl createEquals(JavacNode typeNode, List<JavacNode> fields, boolean callSuper, FieldAccess fieldAccess, boolean needsCanEqual, JCTree source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		Name oName = typeNode.toName("o");
		Name otherName = typeNode.toName("other");
		Name thisName = typeNode.toName("this");
		
		JCAnnotation overrideAnnotation = maker.Annotation(genJavaLangTypeRef(typeNode, "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression objectType = genJavaLangTypeRef(typeNode, "Object");
		JCExpression returnType = maker.TypeIdent(CTC_BOOLEAN);
		
		long finalFlag = JavacHandlerUtil.addFinalIfNeeded(0L, typeNode.getContext());
		
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		final List<JCVariableDecl> params = List.of(maker.VarDef(maker.Modifiers(finalFlag | Flags.PARAMETER), oName, objectType, null));
		
		/* if (o == this) return true; */ {
			statements.append(maker.If(maker.Binary(CTC_EQUAL, maker.Ident(oName),
					maker.Ident(thisName)), returnBool(maker, true), null));
		}
		
		/* if (!(o instanceof Outer.Inner.MyType) return false; */ {
			 
			JCUnary notInstanceOf = maker.Unary(CTC_NOT, maker.TypeTest(maker.Ident(oName), createTypeReference(typeNode)));
			statements.append(maker.If(notInstanceOf, returnBool(maker, false), null));
		}
		
		/* MyType<?> other = (MyType<?>) o; */ {
			if (!fields.isEmpty() || needsCanEqual) {
				final JCExpression selfType1, selfType2;
				ListBuffer<JCExpression> wildcards1 = new ListBuffer<JCExpression>();
				ListBuffer<JCExpression> wildcards2 = new ListBuffer<JCExpression>();
				for (int i = 0 ; i < type.typarams.length() ; i++) {
					wildcards1.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
					wildcards2.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
				}
				
				if (type.typarams.isEmpty()) {
					selfType1 = maker.Ident(type.name);
					selfType2 = maker.Ident(type.name);
				} else {
					selfType1 = maker.TypeApply(maker.Ident(type.name), wildcards1.toList());
					selfType2 = maker.TypeApply(maker.Ident(type.name), wildcards2.toList());
				}
				
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
		
		Name thisDollar = typeNode.toName("this$");
		Name otherDollar = typeNode.toName("other$");
		for (JavacNode fieldNode : fields) {
			JCExpression fType = getFieldType(fieldNode, fieldAccess);
			JCExpression thisFieldAccessor = createFieldAccessor(maker, fieldNode, fieldAccess);
			JCExpression otherFieldAccessor = createFieldAccessor(maker, fieldNode, fieldAccess, maker.Ident(otherName));
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
				/* if (!java.util.Arrays.deepEquals(this.fieldName, other.fieldName)) return false; //use equals for primitive arrays. */
				boolean multiDim = ((JCArrayTypeTree)fType).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)fType).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepEquals = multiDim || !primitiveArray;
				
				JCExpression eqMethod = chainDots(typeNode, "java", "util", "Arrays", useDeepEquals ? "deepEquals" : "equals");
				List<JCExpression> args = List.of(thisFieldAccessor, otherFieldAccessor);
				statements.append(maker.If(maker.Unary(CTC_NOT,
						maker.Apply(List.<JCExpression>nil(), eqMethod, args)), returnBool(maker, false), null));
			} else /* objects */ {
				/* final java.lang.Object this$fieldName = this.fieldName; */
				/* final java.lang.Object other$fieldName = other.fieldName; */
				/* if (this$fieldName == null ? other$fieldName != null : !this$fieldName.equals(other$fieldName)) return false;; */
				Name fieldName = ((JCVariableDecl)fieldNode.get()).name;
				Name thisDollarFieldName = thisDollar.append(fieldName);
				Name otherDollarFieldName = otherDollar.append(fieldName);
				
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
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("equals"), returnType, List.<JCTypeParameter>nil(), params, List.<JCExpression>nil(), body, null), source, typeNode.getContext());
	}

	private JCMethodDecl createCanEqual(JavacNode typeNode, JCTree source) {
		/* public boolean canEqual(final java.lang.Object other) {
		 *     return other instanceof Outer.Inner.MyType;
		 * }
		 */
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.<JCAnnotation>nil());
		JCExpression returnType = maker.TypeIdent(CTC_BOOLEAN);
		Name canEqualName = typeNode.toName("canEqual");
		JCExpression objectType = genJavaLangTypeRef(typeNode, "Object");
		Name otherName = typeNode.toName("other");
		long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
		List<JCVariableDecl> params = List.of(maker.VarDef(maker.Modifiers(flags), otherName, objectType, null));
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(
				maker.Return(maker.TypeTest(maker.Ident(otherName), createTypeReference(typeNode)))));
		
		return recursiveSetGeneratedBy(maker.MethodDef(mods, canEqualName, returnType, List.<JCTypeParameter>nil(), params, List.<JCExpression>nil(), body, null), source, typeNode.getContext());
	}
	
	private JCStatement generateCompareFloatOrDouble(JCExpression thisDotField, JCExpression otherDotField,
			JavacTreeMaker maker, JavacNode node, boolean isDouble) {
		/* if (Float.compare(fieldName, other.fieldName) != 0) return false; */
		JCExpression clazz = genJavaLangTypeRef(node, isDouble ? "Double" : "Float");
		List<JCExpression> args = List.of(thisDotField, otherDotField);
		JCBinary compareCallEquals0 = maker.Binary(CTC_NOT_EQUAL, maker.Apply(
				List.<JCExpression>nil(), maker.Select(clazz, node.toName("compare")), args), maker.Literal(0));
		return maker.If(compareCallEquals0, returnBool(maker, false), null);
	}
	
	private JCStatement returnBool(JavacTreeMaker maker, boolean bool) {
		return maker.Return(maker.Literal(CTC_BOOLEAN, bool ? 1 : 0));
	}
}
