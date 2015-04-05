<#import "../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
<div class="row">
    <h1>Build systems</h1>

    <div role="tabpanel">

        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#maven" aria-controls="home" role="tab" data-toggle="tab">Maven</a>
            </li>
            <li role="presentation"><a href="#ivy" aria-controls="profile" role="tab" data-toggle="tab">Ivy</a></li>
            <li role="presentation"><a href="#sbt" aria-controls="messages" role="tab" data-toggle="tab">SBT</a></li>
            <li role="presentation"><a href="#gradle" aria-controls="settings" role="tab" data-toggle="tab">Gradle</a></li>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="maven">
                You can use lombok with maven by adding the following to your <strong>pom.xml</strong>:
                <div class="snippet">&lt;dependencies&gt;<br>
                    <span style="padding-left: 5em">&lt;dependency&gt;</span><br>
                    <span style="padding-left: 10em">&lt;groupId&gt;org.projectlombok&lt;/groupId&gt;</span><br>
                    <span style="padding-left: 10em">&lt;artifactId&gt;lombok&lt;/artifactId&gt;</span><br>
                    <span style="padding-left: 10em">&lt;version&gt;@VERSION@&lt;/version&gt;</span><br>
                    <span style="padding-left: 10em">&lt;scope&gt;provided&lt;/scope&gt;</span><br>
                    <span style="padding-left: 5em">&lt;/dependency&gt;</span><br>
                    &lt;/dependencies&gt;</div>
            </div>
            <div role="tabpanel" class="tab-pane" id="ivy">
                You can use lombok with ivy by adding the following to your <strong>ivy.xml</strong>:
                <div class="snippet">&lt;dependency org="org.projectlombok" name="lombok" rev="1.16.2" conf="build"
                    /&gt;</div>
            </div>
            <div role="tabpanel" class="tab-pane" id="sbt">
                You can use lombok with SBT by adding the following to your <strong>build.sbt</strong>:
                <div class="snippet">libraryDependencies += "org.projectlombok" % "lombok" % "1.16.2"</div>

            </div>
            <div role="tabpanel" class="tab-pane" id="gradle">
                <div>
                    You can use lombok with gradle by adding the following to your <strong>build.gradle</strong> in the <strong>dependencies</strong>
                    block:
                    <div class="snippet">provided "org.projectlombok:lombok:1.16.2"</div>
                </div>
                <div>
                    <em>NOTE:</em> You'll still need to download lombok, or doubleclick on the lombok.jar file downloaded by
                    maven /
                    ivy / gradle, to install lombok into your eclipse installation.
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <h1>Frameworks</h1>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#gwt" aria-controls="home" role="tab" data-toggle="tab">GWT</a></li>
            <li role="presentation"><a href="#play" aria-controls="profile" role="tab" data-toggle="tab">Play! framework</a>
            </li>
            <li role="presentation"><a href="#javadoc" aria-controls="messages" role="tab" data-toggle="tab">Javadoc</a></li>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="gwt">
                <p>
                    GWT (Google Web Toolkit) is compatible with lombok.
                </p>

                <p>
                    Edit your proj-debug and proj-compile batch scripts to add the following VM arguments:
			<pre>
java <strong>-javaagent:lombok.jar=ECJ</strong> <em>(rest of arguments)</em>
			</pre>
                Thanks to Stephen Haberman for figuring this out.
            </div>
            <div role="tabpanel" class="tab-pane" id="play">
                <p>
                    Use Aaron Freeman's
                    <a href="https://github.com/aaronfreeman/play-lombok#readme">lombok play plugin</a>
            </div>
            <div role="tabpanel" class="tab-pane" id="javadoc">
                <p>
                    First <a
                        href="/features/details/delombok.html">delombok</a> your code then run javadoc on the result.
                </p>
            </div>
        </div>
    </div>
</@u.page>
