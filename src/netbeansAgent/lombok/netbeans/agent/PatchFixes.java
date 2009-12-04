/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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
package lombok.netbeans.agent;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.lang.model.element.Element;

import lombok.Lombok;
import lombok.patcher.inject.LiveInjector;

import org.netbeans.Module;
import org.netbeans.ProxyClassLoader;
import org.netbeans.api.java.source.ClasspathInfo;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

// A lot of the footwork on the netbeans support has been done by Jan Lahoda, who is awesome. (jlahoda@netbeans.org)
// This footwork was converted into a patch script form by me (rzwitserloot). See:
// http://code.google.com/p/projectlombok/issues/detail?id=20#c3
public class PatchFixes {
	public static boolean loadClass_decision(@SuppressWarnings("unused") ClassLoader loader, String name) throws Exception {
		return name.startsWith("lombok.");
	}
	
	public static Class<?> loadClass_value(ClassLoader loader, String name) throws Exception {
		int last = name.lastIndexOf('.');
		String pkg = (last >= 0) ? name.substring(0, last) : "";
		Method m = ProxyClassLoader.class.getDeclaredMethod("selfLoadClass", String.class, String.class);
		m.setAccessible(true);
		return (Class<?>)m.invoke(loader, pkg, name);
	}
	
	public static boolean getResource_decision(@SuppressWarnings("unused") ClassLoader loader, String name) throws Exception {
		return name.startsWith("META-INF/services/lombok.");
	}
	
	public static URL getResource_value(ClassLoader loader, String name) throws Exception {
		Method m = ProxyClassLoader.class.getDeclaredMethod("findResource", String.class);
		m.setAccessible(true);
		return (URL) m.invoke(loader, name);
	}
	
	public static boolean getResources_decision(@SuppressWarnings("unused") ClassLoader loader, String name) throws Exception {
		return name.startsWith("META-INF/services/lombok.");
	}
	
	public static Enumeration<?> getResources_value(ClassLoader loader, String name) throws Exception {
		Method m = ProxyClassLoader.class.getDeclaredMethod("findResources", String.class);
		m.setAccessible(true);
		return (Enumeration<?>) m.invoke(loader, name);
	}
	
	public static void addSelfToClassLoader(Module module, List<File> classPath) {
		if (module.getJarFile().getName().equals("org-netbeans-libs-javacimpl.jar")) {
			String lombokJarLoc = LiveInjector.findPathJar(Lombok.class);
			classPath.add(new File(lombokJarLoc));
		}
	}
	
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
	
	public static long returnMinus1ForGeneratedNode(SourcePositions that, CompilationUnitTree cu, Tree tree) {
		int start = (int) that.getStartPosition(cu, tree);
		if (start < 0) return -1;
		return that.getEndPosition(cu, tree);
	}
	
	public static void addTaskListenerWhenCallingJavac(JavacTaskImpl task,
			@SuppressWarnings("unused") /* Will come in handy later */ ClasspathInfo cpInfo) throws Exception {
		if (task == null) return;
		Class<?> entryPoint = JavacTaskImpl.class.getClassLoader().loadClass("lombok.netbeans.agent.NetbeansEntryPoint");
		if (entryPoint == null) {
			/* TODO tell the user that lombok is not working. loadClass is not supposed to return null, but netbeans' loader does anyway. */
			System.err.println("[LOMBOK] ClassLoader not patched correctly.");
		} else {
			task.setTaskListener((TaskListener) entryPoint.getConstructor(Context.class).newInstance(task.getContext()));
		}
	}
	
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
