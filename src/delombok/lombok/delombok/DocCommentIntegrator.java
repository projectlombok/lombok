package lombok.delombok;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.javac.CommentInfo;
import lombok.javac.Javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class DocCommentIntegrator {
	/**
	 * Returns the same comment list as when this integrator was created, minus all doc comments that have been successfully integrated into the compilation unit.
	 */
	public List<CommentInfo> integrate(List<CommentInfo> comments, JCCompilationUnit unit) {
		List<CommentInfo> out = new ArrayList<CommentInfo>();
		CommentInfo lastExcisedComment = null;
		JCTree lastNode = null;
		
		for (CommentInfo cmt : comments) {
			if (!cmt.isJavadoc()) {
				out.add(cmt);
				continue;
			}
			
			JCTree node = findJavadocableNodeOnOrAfter(unit, cmt.endPos);
			if (node == null) {
				out.add(cmt);
				continue;
			}
			
			if (node == lastNode) {
				out.add(lastExcisedComment);
			}
			if (!attach(unit, node, cmt)) {
				out.add(cmt);
			} else {
				lastNode = node;
				lastExcisedComment = cmt;
			}
		}
		return out;
	}
	
	private static final Pattern CONTENT_STRIPPER = Pattern.compile("^(?:\\s*\\*)?[ \\t]*(.*?)$", Pattern.MULTILINE);
	@SuppressWarnings("unchecked") private boolean attach(JCCompilationUnit top, JCTree node, CommentInfo cmt) {
		String docCommentContent = cmt.content;
		if (docCommentContent.startsWith("/**")) docCommentContent = docCommentContent.substring(3);
		if (docCommentContent.endsWith("*/")) docCommentContent = docCommentContent.substring(0, docCommentContent.length() -2);
		docCommentContent = CONTENT_STRIPPER.matcher(docCommentContent).replaceAll("$1");
		docCommentContent = docCommentContent.trim();
		
		if (Javac.getDocComments(top) == null) Javac.initDocComments(top);
		
		Object map_ = Javac.getDocComments(top);
		if (map_ instanceof Map) {
			((Map<JCTree, String>) map_).put(node, docCommentContent);
			return true;
		}
		
		return false;
	}
	
	private JCTree findJavadocableNodeOnOrAfter(JCCompilationUnit unit, int endPos) {
		if (unit.pid != null && endPos <= unit.pid.pos) return null;
		Iterator<JCTree> it = unit.defs.iterator();
		
		while (it.hasNext()) {
			JCTree node = it.next();
			if (node.pos < endPos) {
				if (node instanceof JCClassDecl) {
					com.sun.tools.javac.util.List<JCTree> defs = ((JCClassDecl) node).defs;
					if (!defs.isEmpty()) while (!defs.tail.isEmpty()) defs = defs.tail;
					if (defs.head != null && defs.head.pos >= endPos) {
						// The associated node is IN this class declaration, so, replace the iterator.
						// There's no point looking beyond this member in the current iteration 'context'
						// so we don't need to save the old ref. Just start over inside this type declaration.
						it = ((JCClassDecl) node).defs.iterator();
					}
				}
				continue;
			}
			
			if (node instanceof JCMethodDecl || node instanceof JCClassDecl || node instanceof JCVariableDecl) return node;
			return null;
		}
		
		return null;
	}
}
