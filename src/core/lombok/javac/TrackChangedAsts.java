package lombok.javac;

import java.util.HashSet;
import java.util.Set;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

public class TrackChangedAsts {
	public final Set<JCCompilationUnit> changed = new HashSet<JCCompilationUnit>();
}