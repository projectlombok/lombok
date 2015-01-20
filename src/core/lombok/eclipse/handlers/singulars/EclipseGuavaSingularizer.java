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
package lombok.eclipse.handlers.singulars;

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.core.GuavaTypeMap;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;

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
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

abstract class EclipseGuavaSingularizer extends EclipseSingularizer {
	protected static final char[][] JAVA_UTIL_MAP = {
		{'j', 'a', 'v', 'a'}, {'u', 't', 'i', 'l'}, {'M', 'a', 'p'}
	};
	
	protected String getSimpleTargetTypeName(SingularData data) {
		return GuavaTypeMap.getGuavaTypeName(data.getTargetFqn());
	}
	
	protected char[] getBuilderMethodName(SingularData data) {
		String simpleTypeName = getSimpleTargetTypeName(data);
		if ("ImmutableSortedSet".equals(simpleTypeName) || "ImmutableSortedMap".equals(simpleTypeName)) return "naturalOrder".toCharArray();
		return "builder".toCharArray();
	}
	
	protected abstract boolean isMap();
	
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
		char[][] tokenizedName = makeGuavaTypeName(getSimpleTargetTypeName(data), true);
		TypeReference type = new QualifiedTypeReference(tokenizedName, NULL_POSS);
		type = addTypeArgs(isMap() ? 2 : 1, false, builderType, type, data.getTypeArgs());
		
		FieldDeclaration buildField = new FieldDeclaration(data.getPluralName(), 0, -1);
		buildField.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		buildField.modifiers = ClassFileConstants.AccPrivate;
		buildField.declarationSourceEnd = -1;
		buildField.type = type;
		data.setGeneratedByRecursive(buildField);
		return Collections.singletonList(injectField(builderType, buildField));
	}
	
	@Override public void generateMethods(SingularData data, EclipseNode builderType, boolean fluent, boolean chain) {
		TypeReference returnType = chain ? cloneSelfType(builderType) : TypeReference.baseTypeReference(TypeIds.T_void, 0);
		Statement returnStatement = chain ? new ReturnStatement(new ThisReference(0, 0), 0, 0) : null;
		generateSingularMethod(returnType, returnStatement, data, builderType, fluent);
		
		returnType = chain ? cloneSelfType(builderType) : TypeReference.baseTypeReference(TypeIds.T_void, 0);
		returnStatement = chain ? new ReturnStatement(new ThisReference(0, 0), 0, 0) : null;
		generatePluralMethod(returnType, returnStatement, data, builderType, fluent);
	}
	
	void generateSingularMethod(TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, boolean fluent) {
		boolean mapMode = isMap();
		char[] keyName = !mapMode ? data.getSingularName() : (new String(data.getSingularName()) + "$key").toCharArray();
		char[] valueName = !mapMode ? null : (new String(data.getSingularName()) + "$value").toCharArray();
		
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = ClassFileConstants.AccPublic;
		
		List<Statement> statements = new ArrayList<Statement>();
		statements.add(createConstructBuilderVarIfNeeded(data, builderType));
		
		FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		MessageSend thisDotFieldDotAdd = new MessageSend();
		if (mapMode) {
			thisDotFieldDotAdd.arguments = new Expression[] {
					new SingleNameReference(keyName, 0L),
					new SingleNameReference(valueName, 0L)};
		} else {
			thisDotFieldDotAdd.arguments = new Expression[] {new SingleNameReference(keyName, 0L)};
		}
		thisDotFieldDotAdd.receiver = thisDotField;
		thisDotFieldDotAdd.selector = (mapMode ? "put" : "add").toCharArray();
		statements.add(thisDotFieldDotAdd);
		if (returnStatement != null) statements.add(returnStatement);
		md.statements = statements.toArray(new Statement[statements.size()]);
		
		if (mapMode) {
			TypeReference keyType = cloneParamType(0, data.getTypeArgs(), builderType);
			Argument keyParam = new Argument(keyName, 0, keyType, 0);
			TypeReference valueType = cloneParamType(1, data.getTypeArgs(), builderType);
			Argument valueParam = new Argument(valueName, 0, valueType, 0);
			md.arguments = new Argument[] {keyParam, valueParam};
		} else {
			TypeReference paramType = cloneParamType(0, data.getTypeArgs(), builderType);
			Argument param = new Argument(keyName, 0, paramType, 0);
			md.arguments = new Argument[] {param};
		}
		md.returnType = returnType;
		md.selector = fluent ? data.getSingularName() : HandlerUtil.buildAccessorName(mapMode ? "put" : "add", new String(data.getSingularName())).toCharArray();
		
		data.setGeneratedByRecursive(md);
		injectMethod(builderType, md);
	}
	
	void generatePluralMethod(TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, boolean fluent) {
		boolean mapMode = isMap();
		
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
		thisDotFieldDotAddAll.selector = (mapMode ? "putAll" : "addAll").toCharArray();
		statements.add(thisDotFieldDotAddAll);
		if (returnStatement != null) statements.add(returnStatement);
		
		md.statements = statements.toArray(new Statement[statements.size()]);
		
		TypeReference paramType;
		if (mapMode) {
			paramType = new QualifiedTypeReference(JAVA_UTIL_MAP, NULL_POSS);
			paramType = addTypeArgs(2, true, builderType, paramType, data.getTypeArgs());
		} else {
			paramType = new QualifiedTypeReference(TypeConstants.JAVA_LANG_ITERABLE, NULL_POSS);
			paramType = addTypeArgs(1, true, builderType, paramType, data.getTypeArgs());
		}
		Argument param = new Argument(data.getPluralName(), 0, paramType, 0);
		md.arguments = new Argument[] {param};
		md.returnType = returnType;
		md.selector = fluent ? data.getPluralName() : HandlerUtil.buildAccessorName(mapMode ? "putAll" : "addAll", new String(data.getPluralName())).toCharArray();
		
		data.setGeneratedByRecursive(md);
		injectMethod(builderType, md);
	}
	
	@Override public void appendBuildCode(SingularData data, EclipseNode builderType, List<Statement> statements, char[] targetVariableName) {
		boolean mapMode = isMap();
		TypeReference varType = new QualifiedTypeReference(fromQualifiedName(data.getTargetFqn()), NULL_POSS);
		varType = addTypeArgs(mapMode ? 2 : 1, false, builderType, varType, data.getTypeArgs());
		
		MessageSend emptyInvoke; {
			//ImmutableX.of()
			emptyInvoke = new MessageSend();
			emptyInvoke.selector = new char[] {'o', 'f'};
			emptyInvoke.receiver = new QualifiedNameReference(makeGuavaTypeName(getSimpleTargetTypeName(data), false), NULL_POSS, 0, 0);
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
}
