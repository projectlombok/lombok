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
package lombok.javac.handlers.singulars;

import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacSingularsRecipes.JavacSingularizer;
import lombok.javac.handlers.JavacSingularsRecipes.SingularData;

abstract class JavacJavaUtilSingularizer extends JavacSingularizer {
	protected final JavacSingularizer guavaListSetSingularizer = new JavacGuavaSetListSingularizer();
	protected final JavacSingularizer guavaMapSingularizer = new JavacGuavaMapSingularizer();
	
	protected boolean useGuavaInstead(JavacNode node) {
		return Boolean.TRUE.equals(node.getAst().readConfiguration(ConfigurationKeys.SINGULAR_USE_GUAVA));
	}
	
	protected List<JCStatement> createJavaUtilSetMapInitialCapacitySwitchStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, boolean mapMode, String emptyCollectionMethod, String singletonCollectionMethod, String targetType, JCTree source) {
		List<JCExpression> jceBlank = List.nil();
		ListBuffer<JCCase> cases = new ListBuffer<JCCase>();
		
		if (emptyCollectionMethod != null) { // case 0: (empty); break;
			JCStatement assignStat; {
				// pluralName = java.util.Collections.emptyCollectionMethod();
				JCExpression invoke = maker.Apply(jceBlank, chainDots(builderType, "java", "util", "Collections", emptyCollectionMethod), jceBlank);
				assignStat = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), invoke));
			}
			JCStatement breakStat = maker.Break(null);
			JCCase emptyCase = maker.Case(maker.Literal(CTC_INT, 0), List.of(assignStat, breakStat));
			cases.append(emptyCase);
		}
		
		if (singletonCollectionMethod != null) { // case 1: (singleton); break;
			JCStatement assignStat; {
				// !mapMode: pluralName = java.util.Collections.singletonCollectionMethod(this.pluralName.get(0));
				//  mapMode: pluralName = java.util.Collections.singletonCollectionMethod(this.pluralName$key.get(0), this.pluralName$value.get(0));
				JCExpression zeroLiteral = maker.Literal(CTC_INT, 0);
				JCExpression arg = maker.Apply(jceBlank, chainDots(builderType, "this", data.getPluralName() + (mapMode ? "$key" : ""), "get"), List.of(zeroLiteral));
				List<JCExpression> args;
				if (mapMode) {
					JCExpression zeroLiteralClone = maker.Literal(CTC_INT, 0);
					JCExpression arg2 = maker.Apply(jceBlank, chainDots(builderType, "this", data.getPluralName() + (mapMode ? "$value" : ""), "get"), List.of(zeroLiteralClone));
					args = List.of(arg, arg2);
				} else {
					args = List.of(arg);
				}
				JCExpression invoke = maker.Apply(jceBlank, chainDots(builderType, "java", "util", "Collections", singletonCollectionMethod), args);
				assignStat = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), invoke));
			}
			JCStatement breakStat = maker.Break(null);
			JCCase singletonCase = maker.Case(maker.Literal(CTC_INT, 1), List.of(assignStat, breakStat));
			cases.append(singletonCase);
		}
		
		{ // default:
			List<JCStatement> statements = createJavaUtilSimpleCreationAndFillStatements(maker, data, builderType, mapMode, false, true, emptyCollectionMethod == null, targetType, source);
			JCCase defaultCase = maker.Case(null, statements);
			cases.append(defaultCase);
		}
		
		JCStatement switchStat = maker.Switch(getSize(maker,  builderType, mapMode ? builderType.toName(data.getPluralName() + "$key") : data.getPluralName(), true, false), cases.toList());
		JCExpression localShadowerType = chainDotsString(builderType, data.getTargetFqn());
		localShadowerType = addTypeArgs(mapMode ? 2 : 1, false, builderType, localShadowerType, data.getTypeArgs(), source);
		JCStatement varDefStat = maker.VarDef(maker.Modifiers(0), data.getPluralName(), localShadowerType, null);
		return List.of(varDefStat, switchStat);
	}
	
	protected JCStatement createConstructBuilderVarIfNeeded(JavacTreeMaker maker, SingularData data, JavacNode builderType, boolean mapMode, JCTree source) {
		List<JCExpression> jceBlank = List.nil();

		Name v1Name = mapMode ? builderType.toName(data.getPluralName() + "$key") : data.getPluralName();
		Name v2Name = mapMode ? builderType.toName(data.getPluralName() + "$value") : null;
		JCExpression thisDotField = maker.Select(maker.Ident(builderType.toName("this")), v1Name);
		JCExpression cond = maker.Binary(CTC_EQUAL, thisDotField, maker.Literal(CTC_BOT, null));
		thisDotField = maker.Select(maker.Ident(builderType.toName("this")), v1Name);
		JCExpression v1Type = chainDots(builderType, "java", "util", "ArrayList");
		v1Type = addTypeArgs(1, false, builderType, v1Type, data.getTypeArgs(), source);
		JCExpression constructArrayList = maker.NewClass(null, jceBlank, v1Type, jceBlank, null);
		JCStatement initV1 = maker.Exec(maker.Assign(thisDotField, constructArrayList));
		JCStatement thenPart;
		if (mapMode) {
			thisDotField = maker.Select(maker.Ident(builderType.toName("this")), v2Name);
			JCExpression v2Type = chainDots(builderType, "java", "util", "ArrayList");
			List<JCExpression> tArgs = data.getTypeArgs();
			if (tArgs != null && tArgs.tail != null) tArgs = tArgs.tail;
			else tArgs = List.nil();
			v2Type = addTypeArgs(1, false, builderType, v2Type, tArgs, source);
			constructArrayList = maker.NewClass(null, jceBlank, v2Type, jceBlank, null);
			JCStatement initV2 = maker.Exec(maker.Assign(thisDotField, constructArrayList));
			thenPart = maker.Block(0, List.of(initV1, initV2));
		} else {
			thenPart = initV1;
		}
		return maker.If(cond, thenPart, null);
	}
	
	protected List<JCStatement> createJavaUtilSimpleCreationAndFillStatements(JavacTreeMaker maker, SingularData data, JavacNode builderType, boolean mapMode, boolean defineVar, boolean addInitialCapacityArg, boolean nullGuard, String targetType, JCTree source) {
		List<JCExpression> jceBlank = List.nil();
		Name thisName = builderType.toName("this");
		
		JCStatement createStat; {
			 // pluralName = new java.util.TargetType(initialCap);
			List<JCExpression> constructorArgs = List.nil();
			if (addInitialCapacityArg) {
				Name varName = mapMode ? builderType.toName(data.getPluralName() + "$key") : data.getPluralName();
				// this.varName.size() < MAX_POWER_OF_2 ? 1 + this.varName.size() + (this.varName.size() - 3) / 3 : Integer.MAX_VALUE;
				// lessThanCutOff = this.varName.size() < MAX_POWER_OF_2
				JCExpression lessThanCutoff = maker.Binary(CTC_LESS_THAN, getSize(maker, builderType, varName, nullGuard, true), maker.Literal(CTC_INT, 0x40000000));
				JCExpression integerMaxValue = genJavaLangTypeRef(builderType, "Integer", "MAX_VALUE");
				JCExpression sizeFormulaLeft = maker.Binary(CTC_PLUS, maker.Literal(CTC_INT, 1), getSize(maker, builderType, varName, nullGuard, true));
				JCExpression sizeFormulaRightLeft = maker.Parens(maker.Binary(CTC_MINUS, getSize(maker, builderType, varName, nullGuard, true), maker.Literal(CTC_INT, 3)));
				JCExpression sizeFormulaRight = maker.Binary(CTC_DIV, sizeFormulaRightLeft, maker.Literal(CTC_INT, 3));
				JCExpression sizeFormula = maker.Binary(CTC_PLUS, sizeFormulaLeft, sizeFormulaRight);
				constructorArgs = List.<JCExpression>of(maker.Conditional(lessThanCutoff, sizeFormula, integerMaxValue));
			}
			
			JCExpression targetTypeExpr = chainDots(builderType, "java", "util", targetType);
			targetTypeExpr = addTypeArgs(mapMode ? 2 : 1, false, builderType, targetTypeExpr, data.getTypeArgs(), source);
			JCExpression constructorCall = maker.NewClass(null, jceBlank, targetTypeExpr, constructorArgs, null);
			if (defineVar) {
				JCExpression localShadowerType = chainDotsString(builderType, data.getTargetFqn());
				localShadowerType = addTypeArgs(mapMode ? 2 : 1, false, builderType, localShadowerType, data.getTypeArgs(), source);
				createStat = maker.VarDef(maker.Modifiers(0), data.getPluralName(), localShadowerType, constructorCall);
			} else {
				createStat = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), constructorCall));
			}
		}
		
		JCStatement fillStat; {
			if (mapMode) {
				// for (int $i = 0; $i < this.pluralname$key.size(); i++) pluralname.put(this.pluralname$key.get($i), this.pluralname$value.get($i));
				Name ivar = builderType.toName("$i");
				Name keyVarName = builderType.toName(data.getPluralName() + "$key");
				JCExpression pluralnameDotPut = maker.Select(maker.Ident(data.getPluralName()), builderType.toName("put"));
				JCExpression arg1 = maker.Apply(jceBlank, chainDots(builderType, "this", data.getPluralName() + "$key", "get"), List.<JCExpression>of(maker.Ident(ivar)));
				JCExpression arg2 = maker.Apply(jceBlank, chainDots(builderType, "this", data.getPluralName() + "$value", "get"), List.<JCExpression>of(maker.Ident(ivar)));
				// [jdk9] We add an unneccessary (V) cast here. Not doing so gives an error in javac (build 9-ea+156-jigsaw-nightly-h6072-20170212):
				//   error: method put in interface Map<K#2,V#2> cannot be applied to given types;
				arg2 = maker.TypeCast(createTypeArgs(2, false, builderType, data.getTypeArgs(), source).get(1), arg2);
				JCStatement putStatement = maker.Exec(maker.Apply(jceBlank, pluralnameDotPut, List.of(arg1, arg2)));
				JCStatement forInit = maker.VarDef(maker.Modifiers(0), ivar, maker.TypeIdent(CTC_INT), maker.Literal(CTC_INT, 0));
				JCExpression checkExpr = maker.Binary(CTC_LESS_THAN, maker.Ident(ivar), getSize(maker, builderType, keyVarName, nullGuard, true));
				JCExpression incrementExpr = maker.Unary(CTC_POSTINC, maker.Ident(ivar));
				fillStat = maker.ForLoop(List.of(forInit), checkExpr, List.of(maker.Exec(incrementExpr)), putStatement);
			} else {
				// pluralname.addAll(this.pluralname);
				JCExpression thisDotPluralName = maker.Select(maker.Ident(thisName), data.getPluralName());
				fillStat = maker.Exec(maker.Apply(jceBlank, maker.Select(maker.Ident(data.getPluralName()), builderType.toName("addAll")), List.of(thisDotPluralName)));
			}
			if (nullGuard) {
				JCExpression thisDotField = maker.Select(maker.Ident(thisName), mapMode ? builderType.toName(data.getPluralName() + "$key") : data.getPluralName());
				JCExpression nullCheck = maker.Binary(CTC_NOT_EQUAL, thisDotField, maker.Literal(CTC_BOT, null));
				fillStat = maker.If(nullCheck, fillStat, null);
			}
		}
		JCStatement unmodifiableStat; {
			// pluralname = Collections.unmodifiableInterfaceType(pluralname);
			JCExpression arg = maker.Ident(data.getPluralName());
			JCExpression invoke = maker.Apply(jceBlank, chainDots(builderType, "java", "util", "Collections", "unmodifiable" + data.getTargetSimpleType()), List.of(arg));
			unmodifiableStat = maker.Exec(maker.Assign(maker.Ident(data.getPluralName()), invoke));
		}
		
		return List.of(createStat, fillStat, unmodifiableStat);
	}
}
