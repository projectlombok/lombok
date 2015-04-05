<#import "../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">

        <h1>Android instructions</h1>

    </div>
    <div class="row">
        <p>
            Android development with lombok is possible. Lombok should be a compile-time only dependency, as otherwise
            the entirety of lombok will end up in your DEX files, wasting precious space on android devices. Also,
            errors will occur due
            to the native libraries present in lombok.jar itself. Unfortunately, android does not (yet) understand the
            concept of a
            compile-time-only dependency, so you need to mess with your build files to make it work.
        </p>

        <p>
            Android also does not have a complete JRE library stack; in particular, it does not have the <code>@java.beans.ConstructorProperties</code>
            annotation, therefore you have to stop lombok from generating these:
        <ul>
            <li>The <code>suppressConstructorProperties</code> property can be set to <code>false</code> when using <a
                    href="/features/details/constructor.ftl">@XArgsConstructor Annotations</a>.
            </li>
            <li>Starting with Lombok <code>&gt;= 1.14.0</code> you can instead a <code>lombok.config</code> file to the
                root of your project to disable <code>ConstructorProperties</code> project wide. Add the following line
                to <code>lombok.config</code>:<br/>
                <pre>lombok.anyConstructor.suppressConstructorProperties = true</pre>
                <br/>
                See the <a href="/features/details/configuration.ftl">configuration</a> documentation for more information on
                how to set up your project with a lombok config file.
        </ul>
        <p>
            The instructions listed below are excerpts from <a
                href="https://github.com/excilys/androidannotations/wiki/Cookbook">The
            AndroidAnnotations project cookbook</a>. You may wish to refer to that documentation for complete instructions;
            lombok is just
            the equivalent to <code>androidannotations-VERSION.jar</code>; there is no <code>-api</code> aspect.
        </p>


        <h3>Environments</h3>

        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#eclipse" aria-controls="home" role="tab"
                                                      data-toggle="tab">Eclipse</a>
            </li>
            <li role="presentation"><a href="#studio" aria-controls="profile" role="tab" data-toggle="tab">Android Studio</a>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="eclipse">
                <div>
                    In eclipse, create a 'lightweight' lombok jar that contains only the annotations by running:<br/><br/>
				<pre>
java -jar lombok.jar publicApi</pre>
                    Then, add the <code>lombok-api.jar</code> file created by running this command
                    to your android project instead of the complete <code>lombok.jar</code>, and,
                    as usual, install lombok into eclipse by double-clicking <code>lombok.jar</code>.
                </div>
            </div>
            <div role="tabpanel" class="tab-pane" id="studio">
                Follow the previous instructions (<em>Gradle</em>). In addition to setting up your gradle project correctly,
                you need to add the <a href="http://plugins.jetbrains.com/plugin/6317">Lombok IntelliJ plugin</a> to add
                lombok support to Android Studio:
                <ul>
                    <li>Go to <code>File &gt; Settings &gt; Plugins</code></li>
                    <li>Click on <code>Browse repositories...</code></li>
                    <li>Search for <code>Lombok Plugin</code></li>
                    <li>Click on <code>Install plugin</code></li>
                    <li>Restart Android Studio</li>
                </ul>

            </div>
        </div>

        <h3>Build systems</h3>

        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#ant" aria-controls="home" role="tab"
                                                      data-toggle="tab">Ant</a>
            </li>
            <li role="presentation"><a href="#maven" aria-controls="profile" role="tab" data-toggle="tab">Maven</a>
            <li role="presentation"><a href="#gradle" aria-controls="profile" role="tab" data-toggle="tab">Gradle</a>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="ant">
                <div>
                    <ul>
                        <li>Find <code>build.xml</code> in <code>${r"${ANDROID_SDK_ROOT}"}/tools/ant/build.xml</code> and copy
                            the
                            <code>-compile</code> target into the paste buffer.
                        <li>Copy this to the <code>build.xml</code> of your own project, right before the <code>&lt;import
                            file="${r"${sdk.dir}"}/tools/ant/build.xml"&gt;</code> line.
                        <li>Create a <code>compile-libs</code> directory in your own project and copy the complete
                            <code>lombok.jar</code>
                            to it.
                        <li>Now modify the <code>&lt;classpath&gt;</code> entry inside the <code>&lt;javac&gt;</code> task in
                            the <code>-compile</code> target you just copied:<br/>
                            add <code>&lt;fileset dir="compile-libs" includes="*.jar" /&gt;</code> to it.
                    </ul>
                </div>
            </div>
            <div role="tabpanel" class="tab-pane" id="maven">
                <div>
                    You should be able to just follow the normal <a href="/install/others.html">lombok with maven
                    instructions</a>.<br/>
                    Note that if you use android, eclipse, and maven together you may have to replace <code>lombok.jar</code> in
                    your eclipse android project's build path
                    (which you can modify in that project's properties page) with <code>lombok-api.jar</code>, as produced in
                    the procedure explained for <em>Eclipse</em>,
                    above.
                </div>
            </div>
            <div role="tabpanel" class="tab-pane" id="gradle">
                <div>
                    <ul>
                        <li>Make sure that the version of your android plugin is <code>&gt;= 0.4.3</code></li>
                        <li>Add Lombok to your application's <code>dependencies</code> block:<br/><br/>
<pre>
	provided "org.projectlombok:lombok:1.12.6"
</pre>
                        </li>
                        <li>When using <a href="https://bitbucket.org/hvisser/android-apt">android-apt</a>, you also have to
                            specify Lombok as an annotation processor (with the <code>apt</code> directive) in the
                            <code>dependencies</code>
                            block:<br/><br/>
<pre>
	provided "org.projectlombok:lombok:1.12.6"
	apt "org.projectlombok:lombok:1.12.6"
</pre>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>
</@u.page>
