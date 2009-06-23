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
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
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
		JCMethodDecl constructor = createConstructor(staticConstructorName.equals(""), typeNode, nodesForConstructorAndToString);
		injectMethod(typeNode, constructor);
		
		//TODO generate static constructors, hashCode, equals, toString.
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
		return maker.MethodDef(mods,
				typeNode.toName("<init>"), null, type.typarams, params, List.<JCExpression>nil(), maker.Block(0L, assigns), null);
	}
}
