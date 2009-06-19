package lombok.javac.handlers;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleData implements JavacAnnotationHandler<Data> {
	@Override public boolean handle(AnnotationValues<Data> annotation, JCAnnotation ast, Node annotationNode) {
		Node typeNode = annotationNode.up();
		JCClassDecl typeDecl = null;
		if ( typeNode.get() instanceof JCClassDecl ) typeDecl = (JCClassDecl)typeNode.get();
		long flags = typeDecl.mods.flags;
		boolean notAClass = (flags & (Flags.INTERFACE | Flags.ENUM | Flags.ANNOTATION)) != 0;
		
		if ( typeDecl != null || notAClass ) {
			annotationNode.addError("@Data is only supported on a class.");
			return false;
		}
		
		List<Node> nodesForEquality = new ArrayList<Node>();
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() != Kind.FIELD ) continue;
			JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
			long fieldFlags = fieldDecl.mods.flags;
			//Skip static fields.
			if ( (fieldFlags & Flags.STATIC) != 0 ) continue;
			if ( (fieldFlags & Flags.TRANSIENT) == 0 ) nodesForEquality.add(child);
			new HandleGetter().generateGetterForField(child, annotationNode.get());
			if ( (fieldFlags & Flags.FINAL) == 0 )
				new HandleSetter().generateSetterForField(child, annotationNode.get());
		}
		
		//TODO generate constructor, hashCode, equals, toString.
		return true;
	}
}
