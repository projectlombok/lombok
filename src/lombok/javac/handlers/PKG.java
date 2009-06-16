package lombok.javac.handlers;

import java.lang.reflect.Modifier;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

import lombok.AccessLevel;
import lombok.core.TransformationsUtil;

class PKG {
	static String toGetterName(Element field) {
		CharSequence fieldName = field.getSimpleName();
		
		boolean isBoolean = field.asType().getKind() == TypeKind.BOOLEAN;
		
		return TransformationsUtil.toGetterName(fieldName, isBoolean);
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
