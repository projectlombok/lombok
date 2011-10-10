/*
 * Copyright © 2010-2011 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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

import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.util.List;

public class TreeMirrorMaker extends TreeCopier<Void> {
	private final IdentityHashMap<JCTree, JCTree> originalToCopy = new IdentityHashMap<JCTree, JCTree>();
	
	public TreeMirrorMaker(JavacNode node) {
		super(node.getTreeMaker());
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
	
	// Fix for NPE in HandleVal. See http://code.google.com/p/projectlombok/issues/detail?id=205
	// Maybe this should be done elsewhere...
	@Override public JCTree visitVariable(VariableTree node, Void p) {
		JCVariableDecl copy = (JCVariableDecl) super.visitVariable(node, p);
		copy.sym = ((JCVariableDecl) node).sym;
		return copy;
	}
}
