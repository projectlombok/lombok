package lombok.javac.handlers;

import static lombok.javac.handlers.PKG.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleGetter implements JavacAnnotationHandler<Getter> {
	public void generateGetterForField(Node fieldNode, DiagnosticPosition pos) {
		AccessLevel level = Getter.DEFAULT_ACCESS_LEVEL;
		Node errorNode = fieldNode;
		
		for ( Node child : fieldNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Javac.annotationTypeMatches(Getter.class, child) ) {
					level = Javac.createAnnotation(Getter.class, child).getInstance().value();
					errorNode = child;
					pos = child.get();
					break;
				}
			}
		}
		
		createGetterForField(level, fieldNode, errorNode, pos);
	}
	
	@Override public boolean handle(AnnotationValues<Getter> annotation, JCAnnotation ast, Node annotationNode) {
		Node fieldNode = annotationNode.up();
		AccessLevel level = annotation.getInstance().value();
		return createGetterForField(level, fieldNode, annotationNode, annotationNode.get());
	}
	
	private boolean createGetterForField(AccessLevel level, Node fieldNode, Node errorNode, DiagnosticPosition pos) {
		if ( fieldNode.getKind() != Kind.FIELD ) {
			errorNode.addError("@Getter is only supported on a field.");
			return false;
		}
		
		JCVariableDecl fieldDecl = (JCVariableDecl)fieldNode.get();
		String methodName = toGetterName(fieldDecl);
		
		if ( methodExists(methodName, fieldNode) ) {
			errorNode.addWarning(
					String.format("Not generating %s(): A method with that name already exists",  methodName));
			return false;
		}
		
		JCClassDecl javacClassTree = (JCClassDecl) fieldNode.up().get();
		
		long access = toJavacModifier(level) | (fieldDecl.mods.flags & Flags.STATIC);
		
		JCMethodDecl getterMethod = createGetter(access, fieldNode, fieldNode.getTreeMaker());
		javacClassTree.defs = javacClassTree.defs.append(getterMethod);
		return true;
	}
	
	private JCMethodDecl createGetter(long access, Node field, TreeMaker treeMaker) {
		JCVariableDecl fieldNode = (JCVariableDecl) field.get();
		JCStatement returnStatement = treeMaker.Return(treeMaker.Ident(fieldNode.getName()));
		
		JCBlock methodBody = treeMaker.Block(0, List.of(returnStatement));
		Name methodName = field.toName(toGetterName(fieldNode));
		JCExpression methodType = fieldNode.type != null ? treeMaker.Type(fieldNode.type) : fieldNode.vartype;
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		return treeMaker.MethodDef(treeMaker.Modifiers(access, List.<JCAnnotation>nil()), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
}
