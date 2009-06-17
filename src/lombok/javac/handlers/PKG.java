package lombok.javac.handlers;

import java.lang.reflect.Modifier;

import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import lombok.AccessLevel;
import lombok.core.TransformationsUtil;

class PKG {
	static String toGetterName(JCVariableDecl field) {
		CharSequence fieldName = field.name;
		
		boolean isBoolean = field.vartype.toString().equals("boolean");
		
		return TransformationsUtil.toGetterName(fieldName, isBoolean);
	}
	
	static String toSetterName(JCVariableDecl field) {
		CharSequence fieldName = field.name;
		
		return TransformationsUtil.toSetterName(fieldName);
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
