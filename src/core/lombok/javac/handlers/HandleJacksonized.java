/*
 * Copyright (C) 2020-2025 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleExperimentalFlagUsage;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.Builder;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.handlers.HandlerUtil;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.spi.Provides;

/**
 * This (javac) handler deals with {@code @Jacksonized} modifying the (already
 * generated) {@code @Builder} or {@code @SuperBuilder} to conform to Jackson's
 * needs for builders.
 */
@Provides
@HandlerPriority(-512) // Above Handle(Super)Builder's level (builders must be already generated), but before all handlers generating getters/setters.
public class HandleJacksonized extends JavacAnnotationHandler<Jacksonized> {
	
	private static enum JacksonAnnotations {
		JSON_POJO_BUILDER("com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder"),
		JSON_DESERIALIZE("com.fasterxml.jackson.databind.annotation.JsonDeserialize"),
		JSON_PROPERTY("com.fasterxml.jackson.annotation.JsonProperty"),
		JSON_IGNORE("com.fasterxml.jackson.annotation.JsonIgnore");
		
		private final String qualifiedName;
		private final String[] chainedDots;
		private JacksonAnnotations(final String qualifiedName) {
			this.qualifiedName = qualifiedName;
			this.chainedDots = qualifiedName.split("\\.");
		}
		
		private boolean isAnnotating(JavacNode node) {
			return hasAnnotation(this.qualifiedName, node);
		}
	}
	
	@Override public void handle(AnnotationValues<Jacksonized> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.JACKSONIZED_FLAG_USAGE, "@Jacksonized");
		
		JavacNode annotatedNode = annotationNode.up();
		deleteAnnotationIfNeccessary(annotationNode, Jacksonized.class);
		
		JavacNode tdNode;
		if (annotatedNode.getKind() != Kind.TYPE) tdNode = annotatedNode.up(); // @Jacksonized on a constructor or a static factory method.
		else tdNode = annotatedNode; // @Jacksonized on the class.
		JCClassDecl td = (JCClassDecl) tdNode.get();
		
		JavacNode builderAnnotationNode = findAnnotation(Builder.class, annotatedNode);
		JavacNode superBuilderAnnotationNode = findAnnotation(SuperBuilder.class, annotatedNode);
		JavacNode accessorsAnnotationNode = findAnnotation(Accessors.class, annotatedNode);
		if (builderAnnotationNode == null && superBuilderAnnotationNode == null && accessorsAnnotationNode == null) {
			annotationNode.addWarning("@Jacksonized requires @Builder, @SuperBuilder, or @Accessors for it to mean anything.");
			return;
		}
		
		if (builderAnnotationNode != null || superBuilderAnnotationNode != null) {
			handleJacksonizedBuilder(annotationNode, annotatedNode, tdNode, td, builderAnnotationNode, superBuilderAnnotationNode);
		}
		
		if (accessorsAnnotationNode != null) {
			handleJacksonizedAccessors(annotationNode, annotatedNode, tdNode, td, accessorsAnnotationNode, builderAnnotationNode != null || superBuilderAnnotationNode != null);
		}
	}
	
	private void handleJacksonizedAccessors(JavacNode annotationNode, JavacNode annotatedNode, JavacNode tdNode, JCClassDecl td, JavacNode accessorsAnnotationNode, boolean jacksonizedBuilder) {
		AnnotationValues<Accessors> accessorsAnnotation = accessorsAnnotationNode != null ? 
			createAnnotation(Accessors.class, accessorsAnnotationNode) : null;
		boolean fluent = accessorsAnnotation != null && accessorsAnnotation.getInstance().fluent();
		
		if (!fluent) {
			// No changes required for chained-only accessors.
			if (!jacksonizedBuilder)
				annotationNode.addWarning("@Jacksonized only affects fluent accessors (@Accessors(fluent=true)).");
			return;
		}
		
		// Add @JsonProperty to all fields. It will be automatically copied to the getter/setters later.
		for (JavacNode javacNode : tdNode.down()) {
			if (javacNode.getKind() == Kind.FIELD) {
				createJsonPropertyForField(javacNode, annotationNode);
			}
		}
	}
	
	private void createJsonPropertyForField(JavacNode fieldNode, JavacNode annotationNode) {
		if (JacksonAnnotations.JSON_PROPERTY.isAnnotating(fieldNode)) {
			return;
		}
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		
		JCExpression jsonPropertyType = chainDots(fieldNode, JacksonAnnotations.JSON_PROPERTY.chainedDots);
		JCAnnotation annotationJsonProperty = maker.Annotation(jsonPropertyType, List.<JCExpression>of(maker.Literal(fieldNode.getName())));
		recursiveSetGeneratedBy(annotationJsonProperty, annotationNode);
		JCVariableDecl fieldDecl = ((JCVariableDecl)fieldNode.get());
		fieldDecl.mods.annotations = fieldDecl.mods.annotations.append(annotationJsonProperty);
	}
	
	private void handleJacksonizedBuilder(JavacNode annotationNode, JavacNode annotatedNode, JavacNode tdNode, JCClassDecl td, JavacNode builderAnnotationNode, JavacNode superBuilderAnnotationNode) {
		if (builderAnnotationNode != null && superBuilderAnnotationNode != null) {
			annotationNode.addError("@Jacksonized cannot process both @Builder and @SuperBuilder on the same class.");
			return;
		}
		
		boolean isAbstract = (td.mods.flags & Flags.ABSTRACT) != 0;
		if (isAbstract) {
			annotationNode.addError("Builders on abstract classes cannot be @Jacksonized (the builder would never be used).");
			return;
		}
		
		AnnotationValues<Builder> builderAnnotation = builderAnnotationNode != null ? 
			createAnnotation(Builder.class, builderAnnotationNode) : null;
		AnnotationValues<SuperBuilder> superBuilderAnnotation = superBuilderAnnotationNode != null ? 
			createAnnotation(SuperBuilder.class, superBuilderAnnotationNode) : null;
		
		String setPrefix = builderAnnotation != null ? 
			builderAnnotation.getInstance().setterPrefix() :
			superBuilderAnnotation.getInstance().setterPrefix();
		String buildMethodName = builderAnnotation != null ? 
			builderAnnotation.getInstance().buildMethodName() :
			superBuilderAnnotation.getInstance().buildMethodName();
		
		JavacTreeMaker maker = annotatedNode.getTreeMaker();
		
		// Now lets find the generated builder class.
		String builderClassName = getBuilderClassName(annotationNode, annotatedNode, td, builderAnnotation, maker);
		
		JCClassDecl builderClass = null;
		for (JCTree member : td.getMembers()) {
			if (member instanceof JCClassDecl && ((JCClassDecl) member).getSimpleName().contentEquals(builderClassName)) {
				builderClass = (JCClassDecl) member;
				break;
			}
		}
		
		if (builderClass == null) {
			annotationNode.addError("Could not find @(Super)Builder's generated builder class for @Jacksonized processing. If there are other compiler errors, fix them first.");
			return;
		}
		
		// Insert @JsonDeserialize on annotated class.
		if (JacksonAnnotations.JSON_DESERIALIZE.isAnnotating(tdNode)) {
			annotationNode.addError("@JsonDeserialize already exists on class. Either delete @JsonDeserialize, or remove @Jacksonized and manually configure Jackson.");
			return;
		}
		JCExpression jsonDeserializeType = chainDots(annotatedNode, JacksonAnnotations.JSON_DESERIALIZE.chainedDots);
		JCExpression builderClassExpression = namePlusTypeParamsToTypeReference(maker, tdNode, annotationNode.toName(builderClassName), false, List.<JCTypeParameter>nil());
		JCFieldAccess builderClassReference = maker.Select(builderClassExpression, annotatedNode.toName("class"));
		JCExpression assign = maker.Assign(maker.Ident(annotationNode.toName("builder")), builderClassReference);
		JCAnnotation annotationJsonDeserialize = maker.Annotation(jsonDeserializeType, List.of(assign));
		recursiveSetGeneratedBy(annotationJsonDeserialize, annotationNode);
		td.mods.annotations = td.mods.annotations.append(annotationJsonDeserialize);
		
		// Copy annotations from the class to the builder class.
		List<JCAnnotation> copyableAnnotations = findJacksonAnnotationsOnClass(tdNode);
		List<JCAnnotation> copiedAnnotations = copyAnnotations(copyableAnnotations, maker);
		for (JCAnnotation anno : copiedAnnotations) {
			recursiveSetGeneratedBy(anno, annotationNode);
		}
		builderClass.mods.annotations = builderClass.mods.annotations.appendList(copiedAnnotations);
		
		// Insert @JsonPOJOBuilder on the builder class.
		JCExpression jsonPOJOBuilderType = chainDots(annotatedNode, JacksonAnnotations.JSON_POJO_BUILDER.chainedDots);
		JCExpression withPrefixExpr = maker.Assign(maker.Ident(annotationNode.toName("withPrefix")), maker.Literal(setPrefix));
		JCExpression buildMethodNameExpr = maker.Assign(maker.Ident(annotationNode.toName("buildMethodName")), maker.Literal(buildMethodName));
		JCAnnotation annotationJsonPOJOBuilder = maker.Annotation(jsonPOJOBuilderType, List.of(withPrefixExpr, buildMethodNameExpr));
		recursiveSetGeneratedBy(annotationJsonPOJOBuilder, annotatedNode);
		builderClass.mods.annotations = builderClass.mods.annotations.append(annotationJsonPOJOBuilder);
		
		// @SuperBuilder? Make it package-private!
		if (superBuilderAnnotationNode != null) builderClass.mods.flags = builderClass.mods.flags & ~Flags.PRIVATE;
	}
	
	private String getBuilderClassName(JavacNode annotationNode, JavacNode annotatedNode, JCClassDecl td, AnnotationValues<Builder> builderAnnotation, JavacTreeMaker maker) {
		String builderClassName = builderAnnotation != null ? builderAnnotation.getInstance().builderClassName() : null;
		if (builderClassName == null || builderClassName.isEmpty()) {
			builderClassName = annotationNode.getAst().readConfiguration(ConfigurationKeys.BUILDER_CLASS_NAME);
			if (builderClassName == null || builderClassName.isEmpty()) builderClassName = "*Builder";
			
			JCMethodDecl fillParametersFrom = annotatedNode.get() instanceof JCMethodDecl ? (JCMethodDecl)annotatedNode.get() : null;
			String replacement;
			if (fillParametersFrom != null && !fillParametersFrom.getName().toString().equals("<init>")) {
				// @Builder on a method: Use name of return type for builder class name.
				JCExpression returnType = fillParametersFrom.restype;
				List<JCTypeParameter> typeParams = fillParametersFrom.typarams;
				if (returnType instanceof JCTypeApply) {
					returnType = cloneType(maker, returnType, annotatedNode);
				}
				replacement = HandleBuilder.returnTypeToBuilderClassName(annotationNode, td, returnType, typeParams);
			} else {
				// @Builder on class or constructor: Use the class name.
				replacement = td.name.toString();
			}
			builderClassName = builderClassName.replace("*", replacement);
		}
		
		if (builderAnnotation == null) builderClassName += "Impl"; // For @SuperBuilder, all Jackson annotations must be put on the BuilderImpl class.
		
		return builderClassName;
	}
	
	private static List<JCAnnotation> findJacksonAnnotationsOnClass(JavacNode node) {
		ListBuffer<JCAnnotation> result = new ListBuffer<JCAnnotation>();
		for (JavacNode child : node.down()) {
			if (child.getKind() == Kind.ANNOTATION) {
				JCAnnotation annotation = (JCAnnotation) child.get();
				for (String bn : HandlerUtil.JACKSON_COPY_TO_BUILDER_ANNOTATIONS) {
					if (typeMatches(bn, node, annotation.annotationType)) {
						result.append(annotation);
						break;
					}
				}
			}
		}
		return result.toList();
	}
}
