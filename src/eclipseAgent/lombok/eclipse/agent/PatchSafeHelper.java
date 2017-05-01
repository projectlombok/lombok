package lombok.eclipse.agent;

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
import static lombok.eclipse.agent.PatchSafeCallCopyHelper.copyOf;
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
	) {
		Expression result;
		Expression falsePart = getFalsePart(falsePartType);
		if (expr instanceof MessageSend) {
			MessageSend messageSend = (MessageSend) expr;
			TypeBinding checkableType = messageSend.actualReceiverType;
			boolean primitive = isPrimitive(checkableType);
			if (primitive) {
				result = newConditional(expr, expr, falsePart);
			} else {
				Expression checkable = messageSend.receiver;
				Expression truePart = expr;
				TypeBinding returnType = messageSend.binding.returnType;
				result = newElvis(checkable, truePart, falsePart, newTypeReference(returnType, expr), falsePartType);
			}
		} else if (expr instanceof SingleNameReference) {
			if (!Eclipse.isPrimitive(truePartType) && Eclipse.isPrimitive(falsePartType)) {
				result = newConditional(expr, expr, falsePart);
			} else {
				result = null;
			}
		} else if (expr instanceof QualifiedNameReference) {
			QualifiedNameReference qnr = (QualifiedNameReference) expr;
			Expression expression = newConditional(qnr, qnr, falsePart);
			char[][] tokens = qnr.tokens;
			long[] sp = qnr.sourcePositions;
			if (tokens.length == 2) {
				SingleNameReference checkable = newSingleNameReference(tokens[0], sp[0]);
				checkable.constant = NotAConstant;
				expression = newConditional(checkable, expression, falsePart);
			} else {
				char[][] parentTokens = copyOf(tokens, tokens.length - 1);
				long[] parentP = copyOf(sp, sp.length - 1);
				QualifiedNameReference reference = new QualifiedNameReference(parentTokens, parentP,
						qnr.sourceStart, qnr.sourceEnd);
				expression = newElvis(reference, truePartType, falsePartType);
			}
			result = expression;
		} else if (expr instanceof FieldReference) {
			result = newElvis(((FieldReference) expr).receiver, expr, falsePart, truePartType, falsePartType);
		} else if (expr instanceof AllocationExpression) result = null;
		else if (expr instanceof ThisReference) result = null;
		else if (expr instanceof Literal) result = null;
		else if (expr instanceof ArrayReference) {
			ArrayReference arrayReference = (ArrayReference) expr;
			Expression receiver = arrayReference.receiver;
			result = newElvis(receiver, expr, falsePart, truePartType, falsePartType);
		} else if (expr instanceof CastExpression) {
			CastExpression castExpression = ((CastExpression) expr);
			Expression casted = castExpression.expression;
			result = newElvis(casted, castExpression, falsePart, truePartType, falsePartType);
		} else if (expr == null) result = null;
		else {
			throw new SafeCallUnexpectedStateException(elvisConditional, expr, expr.getClass());
		}
		return result;
	}

	private static boolean isPrimitive(TypeBinding type) {
		return !(type.isArrayType() || type.isClass());
	}

	public static Expression getFalsePart(TypeReference typeName) {
		return Eclipse.isPrimitive(typeName) ? newDefaultValueLiterall(typeName) : newNullLiteral();
	}

	private static Expression newElvis(
			Expression checkable, Expression truePart,
			Expression falsePart,
			TypeReference exprType, TypeReference falsePartType
	) {
		if (checkable instanceof ThisReference) return null;
		if (checkable instanceof AllocationExpression) return null;

		Expression checkNullCnd = newElvis(checkable, exprType, falsePartType);
		ConditionalExpression conditional = newConditional(checkable, truePart, falsePart);
		Expression result;
		if (checkNullCnd instanceof ConditionalExpression) {
			ConditionalExpression cnd = (ConditionalExpression) checkNullCnd;
			while (cnd.valueIfTrue instanceof ConditionalExpression) {
				cnd = (ConditionalExpression) cnd.valueIfTrue;
			}
			cnd.valueIfTrue = conditional;
			result = checkNullCnd;
		} else result = conditional;
		return result;
	}

	private static ConditionalExpression newConditional(Expression checkable, Expression truePart, Expression falsePart) {
		EqualExpression checkExpr = new EqualExpression(copyOf(checkable), newNullLiteral(), NOT_EQUAL);
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

			IntLiteral zero = buildIntLiteral("0".toCharArray(), 0, 0);
			Expression position = arrayReference.position;

			EqualExpression noLessZero = new EqualExpression(copyOf(position), zero, GREATER_EQUAL);
			EqualExpression lessLength = new EqualExpression(copyOf(position), lengthCall, LESS);
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
		ConditionalExpression cexpr = new ConditionalExpression(checkExpr, copyOf(truePart), falsePart);
		copySourcePosition(truePart, cexpr);
		return cexpr;
	}

	private static Expression newNullLiteral() {
		Literal falsePart = new NullLiteral(0, 0);
		falsePart.computeConstant();
		return falsePart;
	}

	private static Expression newDefaultValueLiterall(TypeReference typeName) {
		Literal expression = getDefaultValue(typeName.getTypeName());
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
			AbstractVariableDeclaration varDecl, Expression expr, long p, BlockScope rootScope) {
		char[] name = varDecl.name;
		ArrayList<Statement> statements = new ArrayList<Statement>();

		VarRef varRef = populateInitStatements(1, varDecl, expr, statements, rootScope);
		if (varRef.var == null) return null;
		Reference childVarRef = varRef.getRef();

		Expression rhs = childVarRef;
		TypeReference varType = varDecl.type;
		if (Eclipse.isPrimitive(varType) && !Eclipse.isPrimitive(varRef.var.type)) {
			rhs = newConditional(childVarRef, childVarRef, getDefaultValue(varType.getTypeName()));
		}

		SingleNameReference lhs = newSingleNameReference(name, p);

		Assignment assignment = new Assignment(lhs, rhs, (int) p);
		statements.add(assignment);
		return statements;
	}

	private static VarRef populateInitStatements(
			final int level,
			AbstractVariableDeclaration var,
			Expression expr,
			List<Statement> statements,
			BlockScope rootScope
	) {
		int notDuplicatedLevel = verifyNotDuplicateLevel(level, var, rootScope);
		char[] varName = newName(notDuplicatedLevel, var.name);

		TypeBinding type = expr.resolvedType;

		LocalDeclaration resultVar;
		int lastLevel;
		if (expr instanceof MessageSend) {
			final MessageSend messageSend = (MessageSend) expr;
			MethodBinding methodBinding = messageSend.binding;
			if (methodBinding.isStatic()) {
				lastLevel = notDuplicatedLevel;
				resultVar = makeLocalDeclaration(statements, varName, expr, type);
			} else {
				Expression receiver = messageSend.receiver;
				VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, var, receiver,
						statements, rootScope);

				MessageSend newMessageSend = copyOf(messageSend);

				if (varRef.var != null) {
					lastLevel = varRef.level;
					newMessageSend.receiver = varRef.getRef();
				} else lastLevel = notDuplicatedLevel;

				TypeBinding exprType = methodBinding.returnType;

				resultVar = newElvisDeclaration(statements, varName, newMessageSend, exprType);
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
						type, statements, var, rootScope);
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
				VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, var, receiver,
						statements, rootScope);
				if (varRef.var != null) {
					fr.receiver = varRef.getRef();
					lastLevel = varRef.level;
				} else lastLevel = notDuplicatedLevel;
				resultVar = newElvisDeclaration(statements, varName, expr, type);
			}
		} else if (expr instanceof AllocationExpression ||
				expr instanceof ArrayAllocationExpression /*||
			    expr instanceof Literal*/) {
			resultVar = makeLocalDeclaration(statements, varName, expr, type);
			lastLevel = notDuplicatedLevel;
		} else if (expr instanceof Literal || isLambda(expr)) {
			resultVar = null;
			lastLevel = NOT_USED;
		} else if (expr instanceof ThisReference) {
			resultVar = null;
			lastLevel = NOT_USED;
		} else if (expr instanceof CastExpression) {
			CastExpression castExpression = (CastExpression) expr;
			Expression expression = castExpression.expression;
			VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, var, expression,
					statements, rootScope);

			LocalDeclaration expressionVar = varRef.var;
			if (expressionVar != null) {
				lastLevel = varRef.level;
				castExpression.expression = varRef.getRef();
				TypeReference varTypeRef = expressionVar.type;
				boolean primitive = Eclipse.isPrimitive(varTypeRef);
				resultVar = !primitive
						? newElvisDeclaration(statements, varName, copyOf(castExpression), type)
						: makeLocalDeclaration(statements, varName, copyOf(castExpression), type);
			} else {
				lastLevel = notDuplicatedLevel;
				resultVar = makeLocalDeclaration(statements, varName, copyOf(castExpression), type);
			}
		} else if (expr instanceof ArrayReference) {
			ArrayReference arrayReference = ((ArrayReference) expr);
			Expression position = arrayReference.position;
			VarRef positionVarRef = populateInitStatements(notDuplicatedLevel + 1, var, position,
					statements, rootScope);
			lastLevel = getLast(positionVarRef, notDuplicatedLevel);
			if (positionVarRef.var != null) {
				TypeReference positionType = positionVarRef.var.type;
				boolean primitive = Eclipse.isPrimitive(positionType);
				Reference ref = positionVarRef.getRef();
				if (primitive) arrayReference.position = ref;
				else {
					lastLevel = verifyNotDuplicateLevel(++lastLevel, var, rootScope);
					char[] positionVarName = newName(lastLevel, var.name);
					TypeReference falsePartType = baseTypeReference(T_int, 0);
					LocalDeclaration positionVar = newElvisDeclaration(statements, positionVarName,
							ref, positionType, falsePartType);
					positionVar.type = baseTypeReference(T_int, 0);
					arrayReference.position = newSingleNameReference(positionVar.name, getP(positionVar));
				}
			}
			Expression receiver = arrayReference.receiver;
			VarRef receiverVarRef = populateInitStatements(lastLevel + 1, var, receiver,
					statements, rootScope);
			arrayReference.receiver = receiverVarRef.getRef();
			resultVar = newElvisDeclaration(statements, varName, arrayReference, type);
			lastLevel = getLast(receiverVarRef, notDuplicatedLevel);
		} else {
			throw new SafeCallUnexpectedStateException(populateInitStatements, expr, expr.getClass());
		}

		if (resultVar != null) {
		}

		return new VarRef(resultVar, lastLevel);
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
			List<Statement> statements, char[] varName, Expression truePart, TypeBinding truePartType) {
		TypeReference truePartTypeRef = newTypeReference(truePartType, truePart);
		TypeReference falsePartTypeRef = newTypeReference(truePartType, truePart);
		return newElvisDeclaration(statements, varName, truePart, truePartTypeRef, falsePartTypeRef);
	}

	private static LocalDeclaration newElvisDeclaration(
			List<Statement> statements, char[] varName, Expression truePart,
			TypeReference truePartType,
			TypeReference falsePartType) {
		LocalDeclaration localDeclaration = newElvisLocalDeclaration(varName, truePart, truePartType, falsePartType);
		statements.add(localDeclaration);
		return localDeclaration;
	}

	private static LocalDeclaration makeLocalDeclaration(
			List<Statement> statements, char[] varName, Expression expr, TypeBinding resolvedType) {
		LocalDeclaration localDeclaration = newLocalDeclaration(varName, expr, resolvedType);
		statements.add(localDeclaration);
		return localDeclaration;
	}

	private static LocalDeclaration newElvisLocalDeclaration(
			char[] varName, Expression expression,
			TypeReference truePartType, TypeReference falsePartType) {
		Expression elvis = newElvis(expression, truePartType, falsePartType);
		Expression expr = elvis != null ? elvis : expression;
		return newLocalDeclaration(varName, expr, truePartType);
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
			AbstractVariableDeclaration var, BlockScope rootScope) {
		VarRef varRef = populateInitStatements(level + 1, var, first, statements, rootScope);
		Reference reference = varRef.getRef();
		char[] name = reference.toString().toCharArray();
		char[][] tokens = new char[][]{name, last};
		QualifiedNameReference qnr = new QualifiedNameReference(tokens, src.sourcePositions, src.sourceStart, src.sourceEnd);

		SingleNameReference reference1 = newSingleNameReference(name, pos);
		Expression elvis = newConditional(reference1, qnr, newNullLiteral());
		LocalDeclaration ld = newLocalDeclaration(newName, elvis, resolvedType);
		statements.add(ld);
		return new VarRef(ld, varRef.level);
	}

	static SingleNameReference newSingleNameReference(char[] name, long pos) {
		SingleNameReference reference = new SingleNameReference(name, pos);
		reference.constant = NotAConstant;
		return reference;
	}

	private static LocalDeclaration newLocalDeclaration(
			char[] newName, Expression expr, TypeBinding resolvedType
	) {
		return newLocalDeclaration(newName, expr, getType(expr, resolvedType));
	}

	private static LocalDeclaration newLocalDeclaration(char[] newName, Expression expr, TypeReference type) {
		LocalDeclaration ld = newLD(newName, expr);
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

	private static LocalDeclaration newLD(char[] newName, Expression expr) {
		LocalDeclaration ld = new LocalDeclaration(newName, expr.sourceStart, expr.sourceEnd);
		ld.initialization = expr;
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
