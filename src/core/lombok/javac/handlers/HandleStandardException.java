/*
 * Copyright (C) 2010-2019 The Project Lombok Authors.
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

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.StandardException;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.delombok.LombokOptionsFactory;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.*;
import org.mangosdk.spi.ProviderFor;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.Javac.CTC_VOID;
import static lombok.javac.handlers.JavacHandlerUtil.*;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleStandardException extends JavacAnnotationHandler<StandardException> {
	private static final String NAME = StandardException.class.getSimpleName();

	@Override
	public void handle(AnnotationValues<StandardException> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.STANDARD_EXCEPTION_FLAG_USAGE, "@StandardException");
		deleteAnnotationIfNeccessary(annotationNode, StandardException.class);
		JavacNode typeNode = annotationNode.up();
		if (!checkLegality(typeNode, annotationNode)) return;

		SuperParameter messageField = new SuperParameter("message", typeNode.getSymbolTable().stringType);
		SuperParameter causeField = new SuperParameter("cause", typeNode.getSymbolTable().throwableType);

		boolean skip = true;
		generateConstructor(typeNode, AccessLevel.PUBLIC, List.<SuperParameter>nil(), skip, annotationNode);
		generateConstructor(typeNode, AccessLevel.PUBLIC, List.of(messageField), skip, annotationNode);
		generateConstructor(typeNode, AccessLevel.PUBLIC, List.of(causeField), skip, annotationNode);
		generateConstructor(typeNode, AccessLevel.PUBLIC, List.of(messageField, causeField), skip, annotationNode);
	}

	private static boolean checkLegality(JavacNode typeNode, JavacNode errorNode) {
		JCClassDecl typeDecl = null;
		if (typeNode.get() instanceof JCClassDecl) typeDecl = (JCClassDecl) typeNode.get();
		long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError(NAME + " is only supported on a class or an enum.");
			return false;
		}
		
		return true;
	}

	public void generateConstructor(JavacNode typeNode, AccessLevel level, List<SuperParameter> fields,
									boolean skipIfConstructorExists, JavacNode source) {
		generate(typeNode, level, fields, skipIfConstructorExists, source);
	}

	private void generate(JavacNode typeNode, AccessLevel level, List<SuperParameter> fields, boolean skipIfConstructorExists,
						  JavacNode source) {
		ListBuffer<Type> argTypes = new ListBuffer<Type>();
		for (SuperParameter field : fields) {
			Type mirror = field.type;
			if (mirror == null) {
				argTypes = null;
				break;
			}
			argTypes.append(mirror);
		}
		List<Type> argTypes_ = argTypes == null ? null : argTypes.toList();

		if (!(skipIfConstructorExists && constructorExists(typeNode, fields) != MemberExistsResult.NOT_EXISTS)) {
			JCMethodDecl constr = createConstructor(level, typeNode, fields, source);
			injectMethod(typeNode, constr, argTypes_, Javac.createVoidType(typeNode.getSymbolTable(), CTC_VOID));
		}
	}

	public static MemberExistsResult constructorExists(JavacNode node, List<SuperParameter> parameters) {
		node = upToTypeNode(node);

		if (node != null && node.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl) node.get()).defs) {
				if (def instanceof JCMethodDecl) {
					JCMethodDecl md = (JCMethodDecl) def;
					if (md.name.contentEquals("<init>") && (md.mods.flags & Flags.GENERATEDCONSTR) == 0) {
						if (!paramsMatch(md.params, parameters)) continue;
						return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}

		return MemberExistsResult.NOT_EXISTS;
	}

	private static boolean paramsMatch(List<JCVariableDecl> params, List<SuperParameter> superParams) {
		if (params == null) {
			return superParams.size() == 0;
		} else if (params.size() != superParams.size()) {
			return false;
		} else {
			for (int i = 0; i < superParams.size(); i++) {
				SuperParameter field = superParams.get(i);
				JCVariableDecl param = params.get(i);
				if (!param.getType().type.equals(field.type))
					return false;
			}
		}

		return true;
	}

	public static void addConstructorProperties(JCModifiers mods, JavacNode node, List<SuperParameter> fields) {
		if (fields.isEmpty()) return;
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression constructorPropertiesType = chainDots(node, "java", "beans", "ConstructorProperties");
		ListBuffer<JCExpression> fieldNames = new ListBuffer<JCExpression>();
		for (SuperParameter field : fields) {
			Name fieldName = node.toName(field.name);
			fieldNames.append(maker.Literal(fieldName.toString()));
		}
		JCExpression fieldNamesArray = maker.NewArray(null, List.<JCExpression>nil(), fieldNames.toList());
		JCAnnotation annotation = maker.Annotation(constructorPropertiesType, List.of(fieldNamesArray));
		mods.annotations = mods.annotations.append(annotation);
	}
	
	@SuppressWarnings("deprecation") public static JCMethodDecl createConstructor(AccessLevel level, JavacNode typeNode,
																				  List<SuperParameter> fieldsToParam, JavacNode source) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		boolean isEnum = (((JCClassDecl) typeNode.get()).mods.flags & Flags.ENUM) != 0;
		if (isEnum) level = AccessLevel.PRIVATE;
		
		boolean addConstructorProperties;

		if (fieldsToParam.isEmpty()) {
			addConstructorProperties = false;
		} else {
			Boolean v = typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES);
			addConstructorProperties = v != null ? v.booleanValue() :
				Boolean.FALSE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES));
		}

		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		ListBuffer<JCExpression> superArgs = new ListBuffer<JCExpression>();
		
		for (SuperParameter fieldNode : fieldsToParam) {
			Name fieldName = source.toName(fieldNode.name);
			long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
			JCExpression pType = maker.getUnderlyingTreeMaker().Ident(fieldNode.type.tsym);
			JCVariableDecl param = maker.VarDef(maker.Modifiers(flags), fieldName, pType, null);
			params.append(param);
			superArgs.append(maker.Ident(fieldName));
		}

		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		JCMethodInvocation callToSuper = maker.Apply(List.<JCExpression>nil(),
				maker.Ident(typeNode.toName("super")),
				superArgs.toList());
		statements.add(maker.Exec(callToSuper));

		JCModifiers mods = maker.Modifiers(toJavacModifier(level), List.<JCAnnotation>nil());
		if (addConstructorProperties && !isLocalType(typeNode) && LombokOptionsFactory.getDelombokOptions(typeNode.getContext()).getFormatPreferences().generateConstructorProperties()) {
			addConstructorProperties(mods, typeNode, fieldsToParam);
		}
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("<init>"),
			null, List.<JCTypeParameter>nil(), params.toList(), List.<JCExpression>nil(),
			maker.Block(0L, statements.toList()), null), source.get(), typeNode.getContext());
	}

	public static boolean isLocalType(JavacNode type) {
		Kind kind = type.up().getKind();
		if (kind == Kind.COMPILATION_UNIT) return false;
		if (kind == Kind.TYPE) return isLocalType(type.up());
		return true;
	}

	private static class SuperParameter {
		private final String name;
		private final Type type;

		private SuperParameter(String name, Type type) {
			this.name = name;
			this.type = type;
		}
	}
}
