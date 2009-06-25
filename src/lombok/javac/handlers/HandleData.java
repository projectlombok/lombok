package lombok.javac.handlers;

import java.lang.reflect.Modifier;

import static lombok.javac.handlers.PKG.*;

import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleData implements JavacAnnotationHandler<Data> {
	@Override public boolean handle(AnnotationValues<Data> annotation, JCAnnotation ast, Node annotationNode) {
		Node typeNode = annotationNode.up();
		JCClassDecl typeDecl = null;
		if ( typeNode.get() instanceof JCClassDecl ) typeDecl = (JCClassDecl)typeNode.get();
		long flags = typeDecl.mods.flags;
		boolean notAClass = (flags & (Flags.INTERFACE | Flags.ENUM | Flags.ANNOTATION)) != 0;
		
		if ( typeDecl == null || notAClass ) {
			annotationNode.addError("@Data is only supported on a class.");
			return false;
		}
		
		List<Node> nodesForEquality = List.nil();
		List<Node> nodesForConstructorAndToString = List.nil();
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() != Kind.FIELD ) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
			long fieldFlags = fieldDecl.mods.flags;
			//Skip static fields.
			if ( (fieldFlags & Flags.STATIC) != 0 ) continue;
			if ( (fieldFlags & Flags.TRANSIENT) == 0 ) nodesForEquality = nodesForEquality.append(child);
			nodesForConstructorAndToString = nodesForConstructorAndToString.append(child);
			new HandleGetter().generateGetterForField(child, annotationNode.get());
			if ( (fieldFlags & Flags.FINAL) == 0 )
				new HandleSetter().generateSetterForField(child, annotationNode.get());
		}
		
		String staticConstructorName = annotation.getInstance().staticConstructor();
		
		if ( constructorExists(typeNode) == MethodExistsResult.NOT_EXISTS ) {
			JCMethodDecl constructor = createConstructor(staticConstructorName.equals(""), typeNode, nodesForConstructorAndToString);
			injectMethod(typeNode, constructor);
		}
		
		if ( !staticConstructorName.isEmpty() && methodExists("of", typeNode) == MethodExistsResult.NOT_EXISTS ) {
			JCMethodDecl staticConstructor = createStaticConstructor(staticConstructorName, typeNode, nodesForConstructorAndToString);
			injectMethod(typeNode, staticConstructor);
		}
		
		if ( methodExists("equals", typeNode) == MethodExistsResult.NOT_EXISTS ) {
			JCMethodDecl method = createEquals(typeNode, nodesForEquality);
			injectMethod(typeNode, method);
		}
		
		if ( methodExists("hashCode", typeNode) == MethodExistsResult.NOT_EXISTS ) {
			JCMethodDecl method = createHashCode(typeNode, nodesForEquality);
			injectMethod(typeNode, method);
		}
		
		//TODO toString.
		return true;
	}
	
	private JCMethodDecl createHashCode(Node typeNode, List<Node> fields) {
		TreeMaker maker = typeNode.getTreeMaker();
		
		JCAnnotation overrideAnnotation = maker.Annotation(chainDots(maker, typeNode, "java", "lang", "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression returnType = maker.TypeIdent(TypeTags.INT);
		List<JCStatement> statements = List.nil();
		
		Name primeName = typeNode.toName("PRIME");
		Name resultName = typeNode.toName("result");
		/* final int PRIME = 31; */ {
			if ( !fields.isEmpty() ) {
				statements = statements.append(
						maker.VarDef(maker.Modifiers(Flags.FINAL), primeName, maker.TypeIdent(TypeTags.INT), maker.Literal(31)));
			}
		}
		
		/* int result = 1; */ {
			statements = statements.append(maker.VarDef(maker.Modifiers(0), resultName, maker.TypeIdent(TypeTags.INT), maker.Literal(1)));
		}
		
		List<JCExpression> intoResult = List.nil();
		
		int tempCounter = 0;
		for ( Node fieldNode : fields ) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			JCExpression fType = field.vartype;
			JCExpression thisDotField = maker.Select(maker.Ident(typeNode.toName("this")), field.name);
			JCExpression thisDotFieldClone = maker.Select(maker.Ident(typeNode.toName("this")), field.name);
			if ( fType instanceof JCPrimitiveTypeTree ) {
				switch ( ((JCPrimitiveTypeTree)fType).getPrimitiveTypeKind() ) {
				case BOOLEAN:
					/* this.fieldName ? 1231 : 1237 */
					intoResult = intoResult.append(maker.Conditional(thisDotField, maker.Literal(1231), maker.Literal(1237)));
					break;
				case LONG:
					intoResult = intoResult.append(longToIntForHashCode(maker, thisDotField, thisDotFieldClone));
					break;
				case FLOAT:
					/* Float.floatToIntBits(this.fieldName) */
					intoResult = intoResult.append(maker.Apply(
							List.<JCExpression>nil(),
							chainDots(maker, typeNode, "java", "lang", "Float", "floatToIntBits"),
							List.of(thisDotField)));
					break;
				case DOUBLE:
					/* longToIntForHashCode(Double.doubleToLongBits(this.fieldName)) */
					Name tempVar = typeNode.toName("temp" + (++tempCounter));
					JCExpression init = maker.Apply(
							List.<JCExpression>nil(),
							chainDots(maker, typeNode, "java", "lang", "Double", "doubleToLongBits"),
							List.of(thisDotField));
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
					intoResult = intoResult.append(thisDotField);
					break;
				}
			} else if ( fType instanceof JCArrayTypeTree ) {
				/* java.util.Arrays.deepHashCode(this.fieldName) //use just hashCode() for primitive arrays. */
				boolean multiDim = ((JCArrayTypeTree)fType).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)fType).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepEquals = multiDim || !primitiveArray;
				
				JCExpression hcMethod = chainDots(maker, typeNode, "java", "util", "Arrays", useDeepEquals ? "deepHashCode" : "hashCode");
				intoResult = intoResult.append(
						maker.Apply(List.<JCExpression>nil(), hcMethod, List.of(thisDotField)));
			} else /* objects */ {
				/* this.fieldName == null ? 0 : this.fieldName.hashCode() */
				JCExpression hcCall = maker.Apply(List.<JCExpression>nil(), maker.Select(thisDotField, typeNode.toName("hashCode")),
						List.<JCExpression>nil());
				JCExpression thisEqualsNull = maker.Binary(JCTree.EQ, thisDotField, maker.Literal(TypeTags.BOT, null));
				intoResult = intoResult.append(
						maker.Conditional(thisEqualsNull, maker.Literal(0), hcCall));
			}
		}
		
		/* fold each intoResult entry into:
		   result = result * PRIME + (item); */
		for ( JCExpression expr : intoResult ) {
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
	
	private JCMethodDecl createEquals(Node typeNode, List<Node> fields) {
		TreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		Name oName = typeNode.toName("o");
		Name otherName = typeNode.toName("other");
		Name thisName = typeNode.toName("this");
		
		JCAnnotation overrideAnnotation = maker.Annotation(chainDots(maker, typeNode, "java", "lang", "Override"), List.<JCExpression>nil());
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation));
		JCExpression objectType = maker.Type(typeNode.getSymbolTable().objectType);
		JCExpression returnType = maker.TypeIdent(TypeTags.BOOLEAN);
		
		List<JCStatement> statements = List.nil();
		List<JCVariableDecl> params = List.of(maker.VarDef(maker.Modifiers(0), oName, objectType, null));
		
		/* if ( o == this ) return true; */ {
			statements = statements.append(
					maker.If(maker.Binary(JCTree.EQ, maker.Ident(oName), maker.Ident(thisName)), returnBool(maker, true), null));
		}
		
		/* if ( o == null ) return false; */ {
			statements = statements.append(
					maker.If(maker.Binary(JCTree.EQ, maker.Ident(oName), maker.Literal(TypeTags.BOT, null)), returnBool(maker, false), null));
		}
		
		/* if ( o.getClass() != this.getClass() ) return false; */ {
			Name getClass = typeNode.toName("getClass");
			List<JCExpression> exprNil = List.nil();
			JCExpression oGetClass = maker.Apply(exprNil, maker.Select(maker.Ident(oName), getClass), exprNil);
			JCExpression thisGetClass = maker.Apply(exprNil, maker.Select(maker.Ident(thisName), getClass), exprNil);
			statements = statements.append(
					maker.If(maker.Binary(JCTree.NE, oGetClass, thisGetClass), returnBool(maker, false), null));
		}
		
		/* MyType<?> other = (MyType<?>) o; */ {
			final JCExpression selfType1, selfType2;
			List<JCExpression> wildcards1 = List.nil();
			List<JCExpression> wildcards2 = List.nil();
			for ( int i = 0 ; i < type.typarams.length() ; i++ ) {
				wildcards1 = wildcards1.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
				wildcards2 = wildcards2.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
			}
			
			if ( type.typarams.isEmpty() ) {
				selfType1 = maker.Ident(type.name);
				selfType2 = maker.Ident(type.name);
			} else {
				selfType1 = maker.TypeApply(maker.Ident(type.name), wildcards1);
				selfType2 = maker.TypeApply(maker.Ident(type.name), wildcards2);
			}
			
			statements = statements.append(
					maker.VarDef(maker.Modifiers(Flags.FINAL), otherName, selfType1, maker.TypeCast(selfType2, maker.Ident(oName))));
		}
		
		for ( Node fieldNode : fields ) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			JCExpression fType = field.vartype;
			JCExpression thisDotField = maker.Select(maker.Ident(thisName), field.name);
			JCExpression otherDotField = maker.Select(maker.Ident(otherName), field.name);
			if ( fType instanceof JCPrimitiveTypeTree ) {
				switch ( ((JCPrimitiveTypeTree)fType).getPrimitiveTypeKind() ) {
				case FLOAT:
					/* if ( Float.compare(this.fieldName, other.fieldName) != 0 ) return false; */
					statements = statements.append(generateCompareFloatOrDouble(thisDotField, otherDotField, maker, typeNode, false));
					break;
				case DOUBLE:
					/* if ( Double(this.fieldName, other.fieldName) != 0 ) return false; */
					statements = statements.append(generateCompareFloatOrDouble(thisDotField, otherDotField, maker, typeNode, true));
					break;
				default:
					/* if ( this.fieldName != other.fieldName ) return false; */
					statements = statements.append(
							maker.If(maker.Binary(JCTree.NE, thisDotField, otherDotField), returnBool(maker, false), null));
					break;
				}
			} else if ( fType instanceof JCArrayTypeTree ) {
				/* if ( !java.util.Arrays.deepEquals(this.fieldName, other.fieldName) ) return false; //use equals for primitive arrays. */
				boolean multiDim = ((JCArrayTypeTree)fType).elemtype instanceof JCArrayTypeTree;
				boolean primitiveArray = ((JCArrayTypeTree)fType).elemtype instanceof JCPrimitiveTypeTree;
				boolean useDeepEquals = multiDim || !primitiveArray;
				
				JCExpression eqMethod = chainDots(maker, typeNode, "java", "util", "Arrays", useDeepEquals ? "deepEquals" : "equals");
				List<JCExpression> args = List.of(thisDotField, otherDotField);
				statements = statements.append(maker.If(maker.Unary(JCTree.NOT,
						maker.Apply(List.<JCExpression>nil(), eqMethod, args)), returnBool(maker, false), null));
			} else /* objects */ {
				/* if ( this.fieldName == null ? other.fieldName != null : !this.fieldName.equals(other.fieldName) ) return false; */
				JCExpression thisEqualsNull = maker.Binary(JCTree.EQ, thisDotField, maker.Literal(TypeTags.BOT, null));
				JCExpression otherNotEqualsNull = maker.Binary(JCTree.NE, otherDotField, maker.Literal(TypeTags.BOT, null));
				JCExpression thisEqualsThat = maker.Apply(
						List.<JCExpression>nil(), maker.Select(thisDotField, typeNode.toName("equals")), List.of(otherDotField));
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
	
	private JCStatement generateCompareFloatOrDouble(JCExpression thisDotField, JCExpression otherDotField, TreeMaker maker, Node node, boolean isDouble) {
		/* if ( Float.compare(fieldName, other.fieldName) != 0 ) return false; */
		JCExpression clazz = chainDots(maker, node, "java", "lang", isDouble ? "Double" : "Float");
		List<JCExpression> args = List.of(thisDotField, otherDotField);
		JCBinary compareCallEquals0 = maker.Binary(JCTree.NE, maker.Apply(
				List.<JCExpression>nil(), maker.Select(clazz, node.toName("compare")), args), maker.Literal(0));
		return maker.If(compareCallEquals0, returnBool(maker, false), null);
	}
	
	private JCStatement returnBool(TreeMaker maker, boolean bool) {
		return maker.Return(maker.Literal(TypeTags.BOOLEAN, bool ? 1 : 0));
	}
	
	private JCMethodDecl createConstructor(boolean isPublic, Node typeNode, List<Node> fields) {
		TreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		List<JCStatement> assigns = List.nil();
		List<JCVariableDecl> params = List.nil();
		
		for ( Node fieldNode : fields ) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			JCVariableDecl param = maker.VarDef(maker.Modifiers(0), field.name, field.vartype, null);
			params = params.append(param);
			JCFieldAccess thisX = maker.Select(maker.Ident(fieldNode.toName("this")), field.name);
			JCAssign assign = maker.Assign(thisX, maker.Ident(field.name));
			assigns = assigns.append(maker.Exec(assign));
		}
		
		JCModifiers mods = maker.Modifiers(isPublic ? Modifier.PUBLIC : Modifier.PRIVATE);
		return maker.MethodDef(mods, typeNode.toName("<init>"),
				null, type.typarams, params, List.<JCExpression>nil(), maker.Block(0L, assigns), null);
	}
	
	private JCMethodDecl createStaticConstructor(String name, Node typeNode, List<Node> fields) {
		TreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		JCModifiers mods = maker.Modifiers(Flags.STATIC);
		
		JCExpression returnType, constructorType;
		
		List<JCTypeParameter> typeParams = List.nil();
		List<JCVariableDecl> params = List.nil();
		List<JCExpression> typeArgs1 = List.nil();
		List<JCExpression> typeArgs2 = List.nil();
		List<JCExpression> args = List.nil();
		
		if ( !type.typarams.isEmpty() ) {
			for ( JCTypeParameter param : type.typarams ) {
				typeArgs1 = typeArgs1.append(maker.Ident(param.name));
				typeArgs2 = typeArgs2.append(maker.Ident(param.name));
				typeParams = typeParams.append(maker.TypeParameter(param.name, param.bounds));
			}
			returnType = maker.TypeApply(maker.Ident(type.name), typeArgs1);
			constructorType = maker.TypeApply(maker.Ident(type.name), typeArgs2);
		} else {
			returnType = maker.Ident(type.name);
			constructorType = maker.Ident(type.name);
		}
		
		for ( Node fieldNode : fields ) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			JCExpression pType;
			if ( field.vartype instanceof JCIdent ) pType = maker.Ident(((JCIdent)field.vartype).name);
			else if ( field.vartype instanceof JCTypeApply ) {
				JCTypeApply typeApply = (JCTypeApply) field.vartype;
				List<JCExpression> tArgs = List.nil();
				for ( JCExpression arg : typeApply.arguments ) tArgs = tArgs.append(arg);
				pType = maker.TypeApply(typeApply.clazz, tArgs);
			} else pType = field.vartype;
			JCVariableDecl param = maker.VarDef(maker.Modifiers(0), field.name, pType, null);
			params = params.append(param);
			args = args.append(maker.Ident(field.name));
		}
		JCReturn returnStatement = maker.Return(maker.NewClass(null, List.<JCExpression>nil(), constructorType, args, null));
		JCBlock body = maker.Block(0, List.<JCStatement>of(returnStatement));
		
		return maker.MethodDef(mods, typeNode.toName(name), returnType, typeParams, params, List.<JCExpression>nil(), body, null);
	}
}
