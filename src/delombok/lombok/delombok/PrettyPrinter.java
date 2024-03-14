/*
 * Copyright (C) 2016-2024 The Project Lombok Authors.
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
package lombok.delombok;

import static com.sun.tools.javac.code.Flags.*;
import static lombok.javac.Javac.*;
import static lombok.javac.JavacTreeMaker.TreeTag.treeTag;
import static lombok.javac.JavacTreeMaker.TypeTag.typeTag;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sun.tools.javac.tree.DocCommentTable;
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
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Position;

import lombok.javac.CommentInfo;
import lombok.javac.Javac;
import lombok.javac.PackageName;
import lombok.permit.Permit;
import lombok.javac.CommentInfo.EndConnection;
import lombok.javac.CommentInfo.StartConnection;
import lombok.javac.JavacTreeMaker.TreeTag;
import lombok.javac.JavacTreeMaker.TypeTag;

public class PrettyPrinter extends JCTree.Visitor {
	private static final String LINE_SEP = System.getProperty("line.separator");
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
	
	private final Writer out;
	private final JCCompilationUnit compilationUnit;
	private List<CommentInfo> comments;
	private final int[] textBlockStarts;
	private final FormatPreferences formatPreferences;
	
	private final Map<JCTree, String> docComments;
	private final DocCommentTable docTable;
	private int indent = 0;
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public PrettyPrinter(Writer out, JCCompilationUnit cu, List<CommentInfo> comments, int[] textBlockStarts, FormatPreferences preferences) {
		this.out = out;
		this.comments = comments;
		this.textBlockStarts = textBlockStarts;
		this.compilationUnit = cu;
		this.formatPreferences = preferences;
		
		/* load doc comments */ {
			Object dc = getDocComments(compilationUnit);
			if (dc instanceof Map<?, ?>) {
				this.docComments = (Map) dc;
				this.docTable = null;
			} else if (dc instanceof DocCommentTable) {
				this.docComments = null;
				this.docTable = (DocCommentTable) dc;
			} else {
				this.docComments = null;
				this.docTable = null;
			}
		}
	}
	
	private int endPos(JCTree tree) {
		return getEndPosition(tree, compilationUnit);
	}
	
	private boolean needsAlign, needsNewLine, onNewLine = true, needsSpace, aligned;
	
	public static final class UncheckedIOException extends RuntimeException {
		UncheckedIOException(IOException source) {
			super(toMsg(source));
			setStackTrace(source.getStackTrace());
		}
		
		private static String toMsg(Throwable t) {
			String msg = t.getMessage();
			String n = t.getClass().getSimpleName();
			if (msg == null || msg.isEmpty()) return n;
			return n + ": " + msg;
		}
	}
	
	private void align() {
		if (!onNewLine) return;
		try {
			for (int i = 0; i < indent; i++) out.write(formatPreferences.indent());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		onNewLine = false;
		aligned = true;
		needsAlign = false;
	}
	
	private void print(JCTree tree) {
		if (tree == null) {
			print("/*missing*/");
			return;
		}
		
		consumeComments(tree);
		tree.accept(this);
		consumeTrailingComments(endPos(tree));
	}
	
	private void print(List<? extends JCTree> trees, String infix) {
		boolean first = true;
		JCTree prev = null;
		for (JCTree tree : trees) {
			if (suppress(tree)) continue;
			if (!first && infix != null && !infix.isEmpty()) {
				if ("\n".equals(infix)) println(prev);
				else print(infix);
			}
			first = false;
			print(tree);
			prev = tree;
		}
	}
	
	private boolean suppress(JCTree tree) {
		if (tree instanceof JCBlock) {
			JCBlock block = (JCBlock) tree;
			return (Position.NOPOS == block.pos) && block.stats.isEmpty();
		}
		
		if (tree instanceof JCExpressionStatement) {
			JCExpression expr = ((JCExpressionStatement)tree).expr;
			if (expr instanceof JCMethodInvocation) {
				JCMethodInvocation inv = (JCMethodInvocation) expr;
				if (!inv.typeargs.isEmpty() || !inv.args.isEmpty()) return false;
				if (!(inv.meth instanceof JCIdent)) return false;
				if (tree.pos != expr.pos) return false; // Explicit super call
				return ((JCIdent) inv.meth).name.toString().equals("super");
			}
		}
		
		return false;
	}
	
	private void print(Name name) {
		if (name.isEmpty()) {
			print("_");
		} else {
			print(name.toString());
		}
	}
	
	private void print(CharSequence s) {
		boolean align = needsAlign;
		if (needsNewLine && !onNewLine) println();
		if (align && !aligned) align();
		try {
			if (needsSpace && !onNewLine && !aligned) out.write(' ');
			out.write(s.toString());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		needsSpace = false;
		onNewLine = false;
		aligned = false;
	}
	
	private void println() {
		try {
			out.write(LINE_SEP);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		onNewLine = true;
		aligned = false;
		needsNewLine = false;
	}
	
	private void println(JCTree completed) {
		if (completed != null) {
			int endPos = endPos(completed);
			consumeTrailingComments(endPos);
		}
		try {
			out.write(LINE_SEP);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		onNewLine = true;
		aligned = false;
		needsNewLine = false;
	}
	
	private void println(CharSequence s) {
		print(s);
		println();
	}
	
	private void println(CharSequence s, JCTree completed) {
		print(s);
		println(completed);
	}
	
	private void aPrint(CharSequence s) {
		align();
		print(s);
	}
	
	private void aPrintln(CharSequence s) {
		align();
		print(s);
		println();
	}
	
	private void aPrintln(CharSequence s, JCTree completed) {
		align();
		print(s);
		println(completed);
	}
	
	private void consumeComments(int until) {
		CommentInfo head = comments.head;
		while (comments.nonEmpty() && head.pos < until) {
			printComment(head);
			comments = comments.tail;
			head = comments.head;
		}
	}
	
	private void consumeComments(JCTree tree) {
		consumeComments(tree.pos);
	}
	
	private void consumeTrailingComments(int from) {
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
	
	private String getJavadocFor(JCTree node) {
		if (docComments != null) return docComments.get(node);
		if (docTable != null) return docTable.getCommentText(node);
		return null;
	}
	
	private int dims(JCExpression vartype) {
		if (vartype instanceof JCArrayTypeTree) {
			return 1 + dims(((JCArrayTypeTree) vartype).elemtype);
		} else if (isJcAnnotatedType(vartype)) {
			JCTree underlyingType = readObject(vartype, "underlyingType", (JCTree) null);
			if (underlyingType instanceof JCArrayTypeTree) return 1 + dims (((JCArrayTypeTree) underlyingType).elemtype);
		}
		
		return 0;
	}
	
	private void printComment(CommentInfo comment) {
		switch (comment.start) {
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
			if (!onNewLine) {
				needsNewLine = true;
				needsAlign = true;
			} else if (!aligned) {
				needsAlign = true;
			}
			break;
		}
		
		if (onNewLine && !aligned && comment.start != StartConnection.START_OF_LINE) needsAlign = true;
		
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
	
	private void printDocComment(JCTree tree) {
		String dc = getJavadocFor(tree);
		if (dc == null) return;
		
		aPrintln("/**");
		boolean atStart = true;
		
		for (String line : dc.split("\\r?\\n")) {
			if (atStart && line.trim().isEmpty()) {
				atStart = false;
				continue;
			}
			
			atStart = false;
			aPrint(" *");
			if (!line.isEmpty() && !Character.isWhitespace(line.charAt(0))) {
				print(" ");
			}
			
			println(line);
		}
		
		aPrintln(" */");
	}
	
	private Name __INIT__, __VALUE__;
	private Name name_init(Name someName) {
		if (__INIT__ == null) __INIT__ = someName.table.fromChars("<init>".toCharArray(), 0, 6);
		return __INIT__;
	}
	private Name name_value(Name someName) {
		if (__VALUE__ == null) __VALUE__ = someName.table.fromChars("value".toCharArray(), 0, 5);
		return __VALUE__;
	}
	
	@Override public void visitTopLevel(JCCompilationUnit tree) {
		printDocComment(tree);
		JCTree n = PackageName.getPackageNode(tree);
		if (n != null) {
			consumeComments(tree);
			aPrint("package ");
			print(n);
			println(";", n);
		}
		
		boolean first = true;
		
		for (JCTree child : tree.defs) {
			if (!(child instanceof JCImport)) continue;
			if (first) println();
			first = false;
			print(child);
		}
		
		for (JCTree child : tree.defs) {
			if (child instanceof JCImport) continue;
			print(child);
		}
		consumeComments(Integer.MAX_VALUE);
	}
	
	@Override public void visitImport(JCImport tree) {
		JCTree qualid = Javac.getQualid(tree);
		if (qualid instanceof JCFieldAccess) {
			JCFieldAccess fa = ((JCFieldAccess) qualid);
			if (fa.name.length() == 1 && fa.name.contentEquals("*")) {
				if (fa.selected instanceof JCFieldAccess) {
					JCFieldAccess lombokExperimental = (JCFieldAccess) fa.selected;
					if (lombokExperimental.name.contentEquals("experimental") && lombokExperimental.selected instanceof JCIdent && ((JCIdent) lombokExperimental.selected).name.contentEquals("lombok")) {
						// do not ever print lombok.experimental.*.
						return;
					}
				}
			}
		}
		
		aPrint("import ");
		if (tree.staticImport) print("static ");
		print(qualid);
		println(";", tree);
	}
	
	private Name currentTypeName;
	@Override public void visitClassDef(JCClassDecl tree) {
		println();
		printDocComment(tree);
		align();
		print(tree.mods);
		
		boolean isInterface = (tree.mods.flags & INTERFACE) != 0;
		boolean isAnnotationInterface = isInterface && (tree.mods.flags & ANNOTATION) != 0;
		boolean isEnum = (tree.mods.flags & ENUM) != 0;
		boolean isRecord = (tree.mods.flags & RECORD) != 0;
		
		if (isAnnotationInterface) print("@interface ");
		else if (isInterface) print("interface ");
		else if (isEnum) print("enum ");
		else if (isRecord) print("record ");
		else print("class ");
		
		print(tree.name);
		Name prevTypeName = currentTypeName;
		currentTypeName = tree.name;
		
		if (tree.typarams.nonEmpty()) {
			print("<");
			print(tree.typarams, ", ");
			print(">");
		}
		
		if (isRecord) printRecordConstructor(tree.defs);
		
		JCTree extendsClause = getExtendsClause(tree);
		if (extendsClause != null) {
			print(" extends ");
			print(extendsClause);
		}
		
		if (tree.implementing.nonEmpty()) {
			print(isInterface ? " extends " : " implements ");
			print(tree.implementing, ", ");
		}
		
		List<JCExpression> permitting = readObject(tree, "permitting", List.<JCExpression>nil());
		if (permitting.nonEmpty()) {
			print(" permits ");
			print(permitting, ", ");
		}
		
		println(" {");
		indent++;
		printClassMembers(tree.defs, isEnum, isInterface);
		consumeComments(endPos(tree));
		indent--;
		aPrintln("}", tree);
		currentTypeName = prevTypeName;
	}
	
	private void printRecordConstructor(List<JCTree> members) {
		boolean first = true;
		print("(");
		for (JCTree member : members) {
			if (member instanceof JCVariableDecl) {
			JCVariableDecl variableDecl = (JCVariableDecl) member;
				if ((variableDecl.mods.flags & GENERATED_MEMBER) != 0) {
					if (!first) print(", ");
					first = false;
					printAnnotations(variableDecl.mods.annotations, false);
					printVarDef0(variableDecl);
				}
			}
		}
		print(")");
	}
	
	private void printClassMembers(List<JCTree> members, boolean isEnum, boolean isInterface) {
		Class<?> prefType = null;
		int typeOfPrevEnumMember = isEnum ? 3 : 0; // 1 = normal, 2 = with body, 3 = no enum field yet.
		boolean prevWasEnumMember = isEnum;
		
		for (JCTree member : members) {
			if (typeOfPrevEnumMember == 3 && member instanceof JCMethodDecl && (((JCMethodDecl) member).mods.flags & GENERATEDCONSTR) != 0) continue;
			boolean isEnumVar = isEnum && member instanceof JCVariableDecl && (((JCVariableDecl) member).mods.flags & ENUM) != 0;
			if (!isEnumVar && prevWasEnumMember) {
				prevWasEnumMember = false;
				if (typeOfPrevEnumMember == 3) align();
				println(";");
			}
			
			if (isEnumVar) {
				if (prefType != null && prefType != JCVariableDecl.class) println();
				switch (typeOfPrevEnumMember) {
				case 1:
					print(", ");
					break;
				case 2:
					println(",");
					align();
					break;
				}
				print(member);
				JCTree init = ((JCVariableDecl) member).init;
				typeOfPrevEnumMember = init instanceof JCNewClass && ((JCNewClass) init).def != null ? 2 : 1;
			} else if (member instanceof JCVariableDecl) {
				if ((((JCVariableDecl) member).mods.flags & GENERATED_MEMBER) != 0) continue;
				if (prefType != null && prefType != JCVariableDecl.class) println();
				if (isInterface) flagMod = -1L & ~(PUBLIC | STATIC | FINAL);
				print(member);
			} else if (member instanceof JCMethodDecl) {
				if ((((JCMethodDecl) member).mods.flags & GENERATEDCONSTR) != 0) continue;
				if (prefType != null) println();
				if (isInterface) flagMod = -1L & ~(PUBLIC | ABSTRACT);
				print(member);
			} else if (member instanceof JCClassDecl) {
				if (prefType != null) println();
				if (isInterface) flagMod = -1L & ~(PUBLIC | STATIC);
				print(member);
			} else {
				if (prefType != null) println();
				print(member);
			}
			
			prefType = member.getClass();
		}
		
		if (prevWasEnumMember) {
			prevWasEnumMember = false;
			if (typeOfPrevEnumMember == 3) align();
			println(";");
		}
	}
	
	@Override public void visitTypeParameter(JCTypeParameter tree) {
		List<JCExpression> annotations = readObject(tree, "annotations", List.<JCExpression>nil());
		if (!annotations.isEmpty()) {
			print(annotations, " ");
			print(" ");
		}
		print(tree.name);
		if (tree.bounds.nonEmpty()) {
			print(" extends ");
			print(tree.bounds, " & ");
		}
		consumeComments(tree);
	}
	
	@Override public void visitVarDef(JCVariableDecl tree) {
		printDocComment(tree);
		align();
		if ((tree.mods.flags & ENUM) != 0) {
			printEnumMember(tree);
			return;
		}
		printAnnotations(tree.mods.annotations, true);
		printModifierKeywords(tree.mods);
		printVarDef0(tree);
		println(";", tree);
	}
	
	private void printVarDefInline(JCVariableDecl tree) {
		printAnnotations(tree.mods.annotations, false);
		printModifierKeywords(tree.mods);
		printVarDef0(tree);
	}
	
	private boolean innermostArrayBracketsAreVarargs = false;
	private void printVarDef0(JCVariableDecl tree) {
		boolean varargs = (tree.mods.flags & VARARGS) != 0;
		
		/* story time!
		 
		 in 'new int[5][6];', the 5 is the outermost and the 6 is the innermost: That means: 5 int arrays, each capable of containing 6 elements.
		 But that's actually a crazy way to read it; you'd think that in FOO[], you should interpret that as 'an array of FOO', but that's not correct;
		 if FOO is for example 'int[]', it's: "Modify the component type of FOO to be an array of whatever it was before.. unless FOO isn't an array, in which case,
		 this is an array of FOO". Which is weird.
		 
		 This is particularly poignant with vargs. In: "int[]... x", the ... are actually the _INNER_ type even though varargs by definition is a modification of
		 how to interpret the outer. The JLS just sort of lets that be: To indicate varargs, replace the lexically last [] with dots even though that's the wrong
		 [] to modify!
		 
		 This becomes an utter shambles when annotations-on-arrays become involved. The annotation on the INNER most type is to be placed right before the ...;
		 and because of that, we have to do crazy stuff with this innermostArrayBracketsAreVarargs flag.
		 */
		try {
			innermostArrayBracketsAreVarargs = varargs;
			if (tree.vartype == null || tree.vartype.pos == -1) {
				print("var");
			} else {
				print(tree.vartype);
			}
		} finally {
			innermostArrayBracketsAreVarargs = false;
		}
		print(" ");
		print(tree.name);
		if (tree.init != null) {
			print(" = ");
			print(tree.init);
		}
	}
	
	private void printEnumMember(JCVariableDecl tree) {
		printAnnotations(tree.mods.annotations, true);
		print(tree.name);
		if (tree.init instanceof JCNewClass) {
			JCNewClass constructor = (JCNewClass) tree.init;
			if (constructor.args != null && constructor.args.nonEmpty()) {
				print("(");
				print(constructor.args, ", ");
				print(")");
			}
			
			if (constructor.def != null && constructor.def.defs != null) {
				println(" {");
				indent++;
				printClassMembers(constructor.def.defs, false, false);
				consumeComments(endPos(tree));
				indent--;
				aPrint("}");
			}
		}
	}
	
	// TODO: Test postfix syntax for methods (?), for decls. Multiline vardefs, possibly with comments. enums with bodies. constructor-local generics, method-local generics, also do/while, finally, try-with-resources, lambdas, annotations in java8 places...
	// TODO: Whatever is JCAnnotatedType? We handle it in the 7+ bucket in the old one...
	
	@Override public void visitTypeApply(JCTypeApply tree) {
		print(tree.clazz);
		print("<");
		boolean temp = innermostArrayBracketsAreVarargs;
		innermostArrayBracketsAreVarargs = false;
		print(tree.arguments, ", ");
		innermostArrayBracketsAreVarargs = temp;
		print(">");
	}
	
	@Override public void visitWildcard(JCWildcard tree) {
		switch (tree.getKind()) {
		default:
		case UNBOUNDED_WILDCARD:
			print("?");
			return;
		case EXTENDS_WILDCARD:
			print("? extends ");
			print(tree.inner);
			return;
		case SUPER_WILDCARD:
			print("? super ");
			print(tree.inner);
			return;
		}
	}
	
	@Override public void visitLiteral(JCLiteral tree) {
		TypeTag typeTag = typeTag(tree);
		if (CTC_INT.equals(typeTag)) print("" + tree.value);
		else if (CTC_LONG.equals(typeTag)) print(tree.value + "L");
		else if (CTC_FLOAT.equals(typeTag)) print(tree.value + "F");
		else if (CTC_DOUBLE.equals(typeTag)) print("" + tree.value);
		else if (CTC_CHAR.equals(typeTag)) {
			print("\'" + quoteChar((char)((Number)tree.value).intValue()) + "\'");
		}
		else if (CTC_BOOLEAN.equals(typeTag)) print(((Number)tree.value).intValue() == 1 ? "true" : "false");
		else if (CTC_BOT.equals(typeTag)) print("null");
		else {
			if (Arrays.binarySearch(textBlockStarts, tree.pos) < 0) {
				print("\"" + quoteChars(tree.value.toString()) + "\"");
			} else {
				printTextBlock(tree.value.toString());
			}
		}
	}
	
	private void printTextBlock(String s) {
		println("\"\"\"");
		needsAlign = true;
		indent++;
		StringBuilder sb = new StringBuilder();
		boolean lineStart = true;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != ' ' && c != '\t') lineStart = false;
			if (c == '\n') {
				println(sb);
				sb.setLength(0);
				needsAlign = true;
				lineStart = true;
				continue;
			}
			if (c == '\t' && lineStart) sb.append("\t");
			else sb.append(quoteChar(s.charAt(i)));
		}
		print(sb);
		print("\"\"\"");
		indent--;
	}
	
	@Override public void visitMethodDef(JCMethodDecl tree) {
		boolean isConstructor = tree.name == name_init(tree.name);
		if (isConstructor && (tree.mods.flags & GENERATEDCONSTR) != 0) return;
		printDocComment(tree);
		align();
		print(tree.mods);
		if (tree.typarams != null && tree.typarams.nonEmpty()) {
			print("<");
			print(tree.typarams, ", ");
			print("> ");
		}
		
		if (isConstructor) {
			print(currentTypeName == null ? "<init>" : currentTypeName);
		} else {
			print(tree.restype);
			print(" ");
			print(tree.name);
		}
		
		boolean argsLessConstructor = false;
		if (isConstructor && (tree.mods.flags & COMPACT_RECORD_CONSTRUCTOR) != 0) {
			argsLessConstructor = true;
		}
		
		boolean first = true;
		if (!argsLessConstructor) {
			print("(");
			
			JCVariableDecl recvparam = readObject(tree, "recvparam", null);
			if (recvparam != null) {
				printVarDefInline(recvparam);
				first = false;
			}
			
			for (JCVariableDecl param : tree.params) {
				if (!first) print(", ");
				first = false;
				printVarDefInline(param);
			}
			print(")");
		}
		
		if (tree.thrown.nonEmpty()) {
			print(" throws ");
			print(tree.thrown, ", ");
		}
		
		if (tree.defaultValue != null) {
			print(" default ");
			print(tree.defaultValue);
		}
		
		if (tree.body != null) {
			print(" ");
			print(tree.body);
		} else println(";", tree);
	}
	
	@Override public void visitSkip(JCSkip that) {
		if (onNewLine && !aligned) {
			align();
		}
		println(";");
	}
	
	@Override public void visitAnnotation(JCAnnotation tree) {
		print("@");
		print(tree.annotationType);
		if (tree.args.isEmpty()) return;
		print("(");
		boolean done = false;
		if (tree.args.length() == 1 && tree.args.get(0) instanceof JCAssign) {
			JCAssign arg1 = (JCAssign) tree.args.get(0);
			JCIdent arg1Name = arg1.lhs instanceof JCIdent ? ((JCIdent) arg1.lhs) : null;
			if (arg1Name != null && arg1Name.name == name_value(arg1Name.name)) {
				print(arg1.rhs);
				done = true;
			}
		}
		if (!done) print(tree.args, ", ");
		print(")");
	}
	
	@Override public void visitTypeArray(JCArrayTypeTree tree) {
		printTypeArray0(tree);
	}
	
	@Override public void visitNewArray(JCNewArray tree) {
		JCTree elem = tree.elemtype;
		int dims = 0;
		if (elem != null) {
			print("new ");
			
			while (elem instanceof JCArrayTypeTree) {
				dims++;
				elem = ((JCArrayTypeTree) elem).elemtype;
			}
			print(elem);
			
			for (JCExpression expr : tree.dims) {
				print("[");
				print(expr);
				print("]");
			}
		}
		
		for (int i = 0; i < dims; i++) print("[]");
		
		if (tree.elems != null) {
			if (elem != null) print("[] ");
			print("{");
			print(tree.elems, ", ");
			print("}");
		}
	}
	
	@Override public void visitNewClass(JCNewClass tree) {
		if (tree.encl != null) {
			print(tree.encl);
			print(".");
		}
		boolean moveFirstParameter = tree.args.nonEmpty() && tree.args.head instanceof JCUnary && tree.args.head.toString().startsWith("<*nullchk*>");
		if (moveFirstParameter) {
			print(((JCUnary) tree.args.head).arg);
			print(".");
		}
		
		print("new ");
		if (!tree.typeargs.isEmpty()) {
			print("<");
			print(tree.typeargs, ", ");
			print(">");
		}
		print(tree.clazz);
		print("(");
		if (moveFirstParameter) {
			print(tree.args.tail, ", ");
		} else {
			print(tree.args, ", ");
		}
		print(")");
		if (tree.def != null) {
			Name previousTypeName = currentTypeName;
			currentTypeName = null;
			println(" {");
			indent++;
			print(tree.def.defs, "");
			indent--;
			aPrint("}");
			currentTypeName = previousTypeName;
		}
	}
	
	@Override public void visitIndexed(JCArrayAccess tree) {
		print(tree.indexed);
		print("[");
		print(tree.index);
		print("]");
	}
	
	@Override public void visitTypeIdent(JCPrimitiveTypeTree tree) {
		TypeTag typeTag = typeTag(tree);
		
		if (CTC_BYTE.equals(typeTag)) print("byte");
		else if (CTC_CHAR.equals(typeTag)) print("char");
		else if (CTC_SHORT.equals(typeTag)) print("short");
		else if (CTC_INT.equals(typeTag)) print("int");
		else if (CTC_LONG.equals(typeTag)) print("long");
		else if (CTC_FLOAT.equals(typeTag)) print("float");
		else if (CTC_DOUBLE.equals(typeTag)) print("double");
		else if (CTC_BOOLEAN.equals(typeTag)) print("boolean");
		else if (CTC_VOID.equals(typeTag)) print("void");
		else print("error");
	}
	
	@Override public void visitLabelled(JCLabeledStatement tree) {
		aPrint(tree.label);
		print(":");
		if (tree.body instanceof JCSkip || suppress(tree)) {
			println(" ;", tree);
		} else if (tree.body instanceof JCBlock) {
			print(" ");
			print(tree.body);
		} else {
			println(tree);
			print(tree.body);
		}
	}
	
	private long flagMod = -1L;
	private static final long DEFAULT = 1L<<43;
	
	@Override public void visitModifiers(JCModifiers tree) {
		printAnnotations(tree.annotations, true);
		printModifierKeywords(tree);
	}
	
	private void printAnnotations(List<JCAnnotation> annotations, boolean newlines) {
		for (JCAnnotation ann : annotations) {
			print(ann);
			if (newlines) {
				println();
				align();
			} else print(" ");
		}
	}
	
	private void printModifierKeywords(JCModifiers tree) {
		long v = flagMod & tree.flags;
		flagMod = -1L;
		
		if ((v & SYNTHETIC) != 0) print("/* synthetic */ ");
		if ((v & PUBLIC) != 0) print("public ");
		if ((v & PRIVATE) != 0) print("private ");
		if ((v & PROTECTED) != 0) print("protected ");
		if ((v & STATIC) != 0) print("static ");
		if ((v & FINAL) != 0) print("final ");
		if ((v & SYNCHRONIZED) != 0) print("synchronized ");
		if ((v & VOLATILE) != 0) print("volatile ");
		if ((v & TRANSIENT) != 0) print("transient ");
		if ((v & NATIVE) != 0) print("native ");
		if ((v & ABSTRACT) != 0) print("abstract ");
		if ((v & SEALED) != 0) print("sealed ");
		if ((v & NON_SEALED) != 0) print("non-sealed ");
		if ((v & STRICTFP) != 0) print("strictfp ");
		if ((v & DEFAULT) != 0 && (v & INTERFACE) == 0) print("default ");
	}
	
	@Override public void visitSelect(JCFieldAccess tree) {
		print(tree.selected);
		print(".");
		print(tree.name);
	}
	
	@Override public void visitIdent(JCIdent tree) {
		print(tree.name);
	}
	
	@Override public void visitApply(JCMethodInvocation tree) {
		if (tree.typeargs.nonEmpty()) {
			if (tree.meth instanceof JCFieldAccess) {
				JCFieldAccess fa = (JCFieldAccess) tree.meth;
				print(fa.selected);
				print(".<");
				print(tree.typeargs, ", ");
				print(">");
				print(fa.name);
			} else {
				print("<");
				print(tree.typeargs, ", ");
				print(">");
				print(tree.meth);
			}
		} else {
			print(tree.meth);
		}
		
		print("(");
		print(tree.args, ", ");
		print(")");
	}
	
	@Override public void visitAssert(JCAssert tree) {
		aPrint("assert ");
		print(tree.cond);
		if (tree.detail != null) {
			print(" : ");
			print(tree.detail);
		}
		println(";", tree);
	}
	
	@Override public void visitAssign(JCAssign tree) {
		print(tree.lhs);
		print(" = ");
		print(tree.rhs);
	}
	
	@Override public void visitAssignop(JCAssignOp tree) {
		print(tree.lhs);
		String opname = operator(treeTag(tree));
		print(" " + opname + " ");
		print(tree.rhs);
	}
	
	private static final int PREFIX = 14;
	
	@Override public void visitUnary(JCUnary tree) {
		String op = operator(treeTag(tree));
		if (treeTag(tree).getOperatorPrecedenceLevel() == PREFIX) {
			print(op);
			print(tree.arg);
		} else {
			print(tree.arg);
			print(op);
		}
	}
	
	@Override public void visitBinary(JCBinary tree) {
		String op = operator(treeTag(tree));
		print(tree.lhs);
		print(" ");
		print(op);
		print(" ");
		print(tree.rhs);
	}
	
	@Override public void visitTypeTest(JCInstanceOf tree) {
		print(tree.expr);
		print(" instanceof ");
		
		/** With java14, instead of a type (field 'clazz'), there's now a pattern (field 'pattern', of type JCTree, which is either a JCPattern for new-style instanceof, or if not it's the same as what 'clazz' held) */
		JCTree c = readObject(tree, "clazz", null); // JDK-13
		if (c == null) c = readObject(tree, "pattern", null); // JDK14+
		print(c);
	}
	
	@Override public void visitTypeCast(JCTypeCast tree) {
		print("(");
		print(tree.clazz);
		print(") ");
		print(tree.expr);
	}
	
	@Override public void visitBlock(JCBlock tree) {
		if (tree.pos == Position.NOPOS && tree.stats.isEmpty()) return;
		if (onNewLine) align();
		if ((tree.flags & STATIC) != 0) print("static ");
		println("{");
		indent++;
		print(tree.stats, "");
		consumeComments(endPos(tree));
		indent--;
		aPrintln("}", tree);
	}
	
	@Override public void visitBreak(JCBreak tree) {
		aPrint("break");
		
		JCExpression value = readObject(tree, "value", null); // JDK 12+
		if (value != null) {
			print(" ");
			print(value);
		} else {
			Name label = readObject(tree, "label", null);
			if (label != null) {
				print(" ");
				print(label);
			}
		}
		println(";", tree);
	}
	
	@Override public void visitContinue(JCContinue tree) {
		aPrint("continue");
		if (tree.label != null) {
			print(" ");
			print(tree.label);
		}
		println(";", tree);
	}
	
	@Override public void visitConditional(JCConditional tree) {
		print(tree.cond);
		print(" ? ");
		print(tree.truepart);
		print(" : ");
		print(tree.falsepart);
	}
	
	@Override public void visitParens(JCParens tree) {
		print("(");
		print(tree.expr);
		print(")");
	}
	
	@Override public void visitReturn(JCReturn tree) {
		aPrint("return");
		if (tree.expr != null) {
			print(" ");
			print(tree.expr);
		}
		println(";", tree);
	}
	
	@Override public void visitThrow(JCThrow tree) {
		aPrint("throw ");
		print(tree.expr);
		println(";", tree);
	}
	
	@Override public void visitWhileLoop(JCWhileLoop tree) {
		aPrint("while ");
		if (tree.cond instanceof JCParens) {
			print(tree.cond);
		} else {
			print("(");
			print(tree.cond);
			print(")");
		}
		print(" ");
		print(tree.body);
		// make sure to test while (true) ; and while(true){} and while(true) x = 5;
	}
	
	@Override public void visitForLoop(JCForLoop tree) {
		aPrint("for (");
		if (tree.init.nonEmpty()) {
			// ForInit is either a StatementExpressionList or a LocalVariableDeclaration
			if (tree.init.head instanceof JCVariableDecl) {
				boolean first = true;
				int dims = 0;
				for (JCStatement i : tree.init) {
					JCVariableDecl vd = (JCVariableDecl) i;
					if (first) {
						printVarDefInline(vd);
						dims = dims(vd.vartype);
					} else {
						print(", ");
						print(vd.name);
						int dimDiff = dims(vd.vartype) - dims;
						for (int j = 0; j < dimDiff; j++) print("[]");
						if (vd.init != null) {
							print(" = ");
							print(vd.init);
						}
					}
					first = false;
				}
			} else {
				boolean first = true;
				for (JCStatement exprStatement : tree.init) {
					if (!first) print(", ");
					first = false;
					print(((JCExpressionStatement) exprStatement).expr);
				}
			}
		}
		print("; ");
		if (tree.cond != null) print(tree.cond);
		print("; ");
		boolean first = true;
		for (JCExpressionStatement exprStatement : tree.step) {
			if (!first) print(", ");
			first = false;
			print(exprStatement.expr);
		}
		print(") ");
		print(tree.body);
	}
	
	@Override public void visitForeachLoop(JCEnhancedForLoop tree) {
		aPrint("for (");
		JCTree varOrRecordPattern = readObject(tree, "varOrRecordPattern", null);
		if (varOrRecordPattern instanceof JCVariableDecl) {
			printVarDefInline((JCVariableDecl) varOrRecordPattern);
		} else if (varOrRecordPattern != null) {
			print(varOrRecordPattern);
		} else {
			printVarDefInline(tree.var);
		}
		print(" : ");
		print(tree.expr);
		print(") ");
		print(tree.body);
	}
	
	@Override public void visitIf(JCIf tree) {
		aPrint("if ");
		if (tree.cond instanceof JCParens) {
			print(tree.cond);
		} else {
			print("(");
			print(tree.cond);
			print(")");
		}
		print(" ");
		if (tree.thenpart instanceof JCBlock) {
			println("{");
			indent++;
			print(((JCBlock) tree.thenpart).stats, "");
			indent--;
			if (tree.elsepart == null) {
				aPrintln("}", tree);
			} else {
				aPrint("}");
			}
		} else {
			print(tree.thenpart);
		}
		if (tree.elsepart != null) {
			aPrint(" else ");
			print(tree.elsepart);
		}
	}
	
	@Override public void visitExec(JCExpressionStatement tree) {
		align();
		print(tree.expr);
		println(";", tree);
	}
	
	@Override public void visitDoLoop(JCDoWhileLoop tree) {
		aPrint("do ");
		if (tree.body instanceof JCBlock) {
			println("{");
			indent++;
			print(((JCBlock) tree.body).stats, "");
			indent--;
			aPrint("}");
			
		} else print(tree.body);
		print(" while ");
		if (tree.cond instanceof JCParens) {
			print(tree.cond);
		} else {
			print("(");
			print(tree.cond);
			print(")");
		}
		println(";", tree);
	}
	
	@Override public void visitSynchronized(JCSynchronized tree) {
		aPrint("synchronized ");
		if (tree.lock instanceof JCParens) {
			print(tree.lock);
		} else {
			print("(");
			print(tree.lock);
			print(")");
		}
		print(" ");
		print(tree.body);
	}
	
	@Override public void visitCase(JCCase tree) {
		// Starting with JDK12, switches allow multiple expressions per case, and can take the form of an expression (preview feature).
		
		List<JCTree> pats = readObject(tree, "labels", null); // JDK 17+
		if (pats == null) {
			pats = readObject(tree, "pats", null); // JDK 12-17
		}
		if (pats == null) {
			JCTree pat = readObject(tree, "pat", null); // JDK -11
			pats = pat == null ? List.<JCTree>nil() : List.of(pat);
		}
		
		if (pats.isEmpty() || pats.size() == 1 && pats.head.getClass().getName().endsWith("$JCDefaultCaseLabel")) {
			aPrint("default");
		} else {
			aPrint("case ");
			print(pats, ", ");
			
			JCExpression guard = readObject(tree, "guard", null); // JDK 21+
			if (guard != null) {
				print(" when ");
				print(guard);
			}
		}
		
		Enum<?> caseKind = readObject(tree, "caseKind", null); // JDK 12+
		
		if (caseKind != null && caseKind.name().equalsIgnoreCase("RULE")) {
			print(" -> ");
			if (tree.stats.head instanceof JCBreak) {
				JCBreak b = (JCBreak) tree.stats.head;
				print((JCExpression) readObject(b, "value", null));
				print(";");
				needsNewLine = true;
				needsAlign = true;
			} else if (tree.stats.head.getClass().getName().endsWith("$JCYield")) {
				print((JCExpression) readObject(tree.stats.head, "value", null));
				print(";");
				needsNewLine = true;
				needsAlign = true;
			} else {
				print(tree.stats.head);
				if (tree.stats.head instanceof JCBlock) needsNewLine = false;
			}
		} else {
			println(": ");
			indent++;
			print(tree.stats, "");
			indent--;
		}
	}
	
	@Override public void visitCatch(JCCatch tree) {
		print(" catch (");
		print(tree.param);
		print(") ");
		print(tree.body);
	}
	
	@Override public void visitSwitch(JCSwitch tree) {
		aPrint("switch ");
		if (tree.selector instanceof JCParens) {
			print(tree.selector);
		} else {
			print("(");
			print(tree.selector);
			print(")");
		}
		println(" {");
		boolean ruleStyle = isCaseRuleStyle(tree.cases.head);
		if (ruleStyle) indent++;
		print(tree.cases, "");
		if (ruleStyle) indent--;
		aPrintln("}", tree);
	}
	
	void printSwitchExpression(JCTree tree) {
		aPrint("switch ");
		JCExpression selector = readObject(tree, "selector", null);
		if (selector instanceof JCParens) {
			print(selector);
		} else {
			print("(");
			print(selector);
			print(")");
		}
		println(" {");
		List<JCCase> cases = readObject(tree, "cases", null);
		boolean ruleStyle = isCaseRuleStyle(cases.head);
		if (ruleStyle) indent++;
		print(cases, "");
		if (ruleStyle) indent--;
		aPrint("}");
	}
	
	void printYieldExpression(JCTree tree) {
		aPrint("yield ");
		JCExpression value = readObject(tree, "value", null);
		print(value);
		println(";", tree);
	}
	
	void printBindingPattern(JCTree tree) {
		JCTree var = readObject(tree, "var", tree);
		if (var instanceof JCVariableDecl) {
			printVarDef0((JCVariableDecl) var);
		} else {
			print((JCExpression) readObject(var, "vartype", null));
			print(" ");
			print((Name) readObject(var, "name", null));
		}
	}
	
	void printDefaultCase(JCTree tree) {
		print("default");
	}
	
	void printGuardPattern(JCTree tree) {
		print((JCTree) readObject(tree, "patt", null));
		print(" && ");
		print((JCExpression) readObject(tree, "expr", null));
	}
	
	void printParenthesizedPattern(JCTree tree) {
		print("(");
		print((JCTree) readObject(tree, "pattern", null));
		print(")");
	}
	
	void printConstantCaseLabel(JCTree tree) {
		print((JCTree) readObject(tree, "expr", null));
	}
	
	void printPatternCaseLabel(JCTree tree) {
		print((JCTree) readObject(tree, "pat", null));
		JCTree guard = readObject(tree, "guard", null);
		if (guard != null) {
			print(" when ");
			print(guard);
		}
	}
	
	void printRecordPattern(JCTree tree) {
		print((JCTree) readObject(tree, "deconstructor", null));
		print("(");
		print(readObject(tree, "nested", List.<JCTree>nil()), ", ");
		print(")");
		JCVariableDecl var = readObject(tree, "var", null);
		if (var != null) {
			print(" ");
			print(var.name);
		}
	}
	
	@Override public void visitTry(JCTry tree) {
		aPrint("try ");
		List<?> resources = readObject(tree, "resources", List.nil());
		int len = resources.length();
		switch (len) {
		case 0:
			break;
		case 1:
			print("(");
			JCTree resource = (JCTree) resources.get(0);
			if (resource instanceof JCVariableDecl) {
				JCVariableDecl decl = (JCVariableDecl) resource;
				flagMod = -1L & ~FINAL;
				printVarDefInline(decl);
			} else {
				print(resource);
			}
			print(") ");
			break;
		default:
			println("(");
			indent++;
			int c = 0;
			for (Object i : resources) {
				align();
				if (i instanceof JCVariableDecl) {
					flagMod = -1L & ~FINAL;
					printVarDefInline((JCVariableDecl) i);
				} else {
					print((JCTree) i);
				}
				if (++c == len) {
					print(") ");
				} else {
					println(";", (JCTree) i);
				}
			}
			indent--;
		}
		println("{");
		indent++;
		for (JCStatement stat : tree.body.stats) print(stat);
		indent--;
		aPrint("}");
		for (JCCatch catchBlock : tree.catchers) {
			printCatch(catchBlock);
		}
		if (tree.finalizer != null) {
			println(" finally {");
			indent++;
			for (JCStatement stat : tree.finalizer.stats) print(stat);
			indent--;
			aPrint("}");
		}
		println(tree);
	}
	
	private void printCatch(JCCatch catchBlock) {
		print(" catch (");
		printVarDefInline(catchBlock.param); // ExprType1 | ExprType2 handled via JCTypeUnion.
		println(") {");
		indent++;
		for (JCStatement stat : catchBlock.body.stats) print(stat);
		indent--;
		aPrint("}");
	}
	
	public void visitErroneous(JCErroneous tree) {
		print("(ERROR)");
	}
	
	private static String operator(TreeTag tag) {
		String op = OPERATORS.get(tag);
		if (op == null) return "(?op?)";
		return op;
	}
	
	private static String quoteChars(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) sb.append(quoteChar(s.charAt(i)));
		return sb.toString();
	}
	
	private static String quoteChar(char ch) {
		switch (ch) {
		case '\b': return "\\b";
		case '\f': return "\\f";
		case '\n': return "\\n";
		case '\r': return "\\r";
		case '\t': return "\\t";
		case '\'': return "\\'";
		case '\"': return "\\\"";
		case '\\': return "\\\\";
		default:
			if (ch < 32) return String.format("\\%03o", (int) ch);
			return String.valueOf(ch);
		}
	}
	
	private static final Method getExtendsClause, getEndPosition, storeEnd;
	
	static {
		getExtendsClause = getMethod(JCClassDecl.class, "getExtendsClause", new Class<?>[0]);
		
		if (getJavaCompilerVersion() < 8) {
			getEndPosition = getMethod(DiagnosticPosition.class, "getEndPosition", java.util.Map.class);
			storeEnd = getMethod(java.util.Map.class, "put", Object.class, Object.class);
		} else {
			getEndPosition = getMethod(DiagnosticPosition.class, "getEndPosition", "com.sun.tools.javac.tree.EndPosTable");
			Method storeEndMethodTemp;
			Class<?> endPosTable;
			try {
				endPosTable = Class.forName("com.sun.tools.javac.tree.EndPosTable");
			} catch (ClassNotFoundException ex) {
				throw sneakyThrow(ex);
			}
			try {
				storeEndMethodTemp = Permit.getMethod(endPosTable, "storeEnd", JCTree.class, int.class);
			} catch (NoSuchMethodException e) {
				try {
					endPosTable = Class.forName("com.sun.tools.javac.parser.JavacParser$AbstractEndPosTable");
					storeEndMethodTemp = Permit.getMethod(endPosTable, "storeEnd", JCTree.class, int.class);
				} catch (NoSuchMethodException ex) {
					throw sneakyThrow(ex);
				} catch (ClassNotFoundException ex) {
					throw sneakyThrow(ex);
				}
			}
			storeEnd = storeEndMethodTemp;
		}
		Permit.setAccessible(getEndPosition);
		Permit.setAccessible(storeEnd);
	}
	
	private static Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		try {
			return Permit.getMethod(clazz, name, paramTypes);
		} catch (NoSuchMethodException e) {
			throw sneakyThrow(e);
		}
	}
	
	private static Method getMethod(Class<?> clazz, String name, String... paramTypes) {
		try {
			Class<?>[] c = new Class[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++) c[i] = Class.forName(paramTypes[i]);
			return Permit.getMethod(clazz, name, c);
		} catch (NoSuchMethodException e) {
			throw sneakyThrow(e);
		} catch (ClassNotFoundException e) {
			throw sneakyThrow(e);
		}
	}
	
	public static JCTree getExtendsClause(JCClassDecl decl) {
		return (JCTree) Permit.invokeSneaky(getExtendsClause, decl);
	}
	
	static RuntimeException sneakyThrow(Throwable t) {
		if (t == null) throw new NullPointerException("t");
		PrettyPrinter.<RuntimeException>sneakyThrow0(t);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T)t;
	}
	
	private static final Map<Class<?>, Map<String, Field>> reflectionCache = new HashMap<Class<?>, Map<String, Field>>();
	
	@SuppressWarnings("unchecked")
	private <T> T readObject(JCTree tree, String fieldName, T defaultValue) {
		Class<?> tClass = tree.getClass();
		Map<String, Field> c = reflectionCache.get(tClass);
		if (c == null) reflectionCache.put(tClass, c = new HashMap<String, Field>());
		Field f = c.get(fieldName);
		if (f == null) {
			try {
				f = Permit.getField(tClass, fieldName);
			} catch (Exception e) {
				return defaultValue;
			}
			c.put(fieldName, f);
		}
		
		try {
			return (T) f.get(tree);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public void visitTypeBoundKind(TypeBoundKind tree) {
		print(String.valueOf(tree.kind));
	}
	
	@Override public void visitTree(JCTree tree) {
		String className = tree.getClass().getName();
		if (className.endsWith("$JCTypeUnion")) {
			List<JCExpression> types = readObject(tree, "alternatives", List.<JCExpression>nil());
			print(types, " | ");
		} else if (className.endsWith("$JCTypeIntersection")) {
			print(readObject(tree, "bounds", List.<JCExpression>nil()), " & ");
		} else if (className.endsWith("$JCMemberReference")) {
			printMemberReference0(tree);
		} else if (className.endsWith("$JCLambda")) {
			printLambda0(tree);
		} else if (className.endsWith("$JCAnnotatedType")) {
			printAnnotatedType0(tree);
		} else if (className.endsWith("$JCPackageDecl")) {
			// Starting with JDK9, this is inside the import list, but we've already printed it. Just ignore it.
		} else if (className.endsWith("$JCSwitchExpression")) { // Introduced as preview feature in JDK12
			printSwitchExpression(tree);
		} else if (className.endsWith("$JCYield")) { // Introduced as preview feature in JDK13, part of switch expressions.
			printYieldExpression(tree);
		} else if (className.endsWith("$JCBindingPattern")) { // Introduced as preview in JDK14
			printBindingPattern(tree);
		} else if (className.endsWith("$JCDefaultCaseLabel")) { // Introduced in JDK17
			printDefaultCase(tree);
		} else if (className.endsWith("$JCGuardPattern")) { // Introduced in JDK17
			printGuardPattern(tree);
		} else if (className.endsWith("$JCParenthesizedPattern")) { // Introduced in JDK17
			printParenthesizedPattern(tree);
		} else if (className.endsWith("$JCConstantCaseLabel")) { // Introduced in JDK19
			printConstantCaseLabel(tree);
		} else if (className.endsWith("$JCPatternCaseLabel")) { // Introduced in JDK19
			printPatternCaseLabel(tree);
		} else if (className.endsWith("$JCRecordPattern")) { // Introduced in JDK19
			printRecordPattern(tree);
		} else if (className.endsWith("$JCAnyPattern")) { // Introduced in JDK22
			print("_");
		} else {
			throw new AssertionError("Unhandled tree type: " + tree.getClass() + ": " + tree);
		}
	}
	
	private boolean isCaseRuleStyle(JCCase tree) {
		if (tree == null) return false;
		Enum<?> caseKind = readObject(tree, "caseKind", null); // JDK 12+
		return caseKind != null && caseKind.name().equalsIgnoreCase("RULE");
	}
	
	private boolean jcAnnotatedTypeInit = false;
	private Class<?> jcAnnotatedTypeClass = null;
	
	private boolean isJcAnnotatedType(Object o) {
		if (o == null) return false;
		if (jcAnnotatedTypeInit) return jcAnnotatedTypeClass == o.getClass();
		Class<?> c = o.getClass();
		if (c.getName().endsWith("$JCAnnotatedType")) {
			jcAnnotatedTypeClass = c;
			jcAnnotatedTypeInit = true;
			return true;
		}
		return false;
	}
	
	private void printMemberReference0(JCTree tree) {
		print(readObject(tree, "expr", (JCExpression) null));
		print("::");
		List<JCExpression> typeArgs = readObject(tree, "typeargs", List.<JCExpression>nil());
		if (typeArgs != null && !typeArgs.isEmpty()) {
			print("<");
			print(typeArgs, ", ");
			print(">");
		}
		print(readObject(tree, "mode", new Object()).toString().equals("INVOKE") ? readObject(tree, "name", (Name) null) : "new");
	}
	
	private void printLambda0(JCTree tree) {
		List<JCVariableDecl> params = readObject(tree, "params", List.<JCVariableDecl>nil());
		boolean explicit = true;
		int paramLength = params.size();
		try {
			explicit = readObject(tree, "paramKind", new Object()).toString().equals("EXPLICIT");
		} catch (Exception e) {}
		boolean useParens = paramLength != 1 || explicit;
		if (useParens) print("(");
		if (explicit) {
			boolean first = true;
			for (JCVariableDecl vd : params) {
				if (!first) print(", ");
				first = false;
				printVarDefInline(vd);
			}
		} else {
			String sep = "";
			for (JCVariableDecl param : params) {
				print(sep);
				print(param.name);
				sep = ", ";
			}
		}
		if (useParens) print(")");
		print(" -> ");
		JCTree body = readObject(tree, "body", (JCTree) null);
		if (body instanceof JCBlock) {
			println("{");
			indent++;
			print(((JCBlock) body).stats, "");
			indent--;
			aPrint("}");
		} else {
			print(body);
		}
	}
	
	private void printAnnotatedType0(JCTree tree) {
		JCTree underlyingType = readObject(tree, "underlyingType", (JCTree) null);
		if (underlyingType instanceof JCFieldAccess) {
			print(((JCFieldAccess) underlyingType).selected);
			print(".");
			print(readObject(tree, "annotations", List.<JCExpression>nil()), " ");
			print(" ");
			print(((JCFieldAccess) underlyingType).name);
		} else if (underlyingType instanceof JCArrayTypeTree) {
			printTypeArray0(tree);
		} else {
			print(readObject(tree, "annotations", List.<JCExpression>nil()), " ");
			print(" ");
			print(underlyingType);
		}
	}
	
	private void printTypeArray0(JCTree tree) {
		JCTree inner = tree;
		int dimCount = 0;
		
		while (true) {
			if (inner instanceof JCArrayTypeTree) {
				inner = ((JCArrayTypeTree) inner).elemtype;
				dimCount++;
				continue;
			} else if (isJcAnnotatedType(inner)) {
				JCTree underlyingType = readObject(inner, "underlyingType", (JCTree) null);
				if (underlyingType instanceof JCArrayTypeTree) {
					inner = ((JCArrayTypeTree) underlyingType).elemtype;
					dimCount++;
					continue;
				}
			}
			break;
		}
		
		print(inner);
		
		inner = tree;
		while (true) {
			if (inner instanceof JCArrayTypeTree) {
				dimCount--;
				print((dimCount == 0 && innermostArrayBracketsAreVarargs) ? "..." : "[]");
				inner = ((JCArrayTypeTree) inner).elemtype;
				continue;
			} else if (isJcAnnotatedType(inner)) {
				JCTree underlyingType = readObject(inner, "underlyingType", (JCTree) null);
				if (underlyingType instanceof JCArrayTypeTree) {
					dimCount--;
					print(" ");
					print(readObject(inner, "annotations", List.<JCExpression>nil()), " ");
					print(" ");
					print((dimCount == 0 && innermostArrayBracketsAreVarargs) ? "..." : "[]");
					inner = ((JCArrayTypeTree) underlyingType).elemtype;
					continue;
				}
			}
			break;
		}
	}
}
