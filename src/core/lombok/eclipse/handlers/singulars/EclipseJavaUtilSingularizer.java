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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

import lombok.ConfigurationKeys;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;

abstract class EclipseJavaUtilSingularizer extends EclipseSingularizer {
	protected static final char[][] JAVA_UTIL_ARRAYLIST = {
		{'j', 'a', 'v', 'a'}, {'u', 't', 'i', 'l'}, {'A', 'r', 'r', 'a', 'y', 'L', 'i', 's', 't'}
	};
	
	protected static final char[][] JAVA_UTIL_LIST = {
		{'j', 'a', 'v', 'a'}, {'u', 't', 'i', 'l'}, {'L', 'i', 's', 't'}
	};
	
	protected static final char[][] JAVA_UTIL_MAP = {
		{'j', 'a', 'v', 'a'}, {'u', 't', 'i', 'l'}, {'M', 'a', 'p'}
	};
	
	protected static final char[][] JAVA_UTIL_MAP_ENTRY = {
		{'j', 'a', 'v', 'a'}, {'u', 't', 'i', 'l'}, {'M', 'a', 'p'}, {'E', 'n', 't', 'r', 'y'}
	};
	
	protected static final char[][] JAVA_UTIL_COLLECTIONS = {
		{'j', 'a', 'v', 'a'}, {'u', 't', 'i', 'l'}, {'C', 'o', 'l', 'l', 'e', 'c', 't', 'i', 'o', 'n', 's'}
	};
	
	protected final EclipseSingularizer guavaListSetSingularizer = new EclipseGuavaSetListSingularizer();
	protected final EclipseSingularizer guavaMapSingularizer = new EclipseGuavaMapSingularizer();
	
	protected boolean useGuavaInstead(EclipseNode node) {
		return Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.SINGULAR_USE_GUAVA));
	}
	
	protected List<Statement> createJavaUtilSetMapInitialCapacitySwitchStatements(SingularData data, EclipseNode builderType, boolean mapMode, String emptyCollectionMethod, String singletonCollectionMethod, String targetType, String builderVariable) {
		List<Statement> switchContents = new ArrayList<Statement>();
		char[] keyName = mapMode ? (new String(data.getPluralName()) + "$key").toCharArray() : data.getPluralName();
		
		if (emptyCollectionMethod != null) { // case 0: (empty); break;
			switchContents.add(new CaseStatement(makeIntLiteral(new char[] {'0'}, null), 0, 0));
			
			/* pluralName = java.util.Collections.emptyCollectionMethod(); */ {
				MessageSend invoke = new MessageSend();
				invoke.receiver = new QualifiedNameReference(JAVA_UTIL_COLLECTIONS, NULL_POSS, 0, 0);
				invoke.selector = emptyCollectionMethod.toCharArray();
				switchContents.add(new Assignment(new SingleNameReference(data.getPluralName(), 0), invoke, 0));
			}
			
			switchContents.add(new BreakStatement(null, 0, 0));
		}
		
		if (singletonCollectionMethod != null) { // case 1: (singleton); break;
			switchContents.add(new CaseStatement(makeIntLiteral(new char[] {'1'}, null), 0, 0));
			/* !mapMode: pluralName = java.util.Collections.singletonCollectionMethod(this.pluralName.get(0));
			   mapMode: pluralName = java.util.Collections.singletonCollectionMethod(this.pluralName$key.get(0), this.pluralName$value.get(0)); */ {
				FieldReference thisDotKey = new FieldReference(keyName, 0L);
				thisDotKey.receiver = getBuilderReference(builderVariable);
				MessageSend thisDotKeyGet0 = new MessageSend();
				thisDotKeyGet0.receiver = thisDotKey;
				thisDotKeyGet0.selector = new char[] {'g', 'e', 't'};
				thisDotKeyGet0.arguments = new Expression[] {makeIntLiteral(new char[] {'0'}, null)};
				
				Expression[] args;
				if (mapMode) {
					char[] valueName = (new String(data.getPluralName()) + "$value").toCharArray();
					FieldReference thisDotValue = new FieldReference(valueName, 0L);
					thisDotValue.receiver = getBuilderReference(builderVariable);
					MessageSend thisDotValueGet0 = new MessageSend();
					thisDotValueGet0.receiver = thisDotValue;
					thisDotValueGet0.selector = new char[] {'g', 'e', 't'};
					thisDotValueGet0.arguments = new Expression[] {makeIntLiteral(new char[] {'0'}, null)};
					args = new Expression[] {thisDotKeyGet0, thisDotValueGet0};
				} else {
					args = new Expression[] {thisDotKeyGet0};
				}
				
				MessageSend invoke = new MessageSend();
				invoke.receiver = new QualifiedNameReference(JAVA_UTIL_COLLECTIONS, NULL_POSS, 0, 0);
				invoke.selector = singletonCollectionMethod.toCharArray();
				invoke.arguments = args;
				switchContents.add(new Assignment(new SingleNameReference(data.getPluralName(), 0), invoke, 0));
			}
			switchContents.add(new BreakStatement(null, 0, 0));
		}
		
		{ // default:
			switchContents.add(new CaseStatement(null, 0, 0));
			switchContents.addAll(createJavaUtilSimpleCreationAndFillStatements(data, builderType, mapMode, false, true, emptyCollectionMethod == null, targetType, builderVariable));
		}
		
		SwitchStatement switchStat = new SwitchStatement();
		switchStat.statements = switchContents.toArray(new Statement[switchContents.size()]);
		switchStat.expression = getSize(builderType, keyName, true, builderVariable);
		
		TypeReference localShadowerType = new QualifiedTypeReference(fromQualifiedName(data.getTargetFqn()), NULL_POSS);
		localShadowerType = addTypeArgs(mapMode ? 2 : 1, false, builderType, localShadowerType, data.getTypeArgs());
		LocalDeclaration varDefStat = new LocalDeclaration(data.getPluralName(), 0, 0);
		varDefStat.type = localShadowerType;
		return Arrays.asList(varDefStat, switchStat);
	}
	
	protected List<Statement> createJavaUtilSimpleCreationAndFillStatements(SingularData data, EclipseNode builderType, boolean mapMode, boolean defineVar, boolean addInitialCapacityArg, boolean nullGuard, String targetType, String builderVariable) {
		char[] varName = mapMode ? (new String(data.getPluralName()) + "$key").toCharArray() : data.getPluralName();
		
		Statement createStat; {
			// pluralName = new java.util.TargetType(initialCap);
			Expression[] constructorArgs = null;
			if (addInitialCapacityArg) {
				// this.varName.size() < MAX_POWER_OF_2 ? 1 + this.varName.size() + (this.varName.size() - 3) / 3 : Integer.MAX_VALUE;
				// lessThanCutOff = this.varName.size() < MAX_POWER_OF_2
				Expression lessThanCutoff = new BinaryExpression(getSize(builderType, varName, nullGuard, builderVariable), makeIntLiteral("0x40000000".toCharArray(), null), OperatorIds.LESS);
				FieldReference integerMaxValue = new FieldReference("MAX_VALUE".toCharArray(), 0L);
				integerMaxValue.receiver = new QualifiedNameReference(TypeConstants.JAVA_LANG_INTEGER, NULL_POSS, 0, 0);
				Expression sizeFormulaLeft = new BinaryExpression(makeIntLiteral(new char[] {'1'}, null), getSize(builderType, varName, nullGuard, builderVariable), OperatorIds.PLUS);
				Expression sizeFormulaRightLeft = new BinaryExpression(getSize(builderType, varName, nullGuard, builderVariable), makeIntLiteral(new char[] {'3'}, null), OperatorIds.MINUS);
				Expression sizeFormulaRight = new BinaryExpression(sizeFormulaRightLeft, makeIntLiteral(new char[] {'3'}, null), OperatorIds.DIVIDE);
				Expression sizeFormula = new BinaryExpression(sizeFormulaLeft, sizeFormulaRight, OperatorIds.PLUS);
				Expression cond = new ConditionalExpression(lessThanCutoff, sizeFormula, integerMaxValue);
				constructorArgs = new Expression[] {cond};
			}
			
			TypeReference targetTypeRef = new QualifiedTypeReference(new char[][] {TypeConstants.JAVA, TypeConstants.UTIL, targetType.toCharArray()}, NULL_POSS);
			targetTypeRef = addTypeArgs(mapMode ? 2 : 1, false, builderType, targetTypeRef, data.getTypeArgs());
			AllocationExpression constructorCall = new AllocationExpression();
			constructorCall.type = targetTypeRef;
			constructorCall.arguments = constructorArgs;
			
			if (defineVar) {
				TypeReference localShadowerType = new QualifiedTypeReference(fromQualifiedName(data.getTargetFqn()), NULL_POSS);
				localShadowerType = addTypeArgs(mapMode ? 2 : 1, false, builderType, localShadowerType, data.getTypeArgs());
				LocalDeclaration localShadowerDecl = new LocalDeclaration(data.getPluralName(), 0, 0);
				localShadowerDecl.type = localShadowerType;
				localShadowerDecl.initialization = constructorCall;
				createStat = localShadowerDecl;
			} else {
				createStat = new Assignment(new SingleNameReference(data.getPluralName(), 0L), constructorCall, 0);
			}
		}
		
		Statement fillStat; {
			if (mapMode) {
				// for (int $i = 0; $i < this.pluralname$key.size(); i++) pluralname.put(this.pluralname$key.get($i), this.pluralname$value.get($i));
				char[] iVar = new char[] {'$', 'i'};
				MessageSend pluralnameDotPut = new MessageSend();
				pluralnameDotPut.selector = new char[] {'p', 'u', 't'};
				pluralnameDotPut.receiver = new SingleNameReference(data.getPluralName(), 0L);
				FieldReference thisDotKey = new FieldReference(varName, 0L);
				thisDotKey.receiver = getBuilderReference(builderVariable);
				FieldReference thisDotValue = new FieldReference((new String(data.getPluralName()) + "$value").toCharArray(), 0L);
				thisDotValue.receiver = getBuilderReference(builderVariable);
				MessageSend keyArg = new MessageSend();
				keyArg.receiver = thisDotKey;
				keyArg.arguments = new Expression[] {new SingleNameReference(iVar, 0L)};
				keyArg.selector = new char[] {'g', 'e', 't'};
				MessageSend valueArg = new MessageSend();
				valueArg.receiver = thisDotValue;
				valueArg.arguments = new Expression[] {new SingleNameReference(iVar, 0L)};
				valueArg.selector = new char[] {'g', 'e', 't'};
				pluralnameDotPut.arguments = new Expression[] {keyArg, valueArg};
				
				LocalDeclaration forInit = new LocalDeclaration(iVar, 0, 0);
				forInit.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
				forInit.initialization = makeIntLiteral(new char[] {'0'}, null);
				Expression checkExpr = new BinaryExpression(new SingleNameReference(iVar, 0L), getSize(builderType, varName, nullGuard, builderVariable), OperatorIds.LESS);
				Expression incrementExpr = new PostfixExpression(new SingleNameReference(iVar, 0L), IntLiteral.One, OperatorIds.PLUS, 0);
				fillStat = new ForStatement(new Statement[] {forInit}, checkExpr, new Statement[] {incrementExpr}, pluralnameDotPut, true, 0, 0);
			} else {
				// pluralname.addAll(this.pluralname);
				MessageSend pluralnameDotAddAll = new MessageSend();
				pluralnameDotAddAll.selector = new char[] {'a', 'd', 'd', 'A', 'l', 'l'};
				pluralnameDotAddAll.receiver = new SingleNameReference(data.getPluralName(), 0L);
				FieldReference thisDotPluralname = new FieldReference(varName, 0L);
				thisDotPluralname.receiver = getBuilderReference(builderVariable);
				pluralnameDotAddAll.arguments = new Expression[] {thisDotPluralname};
				fillStat = pluralnameDotAddAll;
			}
			
			if (nullGuard) {
				FieldReference thisDotField = new FieldReference(varName, 0L);
				thisDotField.receiver = getBuilderReference(builderVariable);
				Expression cond = new EqualExpression(thisDotField, new NullLiteral(0, 0), OperatorIds.NOT_EQUAL);
				fillStat = new IfStatement(cond, fillStat, 0, 0);
			}
		}
		
		Statement unmodifiableStat; {
			// pluralname = Collections.unmodifiableInterfaceType(pluralname);
			Expression arg = new SingleNameReference(data.getPluralName(), 0L);
			MessageSend invoke = new MessageSend();
			invoke.arguments = new Expression[] {arg};
			invoke.selector = ("unmodifiable" + data.getTargetSimpleType()).toCharArray();
			invoke.receiver = new QualifiedNameReference(JAVA_UTIL_COLLECTIONS, NULL_POSS, 0, 0);
			unmodifiableStat = new Assignment(new SingleNameReference(data.getPluralName(), 0L), invoke, 0);
		}
		
		return Arrays.asList(createStat, fillStat, unmodifiableStat);
	}
	
	protected Statement createConstructBuilderVarIfNeeded(SingularData data, EclipseNode builderType, boolean mapMode) {
		char[] v1Name, v2Name;
		if (mapMode) {
			String n = new String(data.getPluralName());
			v1Name = (n + "$key").toCharArray();
			v2Name = (n + "$value").toCharArray();
		} else {
			v1Name = data.getPluralName();
			v2Name = null;
		}
		
		FieldReference thisDotField = new FieldReference(v1Name, 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		Expression cond = new EqualExpression(thisDotField, new NullLiteral(0, 0), OperatorIds.EQUAL_EQUAL);
		
		thisDotField = new FieldReference(v1Name, 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		TypeReference v1Type = new QualifiedTypeReference(JAVA_UTIL_ARRAYLIST, NULL_POSS);
		v1Type = addTypeArgs(1, false, builderType, v1Type, data.getTypeArgs());
		AllocationExpression constructArrayList = new AllocationExpression();
		constructArrayList.type = v1Type;
		Assignment initV1 = new Assignment(thisDotField, constructArrayList, 0);
		Statement thenPart;
		if (mapMode) {
			thisDotField = new FieldReference(v2Name, 0L);
			thisDotField.receiver = new ThisReference(0, 0);
			TypeReference v2Type = new QualifiedTypeReference(JAVA_UTIL_ARRAYLIST, NULL_POSS);
			List<TypeReference> tArgs = data.getTypeArgs();
			if (tArgs != null && tArgs.size() > 1) tArgs = Collections.singletonList(tArgs.get(1));
			else tArgs = Collections.emptyList();
			v2Type = addTypeArgs(1, false, builderType, v2Type, tArgs);
			constructArrayList = new AllocationExpression();
			constructArrayList.type = v2Type;
			Assignment initV2 = new Assignment(thisDotField, constructArrayList, 0);
			Block b = new Block(0);
			b.statements = new Statement[] {initV1, initV2};
			thenPart = b;
		} else {
			thenPart = initV1;
		}
		
		return new IfStatement(cond, thenPart, 0, 0);
	}
}
