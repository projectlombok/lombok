/*
 * Copyright (C) 2010-2015 The Project Lombok Authors.
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
package lombok.javac;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import static lombok.javac.Javac.*;
import lombok.javac.JavacTreeMaker.TypeTag;

import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

/**
 * Makes a copy of any AST node, with some exceptions.
 * Exceptions:<ul>
 * <li>The symbol ('sym') of a copied variable isn't copied.
 * <li>all labels are removed.
 * </ul>
 * 
 * The purpose of this class is to make a copy, and then the copy is attributed (resolution info is added). These exceptions
 * are to work around apparent bugs (or at least inconsistencies) in javac sources.
 */
public class TreeMirrorMaker extends TreeCopier<Void> {
	private final IdentityHashMap<JCTree, JCTree> originalToCopy = new IdentityHashMap<JCTree, JCTree>();
	
	public TreeMirrorMaker(JavacTreeMaker maker, Context context) {
		super(maker.getUnderlyingTreeMaker());
	}
	
	@Override public <T extends JCTree> T copy(T original) {
		T copy = super.copy(original);
		originalToCopy.put(original, copy);
		return copy;
	}
	
	@Override public <T extends JCTree> T copy(T original, Void p) {
		T copy = super.copy(original, p);
		originalToCopy.put(original, copy);
		return copy;
	}
	
	@Override public <T extends JCTree> List<T> copy(List<T> originals) {
		List<T> copies = super.copy(originals);
		if (originals != null) {
			Iterator<T> it1 = originals.iterator();
			Iterator<T> it2 = copies.iterator();
			while (it1.hasNext()) originalToCopy.put(it1.next(), it2.next());
		}
		return copies;
	}
	
	@Override public <T extends JCTree> List<T> copy(List<T> originals, Void p) {
		List<T> copies = super.copy(originals, p);
		if (originals != null) {
			Iterator<T> it1 = originals.iterator();
			Iterator<T> it2 = copies.iterator();
			while (it1.hasNext()) originalToCopy.put(it1.next(), it2.next());
		}
		return copies;
	}
	
	public Map<JCTree, JCTree> getOriginalToCopyMap() {
		return Collections.unmodifiableMap(originalToCopy);
	}
	
	// Monitor the following issues when making changes here.
	// - https://github.com/rzwitserloot/lombok/issues/278
	// - https://github.com/rzwitserloot/lombok/issues/729
	@Override public JCTree visitVariable(VariableTree node, Void p) {
		JCVariableDecl original = node instanceof JCVariableDecl ? (JCVariableDecl) node : null;
		JCVariableDecl copy = (JCVariableDecl) super.visitVariable(node, p);
		if (original == null) return copy;
		copy.sym = original.sym;
		if (copy.sym != null) copy.type = original.type;
		if (copy.type != null) {
			boolean wipeSymAndType = copy.type.isErroneous();
			if (!wipeSymAndType) {
				TypeTag typeTag = TypeTag.typeTag(copy.type);
				wipeSymAndType = (CTC_NONE.equals(typeTag) || CTC_ERROR.equals(typeTag) || CTC_UNKNOWN.equals(typeTag) || CTC_UNDETVAR.equals(typeTag));
			}
			
			if (wipeSymAndType) {
				copy.sym = null;
				copy.type = null;
			}
		}
		
		return copy;
	}
	
	// Fix for NPE in HandleVal. See https://github.com/rzwitserloot/lombok/issues/372
	// This and visitVariable is rather hacky but we're working around evident bugs or at least inconsistencies in javac.
	@Override public JCTree visitLabeledStatement(LabeledStatementTree node, Void p) {
		return node.getStatement().accept(this, p);
	}
}
