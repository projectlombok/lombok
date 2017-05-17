package lombok.javac.handlers;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.handlers.SafeCallAbortProcessing;
import lombok.core.handlers.SafeCallIllegalUsingException;
import lombok.core.handlers.SafeCallIllegalUsingException.Place;
import lombok.core.handlers.SafeCallInternalException;
import lombok.core.handlers.SafeCallUnexpectedStateException;
import lombok.experimental.SafeCall;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution.TypeNotConvertibleException;
import org.mangosdk.spi.ProviderFor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static lombok.core.AST.Kind.FIELD;
import static lombok.core.AST.Kind.LOCAL;
import static lombok.core.handlers.SafeCallIllegalUsingException.Place.*;
import static lombok.core.handlers.SafeCallIllegalUsingException.unsupportedPlaceMessage;
import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.addBlockAfterVarDec;
import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.getParent;
import static lombok.javac.handlers.HandleSafeCallHelper.newInitBlock;
import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;

@ProviderFor(JavacAnnotationHandler.class)
@HandlerPriority(65536 + 1) // before val
public class HandleSafeCall extends JavacAnnotationHandler<SafeCall> {

	@Override
	public void handle(AnnotationValues<SafeCall> annotation, JCAnnotation ast, JavacNode annotationNode) {

		deleteAnnotationIfNeccessary(annotationNode, SafeCall.class);

		JavacNode varNode = annotationNode.up();
		Kind k = varNode.getKind();
		boolean supportedNode = LOCAL == k || FIELD == k;
		if (!supportedNode) {
			annotationNode.addError("'" + SafeCall.class.getSimpleName() + "' is supported only for local variables or class fields");
			return;
		}

		JCVariableDecl varDecl = (JCVariableDecl) varNode.get();

		Place illegalPlace = checkIllegalUsing(varNode, varDecl);

		if (illegalPlace != null) {
			annotationNode.addError(unsupportedPlaceMessage(illegalPlace));
			return;
		}

		if (varDecl.init == null) {
			return;
		}

		try {
			JCBlock initBlock = newInitBlock(varDecl, annotationNode);
			if (initBlock != null) {
				Tree parent = getParentJCNode(varNode);
				addBlockAfterVarDec(varDecl, initBlock, parent);
				varDecl.init = null;
				annotationNode.getAst().setChanged();
			}
		} catch (SafeCallIllegalUsingException e) {
			annotationNode.addError(e.getMessage());
		} catch (SafeCallUnexpectedStateException e) {
			annotationNode.addError(e.getMessage());
		} catch (SafeCallInternalException e) {
			annotationNode.addError(e.getMessage());
		} catch (TypeNotConvertibleException e) {
			//error already must be printed
		} catch (SafeCallAbortProcessing e) {
			annotationNode.addWarning(e.getMessage());
		}
	}

	private Place checkIllegalUsing(JavacNode varNode, JCTree.JCVariableDecl varDecl) {
		JCTree varParent = varNode.up().get();
		Place illegalPlace = null;
		if (varParent instanceof JCTree.JCEnhancedForLoop && ((JCTree.JCEnhancedForLoop) varParent).var == varDecl) {
			illegalPlace = forLoopVariable;
		} else if (varParent instanceof JCTree.JCForLoop) {
			com.sun.tools.javac.util.List<JCStatement> initializer = ((JCTree.JCForLoop) varParent).getInitializer();
			if (initializer != null && initializer.contains(varDecl)) illegalPlace = forLoopInitializer;
		} else if (varParent instanceof JCTry) {
			Collection<JCTree> resources = getResources((JCTry) varParent);
			if (resources != null && resources.contains(varDecl)) {
				illegalPlace = tryResource;
			}
		}
		return illegalPlace;
	}

	private void addBlockAfterVarDec(JCVariableDecl varDecl, JCBlock initBlock, Tree parent) {
		if (parent instanceof JCMethodDecl) {
			JCMethodDecl method = (JCMethodDecl) parent;
			JCBlock body = method.getBody();
			body.stats = HandleSafeCallHelper.addBlockAfterVarDec(varDecl, initBlock, body.stats);
		} else if (parent instanceof JCClassDecl) {
			JCClassDecl clazz = (JCClassDecl) parent;
			clazz.defs = HandleSafeCallHelper.addBlockAfterVarDec(varDecl, initBlock, clazz.defs);
		} else if (parent instanceof JCBlock) {
			JCBlock block = (JCBlock) parent;
			block.stats = HandleSafeCallHelper.addBlockAfterVarDec(varDecl, initBlock, block.stats);
		} else if (parent instanceof JCCase) {
			JCCase jcCase = (JCCase) parent;
			jcCase.stats = HandleSafeCallHelper.addBlockAfterVarDec(varDecl, initBlock, jcCase.stats);
		} else {
			throw new SafeCallUnexpectedStateException(addBlockAfterVarDec, varDecl, parent.getClass());
		}
	}

	private Tree getParentJCNode(
			JavacNode varNode
	) {
		Tree variable = varNode.get();
		JavacNode root = varNode.up();
		JCTree parent = getParent((JCVariableDecl) variable, root.get());
		if (parent == null) throw new SafeCallInternalException(variable, "cannot find parent block for variable " +
				variable);
		return parent;
	}

	private JCTree getParent(JCVariableDecl variable, JCTree root
	) {
		if (root instanceof JCExpressionStatement ||
				root instanceof JCBreak || root instanceof JCContinue || root instanceof JCSkip ||
				root instanceof JCThrow
				) {
			return null;//getParent(variable, root, asList((JCExpressionStatement) root));
		} else if (root instanceof JCBlock) {
			return getParent(variable, root, ((JCBlock) root).getStatements());
		} else if (root instanceof JCMethodDecl) {
			JCMethodDecl md = (JCMethodDecl) root;
			return getParent(variable, root, md.getBody().getStatements());
		} else if (root instanceof ClassTree) {
			return root;
		} else if (root instanceof JCIf) {
			JCIf jcIf = (JCIf) root;
			return getParent(variable, jcIf, jcIf.getThenStatement(), jcIf.getElseStatement());
		} else if (root instanceof JCForLoop) {
			JCForLoop forLoopTree = (JCForLoop) root;
			Tree parent = getParent(variable, forLoopTree, forLoopTree.getInitializer());
			if (parent != null) throw new SafeCallIllegalUsingException(forLoopInitializer, variable);
			return getParent(variable, forLoopTree, forLoopTree.getStatement());
		} else if (root instanceof JCEnhancedForLoop) {
			JCEnhancedForLoop forLoopTree = (JCEnhancedForLoop) root;
			Tree parent = getParent(variable, forLoopTree, forLoopTree.getVariable());
			if (parent != null) throw new SafeCallIllegalUsingException(forLoopVariable, variable);
			return getParent(variable, forLoopTree, forLoopTree.getStatement());
		} else if (root instanceof JCWhileLoop) {
			return getParent(variable, root, ((JCWhileLoop) root).getStatement());
		} else if (root instanceof JCDoWhileLoop) {
			return getParent(variable, root, ((JCDoWhileLoop) root).getStatement());
		} else if (root instanceof JCTry) {
			JCTry jcTry = (JCTry) root;
			Collection<JCTree> resources = getResources(jcTry);
			for (JCTree resource : resources) {
				JCTree parent = getParent(variable, resource);
				if (parent != null) throw new SafeCallIllegalUsingException(tryResource, variable);
			}

			JCTree parent = getParent(variable, jcTry.body);
			if (parent == null) {
				Collection<JCCatch> catchers = jcTry.catchers;
				if (catchers != null) for (JCCatch catcher : catchers) {
					parent = getParent(variable, catcher.body);
					if (parent != null) return parent;
				}
				JCBlock finalizer = jcTry.finalizer;
				if (finalizer != null) {
					parent = getParent(variable, finalizer);
				}
			}
			return parent;
		} else if (root instanceof JCSwitch) {
			JCSwitch jcSwitch = (JCSwitch) root;
			for (JCTree caseTree : jcSwitch.cases) {
				JCTree parent = getParent(variable, caseTree);
				if (parent != null) return parent;
			}
			return null;
		} else if (root instanceof JCCase) {
			return getParent(variable, root, ((JCCase) root).getStatements());
		} else if (root instanceof JCVariableDecl) {
			return root == variable ? root : null;
		} else if (root instanceof JCSynchronized) {
			JCSynchronized sync = (JCSynchronized) root;
			return getParent(variable, sync.body);
		} else {
			throw new SafeCallUnexpectedStateException(getParent, variable, root.getClass());
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<JCTree> getResources(JCTry jcTry) {
		Collection<JCTree> resources;
		if (Javac.getJavaCompilerVersion() > 6) {
			Method getResources;
			try {
				getResources = JCTry.class.getMethod("getResources");
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException(e);
			}
			try {
				resources = (Collection<JCTree>) getResources.invoke(jcTry);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException(e);
			}
		} else resources = Collections.emptyList();
		return resources;
	}

	private JCTree getParent(
			JCVariableDecl variable, JCTree root, JCStatement... statement
	) {
		return getParent(variable, root, asList(statement));
	}

	private JCTree getParent(
			JCVariableDecl variable, JCTree root, Collection<? extends JCStatement> statements
	) {
		List<JCStatement> childBlocks = new ArrayList<JCStatement>();
		for (JCStatement statement : statements) {
			if (statement == variable) {
				return root;
			} else if (!(statement instanceof VariableTree)) {
				childBlocks.add(statement);
			}
		}
		for (JCStatement childBlock : childBlocks) {
			JCTree possibleParent = getParent(variable, childBlock);
			if (possibleParent != null) {
				return possibleParent;
			}
		}
		return null;
	}

}
