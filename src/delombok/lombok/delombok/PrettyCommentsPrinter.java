/*
 * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * Code derived from com.sun.tools.javac.tree.Pretty, from the langtools project.
 * A version can be found at, for example, http://hg.openjdk.java.net/jdk7/build/langtools
 */
package lombok.delombok;

import static com.sun.tools.javac.code.Flags.*;
import static lombok.javac.Javac.*;
import static lombok.javac.JavacTreeMaker.TreeTag.treeTag;
import static lombok.javac.JavacTreeMaker.TypeTag.typeTag;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import lombok.javac.CommentInfo;
import lombok.javac.CommentInfo.EndConnection;
import lombok.javac.CommentInfo.StartConnection;
import lombok.javac.JavacTreeMaker.TreeTag;
import lombok.javac.JavacTreeMaker.TypeTag;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCSkip;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Position;
//import com.sun.tools.javac.code.TypeTags;

/** Prints out a tree as an indented Java source program.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@SuppressWarnings("all") // Mainly sun code that has other warning settings
public class PrettyCommentsPrinter extends JCTree.Visitor {
	private static final TreeTag PARENS = treeTag("PARENS");
	private static final TreeTag IMPORT = treeTag("IMPORT");
	private static final TreeTag VARDEF = treeTag("VARDEF");
	private static final TreeTag SELECT = treeTag("SELECT");
	
	private static final Map<TreeTag, String> OPERATORS;
	
	static {
		Map<TreeTag, String> map = new HashMap<TreeTag, String>();
		
		map.put(treeTag("POS"), "+");
		map.put(treeTag("NEG"), "-");
		map.put(treeTag("NOT"), "!");
		map.put(treeTag("COMPL"), "~");
		map.put(treeTag("PREINC"), "++");
		map.put(treeTag("PREDEC"), "--");
		map.put(treeTag("POSTINC"), "++");
		map.put(treeTag("POSTDEC"), "--");
		map.put(treeTag("NULLCHK"), "<*nullchk*>");
		map.put(treeTag("OR"), "||");
		map.put(treeTag("AND"), "&&");
		map.put(treeTag("EQ"), "==");
		map.put(treeTag("NE"), "!=");
		map.put(treeTag("LT"), "<");
		map.put(treeTag("GT"), ">");
		map.put(treeTag("LE"), "<=");
		map.put(treeTag("GE"), ">=");
		map.put(treeTag("BITOR"), "|");
		map.put(treeTag("BITXOR"), "^");
		map.put(treeTag("BITAND"), "&");
		map.put(treeTag("SL"), "<<");
		map.put(treeTag("SR"), ">>");
		map.put(treeTag("USR"), ">>>");
		map.put(treeTag("PLUS"), "+");
		map.put(treeTag("MINUS"), "-");
		map.put(treeTag("MUL"), "*");
		map.put(treeTag("DIV"), "/");
		map.put(treeTag("MOD"), "%");
		
		map.put(treeTag("BITOR_ASG"), "|=");
		map.put(treeTag("BITXOR_ASG"), "^=");
		map.put(treeTag("BITAND_ASG"), "&=");
		map.put(treeTag("SL_ASG"), "<<=");
		map.put(treeTag("SR_ASG"), ">>=");
		map.put(treeTag("USR_ASG"), ">>>=");
		map.put(treeTag("PLUS_ASG"), "+=");
		map.put(treeTag("MINUS_ASG"), "-=");
		map.put(treeTag("MUL_ASG"), "*=");
		map.put(treeTag("DIV_ASG"), "/=");
		map.put(treeTag("MOD_ASG"), "%=");
		
		OPERATORS = map;
	}
	
	private List<CommentInfo> comments;
	private final JCCompilationUnit cu;
	private boolean onNewLine = true;
	private boolean aligned = false;
	private boolean inParams = false;
	
	private boolean needsSpace = false;
	private boolean needsNewLine = false;
	private boolean needsAlign = false;
	
	// Flag for try-with-resources to make them not final and not print the last semicolon.
	// This flag is set just before printing the vardef and cleared when printing its modifiers.
	private boolean suppressFinalAndSemicolonsInTry = false;
	
	private final FormatPreferences formatPreferences;
	
	public PrettyCommentsPrinter(Writer out, JCCompilationUnit cu, List<CommentInfo> comments, FormatPreferences preferences) {
		this.out = out;
		this.comments = comments;
		this.cu = cu;
		this.formatPreferences = preferences;
	}
	
	private int endPos(JCTree tree) {
		return getEndPosition(tree, cu);
	}
	
	private void consumeComments(int until) throws IOException {
		consumeComments(until, null);
	}
	
	private void consumeComments(int until, JCTree tree) throws IOException {
		boolean prevNewLine = onNewLine;
		boolean found = false;
		CommentInfo head = comments.head;
		while (comments.nonEmpty() && head.pos < until) {
			printComment(head);
			comments = comments.tail;
			head = comments.head;
		}
		if (!onNewLine && prevNewLine) {
			println();
		}
	}
	
	private void consumeTrailingComments(int from) throws IOException {
		boolean prevNewLine = onNewLine;
		CommentInfo head = comments.head;
		boolean stop = false;
		while (comments.nonEmpty() && head.prevEndPos == from && !stop && !(head.start == StartConnection.ON_NEXT_LINE || head.start == StartConnection.START_OF_LINE)) {
			from = head.endPos;
			printComment(head);
			stop = (head.end == EndConnection.ON_NEXT_LINE);
			comments = comments.tail;
			head = comments.head;
		}
		if (!onNewLine && prevNewLine) {
			println();
		}
	}
	
	private void printComment(CommentInfo comment) throws IOException {
		prepareComment(comment.start);
		print(comment.content);
		switch (comment.end) {
		case ON_NEXT_LINE:
			if (!aligned) {
				needsNewLine = true;
				needsAlign = true;
			}
			break;
		case AFTER_COMMENT:
			needsSpace = true;
			break;
		case DIRECT_AFTER_COMMENT:
			// do nothing
			break;
		}
	}
	
	private void prepareComment(StartConnection start) throws IOException {
		switch (start) {
			case DIRECT_AFTER_PREVIOUS:
				needsSpace = false;
				break;
			case AFTER_PREVIOUS:
				needsSpace = true;
				break;
			case START_OF_LINE:
				needsNewLine = true;
				needsAlign = false;
				break;
			case ON_NEXT_LINE:
				if (!aligned) {
					needsNewLine = true;
					needsAlign = true;
				}
				break;
		}
	}
	
	/** The output stream on which trees are printed.
	 */
	Writer out;
	
	/** The current left margin.
	 */
	int lmargin = 0;
	
	/** The enclosing class name.
	 */
	Name enclClassName;
	
	/** A hashtable mapping trees to their documentation comments
	 *  (can be null)
	 */
	Map<JCTree, String> docComments = null;
	DocCommentTable docTable = null;
	
	String getJavadocFor(JCTree node) {
		if (docComments != null) return docComments.get(node);
		if (docTable != null) return docTable.getCommentText(node);
		return null;
	}
	
	/** Align code to be indented to left margin.
	 */
	void align() throws IOException {
		onNewLine = false;
		aligned = true;
		needsAlign = false;
		for (int i = 0; i < lmargin; i++) out.write(formatPreferences.indent());
	}
	
	/** Increase left margin by indentation width.
	 */
	void indent() {
		lmargin++;
	}
	
	/** Decrease left margin by indentation width.
	 */
	void undent() {
		lmargin--;
	}
	
	/** Enter a new precedence level. Emit a `(' if new precedence level
	 *  is less than precedence level so far.
	 *  @param contextPrec	The precedence level in force so far.
	 *  @param ownPrec		The new precedence level.
	 */
	void open(int contextPrec, int ownPrec) throws IOException {
		if (ownPrec < contextPrec) out.write("(");
	}
	
	/** Leave precedence level. Emit a `(' if inner precedence level
	 *  is less than precedence level we revert to.
	 *  @param contextPrec	The precedence level we revert to.
	 *  @param ownPrec		The inner precedence level.
	 */
	void close(int contextPrec, int ownPrec) throws IOException {
		if (ownPrec < contextPrec) out.write(")");
	}
	
	/** Print string, replacing all non-ascii character with unicode escapes.
	 */
	public void print(Object s) throws IOException {
		boolean align = needsAlign;
		if (needsNewLine && !onNewLine) {
			println();
		}
		if (align && !aligned) {
			align();
		}
		if (needsSpace && !onNewLine && !aligned) {
			out.write(' ');
		}
		needsSpace = false;
		
		out.write(Convert.escapeUnicode(s.toString()));
		
		onNewLine = false;
		aligned = false;
	}
	
	/** Print new line.
	 */
	public void println() throws IOException {
		onNewLine = true;
		aligned = false;
		needsNewLine = false;
		out.write(lineSep);
	}
	
	String lineSep = System.getProperty("line.separator");
	
	/**************************************************************************
	 * Traversal methods
	 *************************************************************************/
	
	/** Exception to propagate IOException through visitXXX methods */
	private static class UncheckedIOException extends Error {
		static final long serialVersionUID = -4032692679158424751L;
		UncheckedIOException(IOException e) {
			super(e.getMessage(), e);
		}
	}
	
	/** Visitor argument: the current precedence level.
	 */
	int prec;
	
	/** Visitor method: print expression tree.
	 *  @param prec  The current precedence level.
	 */
	public void printExpr(JCTree tree, int prec) throws IOException {
		
		int prevPrec = this.prec;
		try {
			this.prec = prec;
			if (tree == null) print("/*missing*/");
			else {
				consumeComments(tree.pos, tree);
			   	tree.accept(this);
			   	int endPos = endPos(tree);
				consumeTrailingComments(endPos);
			}
		} catch (UncheckedIOException ex) {
			IOException e = new IOException(ex.getMessage());
			e.initCause(ex);
			throw e;
		} finally {
			this.prec = prevPrec;
		}
	}
	
	/** Derived visitor method: print expression tree at minimum precedence level
	 *  for expression.
	 */
	public void printExpr(JCTree tree) throws IOException {
		printExpr(tree, TreeInfo.noPrec);
	}
	
	/** Derived visitor method: print statement tree.
	 */
	public void printStat(JCTree tree) throws IOException {
		if (isEmptyStat(tree)) {
			// printEmptyStat(); // -- starting in java 7, these get lost, so to be consistent, we never print them.
		} else {
			printExpr(tree, TreeInfo.notExpression);
		}
	}
	
	public void printEmptyStat() throws IOException {
		print(";");
	}
	
	public boolean isEmptyStat(JCTree tree) {
		if (!(tree instanceof JCBlock)) return false;
		JCBlock block = (JCBlock) tree;
		return (Position.NOPOS == block.pos) && block.stats.isEmpty();
	}

	/** Derived visitor method: print list of expression trees, separated by given string.
	 *  @param sep the separator string
	 */
	public <T extends JCTree> void printExprs(List<T> trees, String sep) throws IOException {
		if (trees.nonEmpty()) {
			printExpr(trees.head);
			for (List<T> l = trees.tail; l.nonEmpty(); l = l.tail) {
				print(sep);
				printExpr(l.head);
			}
		}
	}
	
	/** Derived visitor method: print list of expression trees, separated by commas.
	 */
	public <T extends JCTree> void printExprs(List<T> trees) throws IOException {
		printExprs(trees, ", ");
	}
	
	/** Derived visitor method: print list of statements, each on a separate line.
	 */
	public void printStats(List<? extends JCTree> trees) throws IOException {
		for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
			if (isSuppressed(l.head)) continue;
			if (!suppressAlignmentForEmptyLines(l.head)) align();
			printStat(l.head);
			println();
		}
	}
	
	private boolean suppressAlignmentForEmptyLines(JCTree tree) {
		return !formatPreferences.fillEmpties() && startsWithNewLine(tree);
	}
	
	private boolean startsWithNewLine(JCTree tree) {
		return tree instanceof JCMethodDecl || tree instanceof JCClassDecl;
	}
	
	private boolean isSuppressed(JCTree tree) {
		if (isEmptyStat(tree)) {
			return true;
		}
		if (tree instanceof JCExpressionStatement) {
			return isNoArgsSuperCall(((JCExpressionStatement)tree).expr);
		}
		return false;
	}
	
	/** Print a set of modifiers.
	 */
	public void printFlags(long flags) throws IOException {
		if ((flags & SYNTHETIC) != 0) print("/*synthetic*/ ");
		if (suppressFinalAndSemicolonsInTry) {
			flags = flags & ~FINAL;
			suppressFinalAndSemicolonsInTry = false;
		}
		print(TreeInfo.flagNames(flags));
		if ((flags & StandardFlags) != 0) print(" ");
		if ((flags & ANNOTATION) != 0) print("@");
	}
	
	public void printAnnotations(List<JCAnnotation> trees) throws IOException {
		for (List<JCAnnotation> l = trees; l.nonEmpty(); l = l.tail) {
			printStat(l.head);
			if (inParams) { 
				print(" ");
			}
			else {
				println();
				align();
			}
		}
	}
	
	/** Print documentation comment, if it exists
	 *  @param tree	The tree for which a documentation comment should be printed.
	 */
	public void printDocComment(JCTree tree) throws IOException {
		String dc = getJavadocFor(tree);
		if (dc == null) return;
		print("/**"); println();
		int pos = 0;
		int endpos = lineEndPos(dc, pos);
		boolean atStart = true;
		while (pos < dc.length()) {
			String line = dc.substring(pos, endpos);
			if (line.trim().isEmpty() && atStart) {
				atStart = false;
				continue;
			}
			atStart = false;
			align();
			print(" *");
			if (pos < dc.length() && dc.charAt(pos) > ' ') print(" ");
			print(dc.substring(pos, endpos)); println();
			pos = endpos + 1;
			endpos = lineEndPos(dc, pos);
		}
		align(); print(" */"); println();
		align();
	}
//where
	static int lineEndPos(String s, int start) {
		int pos = s.indexOf('\n', start);
		if (pos < 0) pos = s.length();
		return pos;
	}
	
	/** If type parameter list is non-empty, print it enclosed in "<...>" brackets.
	 */
	public void printTypeParameters(List<JCTypeParameter> trees) throws IOException {
		if (trees.nonEmpty()) {
			print("<");
			printExprs(trees);
			print(">");
		}
	}
	
	/** Print a block.
	 */
	public void printBlock(List<? extends JCTree> stats, JCTree container) throws IOException {
		print("{");
		println();
		indent();
		printStats(stats);
		consumeComments(endPos(container));
		undent();
		align();
		print("}");
	}
	
	/** Print a block.
	 */
	public void printEnumBody(List<JCTree> stats) throws IOException {
		print("{");
		println();
		indent();
		boolean first = true;
		for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
			if (isEnumerator(l.head)) {
				if (!first) {
					print(",");
					println();
				}
				align();
				printStat(l.head);
				first = false;
			}
		}
		print(";");
		println();
		int x = 0;
		for (List<JCTree> l = stats; l.nonEmpty(); l = l.tail) {
			x++;
			if (!isEnumerator(l.head)) {
				if (!suppressAlignmentForEmptyLines(l.head)) align();
				printStat(l.head);
				println();
			}
		}
		undent();
		align();
		print("}");
	}
	
	public void printEnumMember(JCVariableDecl tree) throws IOException {
		printAnnotations(tree.mods.annotations);
		print(tree.name);
		if (tree.init instanceof JCNewClass) {
			JCNewClass constructor = (JCNewClass) tree.init;
			if (constructor.args != null && constructor.args.nonEmpty()) {
				print("(");
				printExprs(constructor.args);
				print(")");
			}
			if (constructor.def != null && constructor.def.defs != null) {
				print(" ");
				printBlock(constructor.def.defs, constructor.def);
			}
		}
	}
	
	/** Is the given tree an enumerator definition? */
	boolean isEnumerator(JCTree t) {
		return VARDEF.equals(treeTag(t)) && (((JCVariableDecl) t).mods.flags & ENUM) != 0;
	}
	
	/** Print unit consisting of package clause and import statements in toplevel,
	 *  followed by class definition. if class definition == null,
	 *  print all definitions in toplevel.
	 *  @param tree	 The toplevel tree
	 *  @param cdef	 The class definition, which is assumed to be part of the
	 *				  toplevel tree.
	 */
	public void printUnit(JCCompilationUnit tree, JCClassDecl cdef) throws IOException {
		Object dc = getDocComments(tree);
		loadDocCommentsTable(dc);
		printDocComment(tree);
		if (tree.pid != null) {
			consumeComments(tree.pos, tree);
			print("package ");
			printExpr(tree.pid);
			print(";");
			println();
		}
		boolean firstImport = true;
		for (List<JCTree> l = tree.defs;
		l.nonEmpty() && (cdef == null || IMPORT.equals(treeTag(l.head)));
		l = l.tail) {
			if (IMPORT.equals(treeTag(l.head))) {
				JCImport imp = (JCImport)l.head;
				Name name = TreeInfo.name(imp.qualid);
				if (name == name.table.fromChars(new char[] {'*'}, 0, 1) ||
						cdef == null ||
						isUsed(TreeInfo.symbol(imp.qualid), cdef)) {
					if (firstImport) {
						firstImport = false;
						println();
					}
					printStat(imp);
				}
			} else {
				printStat(l.head);
			}
		}
		if (cdef != null) {
			printStat(cdef);
			println();
		}
	}
	// where
	@SuppressWarnings("unchecked")
	private void loadDocCommentsTable(Object dc) {
		if (dc instanceof Map<?, ?>) this.docComments = (Map) dc;
		else if (dc instanceof DocCommentTable) this.docTable = (DocCommentTable) dc;
	}
	
	boolean isUsed(final Symbol t, JCTree cdef) {
		class UsedVisitor extends TreeScanner {
			public void scan(JCTree tree) {
				if (tree!=null && !result) tree.accept(this);
			}
			boolean result = false;
			public void visitIdent(JCIdent tree) {
				if (tree.sym == t) result = true;
			}
		}
		UsedVisitor v = new UsedVisitor();
		v.scan(cdef);
		return v.result;
	}
	
	/**************************************************************************
	 * Visitor methods
	 *************************************************************************/
	
	public void visitTopLevel(JCCompilationUnit tree) {
		try {
			printUnit(tree, null);
			consumeComments(Integer.MAX_VALUE);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitImport(JCImport tree) {
		try {
			print("import ");
			if (tree.staticImport) print("static ");
			printExpr(tree.qualid);
			print(";");
			println();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitClassDef(JCClassDecl tree) {
		try {
			consumeComments(tree.pos, tree);
			println(); align();
			printDocComment(tree);
			printAnnotations(tree.mods.annotations);
			printFlags(tree.mods.flags & ~INTERFACE);
			Name enclClassNamePrev = enclClassName;
			enclClassName = tree.name;
			if ((tree.mods.flags & INTERFACE) != 0) {
				print("interface " + tree.name);
				printTypeParameters(tree.typarams);
				if (tree.implementing.nonEmpty()) {
					print(" extends ");
					printExprs(tree.implementing);
				}
			} else {
				if ((tree.mods.flags & ENUM) != 0)
					print("enum " + tree.name);
				else
					print("class " + tree.name);
				printTypeParameters(tree.typarams);
				if (getExtendsClause(tree) != null) {
					print(" extends ");
					printExpr(getExtendsClause(tree));
				}
				if (tree.implementing.nonEmpty()) {
					print(" implements ");
					printExprs(tree.implementing);
				}
			}
			print(" ");
			// <Added for delombok by Reinier Zwitserloot>
			if ((tree.mods.flags & INTERFACE) != 0) {
				removeImplicitModifiersForInterfaceMembers(tree.defs);
			}
			// </Added for delombok by Reinier Zwitserloot>
			if ((tree.mods.flags & ENUM) != 0) {
				printEnumBody(tree.defs);
			} else {
				printBlock(tree.defs, tree);
			}
			enclClassName = enclClassNamePrev;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	// Added for delombok by Reinier Zwitserloot
	private void removeImplicitModifiersForInterfaceMembers(List<JCTree> defs) {
		for (JCTree def :defs) {
			if (def instanceof JCVariableDecl) {
				((JCVariableDecl) def).mods.flags &= ~(Flags.PUBLIC | Flags.STATIC | Flags.FINAL);
			}
			if (def instanceof JCMethodDecl) {
				((JCMethodDecl) def).mods.flags &= ~(Flags.PUBLIC | Flags.ABSTRACT);
			}
			if (def instanceof JCClassDecl) {
				((JCClassDecl) def).mods.flags &= ~(Flags.PUBLIC | Flags.STATIC);
			}
		}
	}
	
	public void visitMethodDef(JCMethodDecl tree) {
		try {
			boolean isConstructor = tree.name == tree.name.table.fromChars("<init>".toCharArray(), 0, 6);
			// when producing source output, omit anonymous constructors
			if (isConstructor && enclClassName == null) return;
			boolean isGeneratedConstructor = isConstructor && ((tree.mods.flags & Flags.GENERATEDCONSTR) != 0);
			if (isGeneratedConstructor) return;
			println(); align();
			printDocComment(tree);
			printExpr(tree.mods);
			printTypeParameters(tree.typarams);
			if (tree.typarams != null && tree.typarams.length() > 0) print(" ");
			if (tree.name == tree.name.table.fromChars("<init>".toCharArray(), 0, 6)) {
				print(enclClassName != null ? enclClassName : tree.name);
			} else {
				printExpr(tree.restype);
				print(" " + tree.name);
			}
			print("(");
			inParams = true;
			printExprs(tree.params);
			inParams = false;
			print(")");
			if (tree.thrown.nonEmpty()) {
				print(" throws ");
				printExprs(tree.thrown);
			}
			if (tree.defaultValue != null) {
			  print(" default ");
			  print(tree.defaultValue);
			}
			if (tree.body != null) {
				print(" ");
				printBlock(tree.body.stats, tree.body);
			} else {
				print(";");
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitVarDef(JCVariableDecl tree) {
		try {
			boolean suppressSemi = suppressFinalAndSemicolonsInTry;
			if (getJavadocFor(tree) != null) {
				println(); align();
			}
			printDocComment(tree);
			if ((tree.mods.flags & ENUM) != 0) {
				printEnumMember(tree);
			} else {
				printExpr(tree.mods);
				if ((tree.mods.flags & VARARGS) != 0) {
					printExpr(((JCArrayTypeTree) tree.vartype).elemtype);
					print("... " + tree.name);
				} else {
					printExpr(tree.vartype);
					print(" " + tree.name);
				}
				if (tree.init != null) {
					print(" = ");
					printExpr(tree.init);
				}
				if (prec == TreeInfo.notExpression && !suppressSemi) print(";");
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitSkip(JCSkip tree) {
		try {
			print(";");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitBlock(JCBlock tree) {
		try {
			consumeComments(tree.pos);
			printFlags(tree.flags);
			printBlock(tree.stats, tree);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitDoLoop(JCDoWhileLoop tree) {
		try {
			print("do ");
			printStat(tree.body);
			align();
			print(" while ");
			if (PARENS.equals(treeTag(tree.cond))) {
				printExpr(tree.cond);
			} else {
				print("(");
				printExpr(tree.cond);
				print(")");
			}
			print(";");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitWhileLoop(JCWhileLoop tree) {
		try {
			print("while ");
			if (PARENS.equals(treeTag(tree.cond))) {
				printExpr(tree.cond);
			} else {
				print("(");
				printExpr(tree.cond);
				print(")");
			}
			print(" ");
			printStat(tree.body);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitForLoop(JCForLoop tree) {
		try {
			print("for (");
			if (tree.init.nonEmpty()) {
				if (VARDEF.equals(treeTag(tree.init.head))) {
					printExpr(tree.init.head);
					for (List<JCStatement> l = tree.init.tail; l.nonEmpty(); l = l.tail) {
						JCVariableDecl vdef = (JCVariableDecl)l.head;
						print(", " + vdef.name + " = ");
						printExpr(vdef.init);
					}
				} else {
					printExprs(tree.init);
				}
			}
			print("; ");
			if (tree.cond != null) printExpr(tree.cond);
			print("; ");
			printExprs(tree.step);
			print(") ");
			printStat(tree.body);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitForeachLoop(JCEnhancedForLoop tree) {
		try {
			print("for (");
			printExpr(tree.var);
			print(" : ");
			printExpr(tree.expr);
			print(") ");
			printStat(tree.body);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitLabelled(JCLabeledStatement tree) {
		try {
			print(tree.label + ":");
			if (isEmptyStat(tree.body) || tree.body instanceof JCSkip) {
				print(" ;");
			} else if (tree.body instanceof JCBlock) {
				print(" ");
				printStat(tree.body);
			} else {
				println();
				align();
				printStat(tree.body);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitSwitch(JCSwitch tree) {
		try {
			print("switch ");
			if (PARENS.equals(treeTag(tree.selector))) {
				printExpr(tree.selector);
			} else {
				print("(");
				printExpr(tree.selector);
				print(")");
			}
			print(" {");
			println();
			printStats(tree.cases);
			align();
			print("}");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitCase(JCCase tree) {
		try {
			if (tree.pat == null) {
				print("default");
			} else {
				print("case ");
				printExpr(tree.pat);
			}
			print(": ");
			println();
			indent();
			printStats(tree.stats);
			undent();
			align();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitSynchronized(JCSynchronized tree) {
		try {
			print("synchronized ");
			if (PARENS.equals(treeTag(tree.lock))) {
				printExpr(tree.lock);
			} else {
				print("(");
				printExpr(tree.lock);
				print(")");
			}
			print(" ");
			printStat(tree.body);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitTry(JCTry tree) {
		try {
			print("try ");
			List<?> resources = null;
			try {
				Field f = JCTry.class.getField("resources");
				resources = (List<?>) f.get(tree);
			} catch (Exception ignore) {
				// In JDK6 and down this field does not exist; resources will retain its initializer value which is what we want.
			}
			
			if (resources != null && resources.nonEmpty()) {
				print("(");
				int remaining = resources.size();
				if (remaining == 1) {
					JCTree var = (JCTree) resources.get(0);
					suppressFinalAndSemicolonsInTry = true;
					printStat(var);
					print(") ");
				} else {
					indent(); indent();
					for (Object var0 : resources) {
						println();
						align();
						JCTree var = (JCTree) var0;
						suppressFinalAndSemicolonsInTry = true;
						printStat(var);
						remaining--;
						if (remaining > 0) print(";");
					}
					print(") ");
					undent(); undent();
				}
			}
			
			printStat(tree.body);
			for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
				printStat(l.head);
			}
			if (tree.finalizer != null) {
				print(" finally ");
				printStat(tree.finalizer);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitCatch(JCCatch tree) {
		try {
			print(" catch (");
			printExpr(tree.param);
			print(") ");
			printStat(tree.body);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitConditional(JCConditional tree) {
		try {
			open(prec, TreeInfo.condPrec);
			printExpr(tree.cond, TreeInfo.condPrec);
			print(" ? ");
			printExpr(tree.truepart, TreeInfo.condPrec);
			print(" : ");
			printExpr(tree.falsepart, TreeInfo.condPrec);
			close(prec, TreeInfo.condPrec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitIf(JCIf tree) {
		try {
			print("if ");
			if (PARENS.equals(treeTag(tree.cond))) {
				printExpr(tree.cond);
			} else {
				print("(");
				printExpr(tree.cond);
				print(")");
			}
			print(" ");
			printStat(tree.thenpart);
			if (tree.elsepart != null) {
				print(" else ");
				printStat(tree.elsepart);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private boolean isNoArgsSuperCall(JCExpression expr) {
		if (!(expr instanceof JCMethodInvocation)) return false;
		JCMethodInvocation tree = (JCMethodInvocation) expr;
		if (!tree.typeargs.isEmpty() || !tree.args.isEmpty()) return false;
		if (!(tree.meth instanceof JCIdent)) return false;
		return ((JCIdent) tree.meth).name.toString().equals("super");
	}
	
	public void visitExec(JCExpressionStatement tree) {
		if (isNoArgsSuperCall(tree.expr)) return;
		try {
			printExpr(tree.expr);
			if (prec == TreeInfo.notExpression) print(";");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitBreak(JCBreak tree) {
		try {
			print("break");
			if (tree.label != null) print(" " + tree.label);
			print(";");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitContinue(JCContinue tree) {
		try {
			print("continue");
			if (tree.label != null) print(" " + tree.label);
			print(";");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitReturn(JCReturn tree) {
		try {
			print("return");
			if (tree.expr != null) {
				print(" ");
				printExpr(tree.expr);
			}
			print(";");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitThrow(JCThrow tree) {
		try {
			print("throw ");
			printExpr(tree.expr);
			print(";");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitAssert(JCAssert tree) {
		try {
			print("assert ");
			printExpr(tree.cond);
			if (tree.detail != null) {
				print(" : ");
				printExpr(tree.detail);
			}
			print(";");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitApply(JCMethodInvocation tree) {
		try {
			if (!tree.typeargs.isEmpty()) {
				if (SELECT.equals(treeTag(tree.meth))) {
					JCFieldAccess left = (JCFieldAccess)tree.meth;
					printExpr(left.selected);
					print(".<");
					printExprs(tree.typeargs);
					print(">" + left.name);
				} else {
					print("<");
					printExprs(tree.typeargs);
					print(">");
					printExpr(tree.meth);
				}
			} else {
				printExpr(tree.meth);
			}
			print("(");
			printExprs(tree.args);
			print(")");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitNewClass(JCNewClass tree) {
		try {
			if (tree.encl != null) {
				printExpr(tree.encl);
				print(".");
			}
			print("new ");
			if (!tree.typeargs.isEmpty()) {
				print("<");
				printExprs(tree.typeargs);
				print(">");
			}
			printExpr(tree.clazz);
			print("(");
			printExprs(tree.args);
			print(")");
			if (tree.def != null) {
				Name enclClassNamePrev = enclClassName;
				enclClassName =
						tree.def.name != null ? tree.def.name :
							tree.type != null && tree.type.tsym.name != tree.type.tsym.name.table.fromChars(new char[0], 0, 0) ? tree.type.tsym.name :
								null;
				if ((tree.def.mods.flags & Flags.ENUM) != 0) print("/*enum*/");
				printBlock(tree.def.defs, tree.def);
				enclClassName = enclClassNamePrev;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitNewArray(JCNewArray tree) {
		try {
			if (tree.elemtype != null) {
				print("new ");
				JCTree elem = tree.elemtype;
				if (elem instanceof JCArrayTypeTree)
					printBaseElementType((JCArrayTypeTree) elem);
				else
					printExpr(elem);
				for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
					print("[");
					printExpr(l.head);
					print("]");
				}
				if (elem instanceof JCArrayTypeTree)
					printBrackets((JCArrayTypeTree) elem);
			}
			if (tree.elems != null) {
				if (tree.elemtype != null) print("[]");
				print("{");
				printExprs(tree.elems);
				print("}");
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitParens(JCParens tree) {
		try {
			print("(");
			printExpr(tree.expr);
			print(")");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitAssign(JCAssign tree) {
		try {
			open(prec, TreeInfo.assignPrec);
			printExpr(tree.lhs, TreeInfo.assignPrec + 1);
			print(" = ");
			printExpr(tree.rhs, TreeInfo.assignPrec);
			close(prec, TreeInfo.assignPrec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public String operatorName(TreeTag tag) {
		String result = OPERATORS.get(tag);
		if (result == null) throw new Error();
		return result;
	}
	
	public void visitAssignop(JCAssignOp tree) {
		try {
			open(prec, TreeInfo.assignopPrec);
			printExpr(tree.lhs, TreeInfo.assignopPrec + 1);
			String opname = operatorName(treeTag(tree));
			print(" " + opname + " ");
			printExpr(tree.rhs, TreeInfo.assignopPrec);
			close(prec, TreeInfo.assignopPrec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitUnary(JCUnary tree) {
		try {
			int ownprec = isOwnPrec(tree);
			String opname = operatorName(treeTag(tree));
			open(prec, ownprec);
			if (isPrefixUnary(tree)) {
				print(opname);
				printExpr(tree.arg, ownprec);
			} else {
				printExpr(tree.arg, ownprec);
				print(opname);
			}
			close(prec, ownprec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private int isOwnPrec(JCExpression tree) {
		return treeTag(tree).getOperatorPrecedenceLevel();
	}
	
	private boolean isPrefixUnary(JCUnary tree) {
		return treeTag(tree).isPrefixUnaryOp();
	}
	
	public void visitBinary(JCBinary tree) {
		try {
			int ownprec = isOwnPrec(tree);
			String opname = operatorName(treeTag(tree));
			open(prec, ownprec);
			printExpr(tree.lhs, ownprec);
			print(" " + opname + " ");
			printExpr(tree.rhs, ownprec + 1);
			close(prec, ownprec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitTypeCast(JCTypeCast tree) {
		try {
			open(prec, TreeInfo.prefixPrec);
			print("(");
			printExpr(tree.clazz);
			print(")");
			printExpr(tree.expr, TreeInfo.prefixPrec);
			close(prec, TreeInfo.prefixPrec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitTypeTest(JCInstanceOf tree) {
		try {
			open(prec, TreeInfo.ordPrec);
			printExpr(tree.expr, TreeInfo.ordPrec);
			print(" instanceof ");
			printExpr(tree.clazz, TreeInfo.ordPrec + 1);
			close(prec, TreeInfo.ordPrec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitIndexed(JCArrayAccess tree) {
		try {
			printExpr(tree.indexed, TreeInfo.postfixPrec);
			print("[");
			printExpr(tree.index);
			print("]");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitSelect(JCFieldAccess tree) {
		try {
			printExpr(tree.selected, TreeInfo.postfixPrec);
			print("." + tree.name);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitIdent(JCIdent tree) {
		try {
			print(tree.name);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitLiteral(JCLiteral tree) {
		TypeTag typeTag = typeTag(tree);
		try {
			if (CTC_INT.equals(typeTag)) print(tree.value.toString());
			else if (CTC_LONG.equals(typeTag)) print(tree.value + "L");
			else if (CTC_FLOAT.equals(typeTag)) print(tree.value + "F");
			else if (CTC_DOUBLE.equals(typeTag)) print(tree.value.toString());
			else if (CTC_CHAR.equals(typeTag)) {
				print("\'" + Convert.quote(String.valueOf((char)((Number)tree.value).intValue())) + "\'");
			}
			else if (CTC_BOOLEAN.equals(typeTag)) print(((Number)tree.value).intValue() == 1 ? "true" : "false");
			else if (CTC_BOT.equals(typeTag)) print("null");
			else print("\"" + Convert.quote(tree.value.toString()) + "\"");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitTypeIdent(JCPrimitiveTypeTree tree) {
		TypeTag typetag = typeTag(tree);
		try {
			if (CTC_BYTE.equals(typetag)) print ("byte");
			else if (CTC_CHAR.equals(typetag)) print ("char");
			else if (CTC_SHORT.equals(typetag)) print ("short");
			else if (CTC_INT.equals(typetag)) print ("int");
			else if (CTC_LONG.equals(typetag)) print ("long");
			else if (CTC_FLOAT.equals(typetag)) print ("float");
			else if (CTC_DOUBLE.equals(typetag)) print ("double");
			else if (CTC_BOOLEAN.equals(typetag)) print ("boolean");
			else if (CTC_VOID.equals(typetag)) print ("void");
			else print("error");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitTypeArray(JCArrayTypeTree tree) {
		try {
			printBaseElementType(tree);
			printBrackets(tree);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	// Prints the inner element type of a nested array
	private void printBaseElementType(JCArrayTypeTree tree) throws IOException {
		JCTree elem = tree.elemtype;
		while (elem instanceof JCWildcard)
			elem = ((JCWildcard) elem).inner;
		if (elem instanceof JCArrayTypeTree)
			printBaseElementType((JCArrayTypeTree) elem);
		else
			printExpr(elem);
	}
	
	// prints the brackets of a nested array in reverse order
	private void printBrackets(JCArrayTypeTree tree) throws IOException {
		JCTree elem;
		while (true) {
			elem = tree.elemtype;
			print("[]");
			if (!(elem instanceof JCArrayTypeTree)) break;
			tree = (JCArrayTypeTree) elem;
		}
	}
	
	public void visitTypeApply(JCTypeApply tree) {
		try {
			printExpr(tree.clazz);
			print("<");
			printExprs(tree.arguments);
			print(">");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitTypeParameter(JCTypeParameter tree) {
		try {
			print(tree.name);
			if (tree.bounds.nonEmpty()) {
				print(" extends ");
				printExprs(tree.bounds, " & ");
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	public void visitWildcard(JCWildcard tree) {
		try {
			Object kind = tree.getClass().getField("kind").get(tree);
			print(kind);
			if (kind != null && kind.getClass().getSimpleName().equals("TypeBoundKind")) {
				kind = kind.getClass().getField("kind").get(kind);
			}
			
			if (tree.getKind() != Tree.Kind.UNBOUNDED_WILDCARD)
				printExpr(tree.inner);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void visitTypeBoundKind(TypeBoundKind tree) {
		try {
			print(String.valueOf(tree.kind));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitErroneous(JCErroneous tree) {
		try {
			print("(ERROR)");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitLetExpr(LetExpr tree) {
		try {
			print("(let " + tree.defs + " in " + tree.expr + ")");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitModifiers(JCModifiers mods) {
		try {
			printAnnotations(mods.annotations);
			printFlags(mods.flags);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitAnnotation(JCAnnotation tree) {
		try {
			print("@");
			printExpr(tree.annotationType);
			if (tree.args.nonEmpty()) {
				print("(");
				if (tree.args.length() == 1 && tree.args.get(0) instanceof JCAssign) {
					 JCExpression lhs = ((JCAssign)tree.args.get(0)).lhs;
					 if (lhs instanceof JCIdent && ((JCIdent)lhs).name.toString().equals("value")) tree.args = List.of(((JCAssign)tree.args.get(0)).rhs);
				}
				printExprs(tree.args);
				print(")");
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void visitTree(JCTree tree) {
		try {
			String simpleName = tree.getClass().getSimpleName();
			if ("JCTypeUnion".equals(simpleName)) {
				print(tree.toString());
				return;
			} else if ("JCTypeIntersection".equals(simpleName)) {
				print(tree.toString());
				return;
			} else {
				print("(UNKNOWN: " + tree + ")");
				println();
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
