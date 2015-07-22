/*
 * Copyright (C) 2013-2015 The Project Lombok Authors.
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.ConfigurationKeys;
import lombok.Singular;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.handlers.HandlerUtil;
import lombok.experimental.NonFinal;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;
import static lombok.javac.Javac.*;
import static lombok.javac.JavacTreeMaker.TypeTag.*;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(-1024) //-2^10; to ensure we've picked up @FieldDefault's changes (-2048) but @Value hasn't removed itself yet (-512), so that we can error on presence of it on the builder classes.
public class HandleBuilder extends JavacAnnotationHandler<Builder> {
	private static final boolean toBoolean(Object expr, boolean defaultValue) {
		if (expr == null) return defaultValue;
		if (expr instanceof JCLiteral) return ((Integer) ((JCLiteral) expr).value) != 0;
		return ((Boolean) expr).booleanValue();
	}
	
	private static class BuilderFieldData {
		JCExpression type;
		Name name;
		SingularData singularData;
		
		java.util.List<JavacNode> createdFields = new ArrayList<JavacNode>();
	}
	
	@Override public void handle(AnnotationValues<Builder> annotation, JCAnnotation ast, JavacNode annotationNode) {
		Builder builderInstance = annotation.getInstance();
		
		// These exist just to support the 'old' lombok.experimental.Builder, which had these properties. lombok.Builder no longer has them.
		boolean fluent = toBoolean(annotation.getActualExpression("fluent"), true);
		boolean chain = toBoolean(annotation.getActualExpression("chain"), true);
		
		String builderMethodName = builderInstance.builderMethodName();
		String buildMethodName = builderInstance.buildMethodName();
		String builderClassName = builderInstance.builderClassName();
		
		if (builderMethodName == null) builderMethodName = "builder";
		if (buildMethodName == null) buildMethodName = "build";
		if (builderClassName == null) builderClassName = "";
		
		if (!checkName("builderMethodName", builderMethodName, annotationNode)) return;
		if (!checkName("buildMethodName", buildMethodName, annotationNode)) return;
		if (!builderClassName.isEmpty()) {
			if (!checkName("builderClassName", builderClassName, annotationNode)) return;
		}
		
		@SuppressWarnings("deprecation")
		Class<? extends Annotation> oldExperimentalBuilder = lombok.experimental.Builder.class;
		deleteAnnotationIfNeccessary(annotationNode, Builder.class, oldExperimentalBuilder);
		
		JavacNode parent = annotationNode.up();
		
		java.util.List<BuilderFieldData> builderFields = new ArrayList<BuilderFieldData>();
		JCExpression returnType;
		List<JCTypeParameter> typeParams = List.nil();
		List<JCExpression> thrownExceptions = List.nil();
		Name nameOfStaticBuilderMethod;
		JavacNode tdParent;
		
		JavacNode fillParametersFrom = parent.get() instanceof JCMethodDecl ? parent : null;
		boolean addCleaning = false;
		
		if (parent.get() instanceof JCClassDecl) {
			tdParent = parent;
			JCClassDecl td = (JCClassDecl) tdParent.get();
			ListBuffer<JavacNode> allFields = new ListBuffer<JavacNode>();
			@SuppressWarnings("deprecation")
			boolean valuePresent = (hasAnnotation(lombok.Value.class, parent) || hasAnnotation(lombok.experimental.Value.class, parent));
			for (JavacNode fieldNode : HandleConstructor.findAllFields(tdParent)) {
				JCVariableDecl fd = (JCVariableDecl) fieldNode.get();
				// final fields with an initializer cannot be written to, so they can't be 'builderized'. Unfortunately presence of @Value makes
				// non-final fields final, but @Value's handler hasn't done this yet, so we have to do this math ourselves.
				// Value will only skip making a field final if it has an explicit @NonFinal annotation, so we check for that.
				if (fd.init != null && valuePresent && !hasAnnotation(NonFinal.class, fieldNode)) continue;
				BuilderFieldData bfd = new BuilderFieldData();
				bfd.name = removePrefixFromField(fieldNode);
				bfd.type = fd.vartype;
				bfd.singularData = getSingularData(fieldNode);
				builderFields.add(bfd);
				allFields.append(fieldNode);
			}
			
			new HandleConstructor().generateConstructor(tdParent, AccessLevel.PACKAGE, List.<JCAnnotation>nil(), allFields.toList(), false, null, SkipIfConstructorExists.I_AM_BUILDER, null, annotationNode);
			
			returnType = namePlusTypeParamsToTypeReference(tdParent.getTreeMaker(), td.name, td.typarams);
			typeParams = td.typarams;
			thrownExceptions = List.nil();
			nameOfStaticBuilderMethod = null;
			if (builderClassName.isEmpty()) builderClassName = td.name.toString() + "Builder";
		} else if (fillParametersFrom != null && fillParametersFrom.getName().toString().equals("<init>")) {
			JCMethodDecl jmd = (JCMethodDecl) fillParametersFrom.get();
			if (!jmd.typarams.isEmpty()) {
				annotationNode.addError("@Builder is not supported on constructors with constructor type parameters.");
				return;
			}
			
			tdParent = parent.up();
			JCClassDecl td = (JCClassDecl) tdParent.get();
			returnType = namePlusTypeParamsToTypeReference(tdParent.getTreeMaker(), td.name, td.typarams);
			typeParams = td.typarams;
			thrownExceptions = jmd.thrown;
			nameOfStaticBuilderMethod = null;
			if (builderClassName.isEmpty()) builderClassName = td.name.toString() + "Builder";
		} else if (fillParametersFrom != null) {
			tdParent = parent.up();
			JCClassDecl td = (JCClassDecl) tdParent.get();
			JCMethodDecl jmd = (JCMethodDecl) fillParametersFrom.get();
			if ((jmd.mods.flags & Flags.STATIC) == 0) {
				annotationNode.addError("@Builder is only supported on types, constructors, and static methods.");
				return;
			}
			returnType = jmd.restype;
			typeParams = jmd.typarams;
			thrownExceptions = jmd.thrown;
			nameOfStaticBuilderMethod = jmd.name;
			if (returnType instanceof JCTypeApply) {
				returnType = ((JCTypeApply) returnType).clazz;
			}
			if (builderClassName.isEmpty()) {
				if (returnType instanceof JCFieldAccess) {
					builderClassName = ((JCFieldAccess) returnType).name.toString() + "Builder";
				} else if (returnType instanceof JCIdent) {
					Name n = ((JCIdent) returnType).name;
					
					for (JCTypeParameter tp : typeParams) {
						if (tp.name.equals(n)) {
							annotationNode.addError("@Builder requires specifying 'builderClassName' if used on methods with a type parameter as return type.");
							return;
						}
					}
					builderClassName = n.toString() + "Builder";
				} else if (returnType instanceof JCPrimitiveTypeTree) {
					builderClassName = returnType.toString() + "Builder";
					if (Character.isLowerCase(builderClassName.charAt(0))) {
						builderClassName = Character.toTitleCase(builderClassName.charAt(0)) + builderClassName.substring(1);
					}
				} else {
					// This shouldn't happen.
					System.err.println("Lombok bug ID#20140614-1651: javac HandleBuilder: return type to name conversion failed: " + returnType.getClass());
					builderClassName = td.name.toString() + "Builder";
				}
			}
		} else {
			annotationNode.addError("@Builder is only supported on types, constructors, and static methods.");
			return;
		}
		
		if (fillParametersFrom != null) {
			for (JavacNode param : fillParametersFrom.down()) {
				if (param.getKind() != Kind.ARGUMENT) continue;
				BuilderFieldData bfd = new BuilderFieldData();
				JCVariableDecl raw = (JCVariableDecl) param.get();
				bfd.name = raw.name;
				bfd.type = raw.vartype;
				bfd.singularData = getSingularData(param);
				builderFields.add(bfd);
			}
		}
		
		JavacNode builderType = findInnerClass(tdParent, builderClassName);
		if (builderType == null) {
			builderType = makeBuilderClass(tdParent, builderClassName, typeParams, ast);
		} else {
			sanityCheckForMethodGeneratingAnnotationsOnBuilderClass(builderType, annotationNode);
			/* generate errors for @Singular BFDs that have one already defined node. */ {
				for (BuilderFieldData bfd : builderFields) {
					SingularData sd = bfd.singularData;
					if (sd == null) continue;
					JavacSingularizer singularizer = sd.getSingularizer();
					if (singularizer == null) continue;
					if (singularizer.checkForAlreadyExistingNodesAndGenerateError(builderType, sd)) {
						bfd.singularData = null;
					}
				}
			}

		}
		
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				if (bfd.singularData.getSingularizer().requiresCleaning()) {
					addCleaning = true;
					break;
				}
			}
		}
		
		generateBuilderFields(builderType, builderFields, ast);
		if (addCleaning) {
			JavacTreeMaker maker = builderType.getTreeMaker();
			JCVariableDecl uncleanField = maker.VarDef(maker.Modifiers(Flags.PRIVATE), builderType.toName("$lombokUnclean"), maker.TypeIdent(CTC_BOOLEAN), null);
			injectFieldAndMarkGenerated(builderType, uncleanField);
		}
		
		if (constructorExists(builderType) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl cd = HandleConstructor.createConstructor(AccessLevel.PACKAGE, List.<JCAnnotation>nil(), builderType, List.<JavacNode>nil(), false, null, annotationNode);
			if (cd != null) injectMethod(builderType, cd);
		}
		
		for (BuilderFieldData bfd : builderFields) {
			makeSetterMethodsForBuilder(builderType, bfd, annotationNode, fluent, chain);
		}
		
		if (methodExists(buildMethodName, builderType, -1) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl md = generateBuildMethod(buildMethodName, nameOfStaticBuilderMethod, returnType, builderFields, builderType, thrownExceptions, ast, addCleaning);
			if (md != null) injectMethod(builderType, md);
		}
		
		if (methodExists("toString", builderType, 0) == MemberExistsResult.NOT_EXISTS) {
			java.util.List<JavacNode> fieldNodes = new ArrayList<JavacNode>();
			for (BuilderFieldData bfd : builderFields) {
				fieldNodes.addAll(bfd.createdFields);
			}
			JCMethodDecl md = HandleToString.createToString(builderType, fieldNodes, true, false, FieldAccess.ALWAYS_FIELD, ast);
			if (md != null) injectMethod(builderType, md);
		}
		
		if (addCleaning) injectMethod(builderType, generateCleanMethod(builderFields, builderType, ast));
		
		if (methodExists(builderMethodName, tdParent, -1) == MemberExistsResult.NOT_EXISTS) {
			JCMethodDecl md = generateBuilderMethod(builderMethodName, builderClassName, tdParent, typeParams);
			recursiveSetGeneratedBy(md, ast, annotationNode.getContext());
			if (md != null) injectMethod(tdParent, md);
		}
		
		recursiveSetGeneratedBy(builderType.get(), ast, annotationNode.getContext());
	}
	
	private JCMethodDecl generateCleanMethod(java.util.List<BuilderFieldData> builderFields, JavacNode type, JCTree source) {
		JavacTreeMaker maker = type.getTreeMaker();
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendCleaningCode(bfd.singularData, type, source, statements);
			}
		}
		
		statements.append(maker.Exec(maker.Assign(maker.Select(maker.Ident(type.toName("this")), type.toName("$lombokUnclean")), maker.Literal(CTC_BOOLEAN, false))));
		JCBlock body = maker.Block(0, statements.toList());
		return maker.MethodDef(maker.Modifiers(Flags.PUBLIC), type.toName("$lombokClean"), maker.Type(Javac.createVoidType(maker, CTC_VOID)), List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
		/*
		 * 		if (shouldReturnThis) {
			methodType = cloneSelfType(field);
		}
		
		if (methodType == null) {
			//WARNING: Do not use field.getSymbolTable().voidType - that field has gone through non-backwards compatible API changes within javac1.6.
			methodType = treeMaker.Type(Javac.createVoidType(treeMaker, CTC_VOID));
			shouldReturnThis = false;
		}

		 */
	}
	
	private JCMethodDecl generateBuildMethod(String name, Name staticName, JCExpression returnType, java.util.List<BuilderFieldData> builderFields, JavacNode type, List<JCExpression> thrownExceptions, JCTree source, boolean addCleaning) {
		JavacTreeMaker maker = type.getTreeMaker();
		
		JCExpression call;
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		
		if (addCleaning) {
			JCExpression notClean = maker.Unary(CTC_NOT, maker.Select(maker.Ident(type.toName("this")), type.toName("$lombokUnclean")));
			JCStatement invokeClean = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(type.toName("$lombokClean")), List.<JCExpression>nil()));
			JCIf ifUnclean = maker.If(notClean, invokeClean, null);
			statements.append(ifUnclean);
		}
		
		for (BuilderFieldData bfd : builderFields) {
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.singularData.getSingularizer().appendBuildCode(bfd.singularData, type, source, statements, bfd.name);
			}
		}
		
		ListBuffer<JCExpression> args = new ListBuffer<JCExpression>();
		for (BuilderFieldData bfd : builderFields) {
			args.append(maker.Ident(bfd.name));
		}
		
		if (addCleaning) {
			statements.append(maker.Exec(maker.Assign(maker.Select(maker.Ident(type.toName("this")), type.toName("$lombokUnclean")), maker.Literal(CTC_BOOLEAN, true))));
		}
		
		if (staticName == null) {
			call = maker.NewClass(null, List.<JCExpression>nil(), returnType, args.toList(), null);
			statements.append(maker.Return(call));
		} else {
			ListBuffer<JCExpression> typeParams = new ListBuffer<JCExpression>();
			for (JCTypeParameter tp : ((JCClassDecl) type.get()).typarams) {
				typeParams.append(maker.Ident(tp.name));
			}
			
			JCExpression fn = maker.Select(maker.Ident(((JCClassDecl) type.up().get()).name), staticName);
			call = maker.Apply(typeParams.toList(), fn, args.toList());
			if (returnType instanceof JCPrimitiveTypeTree && CTC_VOID.equals(typeTag(returnType))) {
				statements.append(maker.Exec(call));
			} else {
				statements.append(maker.Return(call));
			}
		}
		
		JCBlock body = maker.Block(0, statements.toList());
		
		return maker.MethodDef(maker.Modifiers(Flags.PUBLIC), type.toName(name), returnType, List.<JCTypeParameter>nil(), List.<JCVariableDecl>nil(), thrownExceptions, body, null);
	}
	
	public JCMethodDecl generateBuilderMethod(String builderMethodName, String builderClassName, JavacNode type, List<JCTypeParameter> typeParams) {
		JavacTreeMaker maker = type.getTreeMaker();
		
		ListBuffer<JCExpression> typeArgs = new ListBuffer<JCExpression>();
		for (JCTypeParameter typeParam : typeParams) {
			typeArgs.append(maker.Ident(typeParam.name));
		}
		
		JCExpression call = maker.NewClass(null, List.<JCExpression>nil(), namePlusTypeParamsToTypeReference(maker, type.toName(builderClassName), typeParams), List.<JCExpression>nil(), null);
		JCStatement statement = maker.Return(call);
		
		JCBlock body = maker.Block(0, List.<JCStatement>of(statement));
		return maker.MethodDef(maker.Modifiers(Flags.STATIC | Flags.PUBLIC), type.toName(builderMethodName), namePlusTypeParamsToTypeReference(maker, type.toName(builderClassName), typeParams), copyTypeParams(maker, typeParams), List.<JCVariableDecl>nil(), List.<JCExpression>nil(), body, null);
	}
	
	public void generateBuilderFields(JavacNode builderType, java.util.List<BuilderFieldData> builderFields, JCTree source) {
		int len = builderFields.size();
		java.util.List<JavacNode> existing = new ArrayList<JavacNode>();
		for (JavacNode child : builderType.down()) {
			if (child.getKind() == Kind.FIELD) existing.add(child);
		}
		
		top:
		for (int i = len - 1; i >= 0; i--) {
			BuilderFieldData bfd = builderFields.get(i);
			if (bfd.singularData != null && bfd.singularData.getSingularizer() != null) {
				bfd.createdFields.addAll(bfd.singularData.getSingularizer().generateFields(bfd.singularData, builderType, source));
			} else {
				for (JavacNode exists : existing) {
					Name n = ((JCVariableDecl) exists.get()).name;
					if (n.equals(bfd.name)) {
						bfd.createdFields.add(exists);
						continue top;
					}
				}
				JavacTreeMaker maker = builderType.getTreeMaker();
				JCModifiers mods = maker.Modifiers(Flags.PRIVATE);
				JCVariableDecl newField = maker.VarDef(mods, bfd.name, cloneType(maker, bfd.type, source, builderType.getContext()), null);
				bfd.createdFields.add(injectFieldAndMarkGenerated(builderType, newField));
			}
		}
	}
	
	public void makeSetterMethodsForBuilder(JavacNode builderType, BuilderFieldData fieldNode, JavacNode source, boolean fluent, boolean chain) {
		if (fieldNode.singularData == null || fieldNode.singularData.getSingularizer() == null) {
			makeSimpleSetterMethodForBuilder(builderType, fieldNode.createdFields.get(0), source, fluent, chain);
		} else {
			fieldNode.singularData.getSingularizer().generateMethods(fieldNode.singularData, builderType, source.get(), fluent, chain);
		}
	}
	
	private void makeSimpleSetterMethodForBuilder(JavacNode builderType, JavacNode fieldNode, JavacNode source, boolean fluent, boolean chain) {
		Name fieldName = ((JCVariableDecl) fieldNode.get()).name;
		
		for (JavacNode child : builderType.down()) {
			if (child.getKind() != Kind.METHOD) continue;
			Name existingName = ((JCMethodDecl) child.get()).name;
			if (existingName.equals(fieldName)) return;
		}
		
		String setterName = fluent ? fieldNode.getName() : HandlerUtil.buildAccessorName("set", fieldNode.getName());
		
		JavacTreeMaker maker = fieldNode.getTreeMaker();
		JCMethodDecl newMethod = HandleSetter.createSetter(Flags.PUBLIC, fieldNode, maker, setterName, chain, source, List.<JCAnnotation>nil(), List.<JCAnnotation>nil());
		injectMethod(builderType, newMethod);
	}
	
	public JavacNode findInnerClass(JavacNode parent, String name) {
		for (JavacNode child : parent.down()) {
			if (child.getKind() != Kind.TYPE) continue;
			JCClassDecl td = (JCClassDecl) child.get();
			if (td.name.contentEquals(name)) return child;
		}
		return null;
	}
	
	public JavacNode makeBuilderClass(JavacNode tdParent, String builderClassName, List<JCTypeParameter> typeParams, JCAnnotation ast) {
		JavacTreeMaker maker = tdParent.getTreeMaker();
		JCModifiers mods = maker.Modifiers(Flags.PUBLIC | Flags.STATIC);
		JCClassDecl builder = maker.ClassDef(mods, tdParent.toName(builderClassName), copyTypeParams(maker, typeParams), null, List.<JCExpression>nil(), List.<JCTree>nil());
		return injectType(tdParent, builder);
	}
	
	/**
	 * Returns the explicitly requested singular annotation on this node (field
	 * or parameter), or null if there's no {@code @Singular} annotation on it.
	 * 
	 * @param node The node (field or method param) to inspect for its name and potential {@code @Singular} annotation.
	 */
	private SingularData getSingularData(JavacNode node) {
		for (JavacNode child : node.down()) {
			if (child.getKind() == Kind.ANNOTATION && annotationTypeMatches(Singular.class, child)) {
				Name pluralName = node.getKind() == Kind.FIELD ? removePrefixFromField(node) : ((JCVariableDecl) node.get()).name;
				AnnotationValues<Singular> ann = createAnnotation(Singular.class, child);
				deleteAnnotationIfNeccessary(child, Singular.class);
				String explicitSingular = ann.getInstance().value();
				if (explicitSingular.isEmpty()) {
					if (Boolean.FALSE.equals(node.getAst().readConfiguration(ConfigurationKeys.SINGULAR_AUTO))) {
						node.addError("The singular must be specified explicitly (e.g. @Singular(\"task\")) because auto singularization is disabled.");
						explicitSingular = pluralName.toString();
					} else {
						explicitSingular = autoSingularize(node.getName());
						if (explicitSingular == null) {
							node.addError("Can't singularize this name; please specify the singular explicitly (i.e. @Singular(\"sheep\"))");
							explicitSingular = pluralName.toString();
						}
					}
				}
				Name singularName = node.toName(explicitSingular);
				
				JCExpression type = null;
				if (node.get() instanceof JCVariableDecl) {
					type = ((JCVariableDecl) node.get()).vartype;
				}
				
				String name = null;
				List<JCExpression> typeArgs = List.nil();
				if (type instanceof JCTypeApply) {
					typeArgs = ((JCTypeApply) type).arguments;
					type = ((JCTypeApply) type).clazz;
				}
				
				name = type.toString();
				
				String targetFqn = JavacSingularsRecipes.get().toQualified(name);
				JavacSingularizer singularizer = JavacSingularsRecipes.get().getSingularizer(targetFqn);
				if (singularizer == null) {
					node.addError("Lombok does not know how to create the singular-form builder methods for type '" + name + "'; they won't be generated.");
					return null;
				}
				
				return new SingularData(child, singularName, pluralName, typeArgs, targetFqn, singularizer);
			}
		}
		
		return null;
	}
}
