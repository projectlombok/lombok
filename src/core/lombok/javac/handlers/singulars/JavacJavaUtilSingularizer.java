/*
 * Copyright (C) 2015 The Project Lombok Authors.
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
package lombok.javac.handlers.singulars;

import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;

public abstract class JavacJavaUtilSingularizer extends JavacSingularizer {
	/**
	 * Adds the requested number of type arguments to the provided type, copying each argument in {@code typeArgs}. If typeArgs is too long, the extra elements are ignored.
	 * If {@code typeArgs} is null or too short, {@code java.lang.Object} will be substituted for each missing type argument.
	 * 
	 * @param count The number of type arguments requested.
	 * @param addExtends If {@code true}, all bounds are either '? extends X' or just '?'. If false, the reverse is applied, and '? extends Foo' is converted to Foo, '?' to Object, etc.
	 * @param node Some node in the same AST. Just used to obtain makers and contexts and such.
	 * @param type The type to add generics to.
	 * @param typeArgs the list of type args to clone.
	 * @param source The source annotation that is the root cause of this code generation.
	 */
	protected JCExpression addTypeArgs(int count, boolean addExtends, JavacNode node, JCExpression type, List<JCExpression> typeArgs, JCTree source) {
		JavacTreeMaker maker = node.getTreeMaker();
		Context context = node.getContext();
		
		if (count < 0) throw new IllegalArgumentException("count is negative");
		if (count == 0) return type;
		ListBuffer<JCExpression> arguments = new ListBuffer<JCExpression>();
		
		if (typeArgs != null) for (JCExpression orig : typeArgs) {
			if (!addExtends) {
				if (orig.getKind() == Kind.UNBOUNDED_WILDCARD || orig.getKind() == Kind.SUPER_WILDCARD) {
					arguments.append(chainDots(node, "java", "lang", "Object"));
				} else if (orig.getKind() == Kind.EXTENDS_WILDCARD) {
					JCExpression inner;
					try {
						inner = (JCExpression) ((JCWildcard) orig).inner;
					} catch (Exception e) {
						inner = chainDots(node, "java", "lang", "Object");
					}
					arguments.append(cloneType(maker, inner, source, context));
				} else {
					arguments.append(cloneType(maker, orig, source, context));
				}
			} else {
				if (orig.getKind() == Kind.UNBOUNDED_WILDCARD || orig.getKind() == Kind.SUPER_WILDCARD) {
					arguments.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
				} else if (orig.getKind() == Kind.EXTENDS_WILDCARD) {
					arguments.append(cloneType(maker, orig, source, context));
				} else {
					arguments.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.EXTENDS), cloneType(maker, orig, source, context)));
				}
			}
			if (--count == 0) break;
		}
		
		while (count-- > 0) {
			if (addExtends) {
				arguments.append(maker.Wildcard(maker.TypeBoundKind(BoundKind.UNBOUND), null));
			} else {
				arguments.append(chainDots(node, "java", "lang", "Object"));
			}
		}
		return maker.TypeApply(type, arguments.toList());
	}
	
}
