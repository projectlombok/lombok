package lombok.javac.handlers;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import lombok.core.AST;
import lombok.core.handlers.SafeCallUnexpectedStateException;
import lombok.javac.JavacAST;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.JavacResolution.TypeNotConvertibleException;
import lombok.javac.JavacTreeMaker;

import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.sun.tools.javac.code.Flags.STATIC;
import static java.util.Arrays.asList;
import static javax.lang.model.type.TypeKind.INT;
import static lombok.core.AST.Kind.TYPE;
import static lombok.core.handlers.SafeCallUnexpectedStateException.Place.*;
import static lombok.javac.Javac.*;

/**
 * Created by Bulgakov Alexander on 20.12.16.
 */
public final class HandleSafeCallHelper {
	public static final int NOT_USED = -1;

	private HandleSafeCallHelper() {
	}

	private static JCExpression getDefaultValue(
			JavacTreeMaker maker, TypeKind typeKind) {
		switch (typeKind) {
			case BOOLEAN:
				return maker.Literal(CTC_BOOLEAN, 0);
			case CHAR:
				return maker.Literal(CTC_CHAR, 0);
			case BYTE:
			case SHORT:
			case INT:
				return maker.Literal(CTC_INT, 0);
			case LONG:
				return maker.Literal(CTC_LONG, 0L);
			case FLOAT:
				return maker.Literal(CTC_FLOAT, 0F);
			case DOUBLE:
				return maker.Literal(CTC_DOUBLE, 0D);
		}
		throw new IllegalArgumentException("unsupported type " + typeKind);
	}

	public static JCExpression newElvis(
			JavacTreeMaker maker,
			JavacAST ast, JCExpression expr,
			Type truePartType
	) {
		return newElvis(maker, ast, expr, truePartType, truePartType);
	}

	public static JCExpression newElvis(
			JavacTreeMaker maker,
			JavacAST ast,
			JCExpression expr,
			Type truePartType,
			Type falsePartType
	) {
		JCExpression result;
		JCExpression falsePart = getFalsePart(maker, falsePartType);
		if (expr instanceof JCMethodInvocation) {
			JCMethodInvocation mi = (JCMethodInvocation) expr;
			JCExpression meth = mi.meth;
			if (meth instanceof JCFieldAccess) {
				JCExpression parentExpr = ((JCFieldAccess) meth).selected;
				if (parentExpr instanceof JCNewClass) {
					result = null;
				} else {
					JCConditional notNullCond = newConditional(maker, ast, parentExpr, mi, falsePart);
					JCExpression newParentCnd = newElvis(maker, ast, parentExpr, falsePartType);
					if (newParentCnd instanceof JCConditional) {
						JCConditional cnd = (JCConditional) newParentCnd;
						JCExpression truepart = cnd.truepart;
						while (truepart instanceof JCConditional) {
							cnd = (JCConditional) truepart;
							truepart = cnd.truepart;
						}
						cnd.truepart = notNullCond;
						result = newParentCnd;
					} else result = notNullCond;
				}
			} else if (meth instanceof JCIdent) {
				result = null;
			} else {
				throw new SafeCallUnexpectedStateException(elvisConditionalMethodInvocation, expr, meth.getClass());
			}
		} else if (expr instanceof JCFieldAccess) {
			result = newConditional(maker, ast, ((JCFieldAccess) expr).selected, expr, falsePart);
		} else if (expr instanceof JCIdent) {
			if (!truePartType.isPrimitive() && falsePartType.isPrimitive()) {
				return newConditional(maker, ast, expr, expr, falsePart);
			} else return null;
		} else if (expr instanceof JCLiteral) {
			result = null;
		} else if (expr instanceof JCArrayAccess) {
			JCArrayAccess arrayAccess = (JCArrayAccess) expr;
			JCExpression indexed = arrayAccess.indexed;
			result = newConditional(maker, ast, indexed, expr, falsePart);
		} else if (expr instanceof JCTypeCast) {
			result = newConditional(maker, ast, ((JCTypeCast) expr).expr, expr, falsePart);
		} else if (expr == null) result = null;
		else {
			throw new SafeCallUnexpectedStateException(elvisConditional, expr, expr.getClass());
		}
		return result;
	}

	public static JCExpression getFalsePart(JavacTreeMaker maker, Type falsePartType) {
		JCExpression falsePart;
		if (falsePartType instanceof ClassType || falsePartType instanceof ArrayType) {
			falsePart = newNullLiteral(maker);
		} else {
			falsePart = getDefaultValue(maker, falsePartType.getKind());
		}
		return falsePart;
	}

	public static JCConditional newConditional(
			JavacTreeMaker maker, JavacAST ast,
			JCExpression checkable, JCExpression truePart, JCExpression falsePart
	) {

		JCBinary checkNull = maker.Binary(CTC_NOT_EQUAL, checkable, newNullLiteral(maker));
		checkNull.pos = checkable.pos;
		if (truePart instanceof JCArrayAccess) {
			JCArrayAccess arrayAccess = (JCArrayAccess) truePart;
			JCExpression indexed = arrayAccess.indexed;
			JCFieldAccess length = maker.Select(indexed, ast.toName("length"));
			length.pos = indexed.pos;
			JCExpression index = arrayAccess.index;
			JCBinary checkMax = maker.Binary(CTC_LESS_THAN, index, length);
			checkMax.pos = index.pos;
			boolean minIsZero = false;
			if (index instanceof JCLiteral) {
				JCLiteral literal = (JCLiteral) index;
				Object value = literal.value;
				if (value instanceof Integer) {
					minIsZero = ((Integer) value).equals(0);
				}
			}

			JCLiteral literal = maker.Literal(0);
			literal.pos = index.pos;
			JCBinary checkMin = maker.Binary(CTC_GREATER_OR_EQUAL, index, literal);
			checkMin.pos = index.pos;
			JCBinary checkLength = minIsZero ? checkMax : maker.Binary(CTC_AND, checkMin, checkMax);
			checkLength.pos = checkMax.pos;
			checkNull = maker.Binary(CTC_AND, checkNull, checkLength);
			checkNull.pos = checkLength.pos;
		}

		JCConditional conditional = maker.Conditional(checkNull, truePart, falsePart);
		conditional.pos = checkable.pos;
		return conditional;
	}

	public static JCLiteral newNullLiteral(JavacTreeMaker maker) {
		return maker.Literal(CTC_BOT, null);
	}

	public static JCExpression resolveExprType(
			JCExpression expression, JavacNode annotationNode, JavacResolution javacResolution
	) throws TypeNotConvertibleException {
		JavacNode javacNode = annotationNode.directUp();
		boolean clazz = javacNode.getKind() == AST.Kind.FIELD;
		Type type;
		JCExpression result;
		if (clazz) {
			javacResolution.resolveClassMember(javacNode);
			type = expression.type;
			result = expression;
		} else {
			Map<JCTree, JCTree> treeMap = javacResolution.resolveMethodMember(javacNode);
			JCExpression tExp = (JCExpression) treeMap.get(expression);
			type = tExp.type;
			result = tExp;
		}
		if (type.isErroneous()) {
			String msg = "'" + expression + "' cannot be resolved";
			throw new TypeNotConvertibleException(msg);
		} else return result;
	}

	public static VarRef populateInitStatements(
			final int level,
			Name name,
			JCExpression expr,
			ListBuffer<JCStatement> statements,
			JavacNode annotationNode,
			JavacResolution javacResolution
	) throws TypeNotConvertibleException {
		JavacAST ast = annotationNode.getAst();
		JavacTreeMaker treeMaker = annotationNode.getTreeMaker();
		JCExpression resolvedExpr = expr;//resolveExprType(expr, annotationNode, javacResolution);
		Type type = resolvedExpr.type;

		int notDuplicatedLevel = verifyNotDuplicateLevel(name, level, annotationNode);
		Name varName = newVarName(name, notDuplicatedLevel, annotationNode);
		JCVariableDecl varDecl;
		int lastLevel;
		if (expr instanceof JCMethodInvocation) {
			JCMethodInvocation mi = (JCMethodInvocation) expr;
			JCExpression meth = mi.meth;
			if (meth instanceof JCFieldAccess) {
				Symbol sym = ((JCFieldAccess) meth).sym;
				boolean isStatic = sym.isStatic();
				if (isStatic) {
					lastLevel = notDuplicatedLevel;
					varDecl = makeVariableDecl(treeMaker, statements, varName, type, expr);
				} else {
					VarRef varRef = populateFieldAccess(javacResolution, annotationNode,
							notDuplicatedLevel, name, type, (JCFieldAccess) meth, true, mi.args,
							statements);
					varName = varRef.varName;
					varDecl = varRef.var;
					lastLevel = varRef.level;
				}
			} else if (meth instanceof JCIdent) {
				lastLevel = notDuplicatedLevel;
				varDecl = makeVariableDecl(treeMaker, statements, varName, type, expr);
			} else {
				throw new SafeCallUnexpectedStateException(populateInitStatementsMethodInvocation, expr, meth.getClass());
			}
		} else if (expr instanceof JCFieldAccess) {
			Symbol sym = ((JCFieldAccess) expr).sym;
			boolean isStatic = sym.isStatic();
			if (isStatic) {
				lastLevel = notDuplicatedLevel;
				varDecl = makeVariableDecl(treeMaker, statements, varName, type, expr);
			} else {
				VarRef varRef = populateFieldAccess(javacResolution, annotationNode,
						notDuplicatedLevel, name, type, (JCFieldAccess) expr,
						false, null, statements);
				varName = varRef.varName;
				varDecl = varRef.var;
				lastLevel = varRef.level;
			}
		} else if (
				expr instanceof JCNewClass ||
						expr instanceof JCNewArray
				) {
			lastLevel = notDuplicatedLevel;
			varDecl = makeVariableDecl(treeMaker, statements, varName, type, expr);
		} else if (expr instanceof JCLiteral || isLambda(expr)) {
			lastLevel = NOT_USED;
			varDecl = null;
			varName = null;
		} else if (expr instanceof JCIdent) {
			JCIdent resolvedIdent = (JCIdent) resolvedExpr;
			Symbol sym = resolvedIdent.sym;
			boolean isClass = sym instanceof ClassSymbol;
			boolean isThis = sym instanceof VarSymbol && sym.name.contentEquals("this");
			if (isClass || isThis) {
				lastLevel = NOT_USED;
				varDecl = null;
				varName = null;
			} else {
				lastLevel = notDuplicatedLevel;
				varDecl = makeVariableDecl(treeMaker, statements, varName, type, expr);
			}
		} else if (expr instanceof JCParens) {
			JCParens parens = (JCParens) expr;
			VarRef varRef = populateInitStatements(notDuplicatedLevel, name,
					parens.expr, statements, annotationNode, javacResolution);
			parens.expr = newIdent(treeMaker, varRef);
			JCVariableDecl var = varRef.var;
			parens.expr = var.init;
			var.init = parens;
			varDecl = var;
			lastLevel = varRef.level;
		} else if (expr instanceof JCTypeCast) {
			JCTypeCast typeCast = (JCTypeCast) expr;
			VarRef varRef = populateInitStatements(notDuplicatedLevel + 1, name,
					typeCast.expr, statements, annotationNode, javacResolution);
			typeCast.expr = newIdent(treeMaker, varRef);

			JCVariableDecl var = varRef.var;
			boolean primitive = isPrimitive(var.vartype);

			JCExpression newExpr = primitive ? typeCast : newElvis(treeMaker, ast, typeCast, type);

			varDecl = makeVariableDecl(treeMaker, statements, varName, type, newExpr);
			lastLevel = varRef.level;
		} else if (expr instanceof JCArrayAccess) {
			JCArrayAccess arrayAccess = (JCArrayAccess) expr;
			JCExpression index = arrayAccess.index;
			VarRef indexVarRef = populateInitStatements(notDuplicatedLevel + 1, name,
					index, statements, annotationNode, javacResolution);


			if (indexVarRef.var != null) {
				lastLevel = indexVarRef.level;
				JCExpression indexType = indexVarRef.var.vartype;
				boolean primitive = isPrimitive(indexType);
				JCIdent indexVarIdent = newIdent(treeMaker, indexVarRef);
				if (primitive) arrayAccess.index = indexVarIdent;
				else {
					Type intType = (Type) ast.getTypesUtil().getPrimitiveType(INT);
					JCExpression newExpr = newElvis(treeMaker, ast, indexVarIdent, indexType.type, intType);
					lastLevel = verifyNotDuplicateLevel(name, ++lastLevel, annotationNode);
					Name indexVarName = newVarName(name, lastLevel, annotationNode);

					JCVariableDecl positionVar = makeVariableDecl(treeMaker, statements, indexVarName, type, newExpr);
					arrayAccess.index = newIdent(treeMaker, positionVar);
				}
			} else lastLevel = notDuplicatedLevel;

			JCExpression indexed = arrayAccess.indexed;
			VarRef indexedVarRef = populateInitStatements(lastLevel + 1, name,
					indexed, statements, annotationNode, javacResolution);
			arrayAccess.indexed = newIdent(treeMaker, indexedVarRef);
			JCExpression checkNullExpr = newElvis(treeMaker, ast, arrayAccess, type);

			varDecl = makeVariableDecl(treeMaker, statements, varName, type, checkNullExpr);
			lastLevel = indexedVarRef.level;
		} else {
			throw new SafeCallUnexpectedStateException(populateInitStatements, expr, expr.getClass());
		}
		return new VarRef(varDecl, varName, lastLevel);
	}

	private static boolean isLambda(JCExpression expr) {
		return expr != null && expr.getClass().getSimpleName().equals("JCLambda");
	}

	private static JCFieldAccess newSelect(JavacTreeMaker treeMaker, JCFieldAccess fa, Name childName) {
		JCFieldAccess select = treeMaker.Select(newIdent(treeMaker, childName, fa.pos), fa.name);
		select.pos = fa.pos;
		return select;
	}

	private static JCTree.JCIdent newIdent(JavacTreeMaker treeMaker, Name childName, int pos) {
		JCIdent ident = treeMaker.Ident(childName);
		ident.pos = pos;
		return ident;
	}

	private static JCIdent newIdent(JavacTreeMaker treeMaker, JCVariableDecl var) {
		JCIdent ident = newIdent(treeMaker, var.name, var.pos);
		ident.pos = var.pos;
		return ident;
	}

	private static JCIdent newIdent(JavacTreeMaker treeMaker, VarRef varRef) {
		return newIdent(treeMaker, varRef.var);
	}

	private static JCVariableDecl makeVariableDecl(
			JavacTreeMaker treeMaker, ListBuffer<JCStatement> statements,
			Name varName, Type type, JCExpression expression) {
		JCVariableDecl varDecl;
		varDecl = newVarDecl(treeMaker, varName, type, expression);
		statements.add(varDecl);
		return varDecl;
	}

	private static Name newVarName(Name name, int notDuplicatedLevel, JavacNode annotationNode) {
		return annotationNode.toName(newVarName(name.toString(), notDuplicatedLevel));
	}

	private static int verifyNotDuplicateLevel(final Name name, final int level, JavacNode annotationNode) {
		int varLevel = level;
		String base = name.toString();
		String newName = newVarName(base, varLevel);
		JavacNode varNode = annotationNode.up();
		JavacNode upNode = varNode.up();
		JavacNode rootNode = upNode;
		while (!TYPE.equals(upNode.getKind())) {
			rootNode = upNode;
			upNode = upNode.up();
		}

		JCTree root = rootNode.get();

		Collection<JCVariableDecl> vars = findDuplicateCandidates((JCVariableDecl) varNode.get(), root);
		boolean hasDuplicate;
		do {
			hasDuplicate = false;
			for (VariableTree var : vars) {
				String nameStr = var.getName().toString();
				if (nameStr.equals(newName)) {
					hasDuplicate = true;
					break;
				}
			}
			if (hasDuplicate) {
				varLevel++;
				if (varLevel < 0) {
					varLevel = level;
					base += "_";
				}
				newName = newVarName(base, varLevel);
			}
		} while (hasDuplicate);

		return varLevel;
	}

	private static Collection<JCVariableDecl> findDuplicateCandidates(JCVariableDecl waterline, JCTree parent) {
		Collection<JCVariableDecl> vars = new ArrayList<JCVariableDecl>();
		findDuplicateCandidates(waterline, parent, vars);
		return vars;

	}

	private static boolean findDuplicateCandidates(
			JCVariableDecl waterline, JCTree tree, Collection<JCVariableDecl> vars
	) {
		if (tree == null) {
			throw new IllegalArgumentException("tree is null");
		}
		if (tree instanceof JCBlock) {
			return findDuplicateCandidates(waterline, ((JCBlock) tree).getStatements(), vars);
		} else if (tree instanceof JCMethodDecl) {
			return findDuplicateCandidates(waterline, ((JCMethodDecl) tree).getBody(), vars);
		} else if (tree instanceof ExpressionStatementTree) {
			return false;
		} else if (tree instanceof ClassTree) {
			return false;
		} else if (tree instanceof JCIf) {
			JCIf jcIf = (JCIf) tree;
			boolean endReached = findDuplicateCandidates(waterline, jcIf.getThenStatement(), vars);
			JCStatement elsepart = jcIf.getElseStatement();
			if (!endReached && elsepart != null) endReached = findDuplicateCandidates(waterline, elsepart, vars);
			return endReached;
		} else if (tree instanceof JCVariableDecl) {
			return findDuplicateCandidates(waterline, asList((JCVariableDecl) tree), vars);
		} else if (tree instanceof JCForLoop) {
			JCForLoop forLoop = (JCForLoop) tree;
			ArrayList<JCStatement> statements = new ArrayList<JCStatement>();
			statements.addAll(forLoop.getInitializer());
			statements.add(forLoop.getStatement());
			return findDuplicateCandidates(waterline, statements, vars);
		} else if (tree instanceof JCEnhancedForLoop) {
			JCEnhancedForLoop forLoop = (JCEnhancedForLoop) tree;
			ArrayList<JCStatement> statements = new ArrayList<JCStatement>();
			statements.add(forLoop.var);
			statements.add(forLoop.getStatement());

			return findDuplicateCandidates(waterline, statements, vars);
		} else if (tree instanceof JCWhileLoop) {
			JCWhileLoop whileLoopTree = (JCWhileLoop) tree;
			return findDuplicateCandidates(waterline, asList((JCStatement) whileLoopTree.getStatement()), vars);
		} else if (tree instanceof JCDoWhileLoop) {
			JCDoWhileLoop doWhileLoop = (JCDoWhileLoop) tree;
			return findDuplicateCandidates(waterline, doWhileLoop.body, vars);
		} else if (tree instanceof JCTry) {
			JCTry jcTry = (JCTry) tree;
			boolean stop = findDuplicateCandidates(waterline, jcTry.body, vars);
			if (!stop) for (JCCatch catcher : jcTry.catchers) {
				stop = findDuplicateCandidates(waterline, catcher.body, vars);
				if (stop) return stop;
			}
			JCBlock finalizer = jcTry.finalizer;
			if (!stop && finalizer != null) {
				stop = findDuplicateCandidates(waterline, finalizer, vars);
			}
			return stop;
		} else if (tree instanceof JCSwitch) {
			JCSwitch jcSwitch = (JCSwitch) tree;
			for (JCCase jcCase : jcSwitch.cases) {
				boolean stop = findDuplicateCandidates(waterline, jcCase.getStatements(), vars);
				if (stop) return true;
			}
			return false;
		} else if (
				tree instanceof JCBreak || tree instanceof JCContinue || tree instanceof JCSkip ||
						tree instanceof JCThrow
				) {
			return false;
		} else {
			throw new UnsupportedOperationException(tree.getKind() + "\n" + tree.toString());
		}
	}

	private static boolean findDuplicateCandidates(
			JCVariableDecl waterline,
			Collection<? extends JCStatement> statements,
			Collection<JCVariableDecl> vars) {
		Collection<JCVariableDecl> foundVars = new ArrayList<JCVariableDecl>();

		boolean found = false;
		for (JCStatement statement : statements) {
			if (statement instanceof JCVariableDecl) {
				JCVariableDecl var = (JCVariableDecl) statement;
				JCExpression init = var.init;
				if (isLambda(init)) {
					JCTree lambdaBody = getLambdaBody(init);
					Collection<JCVariableDecl> childVars = new ArrayList<JCVariableDecl>();
					boolean foundInChild = findDuplicateCandidates(waterline, lambdaBody, childVars);
					if (foundInChild) {
						found = true;
						foundVars.addAll(childVars);
						break;
					}
				} else /*if (init == null ||
						init instanceof JCLiteral ||
						init instanceof JCIdent ||
						init instanceof JCMethodInvocation ||
						init instanceof JCFieldAccess ||
						init instanceof JCConditional ||
						init instanceof JCArrayAccess ||
						init instanceof JCNewArray ||
						init instanceof JCTypeCast ||
						init instanceof JCParens ||
						init instanceof JCNewClass
						)*/ {
					if (var == waterline) {
						found = true;
						break;
					} else foundVars.add(var);
//				} else {
//					throw new SafeCallUnexpectedStateException(findDuplicateCandidates, var, init.getClass());
				}
			} else {
				Collection<JCVariableDecl> childVars = new ArrayList<JCVariableDecl>();
				boolean foundInChild = findDuplicateCandidates(waterline, statement, childVars);
				if (foundInChild) {
					found = true;
					foundVars.addAll(childVars);
					break;
				}
			}
		}

		if (found) {
			vars.addAll(foundVars);
		}
		return found;
	}

	private static JCTree getLambdaBody(JCExpression init) {
		Method method = null;
		try {
			method = init.getClass().getMethod("getBody");
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
		JCTree body;
		try {
			body = (JCTree) method.invoke(init);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
		return body;
	}

	private static String newVarName(String name, int level) {
		return name + level;
	}

	public static JCVariableDecl newVarDecl(JavacTreeMaker treeMaker, Name name, Type type, JCExpression expr) {
		Type checkType = (type instanceof ExecutableType) ? type.getReturnType() : type;
		boolean nonameOwner = hasNonameOwner(checkType);

		JCExpression vartype;
		if (nonameOwner) {
			vartype = newIdent(treeMaker, checkType.asElement().name, expr.pos);
		} else {
			JCExpression type1 = treeMaker.Type(checkType);
			vartype = newType(treeMaker, type1);
		}

		JCVariableDecl variableDecl = treeMaker.VarDef(treeMaker.Modifiers(0), name, vartype, expr);
		variableDecl.pos = expr.pos;
		return variableDecl;
	}

	private static boolean hasNonameOwner(Type type) {
		boolean nonameOwner = false;
		if (type instanceof ClassType) {
			Symbol owner = type.tsym.owner;
			nonameOwner = owner.name.isEmpty();
		} else if (type instanceof ArrayType) {
			ArrayType arrayType = (ArrayType) type;
			Type componentType = arrayType.getComponentType();
			nonameOwner = hasNonameOwner(componentType);
		}
		return nonameOwner;
	}

	private static JCExpression newType(JavacTreeMaker treeMaker, JCExpression expression) {
		if (expression instanceof JCFieldAccess) {
			JCFieldAccess fieldAccess = (JCFieldAccess) expression;
			JCExpression selected = fieldAccess.selected;
			JCFieldAccess parent = null;
			while (selected instanceof JCFieldAccess) {
				parent = fieldAccess;
				fieldAccess = (JCFieldAccess) selected;
				selected = fieldAccess.selected;
			}
			if (parent != null && selected instanceof JCIdent) {
				JCIdent lastIdent = (JCIdent) selected;
				if (lastIdent.name.isEmpty()) {
					lastIdent = newIdent(treeMaker, fieldAccess.name, fieldAccess.pos);
					parent.selected = lastIdent;
				}

			}
		} else if (expression instanceof JCArrayTypeTree) {
			JCArrayTypeTree arrayTypeTree = (JCArrayTypeTree) expression;
			arrayTypeTree.elemtype = newType(treeMaker, arrayTypeTree.elemtype);
		}
		return expression;
	}

	private static VarRef populateFieldAccess(
			JavacResolution javacResolution, JavacNode annotationNode,
			int level, Name name, Type type, JCFieldAccess fa, boolean isMeth,
			List<JCExpression> args, ListBuffer<JCStatement> statements)
			throws TypeNotConvertibleException {
		JavacTreeMaker treeMaker = annotationNode.getTreeMaker();
		JCExpression selected = fa.selected;
		VarRef varRef = populateInitStatements(level + 1, name, selected, statements, annotationNode,
				javacResolution);
		JCExpression variableExpr;
		if (varRef.var != null) {
			Name childName = varRef.varName;
			JCFieldAccess newFa = newSelect(treeMaker, fa, childName);
			newFa.type = annotationNode.getSymbolTable().objectType;
			JCExpression newExpr = isMeth ? args != null ? treeMaker.App(newFa, args) : treeMaker.App(newFa) : newFa;
			newExpr.pos = newFa.pos;
			variableExpr = newElvis(treeMaker, annotationNode.getAst(), newExpr, type);
		} else if (isMeth) {
			fa.type = type;
			variableExpr = args != null ? treeMaker.App(fa, args) : treeMaker.App(fa);
			variableExpr.pos = fa.pos;
		} else variableExpr = fa;

		int verifyLevel = verifyNotDuplicateLevel(name, level, annotationNode);
		Name newName = newVarName(name, verifyLevel, annotationNode);
		JCVariableDecl variableDecl = makeVariableDecl(treeMaker, statements, newName, type, variableExpr);
		int maxLevel = varRef.level > verifyLevel ? varRef.level : verifyLevel;
		return new VarRef(variableDecl, newName, maxLevel);

	}

	static JCBlock newInitBlock(JCVariableDecl varDecl, JavacNode annotationNode) throws TypeNotConvertibleException {
		JavacResolution javacResolution = new JavacResolution(annotationNode.getContext());

		JavacTreeMaker maker = annotationNode.getTreeMaker();
		ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
		JCExpression expr = varDecl.init;
		if (expr == null) return null;

		JCExpression resolveExpr = resolveExprType(expr, annotationNode, javacResolution);
		VarRef varRef = populateInitStatements(1, varDecl.name, resolveExpr, statements,
				annotationNode, javacResolution);
		Name name = varRef.varName;
		if (name == null) return null;
		boolean removeOnlyOneStatement = statements.length() == 1;

		JCExpression rhs = newIdent(maker, varRef);
		JCExpression lhs = newIdent(maker, varDecl);
		JCExpression varType = varDecl.vartype;
		if (isPrimitive(varType) && !isPrimitive(varRef.var.vartype)) {
			TypeKind kind;
			if (varType instanceof PrimitiveTypeTree) {
				PrimitiveTypeTree type = (PrimitiveTypeTree) varType;
				kind = type.getPrimitiveTypeKind();
			} else if (varType.type == null) {
				throw new SafeCallUnexpectedStateException(cannotRecognizeType, varType, varType.getClass());
			} else kind = varType.type.getKind();
			//JCExpression expression = resolveExprType(expr, annotationNode, javacResolution);
			rhs = newConditional(maker, annotationNode.getAst(), rhs, rhs,
					getDefaultValue(maker, kind));
			removeOnlyOneStatement = false;
		}

		if (removeOnlyOneStatement) return null;
		JCExpressionStatement assign = maker.Exec(maker.Assign(lhs, rhs));

		statements.add(assign);
		boolean isStatic = (varDecl.mods.flags & STATIC) != 0;
		int flags = isStatic ? STATIC : 0;
		JCBlock block = maker.Block(flags, statements.toList());
		block.pos = varDecl.pos;
		return block;

	}

	public static <T> List<T> addBlockAfterVarDec(T varDecl, T initBlock, List<T> members) {
		ListBuffer<T> newMembers = new ListBuffer<T>();
		for (T tree : members) {
			newMembers.add(tree);
			if (tree == varDecl) {
				newMembers.add(initBlock);
			}
		}
		return newMembers.toList();
	}

	private static class VarRef {
		Name varName;
		JCVariableDecl var;
		private int level;

		VarRef(JCVariableDecl var, Name varName, int level) {
			this.var = var;
			this.varName = varName;
			this.level = level;
		}
	}
}
