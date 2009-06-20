package lombok.javac.handlers;

import java.lang.reflect.Modifier;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import lombok.AccessLevel;
import lombok.core.TransformationsUtil;
import lombok.javac.JavacAST;

class PKG {
	private PKG() {}
	
	static String toGetterName(JCVariableDecl field) {
		CharSequence fieldName = field.name;
		
		boolean isBoolean = field.vartype.toString().equals("boolean");
		
		return TransformationsUtil.toGetterName(fieldName, isBoolean);
	}
	
	static String toSetterName(JCVariableDecl field) {
		CharSequence fieldName = field.name;
		
		return TransformationsUtil.toSetterName(fieldName);
	}
	
	enum MethodExistsResult {
		NOT_EXISTS, EXISTS_BY_USER, EXISTS_BY_LOMBOK;
	}
	
	static MethodExistsResult methodExists(String methodName, JavacAST.Node node) {
		while ( node != null && !(node.get() instanceof JCClassDecl) ) {
			node = node.up();
		}
		
		if ( node.get() instanceof JCClassDecl ) {
			for ( JCTree def : ((JCClassDecl)node.get()).defs ) {
				if ( def instanceof JCMethodDecl ) {
					if ( ((JCMethodDecl)def).name.contentEquals(methodName) ) {
						JavacAST.Node existing = node.getNodeFor(def);
						if ( existing == null || !existing.isHandled() ) return MethodExistsResult.EXISTS_BY_USER;
						return MethodExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MethodExistsResult.NOT_EXISTS;
	}
	
	static int toJavacModifier(AccessLevel accessLevel) {
		switch ( accessLevel ) {
		case MODULE:
		case PACKAGE:
			return 0;
		default:
		case PUBLIC:
			return Modifier.PUBLIC;
		case PRIVATE:
			return Modifier.PRIVATE;
		case PROTECTED:
			return Modifier.PROTECTED;
		}
	}
}
