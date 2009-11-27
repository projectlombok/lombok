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
package lombok.delombok.ant;

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;

import lombok.delombok.Delombok;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;

public class DelombokTask extends Task {
	private File fromDir, toDir;
	private boolean verbose;
	private String encoding;
	private Path path;
	
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
	
	@Override
	public void execute() throws BuildException {
		if (fromDir == null && path == null) throw new BuildException("Either 'from' attribute, or nested <fileset> tags are required.");
		if (fromDir != null && path != null) throw new BuildException("You can't specify both 'from' attribute and nested filesets. You need one or the other.");
		if (toDir == null) throw new BuildException("The to attribute is required.");
		
		Delombok delombok = new Delombok();
		if (verbose) delombok.setVerbose(true);
		try {
			if (encoding != null) delombok.setCharset(encoding);
		} catch (UnsupportedCharsetException e) {
			throw new BuildException("Unknown charset: " + encoding, getLocation());
		}
		
		delombok.setOutput(toDir);
		try {
			if (fromDir != null) delombok.delombok(fromDir);
			else {
				Iterator<?> it = path.iterator();
				while (it.hasNext()) {
					FileResource fileResource = (FileResource) it.next();
					File baseDir = fileResource.getBaseDir();
					if (baseDir == null) {
						File file = fileResource.getFile();
						delombok.process(file.getParentFile(), file.getName());
					} else {
						delombok.process(baseDir, fileResource.getName());
					}
				}
			}
		} catch (IOException e) {
			throw new BuildException("I/O problem during delombok", e, getLocation());
		}
	}
}
