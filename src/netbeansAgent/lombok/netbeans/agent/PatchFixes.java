package lombok.netbeans.agent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.lang.model.element.Element;

import org.netbeans.api.java.source.ClasspathInfo;
import org.openide.util.Lookup;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

public class PatchFixes {
	//Contributed by Jan Lahoda (jlahoda@netbeans.org)
	//Turned into a patch script by rzwitserloot.
	//see http://code.google.com/p/projectlombok/issues/detail?id=20#c3
	public static void fixContentOnSetTaskListener(JavacTaskImpl that, TaskListener taskListener) throws Throwable {
		Context context = that.getContext();
		if (context.get(TaskListener.class) != null)
			context.put(TaskListener.class, (TaskListener)null);
		if (taskListener != null) {
			try {
				Method m = JavacTaskImpl.class.getDeclaredMethod("wrap", TaskListener.class);
				try {
					m.setAccessible(true);
				} catch (SecurityException ignore) {}
				TaskListener w = (TaskListener)m.invoke(that, taskListener);
				context.put(TaskListener.class, w);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
	}
	
	//Contributed by Jan Lahoda (jlahoda@netbeans.org)
	//Turned into a patch script by rzwitserloot.
	//see http://code.google.com/p/projectlombok/issues/detail?id=20#c3
	public static Tree returnNullForGeneratedNode(Trees trees, Element element, Object o) throws Throwable {
		try {
			Tree tree = trees.getTree(element);
			if (tree == null) return null;
			CompilationUnitTree unit = (CompilationUnitTree) o.getClass().getMethod("getCompilationUnit").invoke(o);
			int startPos = (int) trees.getSourcePositions().getStartPosition(unit, tree);
			if (startPos == -1) return null;
			return tree;
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	//Contributed by Jan Lahoda (jlahoda@netbeans.org)
	//Turned into a patch script by rzwitserloot.
	//see http://code.google.com/p/projectlombok/issues/detail?id=20#c3
	public static long returnMinus1ForGeneratedNode(SourcePositions that, CompilationUnitTree cu, Tree tree) {
		int start = (int) that.getStartPosition(cu, tree);
		if (start < 0) return -1;
		return that.getEndPosition(cu, tree);
	}
	
	//Contributed by Jan Lahoda (jlahoda@netbeans.org)
	//Turned into a patch script by rzwitserloot.
	//see http://code.google.com/p/projectlombok/issues/detail?id=20#c3
	public static void addTaskListenerWhenCallingJavac(JavacTaskImpl task, ClasspathInfo cpInfo) {
		TaskListenerProvider p = Lookup.getDefault().lookup(TaskListenerProvider.class);
		if (p != null) {
			TaskListener l = p.create(task.getContext(), cpInfo);
			task.setTaskListener(l);
		}
	}
	
	//Contributed by Jan Lahoda (jlahoda@netbeans.org)
	//Turned into a patch script by rzwitserloot.
	//see http://code.google.com/p/projectlombok/issues/detail?id=20#c3
	public static Iterator<JCTree> filterGenerated(final Iterator<JCTree> it) {
		return new Iterator<JCTree>() {
			private JCTree next;
			private boolean hasNext;
			
			{
				calc();
			}
			
			private void calc() {
				while (it.hasNext()) {
					JCTree n = it.next();
					if (n.pos != -1) {
						hasNext = true;
						next = n;
						return;
					}
				}
				
				hasNext = false;
				next = null;
			}
			
			@Override public boolean hasNext() {
				return hasNext;
			}
			
			@Override public JCTree next() {
				if (!hasNext) throw new NoSuchElementException();
				JCTree n = next;
				calc();
				return n;
			}
			
			@Override public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
