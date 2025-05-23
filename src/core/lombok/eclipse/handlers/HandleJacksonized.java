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
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.handleExperimentalFlagUsage;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import lombok.Builder;
import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.AST.Kind;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.spi.Provides;

/**
 * This (ecj) handler deals with {@code @Jacksonized} modifying the (already
 * generated) {@code @Builder} or {@code @SuperBuilder} to conform to Jackson's
 * needs for builders.
 */
@Provides
@HandlerPriority(-512) // Above Handle(Super)Builder's level (builders must be already generated).
public class HandleJacksonized extends EclipseAnnotationHandler<Jacksonized> {

	private static final char[][] JSON_POJO_BUILDER_ANNOTATION = Eclipse.fromQualifiedName("com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder");
	private static final char[][] JSON_DESERIALIZE_ANNOTATION = Eclipse.fromQualifiedName("com.fasterxml.jackson.databind.annotation.JsonDeserialize");
	private static final char[][] JSON_PROPERTY_ANNOTATION = Eclipse.fromQualifiedName("com.fasterxml.jackson.annotation.JsonProperty");
	
	@Override public void handle(AnnotationValues<Jacksonized> annotation, Annotation ast, EclipseNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.JACKSONIZED_FLAG_USAGE, "@Jacksonized");
		
		EclipseNode annotatedNode = annotationNode.up();
		
		EclipseNode tdNode;
		if (annotatedNode.getKind() != Kind.TYPE)  tdNode = annotatedNode.up(); // @Jacksonized on a constructor or a static factory method.
		else tdNode = annotatedNode; // @Jacksonized on the class.
		TypeDeclaration td = (TypeDeclaration) tdNode.get();
		
		EclipseNode builderAnnotationNode = findAnnotation(Builder.class, annotatedNode);
		EclipseNode superBuilderAnnotationNode = findAnnotation(SuperBuilder.class, annotatedNode);
		EclipseNode accessorsAnnotationNode = (annotatedNode.getKind() == Kind.TYPE) ? findAnnotation(Accessors.class, annotatedNode) : null;
		
		if (builderAnnotationNode == null && superBuilderAnnotationNode == null && accessorsAnnotationNode == null) {
			annotationNode.addWarning("@Jacksonized requires @Builder, @SuperBuilder, or @Accessors for it to mean anything.");
			return;
		}
		
		if (builderAnnotationNode != null && superBuilderAnnotationNode != null) {
			annotationNode.addError("@Jacksonized cannot process both @Builder and @SuperBuilder on the same class.");
			return;
		}
		
		boolean jacksonizedBuilder = builderAnnotationNode != null || superBuilderAnnotationNode != null;
		if (jacksonizedBuilder) {
			handleJacksonizedBuilder(ast, annotationNode, annotatedNode, tdNode, td, builderAnnotationNode, superBuilderAnnotationNode);
		}
		
		if (accessorsAnnotationNode != null) {
			handleJacksonizedAccessors(ast, annotationNode, annotatedNode, tdNode, td, accessorsAnnotationNode, jacksonizedBuilder);
		}
	}
	
	private void handleJacksonizedBuilder(Annotation ast, EclipseNode annotationNode, EclipseNode annotatedNode, EclipseNode tdNode, TypeDeclaration td, EclipseNode builderAnnotationNode, EclipseNode superBuilderAnnotationNode) {
		boolean isAbstract = (td.modifiers & ClassFileConstants.AccAbstract) != 0;
		if (isAbstract) {
			annotationNode.addError("Builders on abstract classes cannot be @Jacksonized (the builder would never be used).");
			return;
		}
		
		AnnotationValues<Builder> builderAnnotation = builderAnnotationNode != null ? createAnnotation(Builder.class, builderAnnotationNode) : null;
		AnnotationValues<SuperBuilder> superBuilderAnnotation = superBuilderAnnotationNode != null ? createAnnotation(SuperBuilder.class, superBuilderAnnotationNode) : null;
		
		String setPrefix = builderAnnotation != null ? builderAnnotation.getInstance().setterPrefix() : superBuilderAnnotation.getInstance().setterPrefix();
		String buildMethodName = builderAnnotation != null ? builderAnnotation.getInstance().buildMethodName() : superBuilderAnnotation.getInstance().buildMethodName();
		
		// Now lets find the generated builder class.
		EclipseNode builderClassNode = null;
		TypeDeclaration builderClass = null;
		String builderClassName = getBuilderClassName(ast, annotationNode, annotatedNode, td, builderAnnotation);
		for (EclipseNode member : tdNode.down()) {
			ASTNode astNode = member.get();
			if (astNode instanceof TypeDeclaration && Arrays.equals(((TypeDeclaration)astNode).name, builderClassName.toCharArray())) {
				builderClassNode = member;
				builderClass = (TypeDeclaration) astNode;
				break;
			}
		}
		
		if (builderClass == null) {
			annotationNode.addError("Could not find @(Super)Builder's generated builder class for @Jacksonized processing. If there are other compiler errors, fix them first.");
			return;
		}
		
		// Insert @JsonDeserialize on annotated class.
		if (hasAnnotation("com.fasterxml.jackson.databind.annotation.JsonDeserialize", tdNode)) {
			annotationNode.addError("@JsonDeserialize already exists on class. Either delete @JsonDeserialize, or remove @Jacksonized and manually configure Jackson.");
			return;
		}
		long p = (long) ast.sourceStart << 32 | ast.sourceEnd;
		TypeReference builderClassExpression = namePlusTypeParamsToTypeReference(builderClassNode, null, p);
		ClassLiteralAccess builderClassLiteralAccess = new ClassLiteralAccess(td.sourceEnd, builderClassExpression);
		MemberValuePair builderMvp = new MemberValuePair("builder".toCharArray(), td.sourceStart, td.sourceEnd, builderClassLiteralAccess);
		td.annotations = addAnnotation(td, td.annotations, JSON_DESERIALIZE_ANNOTATION, builderMvp);
		
		// Copy annotations from the class to the builder class.
		Annotation[] copyableAnnotations = findJacksonAnnotationsOnClass(td, tdNode);
		builderClass.annotations = copyAnnotations(builderClass, builderClass.annotations, copyableAnnotations);
		
		// Insert @JsonPOJOBuilder on the builder class.
		StringLiteral withPrefixLiteral = new StringLiteral(setPrefix.toCharArray(), builderClass.sourceStart, builderClass.sourceEnd, 0);
		MemberValuePair withPrefixMvp = new MemberValuePair("withPrefix".toCharArray(), builderClass.sourceStart, builderClass.sourceEnd, withPrefixLiteral);
		StringLiteral buildMethodNameLiteral = new StringLiteral(buildMethodName.toCharArray(), builderClass.sourceStart, builderClass.sourceEnd, 0);
		MemberValuePair buildMethodNameMvp = new MemberValuePair("buildMethodName".toCharArray(), builderClass.sourceStart, builderClass.sourceEnd, buildMethodNameLiteral);
		builderClass.annotations = addAnnotation(builderClass, builderClass.annotations, JSON_POJO_BUILDER_ANNOTATION, withPrefixMvp, buildMethodNameMvp);
		
		// @SuperBuilder? Make it package-private!
		if (superBuilderAnnotationNode != null) 
			builderClass.modifiers = builderClass.modifiers & ~ClassFileConstants.AccPrivate;
	}
	
	private void handleJacksonizedAccessors(Annotation ast, EclipseNode annotationNode, EclipseNode annotatedNode, EclipseNode tdNode, TypeDeclaration td, EclipseNode accessorsAnnotationNode, boolean jacksonizedBuilder) {
		AnnotationValues<Accessors> accessorsAnnotation = accessorsAnnotationNode != null ? 
			createAnnotation(Accessors.class, accessorsAnnotationNode) : null;
		boolean fluent = accessorsAnnotation != null && accessorsAnnotation.getInstance().fluent();
		
		if (!fluent) {
			// No changes required for chained-only accessors.
			if (!jacksonizedBuilder) {
				annotationNode.addWarning("@Jacksonized only affects fluent accessors (@Accessors(fluent=true)).");
			}
			return;
		}
		
		// Add @JsonProperty to all fields. It will be automatically copied to the getter/setters later.
		for (EclipseNode eclipseNode : tdNode.down()) {
			if (eclipseNode.getKind() == Kind.FIELD) {
				createJsonPropertyForField(eclipseNode, annotationNode);
			}
		}
		tdNode.rebuild();
	}
	
	private void createJsonPropertyForField(EclipseNode fieldNode, EclipseNode annotationNode) {
		if (hasAnnotation("com.fasterxml.jackson.annotation.JsonProperty", fieldNode)) return;
		ASTNode astNode = fieldNode.get();
		if (astNode instanceof FieldDeclaration) {
			FieldDeclaration fd = (FieldDeclaration)astNode;
			StringLiteral fieldName = new StringLiteral(fd.name, 0, 0, 0);
			((FieldDeclaration) astNode).annotations = addAnnotation(fieldNode.get(), fd.annotations, JSON_PROPERTY_ANNOTATION, fieldName);
		}
	}
	
	private String getBuilderClassName(Annotation ast, EclipseNode annotationNode, EclipseNode annotatedNode, TypeDeclaration td, AnnotationValues<Builder> builderAnnotation) {
		String builderClassName = builderAnnotation != null ? 
			builderAnnotation.getInstance().builderClassName() : null;
		if (builderClassName == null || builderClassName.isEmpty()) {
			builderClassName = annotationNode.getAst().readConfiguration(ConfigurationKeys.BUILDER_CLASS_NAME);
			if (builderClassName == null || builderClassName.isEmpty()) builderClassName = "*Builder";
			
			MethodDeclaration fillParametersFrom = annotatedNode.get() instanceof MethodDeclaration ? (MethodDeclaration) annotatedNode.get() : null;
			char[] replacement;
			if (fillParametersFrom != null) {
				// @Builder on a method: Use name of return type for builder class name.
				replacement = HandleBuilder.returnTypeToBuilderClassName(annotationNode, fillParametersFrom, fillParametersFrom.typeParameters);
			} else {
				// @Builder on class or constructor: Use the class name.
				replacement = td.name;
			}
			builderClassName = builderClassName.replace("*", new String(replacement));
		}
		
		if (builderAnnotation == null) builderClassName += "Impl"; // For @SuperBuilder, all Jackson annotations must be put on the BuilderImpl class.
		
		return builderClassName;
	}
	
	private static final Annotation[] EMPTY_ANNOTATIONS_ARRAY = new Annotation[0];
	
	private static Annotation[] findJacksonAnnotationsOnClass(TypeDeclaration td, EclipseNode node) {
		if (td.annotations == null) return EMPTY_ANNOTATIONS_ARRAY;
		
		List<Annotation> result = new ArrayList<Annotation>();
		for (Annotation annotation : td.annotations) {
			TypeReference typeRef = annotation.type;
			if (typeRef != null && typeRef.getTypeName() != null) {
				for (String bn : HandlerUtil.JACKSON_COPY_TO_BUILDER_ANNOTATIONS) {
					if (typeMatches(bn, node, typeRef)) {
						result.add(annotation);
						break;
					}
				}
			}
		}
		return result.toArray(EMPTY_ANNOTATIONS_ARRAY);
	}
}
