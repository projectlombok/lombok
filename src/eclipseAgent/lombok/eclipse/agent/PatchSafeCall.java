package lombok.eclipse.agent;

import lombok.core.handlers.SafeCallAbortProcessing;
import lombok.core.handlers.SafeCallIllegalUsingException;
import lombok.core.handlers.SafeCallInternalException;
import lombok.core.handlers.SafeCallUnexpectedStateException;
import lombok.experimental.SafeCall;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

import java.util.ArrayList;

import static lombok.eclipse.EclipseAstProblemView.addProblemToCompilationResult;
import static lombok.eclipse.agent.PatchSafeCallHelper.*;
import static lombok.eclipse.agent.PatchVal.is;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccFinal;
import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.AccBlankFinal;


/**
 * Created by Bulgakov Alexander on 28.12.16.
 */
public class PatchSafeCall {

	/**
	 * run after Block.resolve
	 *
	 * @param block
	 * @param upperScope
	 */
	public static void handleSafe(Block block, BlockScope upperScope) {
		ASTNode _var = getVarByBlock(block);
		if (!(_var instanceof AbstractVariableDeclaration)) return;
		AbstractVariableDeclaration var = (AbstractVariableDeclaration) _var;
		if (isSafe(var, upperScope)) {
			final long p = getP(var);
			Expression expression = var.initialization;
			boolean hasTypeError = expression.resolvedType == null;
			if (!hasTypeError) {
				ArrayList<Statement> statements = null;
				try {
					try {
						statements = newInitStatements(var, expression, p, upperScope);
					} catch (SafeCallUnexpectedStateException e) {
						addError(upperScope, (ASTNode) e.getNode(), e.getMessage());
					} catch (SafeCallInternalException e) {
						addError(upperScope, (ASTNode) e.getNode(), e.getMessage());
					} catch (SafeCallIllegalUsingException e) {
						addError(upperScope, (ASTNode) e.getNode(), e.getMessage());
					} catch (SafeCallAbortProcessing e) {
						addWarning(upperScope, (ASTNode) e.getNode(), e.getMessage());
					}
				} catch (AbortCompilation ae) {
					return;
				}

				if (statements != null) {
					boolean isFinal = (var.modifiers & AccFinal) != 0;
					if (isFinal) {
						if (var instanceof LocalDeclaration) {
							LocalDeclaration ld = (LocalDeclaration) var;
							LocalVariableBinding binding = ld.binding;
							binding.modifiers |= AccBlankFinal;
						} else throw new UnsupportedOperationException(var.getClass().toString());
					}

					block.statements = statements.toArray(new Statement[statements.size()]);
					BlockScope blockScope = block.scope;
					for (Statement statement : statements) {
						statement.resolve(blockScope);
					}
					var.initialization = null;
				}
			}
		}
	}

	private static void addWarning(BlockScope upperScope, ASTNode node, String message) {
		ProblemReporter problemReporter = upperScope.problemReporter();
		ReferenceContext referenceContext = problemReporter.referenceContext;
		char[] fileName = referenceContext.getCompilationUnitDeclaration().getFileName();
		CompilationResult compilationResult = referenceContext.compilationResult();
		addProblemToCompilationResult(fileName, compilationResult,
				true, message, node.sourceStart, node.sourceEnd);
	}

	private static void addError(BlockScope upperScope, ASTNode node, String message) {
		ProblemReporter problemReporter = upperScope.problemReporter();
		if (node != null) problemReporter.abortDueToInternalError(message, node);
		else problemReporter.abortDueToInternalError(message);
	}

	private static boolean isSafe(AbstractVariableDeclaration local, BlockScope scope) {
		Annotation[] annotations = local.annotations;
		if (annotations != null) for (Annotation annotation : annotations) {
			if (is(annotation.type, scope, SafeCall.class.getName())) {
				return true;
			}
		}
		return false;
	}
}
