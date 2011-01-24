package lombok.javac;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.tools.javac.tree.JCTree;
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
}
