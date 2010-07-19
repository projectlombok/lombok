/*
 * Copyright © 2009-2010 Reinier Zwitserloot and Roel Spilker.
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

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.handlers.TransformationsUtil;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
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
		Map<JavacNode, Boolean> gettersAndSetters = new LinkedHashMap<JavacNode, Boolean>();
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
			gettersAndSetters.put(child, !isFinal);
		}
		
		String staticConstructorName = annotation.getInstance().staticConstructor();
		
		new HandleConstructor().generateConstructor(AccessLevel.PUBLIC, typeNode, nodesForConstructor, staticConstructorName, true, false);
		
		for (Map.Entry<JavacNode, Boolean> field : gettersAndSetters.entrySet()) {
			new HandleGetter().generateGetterForField(field.getKey(), annotationNode.get(), AccessLevel.PUBLIC, true);
			if (field.getValue()) new HandleSetter().generateSetterForField(field.getKey(), annotationNode.get(), AccessLevel.PUBLIC, true);
		}
		
		new HandleEqualsAndHashCode().generateEqualsAndHashCodeForType(typeNode, annotationNode);
		new HandleToString().generateToStringForType(typeNode, annotationNode);
		
		return true;
	}
}
