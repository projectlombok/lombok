/*
 * Copyright © 2009-2010 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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

import static lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.EqualsAndHashCode;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@code lombok.EqualsAndHashCode} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleEqualsAndHashCode implements JavacAnnotationHandler<EqualsAndHashCode> {
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
	
	@Override public boolean handle(AnnotationValues<EqualsAndHashCode> annotation, JCAnnotation ast, JavacNode annotationNode) {
		markAnnotationAsProcessed(annotationNode, EqualsAndHashCode.class);
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
		
		return generateMethods(typeNode, annotationNode, excludes, includes, callSuper, true, ann.doNotUseGetters());
	}
	
	public void generateEqualsAndHashCodeForType(JavacNode typeNode, JavacNode errorNode) {
		for (JavacNode child : typeNode.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				if (Javac.annotationTypeMatches(EqualsAndHashCode.class, child)) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		generateMethods(typeNode, errorNode, null, null, null, false, false);
	}
	
	private boolean generateMethods(JavacNode typeNode, JavacNode errorNode, List<String> excludes, List<String> includes,
			Boolean callSuper, boolean whineIfExists, boolean useFieldsDirectly) {
		boolean notAClass = true;
		if (typeNode.get() instanceof JCClassDecl) {
			long flags = ((JCClassDecl)typeNode.get()).mods.flags;
			notAClass = (flags & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		}
		
		if (notAClass) {
			errorNode.addError("@EqualsAndHashCode is only supported on a class.");
			return false;
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
		
		JCTree extending = ((JCClassDecl)typeNode.get()).extending;
		if (extending != null) {
			String p = extending.toString();
			isDirectDescendantOfObject = p.equals("Object") || p.equals("java.lang.Object");
		}
		
		if (isDirectDescendantOfObject && callSuper) {
			errorNode.addError("Generating equals/hashCode with a supercall to java.lang.Object is pointless.");
			return true;
		}
		
		if (!isDirectDescendantOfObject && !callSuper && implicitCallSuper) {
			errorNode.addWarning("Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.");
		}
		
		List<JavacNode> nodesForEquality = List.nil();
		if (includes != null) {
			for (JavacNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
				if (includes.contains(fieldDecl.name.toString())) nodesForEquality = nodesForEquality.append(child);
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
				nodesForEquality = nodesForEquality.append(child);
			}
		}
		
		boolean needsCanEqual = false;
		switch (methodExists("equals", typeNode)) {
		case NOT_EXISTS:
			boolean isFinal = (((JCClassDecl)typeNode.get()).mods.flags & Flags.FINAL) != 0;
			needsCanEqual = !isFinal || !isDirectDescendantOfObject;
			
			JCMethodDecl method = createEquals(typeNode, nodesForEquality, callSuper, useFieldsDirectly, needsCanEqual);
			injectMethod(typeNode, method);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if (whineIfExists) {
				errorNode.addWarning("Not generating equals(Object other): A method with that name already exists");
			}
			break;
		}
		
		if (needsCanEqual) {
			switch (methodExists("canEqual", typeNode)) {
			case NOT_EXISTS:
				JCMethodDecl method = createCanEqual(typeNode);
				injectMethod(typeNode, method);
				break;
			case EXISTS_BY_LOMBOK:
			case EXISTS_BY_USER:
			default:
				break;
			}		
		}
		switch (methodExists("hashCode", typeNode)) {
		case NOT_EXISTS:
			JCMethodDecl method = createHashCode(typeNode, nodesForEquality, callSuper, useFieldsDirectly);
			injectMethod(typeNode, method);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if (whineIfExists) {
				errorNode.addWarning("Not generating hashCode(): A method with that name already exists");
			}
			break;
		}
		return true;
	}
	
	private JCMethodDecl createHashCode(JavacNode typeNode, List<JavacNode> fields, boolean callSuper, boolean useFieldsDirectly) {
		TreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(chainDots(maker, typeNode, "java", "lang", "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression returnType = maker.TypeIdent(TypeTags.INT);
		List<JCStatement> statements = List.nil();
		
		Name primeName = typeNode.toName("PRIME");
		Name resultName = typeNode.toName("result");
		/* final int PRIME = 31; */ {
			if (!fields.isEmpty() || callSuper) {
				statements = statements.append(maker.VarDef(maker.Modifiers(Flags.FINAL),
						primeName, maker.TypeIdent(TypeTags.INT), maker.Literal(31)));
			}
		}
		
		/* int result = 1; */ {
			statements = statements.append(maker.VarDef(maker.Modifiers(0), resultName, maker.TypeIdent(TypeTags.INT), maker.Literal(1)));
		}
		
		List<JCExpression> intoResult = List.nil();
		
		if (callSuper) {
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("hashCode")),
					List.<JCExpression>nil());
			intoResult = intoResult.append(callToSuper);
		}
		
		int tempCounter = 0;
		for (JavacNode fieldNode : fields) {
			JCExpression fType = getFieldType(fieldNode, useFieldsDirectly);
			JCExpression fieldAccessor = createFieldAccessor(maker, fieldNode, useFieldsDirectly);
			if (fType instanceof JCPrimitiveTypeTree) {
				switch (((JCPrimitiveTypeTree)fType).getPrimitiveTypeKind()) {
				case BOOLEAN:
					/* this.fieldName ? 1231 : 1237 */
					intoResult = intoResult.append(maker.Conditional(fieldAccessor, maker.Literal(1231), maker.Literal(1237)));
					break;
				case LONG:
					intoResult = intoResult.append(longToIntForHashCode(maker, fieldAccessor, createFieldAccessor(maker, fieldNode, useFieldsDirectly)));
					break;
				case FLOAT:
					/* Float.floatToIntBits(this.fieldName) */
					intoResult = intoResult.append(maker.Apply(
							List.<JCExpression>nil(),
							chainDots(maker, typeNode, "java", "lang", "Float", "floatToIntBits"),
							List.of(fieldAccessor)));
					break;
				case DOUBLE:
					/* longToIntForHashCode(Double.doubleToLongBits(this.fieldName)) */
					Name tempVar = typeNode.toName("temp" + (++tempCounter));
					JCExpression init = maker.Apply(
							List.<JCExpression>nil(),
							chainDots(maker, typeNode, "java", "lang", "Double", "doubleToLongBits"),
							List.of(fieldAccessor));
					statements = statements.append(
							maker.VarDef(maker.Modifiers(Flags.FINAL), tempVar, maker.TypeIdent(TypeTags.LONG), init));
					intoResult = intoResult.append(longToIntForHashCode(maker, maker.Ident(tempVar), maker.Ident(tempVar)));
					break;
				default:
				case BYTE:
				case SHORT:
				case INT:
				case CHAR:
					/* just the field */
					intoResult = intoResult.append(fieldAccessor);
					break;
				}
			} else if (fType instanceof JCArrayTypeTree) {
				/* java.util.Arrays.deepHashCode(this.fieldName) //use just hashCode() for primitive arrays. */
				boolean multiDim = ((JCArrayTypeTree)fType).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)fType).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepHC = multiDim || !primitiveArray;
				
				JCExpression hcMethod = chainDots(maker, typeNode, "java", "util", "Arrays", useDeepHC ? "deepHashCode" : "hashCode");
				intoResult = intoResult.append(
						maker.Apply(List.<JCExpression>nil(), hcMethod, List.of(fieldAccessor)));
			} else /* objects */ {
				/* this.fieldName == null ? 0 : this.fieldName.hashCode() */
				JCExpression hcCall = maker.Apply(List.<JCExpression>nil(), maker.Select(createFieldAccessor(maker, fieldNode, useFieldsDirectly), typeNode.toName("hashCode")),
						List.<JCExpression>nil());
				JCExpression thisEqualsNull = maker.Binary(JCTree.EQ, fieldAccessor, maker.Literal(TypeTags.BOT, null));
				intoResult = intoResult.append(
						maker.Conditional(thisEqualsNull, maker.Literal(0), hcCall));
			}
		}
		
		/* fold each intoResult entry into:
		   result = result * PRIME + (item); */
		for (JCExpression expr : intoResult) {
			JCExpression mult = maker.Binary(JCTree.MUL, maker.Ident(resultName), maker.Ident(primeName));
			JCExpression add = maker.Binary(JCTree.PLUS, mult, expr);
			statements = statements.append(maker.Exec(maker.Assign(maker.Ident(resultName), add)));
		}
		
		/* return result; */ {
			statements = statements.append(maker.Return(maker.Ident(resultName)));
		}
		
		JCBlock body = maker.Block(0, statements);
		return maker.MethodDef(mods, typeNode.toName("hashCode"), returnType,
				List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	/** The 2 references must be clones of each other. */
	private JCExpression longToIntForHashCode(TreeMaker maker, JCExpression ref1, JCExpression ref2) {
		/* (int)(ref >>> 32 ^ ref) */
		JCExpression shift = maker.Binary(JCTree.USR, ref1, maker.Literal(32));
		JCExpression xorBits = maker.Binary(JCTree.BITXOR, shift, ref2);
		return maker.TypeCast(maker.TypeIdent(TypeTags.INT), xorBits);
	}
	
	private JCMethodDecl createEquals(JavacNode typeNode, List<JavacNode> fields, boolean callSuper, boolean useFieldsDirectly, boolean needsCanEqual) {
		TreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		Name oName = typeNode.toName("o");
		Name otherName = typeNode.toName("other");
		Name thisName = typeNode.toName("this");
		
		JCAnnotation overrideAnnotation = maker.Annotation(chainDots(maker, typeNode, "java", "lang", "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression objectType = chainDots(maker, typeNode, "java", "lang", "Object");
		JCExpression returnType = maker.TypeIdent(TypeTags.BOOLEAN);
		
		List<JCStatement> statements = List.nil();
		List<JCVariableDecl> params = List.of(maker.VarDef(maker.Modifiers(Flags.FINAL), oName, objectType, null));
		
		/* if (o == this) return true; */ {
			statements = statements.append(maker.If(maker.Binary(JCTree.EQ, maker.Ident(oName),
					maker.Ident(thisName)), returnBool(maker, true), null));
		}
		
		/* if (!(o instanceof MyType) return false; */ {
			JCUnary notInstanceOf = maker.Unary(JCTree.NOT, maker.TypeTest(maker.Ident(oName), maker.Ident(type.name)));
			statements = statements.append(maker.If(notInstanceOf, returnBool(maker, false), null));
		}
		
		/* MyType<?> other = (MyType<?>) o; */ {
			final JCExpression selfType1, selfType2;
			List<JCExpression> wildcards1 = List.nil();
			List<JCExpression> wildcards2 = List.nil();
			for (int i = 0 ; i < type.typarams.length() ; i++) {
				wildcards1 = wildcards1.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
				wildcards2 = wildcards2.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
			}
			
			if (type.typarams.isEmpty()) {
				selfType1 = maker.Ident(type.name);
				selfType2 = maker.Ident(type.name);
			} else {
				selfType1 = maker.TypeApply(maker.Ident(type.name), wildcards1);
				selfType2 = maker.TypeApply(maker.Ident(type.name), wildcards2);
			}
			
			statements = statements.append(
					maker.VarDef(maker.Modifiers(Flags.FINAL), otherName, selfType1, maker.TypeCast(selfType2, maker.Ident(oName))));
		}
		
		/* if (!other.canEqual(this)) return false; */ {
			if (needsCanEqual) {
				List<JCExpression> exprNil = List.nil();
				JCExpression equalityCheck = maker.Apply(exprNil, 
						maker.Select(maker.Ident(otherName), typeNode.toName("canEqual")),
						List.<JCExpression>of(maker.Ident(thisName)));
				statements = statements.append(maker.If(maker.Unary(JCTree.NOT, equalityCheck), returnBool(maker, false), null));
			}
		}
		
		/* if (!super.equals(o)) return false; */
		if (callSuper) {
			JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
					maker.Select(maker.Ident(typeNode.toName("super")), typeNode.toName("equals")),
					List.<JCExpression>of(maker.Ident(oName)));
			JCUnary superNotEqual = maker.Unary(JCTree.NOT, callToSuper);
			statements = statements.append(maker.If(superNotEqual, returnBool(maker, false), null));
		}
		
		for (JavacNode fieldNode : fields) {
			JCExpression fType = getFieldType(fieldNode, useFieldsDirectly);
			JCExpression thisFieldAccessor = createFieldAccessor(maker, fieldNode, useFieldsDirectly);
			JCExpression otherFieldAccessor = createFieldAccessor(maker, fieldNode, useFieldsDirectly, maker.Ident(otherName));
			if (fType instanceof JCPrimitiveTypeTree) {
				switch (((JCPrimitiveTypeTree)fType).getPrimitiveTypeKind()) {
				case FLOAT:
					/* if (Float.compare(this.fieldName, other.fieldName) != 0) return false; */
					statements = statements.append(generateCompareFloatOrDouble(thisFieldAccessor, otherFieldAccessor, maker, typeNode, false));
					break;
				case DOUBLE:
					/* if (Double(this.fieldName, other.fieldName) != 0) return false; */
					statements = statements.append(generateCompareFloatOrDouble(thisFieldAccessor, otherFieldAccessor, maker, typeNode, true));
					break;
				default:
					/* if (this.fieldName != other.fieldName) return false; */
					statements = statements.append(
							maker.If(maker.Binary(JCTree.NE, thisFieldAccessor, otherFieldAccessor), returnBool(maker, false), null));
					break;
				}
			} else if (fType instanceof JCArrayTypeTree) {
				/* if (!java.util.Arrays.deepEquals(this.fieldName, other.fieldName)) return false; //use equals for primitive arrays. */
				boolean multiDim = ((JCArrayTypeTree)fType).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)fType).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepEquals = multiDim || !primitiveArray;
				
				JCExpression eqMethod = chainDots(maker, typeNode, "java", "util", "Arrays", useDeepEquals ? "deepEquals" : "equals");
				List<JCExpression> args = List.of(thisFieldAccessor, otherFieldAccessor);
				statements = statements.append(maker.If(maker.Unary(JCTree.NOT,
						maker.Apply(List.<JCExpression>nil(), eqMethod, args)), returnBool(maker, false), null));
			} else /* objects */ {
				/* if (this.fieldName == null ? other.fieldName != null : !this.fieldName.equals(other.fieldName)) return false; */
				JCExpression thisEqualsNull = maker.Binary(JCTree.EQ, thisFieldAccessor, maker.Literal(TypeTags.BOT, null));
				JCExpression otherNotEqualsNull = maker.Binary(JCTree.NE, otherFieldAccessor, maker.Literal(TypeTags.BOT, null));
				JCExpression thisEqualsThat = maker.Apply(List.<JCExpression>nil(),
						maker.Select(createFieldAccessor(maker, fieldNode, useFieldsDirectly), typeNode.toName("equals")),
						List.of(createFieldAccessor(maker, fieldNode, useFieldsDirectly, maker.Ident(otherName))));
				JCExpression fieldsAreNotEqual = maker.Conditional(thisEqualsNull, otherNotEqualsNull, maker.Unary(JCTree.NOT, thisEqualsThat));
				statements = statements.append(maker.If(fieldsAreNotEqual, returnBool(maker, false), null));
			}
		}
		
		/* return true; */ {
			statements = statements.append(returnBool(maker, true));
		}
		
		JCBlock body = maker.Block(0, statements);
		return maker.MethodDef(mods, typeNode.toName("equals"), returnType, List.<JCTypeParameter>nil(), params, List.<JCExpression>nil(), body, null);
	}

	private JCMethodDecl createCanEqual(JavacNode typeNode) {
		/* public boolean canEquals(final java.lang.Object other) {
		 *     return other instanceof MyType;
		 * }
		 */
		TreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.<JCAnnotation>nil());
		JCExpression returnType = maker.TypeIdent(TypeTags.BOOLEAN);
		Name canEqualName = typeNode.toName("canEqual");
		JCExpression objectType = chainDots(maker, typeNode, "java", "lang", "Object");
		Name otherName = typeNode.toName("other");
		List<JCVariableDecl> params = List.of(maker.VarDef(maker.Modifiers(Flags.FINAL), otherName, objectType, null));
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(
				maker.Return(maker.TypeTest(maker.Ident(otherName), maker.Ident(type.name)))));
		
		return maker.MethodDef(mods, canEqualName, returnType, List.<JCTypeParameter>nil(), params, List.<JCExpression>nil(), body, null);
	}
	
	private JCStatement generateCompareFloatOrDouble(JCExpression thisDotField, JCExpression otherDotField,
			TreeMaker maker, JavacNode node, boolean isDouble) {
		/* if (Float.compare(fieldName, other.fieldName) != 0) return false; */
		JCExpression clazz = chainDots(maker, node, "java", "lang", isDouble ? "Double" : "Float");
		List<JCExpression> args = List.of(thisDotField, otherDotField);
		JCBinary compareCallEquals0 = maker.Binary(JCTree.NE, maker.Apply(
				List.<JCExpression>nil(), maker.Select(clazz, node.toName("compare")), args), maker.Literal(0));
		return maker.If(compareCallEquals0, returnBool(maker, false), null);
	}
	
	private JCStatement returnBool(TreeMaker maker, boolean bool) {
		return maker.Return(maker.Literal(TypeTags.BOOLEAN, bool ? 1 : 0));
	}
}
