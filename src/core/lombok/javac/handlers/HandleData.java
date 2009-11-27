/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

import java.lang.reflect.Modifier;

import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

/**
 * Handles the {@code lombok.Data} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleData implements JavacAnnotationHandler<Data> {
	@Override public boolean handle(AnnotationValues<Data> annotation, JCAnnotation ast, JavacNode annotationNode) {
		markAnnotationAsProcessed(annotationNode, Data.class);
		JavacNode typeNode = annotationNode.up();
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl)typeNode.get();
		long flags = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (flags & (Flags.INTERFACE | Flags.ENUM | Flags.ANNOTATION)) != 0;
		
		if (typeDecl == null || notAClass) {
			annotationNode.addError("@Data is only supported on a class.");
			return false;
		}
		
		List<JavacNode> nodesForConstructor = List.nil();
		for (JavacNode child : typeNode.down()) {
			if (child.getKind() != Kind.FIELD) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
			//Skip fields that start with $
			if (fieldDecl.name.toString().startsWith("$")) continue;
			long fieldFlags = fieldDecl.mods.flags;
			//Skip static fields.
			if ((fieldFlags & Flags.STATIC) != 0) continue;
			boolean isFinal = (fieldFlags & Flags.FINAL) != 0;
			boolean isNonNull = !findAnnotations(child, TransformationsUtil.NON_NULL_PATTERN).isEmpty();
			if ((isFinal || isNonNull) && fieldDecl.init == null) nodesForConstructor = nodesForConstructor.append(child);
			new HandleGetter().generateGetterForField(child, annotationNode.get());
			if (!isFinal) new HandleSetter().generateSetterForField(child, annotationNode.get());
		}
		
		new HandleToString().generateToStringForType(typeNode, annotationNode);
		new HandleEqualsAndHashCode().generateEqualsAndHashCodeForType(typeNode, annotationNode);
		
		String staticConstructorName = annotation.getInstance().staticConstructor();
		
		if (constructorExists(typeNode) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl constructor = createConstructor(staticConstructorName.equals(""), typeNode, nodesForConstructor);
			injectMethod(typeNode, constructor);
		}
		
		if (!staticConstructorName.isEmpty() && methodExists("of", typeNode) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl staticConstructor = createStaticConstructor(staticConstructorName, typeNode, nodesForConstructor);
			injectMethod(typeNode, staticConstructor);
		}
		
		return true;
	}
	
	private JCMethodDecl createConstructor(boolean isPublic, JavacNode typeNode, List<JavacNode> fields) {
		TreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		List<JCStatement> nullChecks = List.nil();
		List<JCStatement> assigns = List.nil();
		List<JCVariableDecl> params = List.nil();
		
		for (JavacNode fieldNode : fields) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			List<JCAnnotation> nonNulls = findAnnotations(fieldNode, TransformationsUtil.NON_NULL_PATTERN);
			List<JCAnnotation> nullables = findAnnotations(fieldNode, TransformationsUtil.NULLABLE_PATTERN);
			JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.FINAL, nonNulls.appendList(nullables)), field.name, field.vartype, null);
			params = params.append(param);
			JCFieldAccess thisX = maker.Select(maker.Ident(fieldNode.toName("this")), field.name);
			JCAssign assign = maker.Assign(thisX, maker.Ident(field.name));
			assigns = assigns.append(maker.Exec(assign));
			
			if (!nonNulls.isEmpty()) {
				JCStatement nullCheck = generateNullCheck(maker, fieldNode);
				if (nullCheck != null) nullChecks = nullChecks.append(nullCheck);
			}
		}
		
		JCModifiers mods = maker.Modifiers(isPublic ? Modifier.PUBLIC : Modifier.PRIVATE);
		return maker.MethodDef(mods, typeNode.toName("<init>"),
				null, type.typarams, params, List.<JCExpression>nil(), maker.Block(0L, nullChecks.appendList(assigns)), null);
	}
	
	private JCMethodDecl createStaticConstructor(String name, JavacNode typeNode, List<JavacNode> fields) {
		TreeMaker maker = typeNode.getTreeMaker();
		JCClassDecl type = (JCClassDecl) typeNode.get();
		
		JCModifiers mods = maker.Modifiers(Flags.STATIC | Flags.PUBLIC);
		
		JCExpression returnType, constructorType;
		
		List<JCTypeParameter> typeParams = List.nil();
		List<JCVariableDecl> params = List.nil();
		List<JCExpression> typeArgs1 = List.nil();
		List<JCExpression> typeArgs2 = List.nil();
		List<JCExpression> args = List.nil();
		
		if (!type.typarams.isEmpty()) {
			for (JCTypeParameter param : type.typarams) {
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
		
		for (JavacNode fieldNode : fields) {
			JCVariableDecl field = (JCVariableDecl) fieldNode.get();
			JCExpression pType;
			if (field.vartype instanceof JCIdent) pType = maker.Ident(((JCIdent)field.vartype).name);
			else if (field.vartype instanceof JCTypeApply) {
				JCTypeApply typeApply = (JCTypeApply) field.vartype;
				List<JCExpression> tArgs = List.nil();
				for (JCExpression arg : typeApply.arguments) tArgs = tArgs.append(arg);
				pType = maker.TypeApply(typeApply.clazz, tArgs);
			} else {
				pType = field.vartype;
			}
			List<JCAnnotation> nonNulls = findAnnotations(fieldNode, TransformationsUtil.NON_NULL_PATTERN);
			List<JCAnnotation> nullables = findAnnotations(fieldNode, TransformationsUtil.NULLABLE_PATTERN);
			JCVariableDecl param = maker.VarDef(maker.Modifiers(Flags.FINAL, nonNulls.appendList(nullables)), field.name, pType, null);
			params = params.append(param);
			args = args.append(maker.Ident(field.name));
		}
		JCReturn returnStatement = maker.Return(maker.NewClass(null, List.<JCExpression>nil(), constructorType, args, null));
		JCBlock body = maker.Block(0, List.<JCStatement>of(returnStatement));
		
		return maker.MethodDef(mods, typeNode.toName(name), returnType, typeParams, params, List.<JCExpression>nil(), body, null);
	}
}
