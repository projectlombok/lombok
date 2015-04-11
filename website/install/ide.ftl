<#import "../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row">

        <h1>Environments</h1>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active"><a href="#eclipse" aria-controls="home" role="tab"
                                                      data-toggle="tab">Eclipse</a>
            </li>
            <li role="presentation"><a href="#idea" aria-controls="profile" role="tab" data-toggle="tab">IDEA</a>
            <li role="presentation"><a href="#beans" aria-controls="profile" role="tab" data-toggle="tab">NetBeans</a>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="eclipse">
                <p>
                    Run <code>lombok.jar</code> as a java app (i.e. doubleclick it, usually) to
                    install. Also add lombok.jar to your project. <span style="font-size: 0.8em;"><em>Supported
                    variants: Springsource Tool Suite, JBoss Developer Studio</em></span>

                </p>
            </div>
            <div role="tabpanel" class="tab-pane" id="idea">
                <p>
                    <a href="https://code.google.com/p/lombok-intellij-plugin/">A plugin
                        developed by Michael Plushnikov</a> adds support for most features.
                </p>

            </div>
            <div role="tabpanel" class="tab-pane" id="beans">
                <div class="container">
                    <ul>
                        <li>Add <code>lombok.jar</code> to the project Libraries.</li>
                        <li>In the project Properties, in the section Build &ndash; Compiling, check the &#39;Enable Annotation
                            Processing in Editor&#39; checkbox.
                        </li>
                    </ul>
                    <img src="/images/netbeans-enable-annotation-processing-in-editor.png"/>
                </div>
            </div>
        </div>
    </div>
</div>
</@u.page>
