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
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.delombok.Delombok;
import lombok.delombok.Delombok.InvalidFormatOptionException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;

public class DelombokTaskImpl {
	private File fromDir, toDir;
	private Path classpath;
	private Path sourcepath;
	private Path modulepath;
	private boolean verbose;
	private String encoding;
	private Path path;
	private List<String> formatOptions = new ArrayList<String>();
	
	public void execute(Location location) throws BuildException {
		if (fromDir == null && path == null) throw new BuildException("Either 'from' attribute, or nested <fileset> tags are required.");
		if (fromDir != null && path != null) throw new BuildException("You can't specify both 'from' attribute and nested filesets. You need one or the other.");
		if (toDir == null) throw new BuildException("The to attribute is required.");
		
		Delombok delombok = new Delombok();
		if (verbose) delombok.setVerbose(true);
		try {
			if (encoding != null) delombok.setCharset(encoding);
		} catch (UnsupportedCharsetException e) {
			throw new BuildException("Unknown charset: " + encoding, location);
		}
		
		if (classpath != null) delombok.setClasspath(classpath.toString());
		if (sourcepath != null) delombok.setSourcepath(sourcepath.toString());
		if (modulepath != null) delombok.setModulepath(modulepath.toString());
		
		try {
			delombok.setFormatPreferences(Delombok.formatOptionsToMap(formatOptions));
		} catch (InvalidFormatOptionException e) {
			throw new BuildException(e.getMessage() + " Run java -jar lombok.jar --format-help for detailed format help.");
		}
		
		delombok.setOutput(toDir);
		try {
			if (fromDir != null) delombok.addDirectory(fromDir);
			else {
				Iterator<?> it = path.iterator();
				while (it.hasNext()) {
					FileResource fileResource = (FileResource) it.next();
					File baseDir = fileResource.getBaseDir();
					if (baseDir == null) {
						File file = fileResource.getFile();
						delombok.addFile(file.getParentFile(), file.getName());
					} else {
						delombok.addFile(baseDir, fileResource.getName());
					}
				}
			}
			delombok.delombok();
		} catch (IOException e) {
			throw new BuildException("I/O problem during delombok", e, location);
		}
	}
}
