<#import "_features.html" as f>

<@f.scaffold title="Delombok" logline="">
	<@f.overview>
		<p>
			Normally, lombok adds support for all the lombok features directly to your IDE and compiler by plugging into them.<br />
			However, lombok doesn't cover all tools. For example, lombok cannot plug into javadoc, nor can it plug into the Google Widget Toolkit, both of which run on java sources. Delombok still allows you to use lombok with these tools by preprocessing your java code into java code with all of lombok's transformations already applied.
		</p><p>
			Delombok can of course also help understand what's happening with your source by letting you look at exactly what lombok is doing 'under the hood'.
		</p><p>
			Delombok's standard mode of operation is that it copies an entire directory into another directory, recursively, skipping class files, and applying lombok transformations to any java source files it encounters.
		</p><p>
			Delombok's output format can be configured with command line options (use <code>--format-help</code> for a complete list). A few such options are automatically scanned from input if possible (such as indent). If delombok's formatting is not conforming to your preferred code style, have a look!
		</p>

		<@f.main.h3 title="Running delombok on the command line" />

		<p>
			Delombok is included in <code>lombok.jar</code>. To use it, all you need to run on the command line is:
			<pre>java -jar lombok.jar delombok src -d src-delomboked</pre>
			<br />
			Which will duplicate the contents of the <code>src</code> directory into the <code>src-delomboked</code> directory, which will be created if it doesn't already exist, but delomboked of course. Delombok on the command line has a few more options; use the <code>--help</code> parameter to see more options.
		</p><p>
			To let delombok print the transformation result of a single java file directly to standard output, you can use:
			<pre>java -jar lombok.jar delombok -p MyJavaFile.java</pre>
		</p><p>
			The <code>-classpath</code>, <code>-sourcepath</code>, and <code>--module-path</code> options of javac are replicated as <code>--classpath</code>, <code>--sourcepath</code>, and <code>--module-path</code> in delombok.
		</p>

		<@f.main.h3 title="Running delombok in ant" />

		<p>
			<code>lombok.jar</code> includes an ant task which can apply delombok for you. For example, to create javadoc for your project, your <code>build.xml</code> file would look something like:
			<pre>&lt;target name="javadoc"&gt;
&lt;taskdef classname="lombok.delombok.ant.Tasks$Delombok" classpath="lib/lombok.jar" name="delombok" /&gt;
&lt;mkdir dir="build/src-delomboked" /&gt;
<strong>&lt;delombok verbose="true" encoding="UTF-8" to="build/src-delomboked" from="src"&gt;</strong>
	<strong>&lt;format value="suppressWarnings:skip" /&gt;</strong>
<strong>&lt;/delombok&gt;</strong>
&lt;mkdir dir="build/api" /&gt;
&lt;javadoc sourcepath="build/src-delomboked" defaultexcludes="yes" destdir="build/api" /&gt;
&lt;/target&gt;</pre>
			<br />
			Instead of a <code>from</code> attribute, you can also nest <code>&lt;fileset&gt;</code> nodes. The <code>delombok</code> supports <code>sourcepath</code>, <code>classpath</code>, and <code>modulepath</code> as parameter or as nested element, or as nested refid element, similar to the <code>javac</code> task.
		</p>

		<@f.main.h3 title="Running delombok in maven" />

		<p>
			Anthony Whitford has written a <a href="https://github.com/awhitford/lombok.maven">maven plugin</a> for delomboking your source code.
		</p>

		<@f.main.h3 title="Running delombok in sbt" />

		<p>
			Yang Bo has written an <a href="https://github.com/ThoughtWorksInc/sbt-delombok">sbt plugin</a> for delomboking your source code.
		</p>
		
		<@f.main.h3 title="Limitations" />

		<p>
			Delombok tries to preserve your code as much as it can, but comments may move around a little bit, especially comments that are in the middle of a syntax node. For example, any comments appearing in the middle of a list of method modifiers, such as <code>public /*comment*/ static ...</code> will move towards the front of the list of modifiers. In practice, any java source parsing tool will not be affected.<br />
			To keep any changes to your code style to a minimum, delombok just copies a source file directly without changing any of it if the source file contains no lombok transformations.
		</p>
	</@f.overview>
</@f.scaffold>
