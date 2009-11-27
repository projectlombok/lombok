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
						System.out.printf("Processing raw file: %s\n", file);
						delombok.delombok(file.getParentFile(), file.getName());
					} else {
						System.out.printf("Processing based file: %s -- %s\n", baseDir, fileResource.getName());
						delombok.delombok(baseDir, fileResource.getName());
						
					}
				}
			}
		} catch (IOException e) {
			throw new BuildException("I/O problem during delombok", e, getLocation());
		}
	}
}
