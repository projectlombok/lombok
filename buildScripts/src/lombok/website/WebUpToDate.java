/*
 * Modified from http://svn.apache.org/viewvc/ant/core/trunk/src/main/org/apache/tools/ant/taskdefs/UpToDate.java?view=markup
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package lombok.website;

import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.URLResource;
import org.apache.tools.ant.types.selectors.SelectorUtils;

public class WebUpToDate extends Task implements Condition {
	private String property;
	private String value;
	private String urlbase;
	private File sourceFile;
	private Vector sourceFileSets = new Vector();
	private Union sourceResources = new Union();
	
	/**
	 * The property to set if the target file is more up-to-date than
	 * (each of) the source file(s).
	 *
	 * @param property the name of the property to set if Target is up-to-date.
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	
	/**
	 * The value to set the named property to if the target file is more
	 * up-to-date than (each of) the source file(s). Defaults to 'true'.
	 *
	 * @param value the value to set the property to if Target is up-to-date
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Returns the value, or "true" if a specific value wasn't provided.
	 */
	private String getValue() {
		return (value != null) ? value : "true";
	}
	
	/**
	 * The file that must be older than the target file
	 * if the property is to be set.
	 *
	 * @param file the file we are checking against the target file.
	 */
	public void setSrcfile(File file) {
		this.sourceFile = file;
	}
	
	/**
	 * Nested &lt;srcfiles&gt; element.
	 * @param fs the source files
	 */
	public void addSrcfiles(FileSet fs) {
		sourceFileSets.addElement(fs);
	}
	
	/**
	 * Nested resource collections as sources.
	 * @return the source resources to configure.
	 * @since Ant 1.7
	 */
	public Union createSrcResources() {
		return sourceResources;
	}
	
	public void setUrlbase(String base) {
		if (base.charAt(base.length()-1) != '/') this.urlbase = base + "/";
		else this.urlbase = base;
	}
	
	/**
	 * Evaluate (all) target and source file(s) to
	 * see if the target(s) is/are up-to-date.
	 * @return true if the target(s) is/are up-to-date
	 */
	public boolean eval() {
		if (sourceFileSets.size() == 0 && sourceResources.size() == 0 && sourceFile == null) {
			throw new BuildException("At least one srcfile or a nested <srcfiles> or <srcresources> element must be set.");
		}
		
		if ((sourceFileSets.size() > 0 || sourceResources.size() > 0) && sourceFile != null) {
			throw new BuildException("Cannot specify both the srcfile attribute and a nested <srcfiles> or <srcresources> element.");
		}
		
		if (urlbase == null) {
			throw new BuildException("The urlbase attribute must be set.");
		}
		
		// if the source file isn't there, throw an exception
		if (sourceFile != null && !sourceFile.exists()) {
			throw new BuildException(sourceFile.getAbsolutePath() + " not found.");
		}
		
		boolean upToDate = true;
		if (sourceFile != null) {
			Resource fileResource = new FileResource(sourceFile);
			upToDate = isUpToDate(fileResource);
		}
		
		if (upToDate) {
			Enumeration e = sourceFileSets.elements();
			while (upToDate && e.hasMoreElements()) {
				FileSet fs = (FileSet)e.nextElement();
				Iterator it = fs.iterator();
				while (upToDate && it.hasNext()) {
					Resource r = (Resource)it.next();
					upToDate = isUpToDate(r);
				}
			}
		}
		
		if (upToDate) {
			Resource[] r = sourceResources.listResources();
			for (int i = 0; upToDate && i < r.length; i++) {
				upToDate = isUpToDate(r[i]);
			}
		}
		
		return upToDate;
	}
	
	private boolean isUpToDate(Resource r) throws BuildException {
		String url = urlbase + r.getName();
		Resource urlResource;
		try {
			urlResource = new URLResource(new URL(url));
		} catch (MalformedURLException e) {
			throw new BuildException("url is malformed: " + url, e);
		}
		
		if (SelectorUtils.isOutOfDate(r, urlResource, 20)) {
			log(r.getName() + " is newer than " + url, Project.MSG_VERBOSE);
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Sets property to true if target file(s) have a more recent timestamp
	 * than (each of) the corresponding source file(s).
	 * @throws BuildException on error
	 */
	public void execute() throws BuildException {
		if (property == null) {
			throw new BuildException("property attribute is required.", getLocation());
		}
		boolean upToDate = eval();
		
		if (upToDate) {
			getProject().setNewProperty(property, getValue());
			log("Website is up to date.");
		}
	}
}
