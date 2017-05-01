package lombok.eclipse.agent;

import lombok.core.handlers.SafeCallUnexpectedStateException;
import org.eclipse.jdt.internal.compiler.ast.*;

import java.util.Arrays;

import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.copy;
import static lombok.eclipse.agent.PatchSafeHelper.newSingleNameReference;
import static lombok.eclipse.handlers.EclipseHandlerUtil.copySourcePosition;
import static org.eclipse.jdt.internal.compiler.ast.ASTNode.IsImplicitThis;
import static org.eclipse.jdt.internal.compiler.ast.IntLiteral.buildIntLiteral;

/**
 * Created by Bulgakov Alexander on 03.01.17.
 */
public class PatchSafeCallCopyHelper {

	@SuppressWarnings("unchecked")
	static <T extends Expression> T copyOf(T expression) {
		T newRes;
		if (expression instanceof MessageSend) {
			newRes = (T) copy((MessageSend) expression);
		} else if (expression instanceof ThisReference) {
			newRes = (T) copy((ThisReference) expression);
		} else if (expression instanceof SingleNameReference) {
			newRes = (T) copy((SingleNameReference) expression);
		} else if (expression instanceof QualifiedNameReference) {
			newRes = (T) copy((QualifiedNameReference) expression);
		} else if (expression instanceof FieldReference) {
			newRes = (T) copy((FieldReference) expression);
		} else if (expression instanceof AllocationExpression) {
			newRes = (T) copy((AllocationExpression) expression);
		} else if (expression instanceof Literal) {
			newRes = (T) copy((Literal) expression);
		} else if (expression instanceof ArrayReference) {
			newRes = (T) copy((ArrayReference) expression);
		} else if (expression instanceof CastExpression) {
			newRes = (T) copy((CastExpression) expression);
		} else if (expression instanceof ConditionalExpression) {
			newRes = (T) copy((ConditionalExpression) expression);
		} else throw uoe(expression);
		newRes.constant = expression.constant;
		return newRes;
	}

	private static Expression[] copy(Expression[] expressions) {
		if (expressions == null) return null;
		Expression[] result = new Expression[expressions.length];
		for (int i = 0; i < expressions.length; ++i) {
			result[i] = copyOf(expressions[i]);
		}
		return result;
	}


	private static MessageSend copy(MessageSend from) {
		MessageSend result = new MessageSend();
		result.arguments = copy(from.arguments);
		result.selector = from.selector;
		result.receiver = copyOf(from.receiver);
		result.nameSourcePosition = from.nameSourcePosition;
		result.binding = from.binding;
		result.actualReceiverType = from.actualReceiverType;
		copySourcePosition(from, result);
		return result;
	}

	private static SafeCallUnexpectedStateException uoe(Expression expression) {
		return new SafeCallUnexpectedStateException(copy, expression, expression != null ? expression.getClass() : null);
	}

	private static ConditionalExpression copy(ConditionalExpression literal) {
		ConditionalExpression result = new ConditionalExpression(literal.condition,
				literal.valueIfTrue, literal.valueIfFalse);
		copySourcePosition(literal, result);
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

	private static StringLiteral copy(StringLiteral literal) {
		StringLiteral result = new StringLiteral(literal.source(),
				literal.sourceStart,
				literal.sourceEnd,
				0);
		copySourcePosition(literal, result);
		return result;
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
		FieldReference result = new FieldReference(parent.token, PatchSafeHelper.getP(parent));
		result.receiver = copyOf(parent.receiver);
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
		SingleNameReference result = newSingleNameReference(parent.token, PatchSafeHelper.getP(parent));
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
