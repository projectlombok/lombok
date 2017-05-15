package lombok.eclipse.agent;

import lombok.core.handlers.SafeCallAbortProcessing;
import lombok.core.handlers.SafeCallIllegalUsingException;
import lombok.core.handlers.SafeCallInternalException;
import lombok.core.handlers.SafeCallUnexpectedStateException;
import lombok.eclipse.Eclipse;
import lombok.eclipse.handlers.EclipseHandlerUtil;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

import java.util.*;

import static java.util.Arrays.copyOf;
import static java.util.Collections.emptySet;
import static lombok.core.handlers.SafeCallAbortProcessing.Place.fieldErrorType;
import static lombok.core.handlers.SafeCallAbortProcessing.Place.methodErrorType;
import static lombok.core.handlers.SafeCallIllegalUsingException.Place.unsupportedExpression;
import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.*;
import static lombok.eclipse.Eclipse.fromQualifiedName;
import static lombok.eclipse.EclipseAugments.ASTNode_parentNode;
import static lombok.eclipse.agent.PatchSafeCallCopyHelper.copy;
import static lombok.eclipse.agent.PatchSafeCallCopyHelper.getOperator;
import static lombok.eclipse.handlers.EclipseHandlerUtil.copySourcePosition;
import static org.eclipse.jdt.core.compiler.IProblem.NonStaticAccessToStaticField;
import static org.eclipse.jdt.core.compiler.IProblem.NonStaticAccessToStaticMethod;
import static org.eclipse.jdt.internal.compiler.ast.IntLiteral.buildIntLiteral;
import static org.eclipse.jdt.internal.compiler.ast.OperatorIds.*;
import static org.eclipse.jdt.internal.compiler.ast.TypeReference.baseTypeReference;
import static org.eclipse.jdt.internal.compiler.impl.Constant.NotAConstant;
import static org.eclipse.jdt.internal.compiler.lookup.TypeConstants.*;
import static org.eclipse.jdt.internal.compiler.lookup.TypeIds.T_int;

/**
 * Created by Bulgakov Alexander on 21.12.16.
 */
final class PatchSafeCallHelper {

	public static final int NOT_USED = -1;
	private static final long[] NULL_POSS = {0L};

	private PatchSafeCallHelper() {
	}

	static ASTNode getVarByBlock(Block block) {
		return ASTNode_parentNode.get(block);
	}

	private static Expression newElvis(
			Expression expr,
			TypeReference truePartType,
			TypeReference falsePartType
	) {
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
			Expression expression = expr;//newIfNullThenConditional(qnr, qnr, falsePart);
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

	private static Expression newIfNullThenConditional(
			Expression checkable, Expression truePart, Expression falsePart
	) {
		if (checkable instanceof ThisReference) {
			return null;
		}

		final Expression result;

		final EqualExpression baseCheck = new EqualExpression(copy(checkable), newNullLiteral(), NOT_EQUAL);
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

			Constant constant = getConstant(position);
			if (constant != null) {
				int value = constant.intValue();
				if (value < 0) {
					result = new FalseLiteral(position.statementEnd, position.statementEnd);
				} else {
					result = newAnd(baseCheck, newLess(position, lengthCall));
				}
			} else {
				result = newAnd(baseCheck, newIndexRange(lengthCall, position));
			}
		} else {
			result = baseCheck;
		}
		copySourcePosition(checkable, result);
		return newConditional(result, truePart, falsePart);
	}

	private static AND_AND_Expression newAnd(Expression left, BinaryExpression right) {
		return new AND_AND_Expression(left, right, AND_AND);
	}

	private static Constant getConstant(Expression position) {
		return (position instanceof IntLiteral || position instanceof UnaryExpression) ?
				position.constant : null;
	}

	private static AND_AND_Expression newIndexRange(QualifiedNameReference lengthCall, Expression position) {
		return newAnd(newGE(position), newLess(position, lengthCall));
	}

	private static BinaryExpression newGE(Expression position) {
		return new BinaryExpression(copy(position), newZeroLiteral(), GREATER_EQUAL);
	}

	private static BinaryExpression newLess(Expression left, Expression rigth) {
		return new BinaryExpression(copy(left), rigth, LESS);
	}

	private static IntLiteral newZeroLiteral() {
		return buildIntLiteral("0".toCharArray(), 0, 0);
	}

	private static Expression newConditional(Expression condition, Expression truePart, Expression falsePart) {
		if (condition instanceof FalseLiteral) return falsePart;
		if (condition instanceof TrueLiteral) return truePart;
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
	) {
		char[] name = varDecl.name;
		ArrayList<Statement> statements = new ArrayList<Statement>();

		VarRef varRef = populateInitStatements(1, varDecl, expr, statements, rootScope);
		if (varRef.var == null) return null;

		boolean removeOnlyOneStatement = statements.size() == 1;

		Expression rhs = varRef.getRef();

		TypeReference varType = varDecl.type;
		if (mustBeProtected(varType, varRef.var.type)) {
			rhs = newIfNullThenConditional(rhs, rhs, newDefaultValueLiterall(varType));
			removeOnlyOneStatement = false;
		}

		if (removeOnlyOneStatement) return null;

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
	) {
		int notDuplicatedLevel = verifyNotDuplicateLevel(level, rootVar, rootScope);
		char[] templateName = rootVar.name;
		char[] varName = newName(notDuplicatedLevel, templateName);

		TypeBinding type = expr.resolvedType;

		LocalDeclaration resultVar;
		Expression resultRef = null;
		int lastVarLevel;
		if (expr instanceof MessageSend) {
			final MessageSend messageSend = (MessageSend) expr;

			Expression[] arguments = messageSend.arguments;

			MethodBinding methodBinding = messageSend.binding;
			if (methodBinding == null) {
				throw new SafeCallAbortProcessing(methodErrorType, expr);
			}

			Expression[] resultArguments = arguments != null ? new Expression[arguments.length] : null;
			lastVarLevel = populateMethodCallArgs(rootVar, arguments, resultArguments,
					methodBinding, notDuplicatedLevel, statements,
					rootScope);

			Expression receiver;
			TypeBinding exprType;

			if (methodBinding.isStatic()) {
				removeNonStaticAccessToStaticElement(rootVar, rootScope);

				exprType = type;
				MessageSend resultMessageSend = newMessageSend(messageSend, resultArguments,
						newQnrForClass(methodBinding.declaringClass, expr));
				resultVar = makeLocalDeclaration(statements, varName, resultMessageSend, exprType);
			} else {
				VarRef varRef = populateInitStatements(lastVarLevel + 1, rootVar, messageSend.receiver,
						statements, rootScope);
				final Expression ref = varRef.getRef();
				if (ref != null) {
					lastVarLevel = varRef.level != NOT_USED ? varRef.level : lastVarLevel;
					receiver = ref;
				} else receiver = messageSend.receiver;

				exprType = methodBinding.returnType;
				MessageSend resultMessageSend = newMessageSend(messageSend, resultArguments, receiver);
				resultVar = makeElvisDeclaration(statements, varName, resultMessageSend, exprType);
			}

		} else if (expr instanceof SingleNameReference) {
			lastVarLevel = notDuplicatedLevel;
			resultVar = makeLocalDeclaration(statements, varName, expr, type);
		} else if (expr instanceof QualifiedNameReference) {
			QualifiedNameReference qnr = (QualifiedNameReference) expr;
			List<VariableBinding> bindings = getBindings(qnr);
			long[] sourcePositions = qnr.sourcePositions;
			int delta = sourcePositions.length - bindings.size();

			FieldBinding firstStatic = null;

			int staticIndex = -1;
			for (int i = bindings.size() - 1; i >= 0; i--) {
				VariableBinding binding = bindings.get(i);
				boolean isStatic = binding instanceof FieldBinding && ((FieldBinding) binding).isStatic();
				if (isStatic) {
					removeNonStaticAccessToStaticElement(rootVar, rootScope);
					firstStatic = (FieldBinding) binding;
					staticIndex = i;
					break;
				}
			}

			int firstIndex;
			int parentPositionIndex;
			final LocalDeclaration firstVar;
			if (firstStatic != null) {
				parentPositionIndex = staticIndex + delta;
				firstVar = makeLocalDeclarationForFieldReference(statements, varName, firstStatic.type,
						firstStatic, firstStatic.name, sourcePositions[parentPositionIndex]);
				firstIndex = staticIndex;
			} else {
				parentPositionIndex = delta;
				firstIndex = 0;
				VariableBinding binding = bindings.get(firstIndex);
				SingleNameReference firstRef = new SingleNameReference(
						binding.name, sourcePositions[parentPositionIndex]);
				firstVar = makeLocalDeclaration(statements, varName, firstRef, binding.type);
			}
			resultVar = firstVar;
			lastVarLevel = notDuplicatedLevel;

			long parentPosition = sourcePositions[parentPositionIndex];
			for (int i = firstIndex + 1; i < bindings.size(); ++i) {
				VariableBinding binding = bindings.get(i);
				char[] name = binding.name;
				long varPosition = sourcePositions[delta + i];
				long[] position = new long[]{parentPosition, varPosition};

				char[][] newQnrTokens = new char[][]{firstVar.name, name};
				QualifiedNameReference qnrRef = new QualifiedNameReference(
						newQnrTokens, position, qnr.sourceStart, qnr.sourceEnd);
				lastVarLevel = verifyNotDuplicateLevel(lastVarLevel + 1, rootVar, rootScope);
				char[] qnrVarName = newName(lastVarLevel, templateName);
				TypeBinding varType = binding.type;
				resultVar = makeElvisDeclaration(statements, qnrVarName, qnrRef, varType);
				parentPosition = varPosition;

			}

		} else if (expr instanceof FieldReference) {
			FieldReference fr = (FieldReference) expr;
			FieldBinding binding = fr.binding;
			if (binding == null) {
				throw new SafeCallAbortProcessing(fieldErrorType, expr);
			}
			boolean staticField = binding.isStatic();
			if (staticField) {
				removeNonStaticAccessToStaticElement(rootVar, rootScope);
				lastVarLevel = notDuplicatedLevel;
				resultVar = makeLocalDeclarationForFieldReference(statements, varName, type, binding, fr.token, getP(fr));
			} else {
				Expression receiver = fr.receiver;
				VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, receiver,
						statements, rootScope);
				FieldReference result = new FieldReference(fr.token, getP(fr));
				//result.implicitConversion = fr.implicitConversion;
				Expression ref = varRef.getRef();
				if (ref != null) {
					result.receiver = ref;
					lastVarLevel = varRef.level != NOT_USED ? varRef.level : notDuplicatedLevel;
				} else {
					lastVarLevel = notDuplicatedLevel;
					result.receiver = receiver;
				}
				resultVar = makeElvisDeclaration(statements, varName, result, type);
			}
		} else if (expr instanceof AllocationExpression /*||
			    expr instanceof Literal*/) {
			resultVar = makeLocalDeclaration(statements, varName, expr, type);
			lastVarLevel = notDuplicatedLevel;
		} else if (expr instanceof ArrayAllocationExpression) {
			lastVarLevel = notDuplicatedLevel;
			ArrayAllocationExpression arrayAllocation = ((ArrayAllocationExpression) expr);
			ArrayInitializer initializer = arrayAllocation.initializer;
			ArrayAllocationExpression resultArrayAlloc = new ArrayAllocationExpression();
			resultArrayAlloc.type = arrayAllocation.type;
			if (initializer != null) {
				Expression[] args = initializer.expressions;
				Expression[] resulsArgs = args != null ? new Expression[args.length] : null;
				lastVarLevel = populateArrayInitializer(rootVar, args, resulsArgs, arrayAllocation.type,
						lastVarLevel, statements, rootScope);
				ArrayInitializer resultInitializer = new ArrayInitializer();
				resultInitializer.expressions = resulsArgs;
				resultArrayAlloc.initializer = resultInitializer;
			}

			Expression[] dimensions = arrayAllocation.dimensions;
			if (dimensions != null && dimensions.length > 0) {
				Expression[] resultDimensions = new Expression[dimensions.length];
				lastVarLevel = populateArrayDimensions(rootVar, dimensions, resultDimensions,
						lastVarLevel, statements, rootScope);
				resultArrayAlloc.dimensions = resultDimensions;
			} else resultArrayAlloc.dimensions = dimensions;

			resultVar = makeLocalDeclaration(statements, varName, resultArrayAlloc, type);
		} else if (expr instanceof Literal || isLambda(expr)) {
			expr.resolvedType = null;
			resultVar = null;
			lastVarLevel = NOT_USED;
		} else if (expr instanceof ThisReference) {
			resultVar = null;
			ThisReference thisReference = (ThisReference) expr;
			ThisReference result = new ThisReference(thisReference.sourceStart, thisReference.sourceEnd);
			result.bits = thisReference.bits;
			resultRef = result;
			lastVarLevel = NOT_USED;
		} else if (expr instanceof CastExpression) {
			CastExpression castExpression = ((CastExpression) expr);

			Expression expression = castExpression.expression;
			VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, expression,
					statements, rootScope);

			LocalDeclaration expressionVar = varRef.var;
			if (expressionVar != null) {
				lastVarLevel = varRef.level;
				Expression reference = varRef.getRef();
				CastExpression resultCastExpression = new CastExpression(reference, castExpression.type);
				TypeReference varTypeRef = expressionVar.type;
				boolean primitive = Eclipse.isPrimitive(varTypeRef);
				resultVar = !primitive
						? makeElvisDeclaration(statements, varName, (resultCastExpression), type)
						: makeLocalDeclaration(statements, varName, (resultCastExpression), type);
			} else {
				lastVarLevel = notDuplicatedLevel;
				CastExpression resultExpression = new CastExpression(expression, castExpression.type);
				resultVar = makeLocalDeclaration(statements, varName, resultExpression, type);
			}
		} else if (expr instanceof ArrayReference) {
			ArrayReference arrayReference = ((ArrayReference) expr);
			Expression position = arrayReference.position;
			VarRef positionVarRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, position,
					statements, rootScope);
			lastVarLevel = getLast(positionVarRef, notDuplicatedLevel);
			if (positionVarRef.var != null) {
				TypeReference positionType = positionVarRef.var.type;
				boolean primitive = Eclipse.isPrimitive(positionType);
				Expression ref = positionVarRef.getRef();
				if (primitive) arrayReference.position = ref;
				else {
					lastVarLevel = verifyNotDuplicateLevel(++lastVarLevel, rootVar, rootScope);
					char[] positionVarName = newName(lastVarLevel, templateName);
					TypeReference falsePartType = getIntTypeRef();
					LocalDeclaration positionVar = newElvisDeclaration(statements, positionVarName,
							ref, positionType, falsePartType);
					positionVar.type = getIntTypeRef();
					arrayReference.position = newSingleNameReference(positionVar.name, getP(positionVar));
				}
			}
			Expression receiver = arrayReference.receiver;
			VarRef receiverVarRef = populateInitStatements(lastVarLevel + 1, rootVar, receiver,
					statements, rootScope);
			arrayReference.receiver = receiverVarRef.getRef();
			resultVar = makeElvisDeclaration(statements, varName, arrayReference, type);
			lastVarLevel = getLast(receiverVarRef, notDuplicatedLevel);
		} else if (expr instanceof UnaryExpression) {
			final UnaryExpression unaryExpression = (UnaryExpression) expr;

			Expression expression = unaryExpression.expression;
			if (expression instanceof Literal) {
				resultVar = null;
				lastVarLevel = NOT_USED;
//				lastVarLevel = notDuplicatedLevel;
//				resultVar = makeLocalDeclaration(statements, varName, unaryExpression, type);
			} else {
				VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, expression,
						statements, rootScope);
				if (varRef.var != null) {
					varRef = protectIfPrimitive(varRef, expr, type, rootVar, rootScope, statements);
					lastVarLevel = varRef.level;
					UnaryExpression resultExpression = new UnaryExpression(varRef.getRef(), getOperator(unaryExpression));
					resultVar = makeLocalDeclaration(statements, varName, resultExpression, type);
				} else {
					lastVarLevel = notDuplicatedLevel;
					resultVar = makeLocalDeclaration(statements, varName, unaryExpression, type);
				}
			}
		} else if (expr instanceof PostfixExpression || expr instanceof PrefixExpression) {
			CompoundAssignment pe = (CompoundAssignment) expr;
			Expression lhs = pe.lhs;

			VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, rootVar, lhs,
					statements, rootScope);
			if (varRef.var != null) {
				TypeBinding opType = pe.expression.resolvedType;
				varRef = protectIfPrimitive(varRef, expr, opType, rootVar, rootScope, statements);
				lastVarLevel = varRef.level;
				CompoundAssignment resultExpression = pe;// new PostfixExpression(varRef.getRef(), getOperator(unaryExpression));
				resultExpression.lhs = varRef.getRef();
				resultVar = makeLocalDeclaration(statements, varName, resultExpression, opType);
			} else {
				lastVarLevel = notDuplicatedLevel;
				resultVar = makeLocalDeclaration(statements, varName, pe, type);
			}
		} else {
			throw new SafeCallIllegalUsingException(unsupportedExpression, expr);
		}
		if ((expr.implicitConversion & TypeIds.UNBOXING) != 0) {
			expr.implicitConversion ^= TypeIds.UNBOXING;
		}
		return resultRef != null ? new VarRef(resultRef, lastVarLevel) : new VarRef(resultVar, lastVarLevel);
	}

	private static void removeNonStaticAccessToStaticElement(AbstractVariableDeclaration rootVar, BlockScope rootScope) {
		CompilationResult compilationResult = getCompilationResult(rootScope);
		CategorizedProblem[] problems = compilationResult.problems;
		if (problems != null) {
			CategorizedProblem[] newProblems = new CategorizedProblem[problems.length];
			int index = 0;
			for (CategorizedProblem problem : problems) {
				boolean remove = problem != null &&
						(problem.getID() == NonStaticAccessToStaticMethod ||
								problem.getID() == NonStaticAccessToStaticField) &&
						rootVar.declarationSourceStart <= problem.getSourceStart() &&
						rootVar.declarationSourceEnd >= problem.getSourceEnd();
				if (!remove) {
					newProblems[index++] = problem;
				} else {
					compilationResult.removeProblem(problem);
				}
			}
			compilationResult.problems = newProblems;
		}
	}

	private static CompilationResult getCompilationResult(BlockScope rootScope) {
		return rootScope.problemReporter().referenceContext.getCompilationUnitDeclaration().compilationResult;
	}

	private static LocalDeclaration makeLocalDeclarationForFieldReference(
			List<Statement> statements, char[] varName, TypeBinding type, FieldBinding fb, char[] fieldName, long pos) {
		ReferenceBinding clazz = fb.declaringClass;
		FieldReference result = new FieldReference(fieldName, pos);
		result.receiver = newQnrForClass(clazz, result);
		return makeLocalDeclaration(statements, varName, result, type);
	}

	private static QualifiedNameReference newQnrForClass(ReferenceBinding clazz, ASTNode pos) {
		long p = getP(pos);
		long[] positions = new long[clazz.compoundName.length];
		Arrays.fill(positions, p);
		return new QualifiedNameReference(clazz.compoundName, positions, pos.sourceStart, pos.sourceEnd);
	}

	private static MessageSend newMessageSend(
			MessageSend src, Expression[] resultArguments, Expression receiver
	) {
		MessageSend resultMessageSend = new MessageSend();
		resultMessageSend.receiver = receiver;
		resultMessageSend.arguments = resultArguments;
		resultMessageSend.selector = src.selector;
		resultMessageSend.nameSourcePosition = src.nameSourcePosition;
		resultMessageSend.typeArguments = (src.typeArguments);
		copySourcePosition(src, resultMessageSend);
		return resultMessageSend;
	}

	private static VarRef protectIfPrimitive(
			VarRef varRef, Expression expr, TypeBinding expectedType,
			AbstractVariableDeclaration rootVar, BlockScope rootScope, List<Statement> statements
	) {
		return protectIfPrimitive(varRef, getType(expr, expectedType), rootVar, rootScope, statements);
	}

	private static VarRef protectIfPrimitive(
			VarRef varRef, TypeReference expectedType,
			AbstractVariableDeclaration rootVar, BlockScope rootScope, List<Statement> statements
	) {
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
	) {
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
				final Expression condition;
				final Expression baseCnd = newConditional(newGE(resultArg), resultArg, newZeroLiteral());
				final Constant constant = getConstant(resultArg);
				if (constant != null) {
					int value = constant.intValue();
					if (value < 0) {
						condition = newZeroLiteral();
					} else {
						condition = resultArg;
					}
				} else {
					condition = baseCnd;
				}
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
	) {
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
	) {
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
	) {
		VarRef varRef = populateInitStatements(level + 1, rootVar, arg,
				statements, rootScope);
		Expression newInitExpr;
		LocalDeclaration localDeclaration = varRef.var;
		if (localDeclaration != null) {
			varRef = protectIfPrimitive(varRef, expectedType, rootVar, rootScope, statements);
			newInitExpr = varRef.getRef();
			level = varRef.level;
			resultArgs[resultPosition] = newInitExpr;
		} else resultArgs[resultPosition] = arg;
		return level;
	}

	private static TypeReference getParamType(
			AbstractVariableDeclaration var, MethodBinding methodBinding, int argIndex
	) {
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

	private static List<VariableBinding> getBindings(QualifiedNameReference qnr) {
		final List<VariableBinding> result = new ArrayList<VariableBinding>();
		if (qnr.binding instanceof VariableBinding) {
			result.add((VariableBinding) qnr.binding);
		} else if (qnr.binding == null) {
			throw new SafeCallInternalException(qnr, "binding is null");
		} else throw new SafeCallUnexpectedStateException(getBindings, qnr, qnr.binding.getClass());

		FieldBinding[] otherBindings = qnr.otherBindings;
		if (otherBindings != null && otherBindings.length > 0) {
			result.addAll(Arrays.asList(otherBindings));
		}

		return result;
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

	private static LocalDeclaration makeElvisDeclaration(
			List<Statement> statements, char[] varName, Expression truePart, TypeBinding truePartType
	) {
		TypeReference truePartTypeRef = newTypeReference(truePartType, truePart);
		TypeReference falsePartTypeRef = newTypeReference(truePartType, truePart);
		return newElvisDeclaration(statements, varName, truePart, truePartTypeRef, falsePartTypeRef);
	}

	private static LocalDeclaration newElvisDeclaration(
			List<Statement> statements, char[] varName, Expression truePart,
			TypeReference truePartType,
			TypeReference falsePartType) {
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
		final Expression refExpression;
		final int level;

		VarRef(LocalDeclaration var, int level) {
			this.var = var;
			this.level = level;
			refExpression = null;
		}

		public VarRef(Expression refExpression, int level) {
			this.refExpression = refExpression;
			var = null;
			this.level = level;
		}

		public Expression getRef() {
			if (refExpression != null) return refExpression;
			else if (var != null) {
				return newSingleNameReference(var.name, getP(var));
			} else return null;
		}
	}
}
