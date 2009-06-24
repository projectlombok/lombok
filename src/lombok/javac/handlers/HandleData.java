package lombok.javac.handlers;

import java.lang.reflect.Modifier;

import static lombok.javac.handlers.PKG.*;

import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

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
		
		//TODO hashCode, equals, toString.
		return true;
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
