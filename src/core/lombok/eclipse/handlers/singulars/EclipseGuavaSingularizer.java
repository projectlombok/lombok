/*
 * Copyright (C) 2015-2017 The Project Lombok Authors.
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

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.core.GuavaTypeMap;
import lombok.core.LombokImmutableList;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

abstract class EclipseGuavaSingularizer extends EclipseSingularizer {
	protected String getSimpleTargetTypeName(SingularData data) {
		return GuavaTypeMap.getGuavaTypeName(data.getTargetFqn());
	}
	
	protected char[] getBuilderMethodName(SingularData data) {
		String simpleTypeName = getSimpleTargetTypeName(data);
		if ("ImmutableSortedSet".equals(simpleTypeName) || "ImmutableSortedMap".equals(simpleTypeName)) return "naturalOrder".toCharArray();
		return "builder".toCharArray();
	}
	
	protected char[][] makeGuavaTypeName(String simpleName, boolean addBuilder) {
		char[][] tokenizedName = new char[addBuilder ? 6 : 5][];
		tokenizedName[0] = new char[] {'c', 'o', 'm'};
		tokenizedName[1] = new char[] {'g', 'o', 'o', 'g', 'l', 'e'};
		tokenizedName[2] = new char[] {'c', 'o', 'm', 'm', 'o', 'n'};
		tokenizedName[3] = new char[] {'c', 'o', 'l', 'l', 'e', 'c', 't'};
		tokenizedName[4] = simpleName.toCharArray();
		if (addBuilder) tokenizedName[5] = new char[] { 'B', 'u', 'i', 'l', 'd', 'e', 'r'};
		return tokenizedName;
	}
	
	@Override public List<EclipseNode> generateFields(SingularData data, EclipseNode builderType) {
		String simpleTypeName = getSimpleTargetTypeName(data);
		char[][] tokenizedName = makeGuavaTypeName(simpleTypeName, true);
		TypeReference type = new QualifiedTypeReference(tokenizedName, NULL_POSS);
		type = addTypeArgs(getTypeArgumentsCount(), false, builderType, type, data.getTypeArgs());
		
		FieldDeclaration buildField = new FieldDeclaration(data.getPluralName(), 0, -1);
		buildField.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		buildField.modifiers = ClassFileConstants.AccPrivate;
		buildField.declarationSourceEnd = -1;
		buildField.type = type;
		data.setGeneratedByRecursive(buildField);
		return Collections.singletonList(injectFieldAndMarkGenerated(builderType, buildField));
	}
	
	@Override public void generateMethods(SingularData data, boolean deprecate, EclipseNode builderType, boolean fluent, boolean chain) {
		TypeReference returnType = chain ? cloneSelfType(builderType) : TypeReference.baseTypeReference(TypeIds.T_void, 0);
		Statement returnStatement = chain ? new ReturnStatement(new ThisReference(0, 0), 0, 0) : null;
		generateSingularMethod(deprecate, returnType, returnStatement, data, builderType, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : TypeReference.baseTypeReference(TypeIds.T_void, 0);
		returnStatement = chain ? new ReturnStatement(new ThisReference(0, 0), 0, 0) : null;
		generatePluralMethod(deprecate, returnType, returnStatement, data, builderType, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : TypeReference.baseTypeReference(TypeIds.T_void, 0);
		returnStatement = chain ? new ReturnStatement(new ThisReference(0, 0), 0, 0) : null;
		generateClearMethod(deprecate, returnType, returnStatement, data,  builderType);
	}
	
	void generateClearMethod(boolean deprecate, TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType) {
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = ClassFileConstants.AccPublic;
		
		FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		Assignment a = new Assignment(thisDotField, new NullLiteral(0, 0), 0);
		md.selector = HandlerUtil.buildAccessorName("clear", new String(data.getPluralName())).toCharArray();
		md.statements = returnStatement != null ? new Statement[] {a, returnStatement} : new Statement[] {a};
		md.returnType = returnType;
		md.annotations = deprecate ? new Annotation[] { generateDeprecatedAnnotation(data.getSource()) } : null;
		
		injectMethod(builderType, md);
	}
	
	void generateSingularMethod(boolean deprecate, TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, boolean fluent) {
		LombokImmutableList<String> suffixes = getArgumentSuffixes();
		char[][] names = new char[suffixes.size()][];
		for (int i = 0; i < suffixes.size(); i++) {
			String s = suffixes.get(i);
			char[] n = data.getSingularName();
			names[i] = s.isEmpty() ? n : s.toCharArray();
		}
		
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = ClassFileConstants.AccPublic;
		
		List<Statement> statements = new ArrayList<Statement>();
		statements.add(createConstructBuilderVarIfNeeded(data, builderType));
		
		FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		MessageSend thisDotFieldDotAdd = new MessageSend();
		thisDotFieldDotAdd.arguments = new Expression[suffixes.size()];
		for (int i = 0; i < suffixes.size(); i++) {
			thisDotFieldDotAdd.arguments[i] = new SingleNameReference(names[i], 0L);
		}
		thisDotFieldDotAdd.receiver = thisDotField;
		thisDotFieldDotAdd.selector = getAddMethodName().toCharArray();
		statements.add(thisDotFieldDotAdd);
		if (returnStatement != null) statements.add(returnStatement);
		md.statements = statements.toArray(new Statement[statements.size()]);
		md.arguments = new Argument[suffixes.size()];
		for (int i = 0; i < suffixes.size(); i++) {
			TypeReference tr = cloneParamType(i, data.getTypeArgs(), builderType);
			md.arguments[i] = new Argument(names[i], 0, tr, 0);
		}
		md.returnType = returnType;
		md.selector = fluent ? data.getSingularName() : HandlerUtil.buildAccessorName(getAddMethodName(), new String(data.getSingularName())).toCharArray();
		md.annotations = deprecate ? new Annotation[] { generateDeprecatedAnnotation(data.getSource()) } : null;
		
		data.setGeneratedByRecursive(md);
		injectMethod(builderType, md);
	}
	
	void generatePluralMethod(boolean deprecate, TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, boolean fluent) {
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = ClassFileConstants.AccPublic;
		
		List<Statement> statements = new ArrayList<Statement>();
		statements.add(createConstructBuilderVarIfNeeded(data, builderType));
		
		FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		MessageSend thisDotFieldDotAddAll = new MessageSend();
		thisDotFieldDotAddAll.arguments = new Expression[] {new SingleNameReference(data.getPluralName(), 0L)};
		thisDotFieldDotAddAll.receiver = thisDotField;
		thisDotFieldDotAddAll.selector = (getAddMethodName() + "All").toCharArray();
		statements.add(thisDotFieldDotAddAll);
		if (returnStatement != null) statements.add(returnStatement);
		
		md.statements = statements.toArray(new Statement[statements.size()]);
		
		TypeReference paramType;
		paramType = new QualifiedTypeReference(fromQualifiedName(getAddAllTypeName()), NULL_POSS);
		paramType = addTypeArgs(getTypeArgumentsCount(), true, builderType, paramType, data.getTypeArgs());
		Argument param = new Argument(data.getPluralName(), 0, paramType, 0);
		md.arguments = new Argument[] {param};
		md.returnType = returnType;
		md.selector = fluent ? data.getPluralName() : HandlerUtil.buildAccessorName(getAddMethodName() + "All", new String(data.getPluralName())).toCharArray();
		md.annotations = deprecate ? new Annotation[] { generateDeprecatedAnnotation(data.getSource()) } : null;
		
		data.setGeneratedByRecursive(md);
		injectMethod(builderType, md);
	}
	
	@Override public void appendBuildCode(SingularData data, EclipseNode builderType, List<Statement> statements, char[] targetVariableName) {
		TypeReference varType = new QualifiedTypeReference(fromQualifiedName(data.getTargetFqn()), NULL_POSS);
		String simpleTypeName = getSimpleTargetTypeName(data);
		int agrumentsCount = getTypeArgumentsCount();
		varType = addTypeArgs(agrumentsCount, false, builderType, varType, data.getTypeArgs());
		
		MessageSend emptyInvoke; {
			//ImmutableX.of()
			emptyInvoke = new MessageSend();
			emptyInvoke.selector = new char[] {'o', 'f'};
			emptyInvoke.receiver = new QualifiedNameReference(makeGuavaTypeName(simpleTypeName, false), NULL_POSS, 0, 0);
			emptyInvoke.typeArguments = createTypeArgs(agrumentsCount, false, builderType, data.getTypeArgs());
		}
		
		MessageSend invokeBuild; {
			//this.pluralName.build();
			invokeBuild = new MessageSend();
			invokeBuild.selector = new char[] {'b', 'u', 'i', 'l', 'd'};
			FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
			thisDotField.receiver = new ThisReference(0, 0);
			invokeBuild.receiver = thisDotField;
		}
		
		Expression isNull; {
			//this.pluralName == null
			FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
			thisDotField.receiver = new ThisReference(0, 0);
			isNull = new EqualExpression(thisDotField, new NullLiteral(0, 0), OperatorIds.EQUAL_EQUAL);
		}
		
		Expression init = new ConditionalExpression(isNull, emptyInvoke, invokeBuild);
		LocalDeclaration varDefStat = new LocalDeclaration(data.getPluralName(), 0, 0);
		varDefStat.type = varType;
		varDefStat.initialization = init;
		statements.add(varDefStat);
	}
	
	protected Statement createConstructBuilderVarIfNeeded(SingularData data, EclipseNode builderType) {
		FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		FieldReference thisDotField2 = new FieldReference(data.getPluralName(), 0L);
		thisDotField2.receiver = new ThisReference(0, 0);
		Expression cond = new EqualExpression(thisDotField, new NullLiteral(0, 0), OperatorIds.EQUAL_EQUAL);
		
		MessageSend createBuilderInvoke = new MessageSend();
		char[][] tokenizedName = makeGuavaTypeName(getSimpleTargetTypeName(data), false);
		createBuilderInvoke.receiver = new QualifiedNameReference(tokenizedName, NULL_POSS, 0, 0);
		createBuilderInvoke.selector = getBuilderMethodName(data);
		return new IfStatement(cond, new Assignment(thisDotField2, createBuilderInvoke, 0), 0, 0);
	}
	
	protected abstract LombokImmutableList<String> getArgumentSuffixes();
	protected abstract String getAddMethodName();
	protected abstract String getAddAllTypeName();
	
	protected int getTypeArgumentsCount() {
		return getArgumentSuffixes().size();
	}
}
