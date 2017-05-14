package lombok.eclipse.agent;

import lombok.core.handlers.SafeCallUnexpectedStateException;
import org.eclipse.jdt.internal.compiler.ast.*;

import java.util.Arrays;

import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.copyExpr;
import static lombok.eclipse.agent.PatchSafeCallHelper.newSingleNameReference;
import static lombok.eclipse.handlers.EclipseHandlerUtil.copySourcePosition;
import static org.eclipse.jdt.internal.compiler.ast.ASTNode.*;
import static org.eclipse.jdt.internal.compiler.ast.IntLiteral.buildIntLiteral;

/**
 * Created by Bulgakov Alexander on 03.01.17.
 */
public class PatchSafeCallCopyHelper {

	@SuppressWarnings("unchecked")
	static <T extends Expression> T copy(T expression) {
		if (expression == null) return null;
		T newRes;
/*		if (expression instanceof AllocationExpression) {
			newRes = (T) copy((AllocationExpression) expression);
		} else if (expression instanceof ArrayInitializer) {
			newRes = (T) copy((ArrayInitializer) expression);
		} else*/
		if (expression instanceof Reference) {
			newRes = (T) copy((Reference) expression);
//		} else if (expression instanceof TypeReference) {
//			newRes = (T) copy((TypeReference) expression);
//		} else if (expression instanceof MessageSend) {
//			newRes = (T) copy((MessageSend) expression);
		} else if (expression instanceof Literal) {
			newRes = (T) copy((Literal) expression);
//		} else if (expression instanceof CastExpression) {
//			newRes = (T) copy((CastExpression) expression);
		} else if (expression instanceof OperatorExpression) {
			newRes = (T) copy((OperatorExpression) expression);
//		} else if (expression instanceof ArrayAllocationExpression) {
//			newRes = (T) copy((ArrayAllocationExpression) expression);
		} else throw uoe(expression);
		newRes.constant = expression.constant;
//		return expression;
		return newRes;
	}

	private static ArrayInitializer copy(ArrayInitializer expression
	) {
		ArrayInitializer result = new ArrayInitializer();
		result.expressions = copy(expression.expressions);
		return result;
	}

	private static ArrayAllocationExpression copy(ArrayAllocationExpression expression
	) {
		ArrayAllocationExpression result = new ArrayAllocationExpression();
		result.type = copy(expression.type);
		result.dimensions = copy(expression.dimensions);
		result.initializer = copy(expression.initializer);
		return result;
	}


	private static Reference copy(Reference reference) {
		if (reference instanceof ThisReference) {
			return copy((ThisReference) reference);
		} else if (reference instanceof ArrayReference) {
			return copy((ArrayReference) reference);
		} else if (reference instanceof SingleNameReference) {
			return copy((SingleNameReference) reference);
		} else if (reference instanceof QualifiedNameReference) {
			return copy((QualifiedNameReference) reference);
		} else if (reference instanceof FieldReference) {
			return copy((FieldReference) reference);
		} else throw uoe(reference);
	}

	private static Expression[] copy(Expression[] expressions) {
		if (expressions == null) return null;
		Expression[] result = new Expression[expressions.length];
		for (int i = 0; i < expressions.length; ++i) {
			result[i] = copy(expressions[i]);
		}
		return result;
	}

	private static MessageSend copy(MessageSend from) {
		MessageSend result = new MessageSend();
		result.arguments = copy(from.arguments);
		result.selector = from.selector;
		result.receiver = copy(from.receiver);
		result.nameSourcePosition = from.nameSourcePosition;
		result.binding = from.binding;
		result.actualReceiverType = from.actualReceiverType;
		copySourcePosition(from, result);
		return result;
	}

	private static SafeCallUnexpectedStateException uoe(Expression expression) {
		return new SafeCallUnexpectedStateException(copyExpr, expression, expression != null ?
				expression.getClass() : null);
	}

	private static OperatorExpression copy(OperatorExpression expression) {
		if (expression instanceof BinaryExpression) {
			return copy((BinaryExpression) expression);
		} else if (expression instanceof InstanceOfExpression) {
			return copy((InstanceOfExpression) expression);
		} else if (expression instanceof ConditionalExpression) {
			return copy((ConditionalExpression) expression);
		} else if (expression instanceof UnaryExpression) {
			return copy((UnaryExpression) expression);
		} else throw uoe(expression);
	}

	private static BinaryExpression copy(BinaryExpression expression) {
		BinaryExpression result = new BinaryExpression(expression.left,
				expression.right, getOperator(expression));
		copySourcePosition(expression, result);
		return result;
	}

	public static int getOperator(OperatorExpression expression) {
		return (expression.bits & OperatorMASK) >> OperatorSHIFT;
	}

	private static InstanceOfExpression copy(InstanceOfExpression expression) {
		InstanceOfExpression result = new InstanceOfExpression(expression.expression,
				expression.type);
		copySourcePosition(expression, result);
		return result;
	}

	private static ConditionalExpression copy(ConditionalExpression expression) {
		ConditionalExpression result = new ConditionalExpression(expression.condition,
				expression.valueIfTrue, expression.valueIfFalse);
		copySourcePosition(expression, result);
		return result;
	}

	private static UnaryExpression copy(UnaryExpression expression) {
		UnaryExpression result = new UnaryExpression(expression.expression, getOperator(expression));
		copySourcePosition(expression, result);
		return result;
	}

	private static CastExpression copy(CastExpression literal) {
		CastExpression result = new CastExpression(literal.expression, literal.type);
		copySourcePosition(literal, result);
		return result;
	}

	private static ArrayReference copy(ArrayReference literal) {
		ArrayReference result = new ArrayReference(literal.receiver, literal.position);
		copySourcePosition(literal, result);
		return result;
	}

	private static MagicLiteral copy(MagicLiteral literal) {
		if (literal instanceof NullLiteral) {
			return new NullLiteral(literal.sourceStart(), literal.sourceEnd());
		} else if (literal instanceof FalseLiteral) {
			return new FalseLiteral(literal.sourceStart(), literal.sourceEnd());
		} else if (literal instanceof TrueLiteral) {
			return new TrueLiteral(literal.sourceStart(), literal.sourceEnd());
		} else throw uoe(literal);

	}

	private static NumberLiteral copy(NumberLiteral literal) {
		if (literal instanceof IntLiteral) {
			return buildIntLiteral(literal.source(), literal.sourceStart(), literal.sourceEnd());
		} else if (literal instanceof LongLiteral) {
			return LongLiteral.buildLongLiteral(literal.source(), literal.sourceStart(), literal.sourceEnd());
		} else if (literal instanceof CharLiteral) {
			return new CharLiteral(literal.source(), literal.sourceStart(), literal.sourceEnd());
		} else if (literal instanceof DoubleLiteral) {
			return new DoubleLiteral(literal.source(), literal.sourceStart(), literal.sourceEnd());
		} else if (literal instanceof FloatLiteral) {
			return new FloatLiteral(literal.source(), literal.sourceStart(), literal.sourceEnd());
		} else throw uoe(literal);
	}

	private static Literal copy(Literal literal) {
		if (literal instanceof MagicLiteral) {
			return copy((MagicLiteral) literal);
		} else if (literal instanceof StringLiteral) {
			StringLiteral result = new StringLiteral(literal.source(),
					literal.sourceStart,
					literal.sourceEnd,
					0);
			copySourcePosition(literal, result);
			return result;

		} else if (literal instanceof NumberLiteral) {
			return copy((NumberLiteral) literal);
		} else throw uoe(literal);
	}

	private static AllocationExpression copy(AllocationExpression parent) {
		AllocationExpression result = new AllocationExpression();
		copySourcePosition(parent, result);
		result.type = copy(parent.type);
		return result;
	}

	private static TypeReference copy(TypeReference parent) {
		TypeReference result;
		if (parent instanceof SingleTypeReference) {
			SingleTypeReference src = (SingleTypeReference) parent;
			SingleTypeReference str = new SingleTypeReference(src.token, 0);
			copySourcePosition(parent, str);
			result = str;
		} else if (parent instanceof QualifiedTypeReference) {
			QualifiedTypeReference src = (QualifiedTypeReference) parent;
			QualifiedTypeReference qtr = new QualifiedTypeReference(src.tokens, src.sourcePositions);
			result = qtr;
		} else throw uoe(parent);
		return result;
	}

	private static FieldReference copy(FieldReference parent) {
		FieldReference result = new FieldReference(parent.token, PatchSafeCallHelper.getP(parent));
		result.receiver = copy(parent.receiver);
		result.token = parent.token;
		return result;
	}

	private static QualifiedNameReference copy(QualifiedNameReference parent) {
		char[][] original = parent.tokens;
		QualifiedNameReference result = new QualifiedNameReference(original, parent.sourcePositions,
				parent.sourceStart, parent.sourceEnd);
		result.tokens = Arrays.copyOf(original, original.length);
		return result;
	}

	private static SingleNameReference copy(SingleNameReference parent) {
		SingleNameReference result = newSingleNameReference(parent.token, PatchSafeCallHelper.getP(parent));
		result.token = parent.token;
		return result;
	}

	private static ThisReference copy(ThisReference res) {
		ThisReference result = new ThisReference(res.sourceStart, res.sourceEnd);
		boolean implicit = (res.bits & IsImplicitThis) != 0;
		if (implicit) result.bits |= IsImplicitThis;
		return result;
	}
}
