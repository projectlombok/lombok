/*
 * Copyright (C) 2015-2025 The Project Lombok Authors.
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
package lombok.eclipse.handlers.singulars;

import static lombok.eclipse.Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.core.LombokImmutableList;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;
import lombok.eclipse.handlers.EclipseSingularsRecipes.StatementMaker;
import lombok.eclipse.handlers.EclipseSingularsRecipes.TypeReferenceMaker;
import lombok.spi.Provides;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

@Provides(EclipseSingularizer.class)
public class EclipseJavaUtilOptionalSingularizer extends EclipseSingularizer {
	private static final char[][] JAVA_UTIL_OPTIONAL = {
		{'j', 'a', 'v', 'a'},
		{'u', 't', 'i', 'l'},
		{'O', 'p', 't', 'i', 'o', 'n', 'a', 'l'}
	};

	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.Optional");
	}

	@Override public List<char[]> listFieldsToBeGenerated(SingularData data, EclipseNode builderType) {
		char[] valueFieldName = (new String(data.getSingularName()) + "$value").toCharArray();
		char[] setFieldName = (new String(data.getSingularName()) + "$set").toCharArray();
		return Arrays.asList(valueFieldName, setFieldName);
	}

	@Override public List<char[]> listMethodsToBeGenerated(SingularData data, EclipseNode builderType) {
		char[] singularName = data.getSingularName();
		String singularStr = new String(singularName);
		char[] clearName = ("clear" + Character.toUpperCase(singularStr.charAt(0)) + singularStr.substring(1)).toCharArray();
		return Arrays.asList(singularName, clearName);
	}

	@Override public List<EclipseNode> generateFields(SingularData data, EclipseNode builderType) {
		char[] valueFieldName = (new String(data.getSingularName()) + "$value").toCharArray();
		char[] setFieldName = (new String(data.getSingularName()) + "$set").toCharArray();

		FieldDeclaration valueField = new FieldDeclaration(valueFieldName, 0, 0);
		valueField.type = cloneParamType(0, data.getTypeArgs(), builderType);
		valueField.modifiers = ClassFileConstants.AccPrivate;
		valueField.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		EclipseNode valueFieldNode = injectFieldAndMarkGenerated(builderType, valueField);
		data.setGeneratedByRecursive(valueField);

		FieldDeclaration setField = new FieldDeclaration(setFieldName, 0, 0);
		setField.type = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		setField.modifiers = ClassFileConstants.AccPrivate;
		setField.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		EclipseNode setFieldNode = injectFieldAndMarkGenerated(builderType, setField);
		data.setGeneratedByRecursive(setField);

		return Arrays.asList(valueFieldNode, setFieldNode);
	}

	@Override public void generateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, EclipseNode builderType, boolean fluent, TypeReferenceMaker returnTypeMaker, StatementMaker returnStatementMaker, AccessLevel access) {
		generateSingularMethod(cfv, deprecate, returnTypeMaker, returnStatementMaker, data, builderType, fluent, access);
		generateClearMethod(cfv, deprecate, returnTypeMaker, returnStatementMaker, data, builderType, access);
	}

	private void generateSingularMethod(CheckerFrameworkVersion cfv, boolean deprecate, TypeReferenceMaker returnTypeMaker, StatementMaker returnStatementMaker, SingularData data, EclipseNode builderType, boolean fluent, AccessLevel access) {
		char[] valueFieldName = (new String(data.getSingularName()) + "$value").toCharArray();
		char[] setFieldName = (new String(data.getSingularName()) + "$set").toCharArray();

		List<Statement> statements = new ArrayList<Statement>();

		FieldReference thisDotValueField = new FieldReference(valueFieldName, 0L);
		thisDotValueField.receiver = new ThisReference(0, 0);

		FieldReference thisDotSetField = new FieldReference(setFieldName, 0L);
		thisDotSetField.receiver = new ThisReference(0, 0);

		SingleNameReference paramRef = new SingleNameReference(data.getSingularName(), 0L);

		statements.add(new Assignment(thisDotValueField, paramRef, 0));
		statements.add(new Assignment(thisDotSetField, new TrueLiteral(0, 0), 0));

		Statement returnStatement = returnStatementMaker.make();
		if (returnStatement != null) statements.add(returnStatement);

		TypeReference paramType = cloneParamType(0, data.getTypeArgs(), builderType);
		Argument param = new Argument(data.getSingularName(), 0, paramType, ClassFileConstants.AccFinal);

		MethodDeclaration method = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.modifiers = toEclipseModifier(access);

		char[] methodName = data.getSingularName();
		char[] setterPrefix = data.getSetterPrefix();
		if (setterPrefix.length > 0) {
			String prefixStr = new String(setterPrefix);
			String nameStr = new String(methodName);
			methodName = (prefixStr + Character.toUpperCase(nameStr.charAt(0)) + nameStr.substring(1)).toCharArray();
		}

		method.selector = methodName;
		method.returnType = returnTypeMaker.make();
		method.arguments = new Argument[] {param};
		method.statements = statements.toArray(new Statement[0]);

		if (deprecate) {
			method.annotations = new Annotation[] {generateDeprecatedAnnotation(data.getSource())};
		}

		Annotation[] copyableAnnotations = findCopyableToBuilderSingularSetterAnnotations(data.getAnnotation().up());
		if (copyableAnnotations != null && copyableAnnotations.length > 0) {
			method.annotations = copyAnnotations(data.getSource(), copyableAnnotations, method.annotations);
		}

		data.setGeneratedByRecursive(method);
		injectMethod(builderType, method);
	}

	private void generateClearMethod(CheckerFrameworkVersion cfv, boolean deprecate, TypeReferenceMaker returnTypeMaker, StatementMaker returnStatementMaker, SingularData data, EclipseNode builderType, AccessLevel access) {
		char[] setFieldName = (new String(data.getSingularName()) + "$set").toCharArray();

		List<Statement> statements = new ArrayList<Statement>();

		FieldReference thisDotSetField = new FieldReference(setFieldName, 0L);
		thisDotSetField.receiver = new ThisReference(0, 0);

		statements.add(new Assignment(thisDotSetField, new FalseLiteral(0, 0), 0));

		Statement returnStatement = returnStatementMaker.make();
		if (returnStatement != null) statements.add(returnStatement);

		char[] singularName = data.getSingularName();
		String singularStr = new String(singularName);
		char[] methodName = ("clear" + Character.toUpperCase(singularStr.charAt(0)) + singularStr.substring(1)).toCharArray();

		MethodDeclaration method = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.modifiers = toEclipseModifier(access);
		method.selector = methodName;
		method.returnType = returnTypeMaker.make();
		method.statements = statements.toArray(new Statement[0]);

		if (deprecate) {
			method.annotations = new Annotation[] {generateDeprecatedAnnotation(data.getSource())};
		}

		data.setGeneratedByRecursive(method);
		injectMethod(builderType, method);
	}

	@Override public void appendBuildCode(SingularData data, EclipseNode builderType, List<Statement> statements, char[] targetVariableName, String builderVariable) {
		char[] setFieldName = (new String(data.getSingularName()) + "$set").toCharArray();
		char[] valueFieldName = (new String(data.getSingularName()) + "$value").toCharArray();

		TypeReference optionalType = new QualifiedTypeReference(JAVA_UTIL_OPTIONAL, NULL_POSS);
		optionalType = addTypeArgs(1, false, builderType, optionalType, data.getTypeArgs());

		LocalDeclaration varDefStat = new LocalDeclaration(data.getPluralName(), 0, 0);
		varDefStat.type = optionalType;
		statements.add(varDefStat);

		FieldReference setFieldRef = new FieldReference(setFieldName, 0L);
		setFieldRef.receiver = new SingleNameReference(builderVariable.toCharArray(), 0L);

		FieldReference valueFieldRef = new FieldReference(valueFieldName, 0L);
		valueFieldRef.receiver = new SingleNameReference(builderVariable.toCharArray(), 0L);

		MessageSend emptyCall = new MessageSend();
		emptyCall.receiver = new QualifiedNameReference(JAVA_UTIL_OPTIONAL, NULL_POSS, 0, 0);
		emptyCall.selector = "empty".toCharArray();

		MessageSend ofNullableCall = new MessageSend();
		ofNullableCall.receiver = new QualifiedNameReference(JAVA_UTIL_OPTIONAL, NULL_POSS, 0, 0);
		ofNullableCall.selector = "ofNullable".toCharArray();
		ofNullableCall.arguments = new Expression[] {valueFieldRef};

		Assignment assignValue = new Assignment(new SingleNameReference(data.getPluralName(), 0), ofNullableCall, 0);
		Assignment assignEmpty = new Assignment(new SingleNameReference(data.getPluralName(), 0), emptyCall, 0);

		IfStatement ifStat = new IfStatement(setFieldRef, assignValue, assignEmpty, 0, 0);
		statements.add(ifStat);
	}

	@Override public boolean shadowedDuringBuild() {
		return true;
	}

	@Override protected int getTypeArgumentsCount() {
		return 1;
	}

	@Override protected char[][] getEmptyMakerReceiver(String targetFqn) {
		return JAVA_UTIL_OPTIONAL;
	}

	@Override protected char[] getEmptyMakerSelector(String targetFqn) {
		return "empty".toCharArray();
	}
}
