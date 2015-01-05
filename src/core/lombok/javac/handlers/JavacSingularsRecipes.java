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
package lombok.javac.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.core.LombokImmutableList;
import lombok.core.SpiLoadUtil;
import lombok.core.TypeLibrary;
import lombok.javac.JavacNode;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

public class JavacSingularsRecipes {
	private static final JavacSingularsRecipes INSTANCE = new JavacSingularsRecipes();
	private final Map<String, JavacSingularizer> singularizers = new HashMap<String, JavacSingularizer>();
	private final TypeLibrary singularizableTypes = new TypeLibrary();
	
	private JavacSingularsRecipes() {
		try {
			loadAll(singularizableTypes, singularizers);
			singularizableTypes.lock();
		} catch (IOException e) {
			System.err.println("Lombok's @Singularizable feature is broken due to misconfigured SPI files: " + e);
		}
	}
	
	private static void loadAll(TypeLibrary library, Map<String, JavacSingularizer> map) throws IOException {
		for (JavacSingularizer handler : SpiLoadUtil.findServices(JavacSingularizer.class, JavacSingularizer.class.getClassLoader())) {
			for (String type : handler.getSupportedTypes()) {
				JavacSingularizer existingSingularizer = map.get(type);
				if (existingSingularizer != null) {
					JavacSingularizer toKeep = existingSingularizer.getClass().getName().compareTo(handler.getClass().getName()) > 0 ? handler : existingSingularizer;
					System.err.println("Multiple singularizers found for type " + type + "; the alphabetically first class is used: " + toKeep.getClass().getName());
					map.put(type, toKeep);
				} else {
					map.put(type, handler);
					library.addType(type);
				}
			}
		}
	}
	
	public static JavacSingularsRecipes get() {
		return INSTANCE;
	}
	
	public String toQualified(String typeReference) {
		return singularizableTypes.toQualified(typeReference);
	}
	
	public JavacSingularizer getSingularizer(String fqn) {
		return singularizers.get(fqn);
	}
	
	public static final class SingularData {
		private final JavacNode annotation;
		private final Name singularName;
		private final Name pluralName;
		private final List<JCExpression> typeArgs;
		private final String targetFqn;
		private final JavacSingularizer singularizer;
		
		public SingularData(JavacNode annotation, Name singularName, Name pluralName, List<JCExpression> typeArgs, String targetFqn, JavacSingularizer singularizer) {
			this.annotation = annotation;
			this.singularName = singularName;
			this.pluralName = pluralName;
			this.typeArgs = typeArgs;
			this.targetFqn = targetFqn;
			this.singularizer = singularizer;
		}
		
		public JavacNode getAnnotation() {
			return annotation;
		}
		
		public Name getSingularName() {
			return singularName;
		}
		
		public Name getPluralName() {
			return pluralName;
		}
		
		public List<JCExpression> getTypeArgs() {
			return typeArgs;
		}
		
		public String getTargetFqn() {
			return targetFqn;
		}
		
		public JavacSingularizer getSingularizer() {
			return singularizer;
		}
	}
	
	public static abstract class JavacSingularizer {
		public abstract LombokImmutableList<String> getSupportedTypes();
		
		public abstract JavacNode generateFields(SingularData data, JavacNode builderType, JCTree source);
		public abstract void generateMethods(SingularData data, JavacNode builderType, JCTree source, boolean fluent, boolean chain);
		public abstract void appendBuildCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements, Name targetVariableName);
		
		public boolean requiresCleaning() {
			try {
				return !getClass().getMethod("appendCleaningCode", ListBuffer.class).getDeclaringClass().equals(JavacSingularizer.class);
			} catch (NoSuchMethodException e) {
				return false;
			}
		}
		
		public void appendCleaningCode(SingularData data, JavacNode builderType, JCTree source, ListBuffer<JCStatement> statements) {
		}
	}
}
