/*
 * Copyright (C) 2009-2018 The Project Lombok Authors.
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
package lombok.delombok.ant;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

@SuppressWarnings("unused") // we use reflection to transfer fields.
class Tasks {
	public static class Format {
		private String value;
		
		@Override public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		
		@Override public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Format other = (Format) obj;
			if (value == null) {
				if (other.value != null) return false;
			} else if (!value.equals(other.value)) return false;
			return true;
		}
		
		@Override public String toString() {
			return "FormatOption [value=" + value + "]";
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public static class Delombok extends Task {
		private File fromDir, toDir;
		private Path classpath;
		private Path sourcepath;
		private Path modulepath;
		private boolean verbose;
		private String encoding;
		private Path path;
		private List<Format> formatOptions = new ArrayList<Format>();
		
		public void setClasspath(Path classpath) {
			if (this.classpath == null) {
				this.classpath = classpath;
			} else {
				this.classpath.append(classpath);
			}
		}
		
		public Path createClasspath() {
			if (classpath == null) classpath = new Path(getProject());
			return classpath.createPath();
		}
		
		public void setClasspathRef(Reference r) {
			createClasspath().setRefid(r);
		}
		
		public void setSourcepath(Path sourcepath) {
			if (this.sourcepath == null) {
				this.sourcepath = sourcepath;
			} else {
				this.sourcepath.append(sourcepath);
			}
		}
		
		public Path createSourcepath() {
			if (sourcepath == null) sourcepath = new Path(getProject());
			return sourcepath.createPath();
		}
		
		public void setSourcepathRef(Reference r) {
			createSourcepath().setRefid(r);
		}
		
		public void setModulepath(Path modulepath) {
			if (this.modulepath == null) {
				this.modulepath = modulepath;
			} else {
				this.modulepath.append(modulepath);
			}
		}
		
		public Path createModulepath() {
			if (modulepath == null) modulepath = new Path(getProject());
			return modulepath.createPath();
		}
		
		public void setModulepathRef(Reference r) {
			createModulepath().setRefid(r);
		}
		
		public void setFrom(File dir) {
			this.fromDir = dir;
		}
		
		public void setTo(File dir) {
			this.toDir = dir;
		}
		
		public void setVerbose(boolean verbose) {
			this.verbose = verbose;
		}
		
		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}
		
		public void addFileset(FileSet set) {
			if (path == null) path = new Path(getProject());
			path.add(set);
		}
		
		public void addFormat(Format format) {
			formatOptions.add(format);
		}
		
		private static ClassLoader shadowLoader;
		
		public static Class<?> shadowLoadClass(String name) {
			try {
				if (shadowLoader == null) {
					try {
						Class.forName("lombok.core.LombokNode");
						// If we get here, then lombok is already available.
						shadowLoader = Delombok.class.getClassLoader();
					} catch (ClassNotFoundException e) {
						// If we get here, it isn't, and we should use the shadowloader.
						Class<?> launcherMain = Class.forName("lombok.launch.Main");
						Method m = launcherMain.getDeclaredMethod("getShadowClassLoader");
						m.setAccessible(true);
						shadowLoader = (ClassLoader) m.invoke(null);
					}
				}
				
				return Class.forName(name, true, shadowLoader);
			} catch (Exception e) {
				if (e instanceof RuntimeException) throw (RuntimeException) e;
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void execute() throws BuildException {
			Location loc = getLocation();
			
			try {
				Object instance = shadowLoadClass("lombok.delombok.ant.DelombokTaskImpl").getConstructor().newInstance();
				for (Field selfField : getClass().getDeclaredFields()) {
					if (selfField.isSynthetic() || Modifier.isStatic(selfField.getModifiers())) continue;
					Field otherField = instance.getClass().getDeclaredField(selfField.getName());
					otherField.setAccessible(true);
					if (selfField.getName().equals("formatOptions")) {
						List<String> rep = new ArrayList<String>();
						for (Format f : formatOptions) {
							if (f.getValue() == null) throw new BuildException("'value' property required for <format>");
							rep.add(f.getValue());
						}
						otherField.set(instance, rep);
					} else {
						otherField.set(instance, selfField.get(this));
					}
				}
				
				Method m = instance.getClass().getMethod("execute", Location.class);
				m.invoke(instance, loc);
			} catch (Exception e) {
				Throwable t = (e instanceof InvocationTargetException) ? e.getCause() : e;
				if (t instanceof Error) throw (Error) t;
				if (t instanceof RuntimeException) throw (RuntimeException) t;
				throw new RuntimeException(t);
			}
		}
	}
}
