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

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
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

import lombok.AccessLevel;
import lombok.core.LombokImmutableList;
import lombok.core.configuration.CheckerFrameworkVersion;
import lombok.core.handlers.HandlerUtil;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;
import lombok.eclipse.handlers.EclipseSingularsRecipes.StatementMaker;
import lombok.eclipse.handlers.EclipseSingularsRecipes.TypeReferenceMaker;
import lombok.eclipse.handlers.HandleNonNull;
import lombok.spi.Provides;

@Provides(EclipseSingularizer.class)
public class EclipseJavaUtilMapSingularizer extends EclipseJavaUtilSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.Map", "java.util.SortedMap", "java.util.NavigableMap", "java.util.SequencedMap");
	}
	
	private static final char[] EMPTY_SORTED_MAP = {'e', 'm', 'p', 't', 'y', 'S', 'o', 'r', 't', 'e', 'd', 'M', 'a', 'p'};
	private static final char[] EMPTY_NAVIGABLE_MAP = {'e', 'm', 'p', 't', 'y', 'N', 'a', 'v', 'i', 'g', 'a', 'b', 'l', 'e', 'M', 'a', 'p'};
	private static final char[] EMPTY_MAP = {'e', 'm', 'p', 't', 'y', 'M', 'a', 'p'};
	
	@Override protected char[][] getEmptyMakerReceiver(String targetFqn) {
		return JAVA_UTIL_COLLECTIONS;
	}
	
	@Override protected char[] getEmptyMakerSelector(String targetFqn) {
		if (targetFqn.endsWith("SortedMap") || targetFqn.endsWith("SequencedMap")) return EMPTY_SORTED_MAP;
		if (targetFqn.endsWith("NavigableMap")) return EMPTY_NAVIGABLE_MAP;
		return EMPTY_MAP;
	}
	
	@Override public List<char[]> listFieldsToBeGenerated(SingularData data, EclipseNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaMapSingularizer.listFieldsToBeGenerated(data, builderType);
		}
		
		char[] p = data.getPluralName();
		int len = p.length;
		char[] k = new char[len + 4];
		char[] v = new char[len + 6];
		System.arraycopy(p, 0, k, 0, len);
		System.arraycopy(p, 0, v, 0, len);
		k[len] = '$';
		k[len + 1] = 'k';
		k[len + 2] = 'e';
		k[len + 3] = 'y';
		v[len] = '$';
		v[len + 1] = 'v';
		v[len + 2] = 'a';
		v[len + 3] = 'l';
		v[len + 4] = 'u';
		v[len + 5] = 'e';
		return Arrays.asList(k, v);
	}
	
	@Override public List<char[]> listMethodsToBeGenerated(SingularData data, EclipseNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaMapSingularizer.listFieldsToBeGenerated(data, builderType);
		} else {
			return super.listMethodsToBeGenerated(data, builderType);
		}
	}
	
	@Override public List<EclipseNode> generateFields(SingularData data, EclipseNode builderType) {
		if (useGuavaInstead(builderType)) {
			return guavaMapSingularizer.generateFields(data, builderType);
		}
		
		char[] keyName = (new String(data.getPluralName()) + "$key").toCharArray();
		char[] valueName = (new String(data.getPluralName()) + "$value").toCharArray();
		FieldDeclaration buildKeyField; {
			TypeReference type = new QualifiedTypeReference(JAVA_UTIL_ARRAYLIST, NULL_POSS);
			type = addTypeArgs(1, false, builderType, type, data.getTypeArgs());
			buildKeyField = new FieldDeclaration(keyName, 0, -1);
			buildKeyField.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
			buildKeyField.modifiers = ClassFileConstants.AccPrivate;
			buildKeyField.declarationSourceEnd = -1;
			buildKeyField.type = type;
		}
		FieldDeclaration buildValueField; {
			TypeReference type = new QualifiedTypeReference(JAVA_UTIL_ARRAYLIST, NULL_POSS);
			List<TypeReference> tArgs = data.getTypeArgs();
			if (tArgs != null && tArgs.size() > 1) tArgs = Collections.singletonList(tArgs.get(1));
			else tArgs = Collections.emptyList();
			type = addTypeArgs(1, false, builderType, type, tArgs);
			buildValueField = new FieldDeclaration(valueName, 0, -1);
			buildValueField.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
			buildValueField.modifiers = ClassFileConstants.AccPrivate;
			buildValueField.declarationSourceEnd = -1;
			buildValueField.type = type;
		}
		
		data.setGeneratedByRecursive(buildKeyField);
		data.setGeneratedByRecursive(buildValueField);
		EclipseNode keyFieldNode = injectFieldAndMarkGenerated(builderType, buildKeyField);
		EclipseNode valueFieldNode = injectFieldAndMarkGenerated(builderType, buildValueField);
		return Arrays.asList(keyFieldNode, valueFieldNode);
	}
	
	@Override public void generateMethods(CheckerFrameworkVersion cfv, SingularData data, boolean deprecate, EclipseNode builderType, boolean fluent, TypeReferenceMaker returnTypeMaker, StatementMaker returnStatementMaker, AccessLevel access) {
		if (useGuavaInstead(builderType)) {
			guavaMapSingularizer.generateMethods(cfv, data, deprecate, builderType, fluent, returnTypeMaker, returnStatementMaker, access);
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
		
		String pN = new String(data.getPluralName());
		char[] keyFieldName = (pN + "$key").toCharArray();
		char[] valueFieldName = (pN + "$value").toCharArray();
		
		FieldReference thisDotField = new FieldReference(keyFieldName, 0L);
		thisDotField.receiver = new ThisReference(0, 0);
		FieldReference thisDotField2 = new FieldReference(keyFieldName, 0L);
		thisDotField2.receiver = new ThisReference(0, 0);
		FieldReference thisDotField3 = new FieldReference(valueFieldName, 0L);
		thisDotField3.receiver = new ThisReference(0, 0);
		md.selector = HandlerUtil.buildAccessorName(builderType, "clear", new String(data.getPluralName())).toCharArray();
		MessageSend clearMsg1 = new MessageSend();
		clearMsg1.receiver = thisDotField2;
		clearMsg1.selector = "clear".toCharArray();
		MessageSend clearMsg2 = new MessageSend();
		clearMsg2.receiver = thisDotField3;
		clearMsg2.selector = "clear".toCharArray();
		Block clearMsgs = new Block(2);
		clearMsgs.statements = new Statement[] {clearMsg1, clearMsg2};
		Statement clearStatement = new IfStatement(new EqualExpression(thisDotField, new NullLiteral(0, 0), OperatorIds.NOT_EQUAL), clearMsgs, 0, 0);
		md.statements = returnStatement != null ? new Statement[] {clearStatement, returnStatement} : new Statement[] {clearStatement};
		md.returnType = returnType;
		addCheckerFrameworkReturnsReceiver(md.returnType, data.getSource(), cfv);
		md.annotations = generateSelfReturnAnnotations(deprecate, data.getSource());
		
		if (returnStatement != null) createRelevantNonNullAnnotation(builderType, md);
		data.setGeneratedByRecursive(md);
		injectMethod(builderType, md);
	}
	
	private void generateSingularMethod(CheckerFrameworkVersion cfv, boolean deprecate, TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, boolean fluent, AccessLevel access) {
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = toEclipseModifier(access);
		
		List<Statement> statements = new ArrayList<Statement>();
		statements.add(createConstructBuilderVarIfNeeded(data, builderType, true));
		
		String sN = new String(data.getSingularName());
		String pN = new String(data.getPluralName());
		char[] keyParamName = (sN + "Key").toCharArray();
		char[] valueParamName = (sN + "Value").toCharArray();
		char[] keyFieldName = (pN + "$key").toCharArray();
		char[] valueFieldName = (pN + "$value").toCharArray();
		
		/* this.pluralname$key.add(singularnameKey); */ {
			FieldReference thisDotKeyField = new FieldReference(keyFieldName, 0L);
			thisDotKeyField.receiver = new ThisReference(0, 0);
			MessageSend thisDotKeyFieldDotAdd = new MessageSend();
			thisDotKeyFieldDotAdd.arguments = new Expression[] {new SingleNameReference(keyParamName, 0L)};
			thisDotKeyFieldDotAdd.receiver = thisDotKeyField;
			thisDotKeyFieldDotAdd.selector = "add".toCharArray();
			statements.add(thisDotKeyFieldDotAdd);
		}
		
		/* this.pluralname$value.add(singularnameValue); */ {
			FieldReference thisDotValueField = new FieldReference(valueFieldName, 0L);
			thisDotValueField.receiver = new ThisReference(0, 0);
			MessageSend thisDotValueFieldDotAdd = new MessageSend();
			thisDotValueFieldDotAdd.arguments = new Expression[] {new SingleNameReference(valueParamName, 0L)};
			thisDotValueFieldDotAdd.receiver = thisDotValueField;
			thisDotValueFieldDotAdd.selector = "add".toCharArray();
			statements.add(thisDotValueFieldDotAdd);
		}
		if (returnStatement != null) statements.add(returnStatement);
		
		md.statements = statements.toArray(new Statement[0]);
		TypeReference keyParamType = cloneParamType(0, data.getTypeArgs(), builderType);
		TypeReference valueParamType = cloneParamType(1, data.getTypeArgs(), builderType);
		Annotation[] typeUseAnnsKey = getTypeUseAnnotations(keyParamType);
		Annotation[] typeUseAnnsValue = getTypeUseAnnotations(valueParamType);
		
		removeTypeUseAnnotations(keyParamType);
		removeTypeUseAnnotations(valueParamType);
		Argument keyParam = new Argument(keyParamName, 0, keyParamType, ClassFileConstants.AccFinal);
		Argument valueParam = new Argument(valueParamName, 0, valueParamType, ClassFileConstants.AccFinal);
		keyParam.annotations = typeUseAnnsKey;
		valueParam.annotations = typeUseAnnsValue;
		md.arguments = new Argument[] {keyParam, valueParam};
		md.returnType = returnType;
		addCheckerFrameworkReturnsReceiver(md.returnType, data.getSource(), cfv);
		
		String name = new String(data.getSingularName());
		String setterPrefix = data.getSetterPrefix().length > 0 ? new String(data.getSetterPrefix()) : fluent ? "" : "put";
		String setterName = HandlerUtil.buildAccessorName(builderType, setterPrefix, name);
		
		md.selector = setterName.toCharArray();
		Annotation[] selfReturnAnnotations = generateSelfReturnAnnotations(deprecate, data.getSource());
		Annotation[] copyToSetterAnnotations = copyAnnotations(md, findCopyableToBuilderSingularSetterAnnotations(data.getAnnotation().up()));
		md.annotations = concat(selfReturnAnnotations, copyToSetterAnnotations, Annotation.class);
		
		if (returnStatement != null) createRelevantNonNullAnnotation(builderType, md);
		data.setGeneratedByRecursive(md);
		HandleNonNull.INSTANCE.fix(injectMethod(builderType, md));
	}
	
	private void generatePluralMethod(CheckerFrameworkVersion cfv, boolean deprecate, TypeReference returnType, Statement returnStatement, SingularData data, EclipseNode builderType, boolean fluent, AccessLevel access) {
		MethodDeclaration md = new MethodDeclaration(((CompilationUnitDeclaration) builderType.top().get()).compilationResult);
		md.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		md.modifiers = toEclipseModifier(access);
		
		String pN = new String(data.getPluralName());
		char[] keyFieldName = (pN + "$key").toCharArray();
		char[] valueFieldName = (pN + "$value").toCharArray();
		
		List<Statement> statements = new ArrayList<Statement>();
		statements.add(createConstructBuilderVarIfNeeded(data, builderType, true));
		
		char[] entryName = "$lombokEntry".toCharArray();
		
		TypeReference forEachType = new QualifiedTypeReference(JAVA_UTIL_MAP_ENTRY, NULL_POSS);
		forEachType = addTypeArgs(2, true, builderType, forEachType, data.getTypeArgs());
		
		MessageSend keyArg = new MessageSend();
		keyArg.receiver = new SingleNameReference(entryName, 0L);
		keyArg.selector = "getKey".toCharArray();
		MessageSend addKey = new MessageSend();
		FieldReference thisDotKeyField = new FieldReference(keyFieldName, 0L);
		thisDotKeyField.receiver = new ThisReference(0, 0);
		addKey.receiver = thisDotKeyField;
		addKey.selector = new char[] {'a', 'd', 'd'};
		addKey.arguments = new Expression[] {keyArg};
		
		MessageSend valueArg = new MessageSend();
		valueArg.receiver = new SingleNameReference(entryName, 0L);
		valueArg.selector = "getValue".toCharArray();
		MessageSend addValue = new MessageSend();
		FieldReference thisDotValueField = new FieldReference(valueFieldName, 0L);
		thisDotValueField.receiver = new ThisReference(0, 0);
		addValue.receiver = thisDotValueField;
		addValue.selector = new char[] {'a', 'd', 'd'};
		addValue.arguments = new Expression[] {valueArg};
		
		LocalDeclaration elementVariable = new LocalDeclaration(entryName, 0, 0);
		elementVariable.type = forEachType;
		ForeachStatement forEach = new ForeachStatement(elementVariable, 0);
		MessageSend invokeEntrySet = new MessageSend();
		invokeEntrySet.selector = new char[] { 'e', 'n', 't', 'r', 'y', 'S', 'e', 't'};
		invokeEntrySet.receiver = new SingleNameReference(data.getPluralName(), 0L);
		forEach.collection = invokeEntrySet;
		Block forEachContent = new Block(0);
		forEachContent.statements = new Statement[] {addKey, addValue};
		forEach.action = forEachContent;
		statements.add(forEach);
		
		TypeReference paramType = new QualifiedTypeReference(JAVA_UTIL_MAP, NULL_POSS);
		paramType = addTypeArgs(2, true, builderType, paramType, data.getTypeArgs());
		Argument param = new Argument(data.getPluralName(), 0, paramType, ClassFileConstants.AccFinal);
		
		nullBehaviorize(builderType, data, statements, param, md);
		
		if (returnStatement != null) statements.add(returnStatement);
		
		md.statements = statements.toArray(new Statement[0]);
		
		md.arguments = new Argument[] {param};
		md.returnType = returnType;
		addCheckerFrameworkReturnsReceiver(md.returnType, data.getSource(), cfv);
		
		String name = new String(data.getPluralName());
		String setterPrefix = data.getSetterPrefix().length > 0 ? new String(data.getSetterPrefix()) : fluent ? "" : "put";
		String setterName = HandlerUtil.buildAccessorName(builderType, setterPrefix, name);
		
		md.selector = setterName.toCharArray();
		Annotation[] selfReturnAnnotations = generateSelfReturnAnnotations(deprecate, data.getSource());
		Annotation[] copyToSetterAnnotations = copyAnnotations(md, findCopyableToSetterAnnotations(data.getAnnotation().up(), true));
		md.annotations = concat(selfReturnAnnotations, copyToSetterAnnotations, Annotation.class);
		
		if (returnStatement != null) createRelevantNonNullAnnotation(builderType, md);
		data.setGeneratedByRecursive(md);
		injectMethod(builderType, md);
	}
	
	@Override public void appendBuildCode(SingularData data, EclipseNode builderType, List<Statement> statements, char[] targetVariableName, String builderVariable) {
		if (useGuavaInstead(builderType)) {
			guavaMapSingularizer.appendBuildCode(data, builderType, statements, targetVariableName, builderVariable);
			return;
		}
		
		if (data.getTargetFqn().equals("java.util.Map")) {
			statements.addAll(createJavaUtilSetMapInitialCapacitySwitchStatements(data, builderType, true, "emptyMap", "singletonMap", "LinkedHashMap", builderVariable));
		} else if (data.getTargetFqn().equals("java.util.SequencedMap")) {
			statements.addAll(createJavaUtilSimpleCreationAndFillStatements(data, builderType, true, true, false, true, "LinkedHashMap", builderVariable));
		} else {
			statements.addAll(createJavaUtilSimpleCreationAndFillStatements(data, builderType, true, true, false, true, "TreeMap", builderVariable));
		}
	}
	
	@Override protected int getTypeArgumentsCount() {
		return 2;
	}
}
