/*
 * Copyright (C) 2015-2019 The Project Lombok Authors.
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
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.HandleNonNull;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;
import lombok.eclipse.handlers.EclipseSingularsRecipes.StatementMaker;
import lombok.eclipse.handlers.EclipseSingularsRecipes.TypeReferenceMaker;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

abstract class EclipseJavaUtilListSetSingularizer extends EclipseJavaUtilSingularizer {
	@Override public List<char[]> listFieldsToBeGenerated(SingularData data, EclipseNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaListSetSingularizer.listFieldsToBeGenerated(data, builderType);
		}
		
		return super.listFieldsToBeGenerated(data, builderType);
	}
	
	@Override public List<char[]> listMethodsToBeGenerated(SingularData data, EclipseNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaListSetSingularizer.listMethodsToBeGenerated(data, builderType);
		}
		
		return super.listMethodsToBeGenerated(data, builderType);
	}
	
	@Override public List<EclipseNode> generateFields(SingularData data, EclipseNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaListSetSingularizer.generateFields(data, builderType);
		}
		
		TypeReference type = new QualifiedTypeReference(JAVA_UTIL_ARRAYLIST, NULL_POSS);
		type = addTypeArgs(1, false, builderType, type, data.getTypeArgs());
		
		FieldDeclaration buildField = new FieldDeclaration(data.getPluralName(), 0, -1);
		buildField.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		buildField.modifiers = ClassFileConstants.AccPrivate;
		buildField.declarationSourceEnd = -1;
		buildField.type = type;
		
		data.setGeneratedByRecursive(buildField);
		return Collections.singletonList(injectFieldAndMarkGenerated(builderType, buildField));
	}
	
	@Override public void generateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, EclipseNode builderType, boolean fluent, TypeReferenceMaker returnTypeMaker, StatementMaker returnStatementMaker, AccessLevel access) {
		if (useGuavaInstead(builderType)) {
			guavaListSetSingularizer.generateMethods(cfv, data, deprecate, builderType, fluent, returnTypeMaker, returnStatementMaker, access);
			return;
		}
		
		generateSingularMethod(cfv, deprecate, returnTypeMaker.make(), returnStatementMaker.make(), data, builderType, fluent, access);
		generatePluralMethod(cfv, deprecate, returnTypeMaker.make(), returnStatementMaker.make(), data, builderType, fluent, access);
		generateClearMethod(cfv, deprecate, returnTypeMaker.make(), returnStatementMaker.make(), data, builderType, access);
	}
	
	private void generateClearMethod(CheckerFrameworkVersion cfv, boolean deprecate, TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, AccessLevel access) {
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = toEclipseModifier(access);
		
		FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		FieldReference thisDotField2 = new FieldReference(data.getPluralName(), 0L);
		thisDotField2.receiver = new ThisReference(0, 0);
		md.selector = HandlerUtil.buildAccessorName("clear", new String(data.getPluralName())).toCharArray();
		MessageSend clearMsg = new MessageSend();
		clearMsg.receiver = thisDotField2;
		clearMsg.selector = "clear".toCharArray();
		Statement clearStatement = new IfStatement(new EqualExpression(thisDotField, new NullLiteral(0, 0), OperatorIds.NOT_EQUAL), clearMsg, 0, 0);
		md.statements = returnStatement != null ? new Statement[] {clearStatement, returnStatement} : new Statement[] {clearStatement};
		md.returnType = returnType;
		md.annotations = generateSelfReturnAnnotations(deprecate, cfv, data.getSource());
		
		data.setGeneratedByRecursive(md);
		injectMethod(builderType, md);
	}
	
	void generateSingularMethod(CheckerFrameworkVersion cfv, boolean deprecate, TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, boolean fluent, AccessLevel access) {
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = toEclipseModifier(access);
		
		List<Statement> statements = new ArrayList<Statement>();
		statements.add(createConstructBuilderVarIfNeeded(data, builderType, false));
		
		FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		MessageSend thisDotFieldDotAdd = new MessageSend();
		thisDotFieldDotAdd.arguments = new Expression[] {new SingleNameReference(data.getSingularName(), 0L)};
		thisDotFieldDotAdd.receiver = thisDotField;
		thisDotFieldDotAdd.selector = "add".toCharArray();
		statements.add(thisDotFieldDotAdd);
		if (returnStatement != null) statements.add(returnStatement);
		
		md.statements = statements.toArray(new Statement[0]);
		TypeReference paramType = cloneParamType(0, data.getTypeArgs(), builderType);
		Annotation[] typeUseAnns = getTypeUseAnnotations(paramType);
		removeTypeUseAnnotations(paramType);
		Argument param = new Argument(data.getSingularName(), 0, paramType, ClassFileConstants.AccFinal);
		param.annotations = typeUseAnns;
		md.arguments = new Argument[] {param};
		md.returnType = returnType;
		md.selector = fluent ? data.getSingularName() : HandlerUtil.buildAccessorName("add", new String(data.getSingularName())).toCharArray();
		md.annotations = generateSelfReturnAnnotations(deprecate, cfv, data.getSource());
		
		data.setGeneratedByRecursive(md);
		HandleNonNull.INSTANCE.fix(injectMethod(builderType, md));
	}
	
	void generatePluralMethod(CheckerFrameworkVersion cfv, boolean deprecate, TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, boolean fluent, AccessLevel access) {
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = toEclipseModifier(access);
		
		List<Statement> statements = new ArrayList<Statement>();
		statements.add(createConstructBuilderVarIfNeeded(data, builderType, false));
		
		FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		MessageSend thisDotFieldDotAddAll = new MessageSend();
		thisDotFieldDotAddAll.arguments = new Expression[] {new SingleNameReference(data.getPluralName(), 0L)};
		thisDotFieldDotAddAll.receiver = thisDotField;
		thisDotFieldDotAddAll.selector = "addAll".toCharArray();
		statements.add(thisDotFieldDotAddAll);
		if (returnStatement != null) statements.add(returnStatement);
		
		md.statements = statements.toArray(new Statement[0]);
		
		TypeReference paramType = new QualifiedTypeReference(TypeConstants.JAVA_UTIL_COLLECTION, NULL_POSS);
		paramType = addTypeArgs(1, true, builderType, paramType, data.getTypeArgs());
		Argument param = new Argument(data.getPluralName(), 0, paramType, ClassFileConstants.AccFinal);
		md.arguments = new Argument[] {param};
		md.returnType = returnType;
		md.selector = fluent ? data.getPluralName() : HandlerUtil.buildAccessorName("addAll", new String(data.getPluralName())).toCharArray();
		md.annotations = generateSelfReturnAnnotations(deprecate, cfv, data.getSource());
		
		data.setGeneratedByRecursive(md);
		injectMethod(builderType, md);
	}
}
