/*
 * Copyright (C) 2021 The Project Lombok Authors.
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
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.experimental.StandardException;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.delombok.LombokOptionsFactory;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.*;
import lombok.spi.Provides;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

@Provides
public class HandleStandardException extends JavacAnnotationHandler<StandardException> {
	@Override
	public void handle(AnnotationValues<StandardException> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.STANDARD_EXCEPTION_FLAG_USAGE, "@StandardException");
		deleteAnnotationIfNeccessary(annotationNode, StandardException.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode typeNode = annotationNode.up();
		
		if (!isClass(typeNode)) {
			annotationNode.addError("@StandardException is only supported on a class");
			return;
		}
		
		JCTree extending = Javac.getExtendsClause((JCClassDecl) typeNode.get());
		if (extending == null) {
			annotationNode.addError("@StandardException requires that you extend a Throwable type");
			return;
		}
		
		AccessLevel access = annotation.getInstance().access();
		if (access == null) access = AccessLevel.PUBLIC;
		if (access == AccessLevel.NONE) {
			annotationNode.addError("AccessLevel.NONE is not valid here");
			access = AccessLevel.PUBLIC;
		}
		
		generateNoArgsConstructor(typeNode, access, annotationNode);
		generateMsgOnlyConstructor(typeNode, access, annotationNode);
		generateCauseOnlyConstructor(typeNode, access, annotationNode);
		generateFullConstructor(typeNode, access, annotationNode);
	}
	
	private void generateNoArgsConstructor(JavacNode typeNode, AccessLevel level, JavacNode source) {
		if (hasConstructor(typeNode) != MemberExistsResult.NOT_EXISTS) return;
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		List<JCExpression> args = List.<JCExpression>of(maker.Literal(CTC_BOT, null), maker.Literal(CTC_BOT, null));
		JCStatement thisCall = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(typeNode.toName("this")), args));
		JCMethodDecl constr = createConstructor(level, typeNode, false, false, source, List.of(thisCall));
		injectMethod(typeNode, constr);
	}
	
	private void generateMsgOnlyConstructor(JavacNode typeNode, AccessLevel level, JavacNode source) {
		if (hasConstructor(typeNode, String.class) != MemberExistsResult.NOT_EXISTS) return;
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		List<JCExpression> args = List.<JCExpression>of(maker.Ident(typeNode.toName("message")), maker.Literal(CTC_BOT, null));
		JCStatement thisCall = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(typeNode.toName("this")), args));
		JCMethodDecl constr = createConstructor(level, typeNode, true, false, source, List.of(thisCall));
		injectMethod(typeNode, constr);
	}
	
	private void generateCauseOnlyConstructor(JavacNode typeNode, AccessLevel level, JavacNode source) {
		if (hasConstructor(typeNode, Throwable.class) != MemberExistsResult.NOT_EXISTS) return;
		JavacTreeMaker maker = typeNode.getTreeMaker();
		Name causeName = typeNode.toName("cause");
		
		JCExpression causeDotGetMessage = maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(causeName), typeNode.toName("getMessage")), List.<JCExpression>nil());
		JCExpression msgExpression = maker.Conditional(maker.Binary(CTC_NOT_EQUAL, maker.Ident(causeName), maker.Literal(CTC_BOT, null)), causeDotGetMessage, maker.Literal(CTC_BOT, null));
		
		List<JCExpression> args = List.<JCExpression>of(msgExpression, maker.Ident(causeName));
		JCStatement thisCall = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(typeNode.toName("this")), args));
		JCMethodDecl constr = createConstructor(level, typeNode, false, true, source, List.of(thisCall));
		injectMethod(typeNode, constr);
	}
	
	private void generateFullConstructor(JavacNode typeNode, AccessLevel level, JavacNode source) {
		if (hasConstructor(typeNode, String.class, Throwable.class) != MemberExistsResult.NOT_EXISTS) return;
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		Name causeName = typeNode.toName("cause");
		Name superName = typeNode.toName("super");
		
		List<JCExpression> args = List.<JCExpression>of(maker.Ident(typeNode.toName("message")));
		JCStatement superCall = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Ident(superName), args));
		JCExpression causeNotNull = maker.Binary(CTC_NOT_EQUAL, maker.Ident(causeName), maker.Literal(CTC_BOT, null));
		JCStatement initCauseCall = maker.Exec(maker.Apply(List.<JCExpression>nil(), maker.Select(maker.Ident(superName), typeNode.toName("initCause")), List.<JCExpression>of(maker.Ident(causeName))));
		JCStatement initCause = maker.If(causeNotNull, initCauseCall, null);
		JCMethodDecl constr = createConstructor(level, typeNode, true, true, source, List.of(superCall, initCause));
		injectMethod(typeNode, constr);
	}
	
	private static MemberExistsResult hasConstructor(JavacNode node, Class<?>... paramTypes) {
		node = upToTypeNode(node);
		
		if (node != null && node.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl) node.get()).defs) {
				if (def instanceof JCMethodDecl) {
					JCMethodDecl md = (JCMethodDecl) def;
					if (md.name.contentEquals("<init>") && (md.mods.flags & Flags.GENERATEDCONSTR) == 0) {
						if (!paramsMatch(node, md.params, paramTypes)) continue;
						return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
					}
				}
			}
		}
		
		return MemberExistsResult.NOT_EXISTS;
	}
	
	private static boolean paramsMatch(JavacNode node, List<JCVariableDecl> a, Class<?>[] b) {
		if (a == null) return b == null || b.length == 0;
		if (b == null) return a.size() == 0;
		if (a.size() != b.length) return false;
		
		for (int i = 0; i < a.size(); i++) {
			JCVariableDecl param = a.get(i);
			Class<?> c = b[i];
			if (!typeMatches(c, node, param.vartype)) return false;
		}
		
		return true;
	}

	private static void addConstructorProperties(JCModifiers mods, JavacNode node, boolean msgParam, boolean causeParam) {
		if (!msgParam && !causeParam) return;
		JavacTreeMaker maker = node.getTreeMaker();
		JCExpression constructorPropertiesType = chainDots(node, "java", "beans", "ConstructorProperties");
		ListBuffer<JCExpression> fieldNames = new ListBuffer<JCExpression>();
		if (msgParam) fieldNames.append(maker.Literal("message"));
		if (causeParam) fieldNames.append(maker.Literal("cause"));
		JCExpression fieldNamesArray = maker.NewArray(null, List.<JCExpression>nil(), fieldNames.toList());
		JCAnnotation annotation = maker.Annotation(constructorPropertiesType, List.of(fieldNamesArray));
		mods.annotations = mods.annotations.append(annotation);
	}
	
	@SuppressWarnings("deprecation") private static JCMethodDecl createConstructor(AccessLevel level, JavacNode typeNode, boolean msgParam, boolean causeParam, JavacNode source, List<JCStatement> statements) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		boolean addConstructorProperties;
		if ((!msgParam && !causeParam) || isLocalType(typeNode) || !LombokOptionsFactory.getDelombokOptions(typeNode.getContext()).getFormatPreferences().generateConstructorProperties()) {
			addConstructorProperties = false;
		} else {
			Boolean v = typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES);
			addConstructorProperties = v != null ? v.booleanValue() :
				Boolean.FALSE.equals(typeNode.getAst().readConfiguration(ConfigurationKeys.ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES));
		}
		
		ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
		
		if (msgParam) {
			Name fieldName = typeNode.toName("message");
			long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
			JCExpression pType = genJavaLangTypeRef(typeNode, "String");
			JCVariableDecl param = maker.VarDef(maker.Modifiers(flags), fieldName, pType, null);
			params.append(param);
		}
		
		if (causeParam) {
			Name fieldName = typeNode.toName("cause");
			long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, typeNode.getContext());
			JCExpression pType = genJavaLangTypeRef(typeNode, "Throwable");
			JCVariableDecl param = maker.VarDef(maker.Modifiers(flags), fieldName, pType, null);
			params.append(param);
		}
		
		JCModifiers mods = maker.Modifiers(toJavacModifier(level), List.<JCAnnotation>nil());
		if (addConstructorProperties) addConstructorProperties(mods, typeNode, msgParam, causeParam);
		return recursiveSetGeneratedBy(maker.MethodDef(mods, typeNode.toName("<init>"),
			null, List.<JCTypeParameter>nil(), params.toList(), List.<JCExpression>nil(),
			maker.Block(0L, statements), null), source);
	}
	
	public static boolean isLocalType(JavacNode type) {
		Kind kind = type.up().getKind();
		if (kind == Kind.COMPILATION_UNIT) return false;
		if (kind == Kind.TYPE) return isLocalType(type.up());
		return true;
	}
}
