<#import "../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">

    <div class="row">
        <h1>Compilers</h1>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#javac" aria-controls="home" role="tab" data-toggle="tab">Javac</a>
            </li>
            <li role="presentation"><a href="#ecj" aria-controls="profile" role="tab" data-toggle="tab">ECJ</a>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="javac">
                <p>
                    Just put <code>lombok.jar</code> on the classpath.
                </p>
            </div>
            <div role="tabpanel" class="tab-pane" id="ecj">
                <p>
                    ecj (the eclipse standalone compiler) is compatible with lombok. Use the following command line to enable
                    lombok with ecj:
			<pre>
                java <strong>-javaagent:lombok.jar=ECJ -Xbootclasspath/p:lombok.jar</strong> -jar ecj.jar -cp lombok.jar <em>(rest of arguments)</em>
            </pre>
                If you're using a tool based on ecj, adding these VM arguments and adding lombok.jar to the classpath should
                work.
            </div>
        </div>
    </div>
</div>
</@u.page>
