package lombok.netbeans.agent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.lang.model.element.Element;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
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
	public static void addTaskListenerWhenCallingJavac() {
		TaskListenerProvider p = /* Lookup.getDefault().lookup(TLP.class) */;
		if (p != null) {
			TaskListener l = p.create(context, cpInfo);
			task.setTaskListener(l);
		}
		
		return;
	}
}
