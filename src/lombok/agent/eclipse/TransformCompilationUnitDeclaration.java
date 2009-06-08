package lombok.agent.eclipse;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public class TransformCompilationUnitDeclaration {
	/** This is a 'magic' method signature - it is this one that will be called. Don't rename anything! */
	
	public static void transform(CompilationUnitDeclaration ast) {
		if ( ast.types != null ) for ( TypeDeclaration type : ast.types ) {
			if ( type.fields != null ) for ( FieldDeclaration field : type.fields ) {
				if ( field.annotations != null ) for ( Annotation annotation : field.annotations ) {
					if ( annotation.type.toString().equals("Getter") ) {
						new HandleGetter_ecj().apply(type, field);
					}
				}
			}
		}
	}
}
