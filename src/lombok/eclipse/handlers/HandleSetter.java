package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.PKG.*;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

import lombok.Setter;
import lombok.core.AnnotationValues;
import lombok.core.TransformationsUtil;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSetter implements EclipseAnnotationHandler<Setter> {
	@Override public boolean handle(AnnotationValues<Setter> annotation, Annotation ast, Node annotationNode) {
		if ( !(annotationNode.up().get() instanceof FieldDeclaration) ) return false;
		FieldDeclaration field = (FieldDeclaration) annotationNode.up().get();
		String setterName = TransformationsUtil.toSetterName(new String(field.name));
		
		TypeDeclaration parent = (TypeDeclaration) annotationNode.up().up().get();
		if ( parent.methods != null ) for ( AbstractMethodDeclaration method : parent.methods ) {
			if ( method.selector != null && new String(method.selector).equals(setterName) ) {
				annotationNode.addWarning(String.format(
						"Not generating %s(%s %s): A method with that name already exists",
						setterName, field.type, new String(field.name)));
				return false;
			}
		}
		
		int modifier = toModifier(annotation.getInstance().value());
		
		MethodDeclaration method = generateSetter(parent, field, setterName, modifier, ast);
		
		if ( parent.methods == null ) {
			parent.methods = new AbstractMethodDeclaration[1];
			parent.methods[0] = method;
		} else {
			AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[parent.methods.length + 1];
			System.arraycopy(parent.methods, 0, newArray, 0, parent.methods.length);
			newArray[parent.methods.length] = method;
			parent.methods = newArray;
		}
		
		return true;
	}
	
	private MethodDeclaration generateSetter(TypeDeclaration parent, FieldDeclaration field, String name,
			int modifier, Annotation ast) {
		long pos = (((long)ast.sourceStart) << 32) | ast.sourceEnd;
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = modifier;
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		method.annotations = null;
		Argument param = new Argument(field.name, pos, field.type, 0);
		method.arguments = new Argument[] { param };
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.scope = parent.scope == null ? null : new MethodScope(parent.scope, method, false);
		method.bits |= ASTNode.Bit24;
		FieldReference thisX = new FieldReference(("this." + new String(field.name)).toCharArray(), pos);
		thisX.receiver = new ThisReference(ast.sourceStart, ast.sourceEnd);
		thisX.token = field.name;
		Assignment assignment = new Assignment(thisX, new SingleNameReference(field.name, pos), (int)pos);
		method.bodyStart = method.declarationSourceStart = method.sourceStart = ast.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = ast.sourceEnd;
		method.statements = new Statement[] { assignment };
		return method;
	}
}
