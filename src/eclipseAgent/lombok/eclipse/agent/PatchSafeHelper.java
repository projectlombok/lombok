package lombok.eclipse.agent;

import lombok.core.handlers.SafeCallIllegalUsingException;
import lombok.core.handlers.SafeCallInternalException;
import lombok.core.handlers.SafeCallUnexpectedStateException;
import lombok.eclipse.Eclipse;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

import java.util.*;

import static java.util.Arrays.copyOf;
import static java.util.Collections.emptySet;
import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.*;
import static lombok.eclipse.Eclipse.fromQualifiedName;
import static lombok.eclipse.EclipseAugments.ASTNode_parentNode;
import static lombok.eclipse.agent.PatchSafeCallCopyHelper.copy;
import static lombok.eclipse.agent.PatchSafeCallCopyHelper.getOperator;
import static lombok.eclipse.handlers.EclipseHandlerUtil.copySourcePosition;
import static org.eclipse.jdt.internal.compiler.ast.IntLiteral.buildIntLiteral;
import static org.eclipse.jdt.internal.compiler.ast.OperatorIds.*;
import static org.eclipse.jdt.internal.compiler.ast.TypeReference.baseTypeReference;
import static org.eclipse.jdt.internal.compiler.impl.Constant.NotAConstant;
import static org.eclipse.jdt.internal.compiler.lookup.TypeConstants.*;
import static org.eclipse.jdt.internal.compiler.lookup.TypeIds.T_int;

/**
 * Created by Bulgakov Alexander on 21.12.16.
 */
final class PatchSafeHelper {

	public static final int NOT_USED = -1;
	private static final long[] NULL_POSS = {0L};

	private PatchSafeHelper() {
	}

	static ASTNode getVarByBlock(Block block) {
		return ASTNode_parentNode.get(block);
	}

	private static Expression newElvis(
			Expression expr,
			TypeReference truePartType,
			TypeReference falsePartType
	) throws SafeCallUnexpectedStateException {
		Expression result;
		Expression falsePart = getFalsePart(falsePartType);
		if (expr instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) expr;
			Expression checkable = messageSend.receiver;
			result = newIfNullThenConditional(checkable, expr, falsePart);
		} else if (expr instanceof SingleNameReference) {
			if (!Eclipse.isPrimitive(truePartType) && Eclipse.isPrimitive(falsePartType)) {
				result = newIfNullThenConditional(expr, expr, falsePart);
			} else {
				result = null;
			}
		} else if (expr instanceof QualifiedNameReference) {
			QualifiedNameReference qnr = (QualifiedNameReference) expr;
			Expression expression = newIfNullThenConditional(qnr, qnr, falsePart);
			char[][] tokens = qnr.tokens;
			long[] sp = qnr.sourcePositions;
			if (tokens.length == 2) {
				SingleNameReference checkable = newSingleNameReference(tokens[0], sp[0]);
				checkable.constant = NotAConstant;
				expression = newIfNullThenConditional(checkable, expression, falsePart);
			} else {
				char[][] parentTokens = copyOf(tokens, tokens.length - 1);
				long[] parentP = copyOf(sp, sp.length - 1);
				QualifiedNameReference reference = new QualifiedNameReference(parentTokens, parentP,
						qnr.sourceStart, qnr.sourceEnd);
				expression = newElvis(reference, truePartType, falsePartType);
			}
			result = expression;
		} else if (expr instanceof FieldReference) {
			result = newIfNullThenConditional(((FieldReference) expr).receiver, expr, falsePart);
		} else if (expr instanceof AllocationExpression) result = null;
		else if (expr instanceof ThisReference) result = null;
		else if (expr instanceof Literal) result = null;
		else if (expr instanceof ArrayReference) {
			ArrayReference arrayReference = (ArrayReference) expr;
			Expression receiver = arrayReference.receiver;
			result = newIfNullThenConditional(receiver, expr, falsePart);
		} else if (expr instanceof CastExpression) {
			CastExpression castExpression = ((CastExpression) expr);
			Expression casted = castExpression.expression;
			result = newIfNullThenConditional(casted, castExpression, falsePart);
		} else if (expr == null) result = null;
		else {
			throw new SafeCallUnexpectedStateException(elvisConditional, expr, expr.getClass());
		}
		return result;
	}

	private static Expression getFalsePart(TypeReference typeName) {
		return Eclipse.isPrimitive(typeName) ? newDefaultValueLiterall(typeName) : newNullLiteral();
	}

	private static ConditionalExpression newIfNullThenConditional(
			Expression checkable, Expression truePart, Expression falsePart
	) throws SafeCallUnexpectedStateException {
		if (checkable instanceof ThisReference) {
			return null;
		}

		EqualExpression checkExpr = newBinaryExpresion(checkable, NOT_EQUAL, newNullLiteral());
		//checkExpr.constant = NotAConstant;

		if (truePart instanceof ArrayReference) {
			ArrayReference arrayReference = (ArrayReference) truePart;
			char[][] arrayLengthCall = new char[2][];
			if (checkable instanceof SingleNameReference) {
				SingleNameReference ref = (SingleNameReference) checkable;
				arrayLengthCall[0] = ref.token;
			} else {
				throw new SafeCallUnexpectedStateException(newConditional, checkable, checkable.getClass());
			}
			arrayLengthCall[1] = "length".toCharArray();
			QualifiedNameReference lengthCall = new QualifiedNameReference(arrayLengthCall,
					new long[2], arrayReference.sourceStart, arrayReference.sourceEnd);

			Expression position = arrayReference.position;

			EqualExpression noLessZero = newBinaryExpresion(position, GREATER_EQUAL, newZeroLiteral());
			EqualExpression lessLength = newBinaryExpresion(position, LESS, lengthCall);
			EqualExpression indexRangeCondition = new EqualExpression(noLessZero, lessLength, AND_AND);
			if (position instanceof IntLiteral) {
				IntLiteral intLiteral = (IntLiteral) position;
				int value = intLiteral.constant.intValue();
				if (value == 0) {
					indexRangeCondition = lessLength;
				}
			}

			checkExpr = new EqualExpression(checkExpr, indexRangeCondition, AND_AND);
		}
		copySourcePosition(checkable, checkExpr);
		return newConditional(checkExpr, truePart, falsePart);
	}

	private static EqualExpression newBinaryExpresion(
			Expression left, int operator, Expression right) throws SafeCallUnexpectedStateException {
		return new EqualExpression(copy(left), right, operator);
	}

	private static IntLiteral newZeroLiteral() {
		return buildIntLiteral("0".toCharArray(), 0, 0);
	}

	private static ConditionalExpression newConditional(BinaryExpression condition, Expression truePart, Expression falsePart) {
		ConditionalExpression cexpr = new ConditionalExpression(condition, truePart, falsePart);
		copySourcePosition(truePart, cexpr);
		return cexpr;
	}

	private static Expression newNullLiteral() {
		Literal falsePart = new NullLiteral(0, 0);
		falsePart.computeConstant();
		return falsePart;
	}

	private static Expression newDefaultValueLiterall(TypeReference typeName) {
		return newDefaultValueLiterall(typeName.getTypeName());
	}

	private static Expression newDefaultValueLiterall(char[][] name) {
		Literal expression = getDefaultValue(name);
		expression.computeConstant();
		return expression;
	}

	private static Literal getDefaultValue(char[][] qname) {
		char[] name = qname.length == 1 ? qname[0] : null;
		int s = 0, e = 0;
		if (Arrays.equals(BOOLEAN, name)) return new FalseLiteral(s, e);
		if (Arrays.equals(CHAR, name)) return new CharLiteral(new char[]{'\'', '\\', '0', '\''}, s, e);
		if (Arrays.equals(BYTE, name) ||
				Arrays.equals(SHORT, name) ||
				Arrays.equals(INT, name))
			return buildIntLiteral(new char[]{'0'}, s, e);
		if (Arrays.equals(LONG, name)) return LongLiteral.buildLongLiteral(new char[]{'0', 'L'}, s, e);
		if (Arrays.equals(FLOAT, name)) return new FloatLiteral(new char[]{'0', 'F'}, s, e);
		if (Arrays.equals(DOUBLE, name)) return new DoubleLiteral(new char[]{'0', 'D'}, s, e);

		String s1 = new QualifiedNameReference(qname, new long[qname.length], 0, 0).toString();
		throw new IllegalArgumentException("unsupported primitive " + s1);
	}

	static long getP(ASTNode parent) {
		int pS = parent.sourceStart, pE = parent.sourceEnd;
		return (long) pS << 32 | pE;
	}

	static ArrayList<Statement> newInitStatements(
			AbstractVariableDeclaration varDecl, Expression expr, long p, BlockScope rootScope
	) throws SafeCallUnexpectedStateException, SafeCallInternalException, SafeCallIllegalUsingException {
		char[] name = varDecl.name;
		ArrayList<Statement> statements = new ArrayList<Statement>();

		VarRef varRef = populateInitStatements(1, varDecl, expr, statements, rootScope);
		if (varRef.var == null) return null;
		Reference childVarRef = varRef.getRef();

		Expression rhs = childVarRef;
		TypeReference varType = varDecl.type;
		if (mustBeProtected(varType, varRef.var.type)) {
			rhs = newIfNullThenConditional(childVarRef, childVarRef, newDefaultValueLiterall(varType));
		}

		SingleNameReference lhs = newSingleNameReference(name, p);

		Assignment assignment = new Assignment(lhs, rhs, (int) p);
		statements.add(assignment);
		return statements;
	}

	private static VarRef populateInitStatements(
			final int level,
			AbstractVariableDeclaration rootVar,
			Expression expr,
			List<Statement> statements,
			BlockScope rootScope
	) throws SafeCallUnexpectedStateException, SafeCallInternalException, SafeCallIllegalUsingException {
		int notDuplicatedLevel = verifyNotDuplicateLevel(level, rootVar, rootScope);
		char[] templateName = rootVar.name;
		char[] varName = newName(notDuplicatedLevel, templateName);

		TypeBinding type = expr.resolvedType;

		LocalDeclaration resultVar;
		int lastLevel;
		if (expr instanceof MessageSend) {
			final MessageSend messageSend = (MessageSend) expr;

			Expression[] arguments = messageSend.arguments;

			MethodBinding methodBinding = messageSend.binding;

			Expression[] resultArguments = arguments != null ? new Expression[arguments.length] : null;
			lastLevel = populateMethodCallArgs(rootVar, arguments, resultArguments,
					methodBinding, notDuplicatedLevel, statements,
					rootScope);

			if (methodBinding.isStatic()) {
				resultVar = makeLocalDeclaration(statements, varName, messageSend, type);
			} else {
				VarRef varRef = populateInitStatements(lastLevel + 1, rootVar, messageSend.receiver,
						statements, rootScope);

				Expression receiver;
				if (varRef.var != null) {
					lastLevel = varRef.level;
					receiver = varRef.getRef();
				} else receiver = messageSend.receiver;

				TypeBinding exprType = methodBinding.returnType;


				MessageSend resultMessageSend = new MessageSend();
				resultMessageSend.receiver = receiver;
				resultMessageSend.arguments = resultArguments;
				resultMessageSend.selector = messageSend.selector;
				resultMessageSend.nameSourcePosition = messageSend.nameSourcePosition;
				resultMessageSend.binding = messageSend.binding;
				resultMessageSend.actualReceiverType = messageSend.actualReceiverType;
				copySourcePosition(messageSend, resultMessageSend);

				resultVar = newElvisDeclaration(statements, varName, resultMessageSend, exprType);
			}
		} else if (expr instanceof SingleNameReference) {
			lastLevel = notDuplicatedLevel;
			resultVar = makeLocalDeclaration(statements, varName, expr, type);
		} else if (expr instanceof QualifiedNameReference) {
			QualifiedNameReference qnr = (QualifiedNameReference) expr;
			boolean staticField = isStaticField(qnr);
			if (staticField) {
				lastLevel = notDuplicatedLevel;
				resultVar = makeLocalDeclaration(statements, varName, expr, type);
			} else {
				char[][] tokens = qnr.tokens;
				long[] sp = qnr.sourcePositions;
				long pos = sp[0];
				char[] last = tokens[tokens.length - 1];
				NameReference first;
				if (tokens.length == 2) first = newSingleNameReference(tokens[0], pos);
				else if (tokens.length > 2) {
					char[][] parentTokens = copyOf(tokens, tokens.length - 1);
					long[] parentP = copyOf(sp, sp.length - 1);
					first = new QualifiedNameReference(parentTokens, parentP,
							qnr.sourceStart, qnr.sourceEnd);
				} else {
					throw new SafeCallUnexpectedStateException(populateInitStatementsTokenAmount,
							expr, expr.getClass());
				}

				VarRef varRef = populateBlockForQNR(notDuplicatedLevel, varName, qnr, first, last, pos,
						type, statements, rootVar, rootScope);
				resultVar = varRef.var;
				lastLevel = getLast(varRef, notDuplicatedLevel);
			}
		} else if (expr instanceof FieldReference) {
			FieldReference fr = (FieldReference) expr;
			boolean staticField = fr.binding.isStatic();
			if (staticField) {
				lastLevel = notDuplicatedLevel;
				resultVar = makeLocalDeclaration(statements, varName, expr, type);
			} else {
				Expression receiver = fr.receiver;
				VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, receiver,
						statements, rootScope);
				if (varRef.var != null) {
					fr.receiver = varRef.getRef();
					lastLevel = varRef.level;
				} else lastLevel = notDuplicatedLevel;
				resultVar = newElvisDeclaration(statements, varName, expr, type);
			}
		} else if (expr instanceof AllocationExpression /*||
			    expr instanceof Literal*/) {
			resultVar = makeLocalDeclaration(statements, varName, expr, type);
			lastLevel = notDuplicatedLevel;
		} else if (expr instanceof ArrayAllocationExpression) {
			lastLevel = notDuplicatedLevel;
			ArrayAllocationExpression arrayAllocation = ((ArrayAllocationExpression) expr);
			ArrayInitializer initializer = arrayAllocation.initializer;
			ArrayAllocationExpression resultArrayAlloc = new ArrayAllocationExpression();
			resultArrayAlloc.type = arrayAllocation.type;
			if (initializer != null) {
				Expression[] args = initializer.expressions;
				Expression[] resulsArgs = args != null ? new Expression[args.length] : null;
				lastLevel = populateArrayInitializer(rootVar, args, resulsArgs, arrayAllocation.type,
						lastLevel, statements, rootScope);
				ArrayInitializer resultInitializer = new ArrayInitializer();
				resultInitializer.expressions = resulsArgs;
				resultArrayAlloc.initializer = resultInitializer;
			}

			Expression[] dimensions = arrayAllocation.dimensions;
			if (dimensions != null && dimensions.length > 0) {
				Expression[] resultDimensions = new Expression[dimensions.length];
				lastLevel = populateArrayDimensions(rootVar, dimensions, resultDimensions,
						lastLevel, statements, rootScope);
				resultArrayAlloc.dimensions = resultDimensions;
			} else resultArrayAlloc.dimensions = dimensions;

			resultVar = makeLocalDeclaration(statements, varName, (resultArrayAlloc), type);

		} else if (expr instanceof Literal || isLambda(expr)) {
			resultVar = null;
			lastLevel = NOT_USED;
		} else if (expr instanceof ThisReference) {
			resultVar = null;
			lastLevel = NOT_USED;
		} else if (expr instanceof CastExpression) {
			CastExpression castExpression = ((CastExpression) expr);

			Expression expression = castExpression.expression;
			VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, expression,
					statements, rootScope);

			LocalDeclaration expressionVar = varRef.var;
			if (expressionVar != null) {
				lastLevel = varRef.level;
				Reference reference = varRef.getRef();
				CastExpression resultCastExpression = new CastExpression(reference, castExpression.type);
				TypeReference varTypeRef = expressionVar.type;
				boolean primitive = Eclipse.isPrimitive(varTypeRef);
				resultVar = !primitive
						? newElvisDeclaration(statements, varName, (resultCastExpression), type)
						: makeLocalDeclaration(statements, varName, (resultCastExpression), type);
			} else {
				lastLevel = notDuplicatedLevel;
				CastExpression resultExpression = new CastExpression(expression, castExpression.type);
				resultVar = makeLocalDeclaration(statements, varName, resultExpression, type);
			}
		} else if (expr instanceof ArrayReference) {
			ArrayReference arrayReference = ((ArrayReference) expr);
			Expression position = arrayReference.position;
			VarRef positionVarRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, position,
					statements, rootScope);
			lastLevel = getLast(positionVarRef, notDuplicatedLevel);
			if (positionVarRef.var != null) {
				TypeReference positionType = positionVarRef.var.type;
				boolean primitive = Eclipse.isPrimitive(positionType);
				Reference ref = positionVarRef.getRef();
				if (primitive) arrayReference.position = ref;
				else {
					lastLevel = verifyNotDuplicateLevel(++lastLevel, rootVar, rootScope);
					char[] positionVarName = newName(lastLevel, templateName);
					TypeReference falsePartType = getIntTypeRef();
					LocalDeclaration positionVar = newElvisDeclaration(statements, positionVarName,
							ref, positionType, falsePartType);
					positionVar.type = getIntTypeRef();
					arrayReference.position = newSingleNameReference(positionVar.name, getP(positionVar));
				}
			}
			Expression receiver = arrayReference.receiver;
			VarRef receiverVarRef = populateInitStatements(lastLevel + 1, rootVar, receiver,
					statements, rootScope);
			arrayReference.receiver = receiverVarRef.getRef();
			resultVar = newElvisDeclaration(statements, varName, arrayReference, type);
			lastLevel = getLast(receiverVarRef, notDuplicatedLevel);
		} else if (expr instanceof UnaryExpression) {
			UnaryExpression unaryExpression = (UnaryExpression) expr;

			Expression expression = unaryExpression.expression;
			VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, expression,
					statements, rootScope);
			if (varRef.var != null) {
				TypeBinding typeBinding = unaryExpression.resolvedType;
				varRef = protectIfPrimitive(varRef, expr, typeBinding, rootVar, rootScope, statements);
				lastLevel = varRef.level;
				UnaryExpression resultExpression = new UnaryExpression(varRef.getRef(), getOperator(unaryExpression));
				resultVar = makeLocalDeclaration(statements, varName, resultExpression, type);
			} else {
				lastLevel = notDuplicatedLevel;
				resultVar = makeLocalDeclaration(statements, varName, unaryExpression, type);
			}
		} else if (expr instanceof PostfixExpression || expr instanceof PrefixExpression) {
			CompoundAssignment pe = (CompoundAssignment) expr;
			Expression lhs = pe.lhs;

			VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, lhs,
					statements, rootScope);
			if (varRef.var != null) {
				TypeBinding opType = pe.expression.resolvedType;
				varRef = protectIfPrimitive(varRef, expr, opType, rootVar, rootScope, statements);
				lastLevel = varRef.level;
				CompoundAssignment resultExpression = pe;// new PostfixExpression(varRef.getRef(), getOperator(unaryExpression));
				resultExpression.lhs = varRef.getRef();
				resultVar = makeLocalDeclaration(statements, varName, resultExpression, opType);
			} else {
				lastLevel = notDuplicatedLevel;
				resultVar = makeLocalDeclaration(statements, varName, pe, type);
			}
		} else {
			throw new SafeCallIllegalUsingException(null, expr);
		}
		return new VarRef(resultVar, lastLevel);
	}

	private static VarRef protectIfPrimitive(
			VarRef varRef, Expression expr, TypeBinding expectedType,
			AbstractVariableDeclaration rootVar, BlockScope rootScope, List<Statement> statements
	) throws SafeCallUnexpectedStateException {
		return protectIfPrimitive(varRef, getType(expr, expectedType), rootVar, rootScope, statements);
	}

	private static VarRef protectIfPrimitive(
			VarRef varRef, TypeReference expectedType,
			AbstractVariableDeclaration rootVar, BlockScope rootScope, List<Statement> statements
	) throws SafeCallUnexpectedStateException {
		char[] templateName = rootVar.name;
		TypeReference actualType = varRef.var.type;
		if (mustBeProtected(expectedType, actualType)) {
			int lastLevel = varRef.level;
			lastLevel = verifyNotDuplicateLevel(++lastLevel, rootVar, rootScope);
			char[] varName = newName(lastLevel, templateName);
			LocalDeclaration localDeclaration = newElvisDeclaration(statements, varName, varRef.getRef(),
					actualType, expectedType);
			return new VarRef(localDeclaration, lastLevel);
		} else return varRef;
	}

	private static boolean mustBeProtected(TypeReference expectedType, TypeReference actualType) {
		return Eclipse.isPrimitive(expectedType) && !Eclipse.isPrimitive(actualType);
	}

	private static TypeReference getIntTypeRef() {
		return baseTypeReference(T_int, 0);
	}

	private static int populateArrayDimensions(
			AbstractVariableDeclaration rootVar,
			Expression[] args, Expression[] resultArgs,
			int startLevel, List<Statement> statements,
			BlockScope rootScope
	) throws SafeCallInternalException, SafeCallUnexpectedStateException, SafeCallIllegalUsingException {
		if (args == null) return startLevel;

		if (resultArgs == null) throw new SafeCallInternalException(rootVar, "resultArgs cannot be null");
		int initializerLevel = startLevel;
		for (int i = 0; i < args.length; i++) {
			Expression arg = args[i];
			if (arg != null) {
				initializerLevel = populateArgument(rootVar, initializerLevel, arg, getIntTypeRef(), resultArgs,
						i, statements, rootScope
				);
				Expression resultArg = resultArgs[i];
				EqualExpression noLessZero = newBinaryExpresion(resultArg, GREATER_EQUAL, newZeroLiteral());
				ConditionalExpression condition = newConditional(noLessZero, resultArg, newZeroLiteral());
				initializerLevel = verifyNotDuplicateLevel(initializerLevel + 1, rootVar, rootScope);
				char[] templateName = rootVar.name;
				char[] conditionVarName = newName(initializerLevel, templateName);

				LocalDeclaration conditionVar = makeLocalDeclaration(statements, conditionVarName,
						condition, getIntTypeRef());
				SingleNameReference conditionReference = newSingleNameReference(conditionVar.name, getP(condition));
				resultArgs[i] = conditionReference;

			}
		}
		return initializerLevel;
	}

	private static int populateArrayInitializer(
			AbstractVariableDeclaration var,
			Expression[] args, Expression[] resultArgs, TypeReference arrayType,
			int startLevel, List<Statement> statements,
			BlockScope rootScope
	) throws SafeCallInternalException, SafeCallUnexpectedStateException, SafeCallIllegalUsingException {
		if (args == null) return startLevel;

		if (resultArgs == null) throw new SafeCallInternalException(var, "resultArgs cannot be null");
		int initializerLevel = startLevel;
		for (int i = 0; i < args.length; i++) {
			Expression arg = args[i];
			if (arg != null) {
				initializerLevel = populateArgument(var, initializerLevel, arg, arrayType, resultArgs,
						i, statements, rootScope
				);
			}
		}
		return initializerLevel;
	}

	private static int populateMethodCallArgs(
			AbstractVariableDeclaration var,
			Expression[] args, Expression[] resultArgs, MethodBinding methodBinding,
			int startLevel, List<Statement> statements,
			BlockScope rootScope
	) throws SafeCallInternalException, SafeCallUnexpectedStateException, SafeCallIllegalUsingException {
		if (args == null) return startLevel;

		if (resultArgs == null) throw new SafeCallInternalException(var, "resultArgs cannot be null");
		int initializerLevel = startLevel;
		for (int i = 0; i < args.length; i++) {
			TypeReference arrayType = getParamType(var, methodBinding, i);
			initializerLevel = populateArgument(var, initializerLevel, args[i], arrayType, resultArgs,
					i, statements, rootScope
			);
		}
		return initializerLevel;
	}

	private static int populateArgument(
			AbstractVariableDeclaration rootVar, int level,
			Expression arg, TypeReference expectedType,
			Expression[] resultArgs, int resultPosition,
			List<Statement> statements,
			BlockScope rootScope
	) throws SafeCallInternalException, SafeCallUnexpectedStateException, SafeCallIllegalUsingException {
		VarRef varRef = populateInitStatements(level + 1, rootVar, arg,
				statements, rootScope);
		Reference newInitExpr;
		LocalDeclaration localDeclaration = varRef.var;
		if (localDeclaration != null) {

			varRef = protectIfPrimitive(varRef, expectedType, rootVar, rootScope, statements);

			newInitExpr = varRef.getRef();
//			boolean mustBeConditional = mustBeProtected(expectedType, localDeclaration.type);
//			if (mustBeConditional) {
//				int conditionalLevel = verifyNotDuplicateLevel(varRef.level + 1, rootVar, rootScope);
//				char[] conditionalVarName = newName(conditionalLevel, rootVar.name);
//				LocalDeclaration condition = newElvisDeclaration(statements, conditionalVarName,
//						newInitExpr, localDeclaration.type, expectedType);
//				newInitExpr = newSingleNameReference(condition.name, getP(condition));
//				level = conditionalLevel;
//			} else
			level = varRef.level;
			resultArgs[resultPosition] = newInitExpr;
		} else resultArgs[resultPosition] = arg;
		return level;
	}

	private static TypeReference getParamType(
			AbstractVariableDeclaration var, MethodBinding methodBinding, int argIndex
	) throws SafeCallInternalException {
		TypeBinding[] paramTypes = methodBinding.parameters;

		TypeBinding parameterTypeBinding;
		if (methodBinding.isVarargs()) {
			int lastParam = paramTypes.length - 1;
			if (argIndex < lastParam) {
				parameterTypeBinding = paramTypes[argIndex];
			} else {
				TypeBinding type = paramTypes[lastParam];
				boolean isArray = type.isArrayType();
				if (isArray) {
					parameterTypeBinding = type.leafComponentType();
				} else {
					throw new SafeCallInternalException(var, "last method parameter is not array");
				}
			}
		} else parameterTypeBinding = paramTypes[argIndex];

		return newTypeReference(parameterTypeBinding, var);
	}

	private static boolean isLambda(Expression expr) {
		return expr != null && expr.getClass().getSimpleName().equals("LambdaExpression");
	}

	private static boolean isStaticField(QualifiedNameReference qnr) {
		Binding binding = qnr.binding;

		boolean isStatic = false;
		if (binding instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding) binding;
			isStatic = fieldBinding.isStatic();
		}
		return isStatic;
	}

	private static int getLast(VarRef varRef, int perentLevel) {
		return varRef.level < perentLevel ? perentLevel : varRef.level;
	}

	private static int verifyNotDuplicateLevel(int level, AbstractVariableDeclaration var, BlockScope rootScope) {
		int varLevel = level;
		if (var instanceof LocalDeclaration) {
			char[] baseName = var.name;
			char[] varName1 = newName(level, baseName);
			final Set<String> variableNames = getPossibleDuplicates(rootScope, var);
			while (isDuplicateLocalVariable(varName1, variableNames)) {
				varLevel++;
				if (varLevel < 0) {
					//overflow
					varLevel = level;
					baseName = (new String(baseName) + "_").toCharArray();
					varName1 = newName(varLevel, baseName);
				} else varName1 = newName(varLevel, baseName);
			}
		}
		return varLevel;
	}

	private static TypeReference newTypeReference(TypeBinding typeBinding, ASTNode ast) {
		return EclipseHandlerUtil.makeType(typeBinding, ast, false);
	}

	private static LocalDeclaration newElvisDeclaration(
			List<Statement> statements, char[] varName, Expression truePart, TypeBinding truePartType
	) throws SafeCallUnexpectedStateException {
		TypeReference truePartTypeRef = newTypeReference(truePartType, truePart);
		TypeReference falsePartTypeRef = newTypeReference(truePartType, truePart);
		return newElvisDeclaration(statements, varName, truePart, truePartTypeRef, falsePartTypeRef);
	}

	private static LocalDeclaration newElvisDeclaration(
			List<Statement> statements, char[] varName, Expression truePart,
			TypeReference truePartType,
			TypeReference falsePartType) throws SafeCallUnexpectedStateException {
		Expression elvis = newElvis(truePart, truePartType, falsePartType);
		Expression initialization = elvis != null ? elvis : truePart;
		return makeLocalDeclaration(statements, varName, initialization, falsePartType);
	}

	private static LocalDeclaration makeLocalDeclaration(
			List<Statement> statements, char[] varName, Expression expr, TypeBinding resolvedType) {
		return makeLocalDeclaration(statements, varName, expr, getType(expr, resolvedType));
	}

	private static LocalDeclaration makeLocalDeclaration(
			List<Statement> statements, char[] varName, Expression expr, TypeReference typeReference) {
		LocalDeclaration localDeclaration = newLocalDeclaration(varName, expr, typeReference);
		statements.add(localDeclaration);
		return localDeclaration;
	}

	private static boolean isDuplicateLocalVariable(
			char[] varName, Collection<String> variableNames) {

		String varNameAsString = new String(varName);
		return variableNames.contains(varNameAsString);
	}

	private static Set<String> getPossibleDuplicates(
			final Scope scope, AbstractVariableDeclaration endOn) {

		if (!(scope instanceof BlockScope)) return emptySet();

		BlockScope block = (BlockScope) scope;
		Set<String> vars = new HashSet<String>();
		findVars(vars, block, endOn);

		return vars;
	}

	private static void findVars(Set<String> result, BlockScope block, AbstractVariableDeclaration endOn) {
		for (int i = 0; i < block.localIndex; ++i) {
			LocalVariableBinding varBinding = block.locals[i];
			if (Arrays.equals(varBinding.name, endOn.name)) {
				break;
			}
			result.add(new String(varBinding.name));
		}
		Scope parent = block.parent;
		if (parent instanceof BlockScope) {
			findVars(result, (BlockScope) parent, endOn);
		}
	}

	private static boolean findVars(
			Statement[] statements, AbstractVariableDeclaration endOn, Collection<String> result) {
		for (Statement statement : statements) {
			if (statement == endOn) {
				return true;
			} else if (statement instanceof AbstractVariableDeclaration) {
				result.add(new String(((AbstractVariableDeclaration) statement).name));
			} else if (statement instanceof Block) {
				Block block = (Block) statement;
				Collection<String> blockResult = new HashSet<String>();
				boolean endReached = findVars(block.statements, endOn, blockResult);
				if (endReached) {
					result.addAll(blockResult);
					return true;
				}
			}
		}
		return false;
	}

	private static VarRef populateBlockForQNR(
			int level, char[] newName,
			QualifiedNameReference src, Expression first,
			char[] last, long pos, TypeBinding resolvedType, List<Statement> statements,
			AbstractVariableDeclaration var, BlockScope rootScope
	) throws SafeCallUnexpectedStateException, SafeCallInternalException, SafeCallIllegalUsingException {
		VarRef varRef = populateInitStatements(level + 1, var, first, statements, rootScope);
		Reference reference = varRef.getRef();
		char[] name = reference.toString().toCharArray();
		char[][] tokens = new char[][]{name, last};
		QualifiedNameReference qnr = new QualifiedNameReference(tokens, src.sourcePositions, src.sourceStart, src.sourceEnd);

		SingleNameReference reference1 = newSingleNameReference(name, pos);
		Expression elvis = newIfNullThenConditional(reference1, qnr, newNullLiteral());
		LocalDeclaration ld = newLocalDeclaration(newName, elvis, getType(elvis, resolvedType));
		statements.add(ld);
		return new VarRef(ld, varRef.level);
	}

	static SingleNameReference newSingleNameReference(char[] name, long pos) {
		SingleNameReference reference = new SingleNameReference(name, pos);
		reference.constant = NotAConstant;
		return reference;
	}

	private static LocalDeclaration newLocalDeclaration(
			char[] newName, Expression initialization, TypeReference type) {
		LocalDeclaration ld = newLD(newName, initialization);
		ld.type = type;
		return ld;
	}

	private static TypeReference getType(Expression expr, TypeBinding resolvedType) {
		TypeReference type;
		if (expr instanceof AllocationExpression) {
			type = ((AllocationExpression) expr).type;
		} else type = null;

		if (type == null) {
			QualifiedTypeReference obj = new QualifiedTypeReference(fromQualifiedName("lombok.val"), NULL_POSS);
			obj.constant = NotAConstant;

			type = (resolvedType != null) ? newTypeReference(resolvedType, expr)
					: obj;
		}
		return type;
	}

	private static LocalDeclaration newLD(char[] newName, Expression initialization) {
		LocalDeclaration ld = new LocalDeclaration(newName, initialization.sourceStart, initialization.sourceEnd);
		ld.initialization = initialization;
		return ld;
	}

	private static char[] newName(int level, char[] name) {
		return (new String(name) + level).toCharArray();
	}

	private static class VarRef {
		final LocalDeclaration var;
		final int level;

		VarRef(LocalDeclaration var, int level) {
			this.var = var;
			this.level = level;
		}

		public Reference getRef() {
			if (var != null) {
				return newSingleNameReference(var.name, getP(var));
			}
			return null;
		}
	}

}
