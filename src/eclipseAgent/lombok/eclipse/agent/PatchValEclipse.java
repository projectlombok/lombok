/*
 * Copyright (C) 2010-2018 The Project Lombok Authors.
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
 * 
 * Thanks to Stephen Haberman for a patch to solve some NPEs in Eclipse.
 */
package lombok.eclipse.agent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import lombok.Lombok;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class PatchValEclipse {
	public static void copyInitializationOfForEachIterable(Parser parser) {
		ASTNode[] astStack;
		int astPtr;
		try {
			astStack = (ASTNode[]) Reflection.astStackField.get(parser);
			astPtr = (Integer) Reflection.astPtrField.get(parser);
		} catch (Exception e) {
			// Most likely we're in ecj or some other plugin usage of the eclipse compiler. No need for this.
			return;
		}
		
		ForeachStatement foreachDecl = (ForeachStatement) astStack[astPtr];
		ASTNode init = foreachDecl.collection;
		if (init == null) return;
		boolean val = couldBeVal(foreachDecl.elementVariable.type);
		boolean var = couldBeVar(foreachDecl.elementVariable.type);
		if (foreachDecl.elementVariable == null || !(val || var)) return;
		
		try {
			if (Reflection.iterableCopyField != null) Reflection.iterableCopyField.set(foreachDecl.elementVariable, init);
		} catch (Exception e) {
			// In ecj mode this field isn't there and we don't need the copy anyway, so, we ignore the exception.
		}
	}
	
	public static void copyInitializationOfLocalDeclaration(Parser parser) {
		ASTNode[] astStack;
		int astPtr;
		try {
			astStack = (ASTNode[]) Reflection.astStackField.get(parser);
			astPtr = (Integer)Reflection.astPtrField.get(parser);
		} catch (Exception e) {
			// Most likely we're in ecj or some other plugin usage of the eclipse compiler. No need for this.
			return;
		}
		AbstractVariableDeclaration variableDecl = (AbstractVariableDeclaration) astStack[astPtr];
		if (!(variableDecl instanceof LocalDeclaration)) return;
		ASTNode init = variableDecl.initialization;
		if (init == null) return;
		boolean val = couldBeVal(variableDecl.type);
		boolean var = couldBeVar(variableDecl.type);
		if (!(val || var)) return;
		
		try {
			if (Reflection.initCopyField != null) Reflection.initCopyField.set(variableDecl, init);
		} catch (Exception e) {
			// In ecj mode this field isn't there and we don't need the copy anyway, so, we ignore the exception.
		}
	}
	
	private static boolean couldBeVar(TypeReference type) {
		return PatchVal.couldBe("lombok.experimental.var", type) || PatchVal.couldBe("lombok.var", type);
	}
	
	public static void addFinalAndValAnnotationToSingleVariableDeclaration(Object converter, SingleVariableDeclaration out, LocalDeclaration in) {
		@SuppressWarnings("unchecked") List<IExtendedModifier> modifiers = out.modifiers();
		addFinalAndValAnnotationToModifierList(converter, modifiers, out.getAST(), in);
	}
	
	public static void addFinalAndValAnnotationToVariableDeclarationStatement(Object converter, VariableDeclarationStatement out, LocalDeclaration in) {
		@SuppressWarnings("unchecked") List<IExtendedModifier> modifiers = out.modifiers();
		addFinalAndValAnnotationToModifierList(converter, modifiers, out.getAST(), in);
	}
	
	public static void addFinalAndValAnnotationToModifierList(Object converter, List<IExtendedModifier> modifiers, AST ast, LocalDeclaration in) {
		// First check that 'in' has the final flag on, and a @val / @lombok.val annotation.
		if ((in.modifiers & ClassFileConstants.AccFinal) == 0) return;
		if (in.annotations == null) return;
		boolean found = false;
		Annotation valAnnotation = null;
		
		for (Annotation ann : in.annotations) {
			if (couldBeVal(ann.type)) {
				found = true;
				valAnnotation = ann;
				break;
			}
		}
		
		if (!found) return;
		
		// Now check that 'out' is missing either of these.
		
		if (modifiers == null) return; // This is null only if the project is 1.4 or less. Lombok doesn't work in that.
		boolean finalIsPresent = false;
		boolean valIsPresent = false;
		
		for (Object present : modifiers) {
			if (present instanceof Modifier) {
				ModifierKeyword keyword = ((Modifier)present).getKeyword();
				if (keyword == null) continue;
				if (keyword.toFlagValue() == Modifier.FINAL) finalIsPresent = true;
			}
			
			if (present instanceof org.eclipse.jdt.core.dom.Annotation) {
				Name typeName = ((org.eclipse.jdt.core.dom.Annotation) present).getTypeName();
				if (typeName != null) {
					String fullyQualifiedName = typeName.getFullyQualifiedName();
					if ("val".equals(fullyQualifiedName) || "lombok.val".equals(fullyQualifiedName)) {
						valIsPresent = true;
					}
				}
			}
		}
		
		if (!finalIsPresent) {
			modifiers.add(
					createModifier(ast, ModifierKeyword.FINAL_KEYWORD, valAnnotation.sourceStart, valAnnotation.sourceEnd));
		}
		
		if (!valIsPresent) {
			MarkerAnnotation newAnnotation = createValAnnotation(ast, valAnnotation, valAnnotation.sourceStart, valAnnotation.sourceEnd);
			try {
				Reflection.astConverterRecordNodes.invoke(converter, newAnnotation, valAnnotation);
				Reflection.astConverterRecordNodes.invoke(converter, newAnnotation.getTypeName(), valAnnotation.type);
			} catch (IllegalAccessException e) {
				throw Lombok.sneakyThrow(e);
			} catch (InvocationTargetException e) {
				throw Lombok.sneakyThrow(e.getCause());
			}
			modifiers.add(newAnnotation);
		}
	}
	
	private static boolean couldBeVal(TypeReference type) {
		return PatchVal.couldBe("lombok.val", type);
	}
	
	public static Modifier createModifier(AST ast, ModifierKeyword keyword, int start, int end) {
		Modifier modifier = null;
		try {
			modifier = Reflection.modifierConstructor.newInstance(ast);
		} catch (InstantiationException e) {
			throw Lombok.sneakyThrow(e);
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e);
		}
		
		if (modifier != null) {
			modifier.setKeyword(keyword);
			modifier.setSourceRange(start, end - start + 1);
		}
		return modifier;
	}
	
	public static MarkerAnnotation createValAnnotation(AST ast, Annotation original, int start, int end) {
		MarkerAnnotation out = null;
		try {
			out = Reflection.markerAnnotationConstructor.newInstance(ast);
		} catch (InstantiationException e) {
			throw Lombok.sneakyThrow(e);
		} catch (IllegalAccessException e) {
			throw Lombok.sneakyThrow(e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e);
		}
		
		if (out != null) {
			SimpleName valName = ast.newSimpleName("val");
			valName.setSourceRange(start, end - start + 1);
			if (original.type instanceof SingleTypeReference) {
				out.setTypeName(valName);
				setIndex(valName, 1);
			} else {
				SimpleName lombokName = ast.newSimpleName("lombok");
				lombokName.setSourceRange(start, end - start + 1);
				setIndex(lombokName, 1);
				setIndex(valName, 2);
				QualifiedName fullName = ast.newQualifiedName(lombokName, valName);
				setIndex(fullName, 1);
				fullName.setSourceRange(start, end - start + 1);
				out.setTypeName(fullName);
			}
			out.setSourceRange(start, end - start + 1);
		}
		
		return out;
	}
	
	private static final Field FIELD_NAME_INDEX;
	
	static {
		Field f = null;
		try {
			f = Name.class.getDeclaredField("index");
			f.setAccessible(true);
		} catch (Throwable t) {
			// Leave it null, in which case we don't set index. That'll result in error log messages but its better than crashing here.
		}
		
		FIELD_NAME_INDEX = f;
	}
	
	private static void setIndex(Name name, int index) {
		try {
			if (FIELD_NAME_INDEX != null) FIELD_NAME_INDEX.set(name, index);
		} catch (Exception e) {
			// Don't do anything - safest fallback behaviour.
		}
	}
	
	public static final class Reflection {
		private static final Field initCopyField, iterableCopyField;
		private static final Field astStackField, astPtrField;
		private static final Constructor<Modifier> modifierConstructor;
		private static final Constructor<MarkerAnnotation> markerAnnotationConstructor;
		private static final Method astConverterRecordNodes;
		
		static {
			Field a = null, b = null, c = null, d = null;
			Constructor<Modifier> f = null;
			Constructor<MarkerAnnotation> g = null;
			Method h = null;
			
			try {
				a = LocalDeclaration.class.getDeclaredField("$initCopy");
				b = LocalDeclaration.class.getDeclaredField("$iterableCopy");
			} catch (Throwable t) {
				//ignore - no $initCopy exists when running in ecj.
			}
			
			try {
				c = Parser.class.getDeclaredField("astStack");
				c.setAccessible(true);
				d = Parser.class.getDeclaredField("astPtr");
				d.setAccessible(true);
				f = Modifier.class.getDeclaredConstructor(AST.class);
				f.setAccessible(true);
				g = MarkerAnnotation.class.getDeclaredConstructor(AST.class);
				g.setAccessible(true);
				Class<?> z = Class.forName("org.eclipse.jdt.core.dom.ASTConverter");
				h = z.getDeclaredMethod("recordNodes", org.eclipse.jdt.core.dom.ASTNode.class, org.eclipse.jdt.internal.compiler.ast.ASTNode.class);
				h.setAccessible(true);
			} catch (Throwable t) {
				// Most likely we're in ecj or some other plugin usage of the eclipse compiler. No need for this.
			}
			
			initCopyField = a;
			iterableCopyField = b;
			astStackField = c;
			astPtrField = d;
			modifierConstructor = f;
			markerAnnotationConstructor = g;
			astConverterRecordNodes = h;
		}
	}
}
