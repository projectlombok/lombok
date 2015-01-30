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

import static lombok.eclipse.handlers.EclipseHandlerUtil.makeIntLiteral;

import java.util.ArrayList;
import java.util.List;

import lombok.core.LombokImmutableList;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseSingularsRecipes.EclipseSingularizer;
import lombok.eclipse.handlers.EclipseSingularsRecipes.SingularData;

import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.mangosdk.spi.ProviderFor;


@ProviderFor(EclipseSingularizer.class)
public class EclipseJavaUtilListSingularizer extends EclipseJavaUtilListSetSingularizer {
	@Override public LombokImmutableList<String> getSupportedTypes() {
		return LombokImmutableList.of("java.util.List", "java.util.Collection", "java.lang.Iterable");
	}
	
	@Override public void appendBuildCode(SingularData data, EclipseNode builderType, List<Statement> statements, char[] targetVariableName) {
		if (useGuavaInstead(builderType)) {
			guavaListSetSingularizer.appendBuildCode(data, builderType, statements, targetVariableName);
			return;
		}
		
		List<Statement> switchContents = new ArrayList<Statement>();
		
		/* case 0: (empty) break; */ {
			switchContents.add(new CaseStatement(makeIntLiteral(new char[] {'0'}, null), 0, 0));
			MessageSend invoke = new MessageSend();
			invoke.receiver = new QualifiedNameReference(JAVA_UTIL_COLLECTIONS, NULL_POSS, 0, 0);
			invoke.selector = "emptyList".toCharArray();
			switchContents.add(new Assignment(new SingleNameReference(data.getPluralName(), 0), invoke, 0));
			switchContents.add(new BreakStatement(null, 0, 0));
		}
		
		/* case 1: (singleton) break; */ {
			switchContents.add(new CaseStatement(makeIntLiteral(new char[] {'1'}, null), 0, 0));
			FieldReference thisDotField = new FieldReference(data.getPluralName(), 0L);
			thisDotField.receiver = new ThisReference(0, 0);
			MessageSend thisDotFieldGet0 = new MessageSend();
			thisDotFieldGet0.receiver = thisDotField;
			thisDotFieldGet0.selector = new char[] {'g', 'e', 't'};
			thisDotFieldGet0.arguments = new Expression[] {makeIntLiteral(new char[] {'0'}, null)};
			
			Expression[] args = new Expression[] {thisDotFieldGet0};
			MessageSend invoke = new MessageSend();
			invoke.receiver = new QualifiedNameReference(JAVA_UTIL_COLLECTIONS, NULL_POSS, 0, 0);
			invoke.selector = "singletonList".toCharArray();
			invoke.arguments = args;
			switchContents.add(new Assignment(new SingleNameReference(data.getPluralName(), 0), invoke, 0));
			switchContents.add(new BreakStatement(null, 0, 0));
		}
		
		/* default: Create by passing builder field to constructor. */ {
			switchContents.add(new CaseStatement(null, 0, 0));
			
			Expression argToUnmodifiable;
			/* new j.u.ArrayList<Generics>(this.pluralName); */ {
				FieldReference thisDotPluralName = new FieldReference(data.getPluralName(), 0L);
				thisDotPluralName.receiver = new ThisReference(0, 0);
				TypeReference targetTypeExpr = new QualifiedTypeReference(JAVA_UTIL_ARRAYLIST, NULL_POSS);
				targetTypeExpr = addTypeArgs(1, false, builderType, targetTypeExpr, data.getTypeArgs());
				AllocationExpression constructorCall = new AllocationExpression();
				constructorCall.type = targetTypeExpr;
				constructorCall.arguments = new Expression[] {thisDotPluralName};
				argToUnmodifiable = constructorCall;
			}
			
			/* pluralname = Collections.unmodifiableList(-newlist-); */ {
				MessageSend unmodInvoke = new MessageSend();
				unmodInvoke.receiver = new QualifiedNameReference(JAVA_UTIL_COLLECTIONS, NULL_POSS, 0, 0);
				unmodInvoke.selector = "unmodifiableList".toCharArray();
				unmodInvoke.arguments = new Expression[] {argToUnmodifiable};
				switchContents.add(new Assignment(new SingleNameReference(data.getPluralName(), 0), unmodInvoke, 0));
			}
		}
		
		SwitchStatement switchStat = new SwitchStatement();
		switchStat.statements = switchContents.toArray(new Statement[switchContents.size()]);
		switchStat.expression = getSize(builderType, data.getPluralName(), true);
		
		TypeReference localShadowerType = new QualifiedTypeReference(Eclipse.fromQualifiedName(data.getTargetFqn()), NULL_POSS);
		localShadowerType = addTypeArgs(1, false, builderType, localShadowerType, data.getTypeArgs());
		LocalDeclaration varDefStat = new LocalDeclaration(data.getPluralName(), 0, 0);
		varDefStat.type = localShadowerType;
		statements.add(varDefStat);
		statements.add(switchStat);
	}
}
