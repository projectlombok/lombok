<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>@Log (and friends)</h1>

            <h3>Captain's Log, stardate 24435.7: &quot;What was that line again?&quot;</h3>
        </div>
    </div>
    <div class="row">
        <h3>Overview</h3>

        <p>
            <em>NEW in lombok 0.10: </em>You can annotate any class with a log annotation to let lombok generate a
            logger field.<br/>
            The logger is named <code>log</code> and the field's type depends on which logger you have selected.
        </p>

        <p>
            There are six choices available:<br/>
        <dl>
            <dt><code>@CommonsLog</code></dt>
            <dd>Creates <pre><span class="keyword">private&nbsp;static&nbsp;final&nbsp;</span><a
                    href="http://commons.apache.org/logging/apidocs/org/apache/commons/logging/Log.html">org.apache.commons.logging.Log</a>&nbsp;<span
                    class="staticfield">log</span>&nbsp;=&nbsp;<a
                    href="http://commons.apache.org/logging/apidocs/org/apache/commons/logging/LogFactory.html#getLog(java.lang.Class)">org.apache.commons.logging.LogFactory.getLog</a>(LogExample.<span
                    class="keyword">class</span>);</pre></dd>
            <dt><code>@Log</code></dt>
            <dd>Creates <pre><span class="keyword">private&nbsp;static&nbsp;final&nbsp;</span><a
                    href="http://download.oracle.com/javase/6/docs/api/java/util/logging/Logger.html">java.util.logging.Logger</a>&nbsp;<span
                    class="staticfield">log</span>&nbsp;=&nbsp;<a
                    href="http://download.oracle.com/javase/6/docs/api/java/util/logging/Logger.html#getLogger(java.lang.String)">java.util.logging.Logger.getLogger</a>(LogExample.<span
                    class="keyword">class</span>.getName());</pre></dd>
            <dt><code>@Log4j</code></dt>
            <dd>Creates <pre><span class="keyword">private&nbsp;static&nbsp;final&nbsp;</span><a
                    href="http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Logger.html">org.apache.log4j.Logger</a>&nbsp;<span
                    class="staticfield">log</span>&nbsp;=&nbsp;<a
                    href="http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Logger.html#getLogger(java.lang.Class)">org.apache.log4j.Logger.getLogger</a>(LogExample.<span
                    class="keyword">class</span>);</pre></dd>
            <dt><code>@Log4j2</code></dt>
            <dd>Creates <pre><span class="keyword">private&nbsp;static&nbsp;final&nbsp;</span><a
                    href="http://logging.apache.org/log4j/2.0/log4j-api/apidocs/org/apache/logging/log4j/Logger.html">org.apache.logging.log4j.Logger</a>&nbsp;<span
                    class="staticfield">log</span>&nbsp;=&nbsp;<a
                    href="http://logging.apache.org/log4j/2.0/log4j-api/apidocs/org/apache/logging/log4j/LogManager.html#getLogger(java.lang.Class)">org.apache.logging.log4j.LogManager.getLogger</a>(LogExample.<span
                    class="keyword">class</span>);</pre></dd>
            <dt><code>@Slf4j</code></dt>
            <dd>Creates <pre><span class="keyword">private&nbsp;static&nbsp;final&nbsp;</span><a
                    href="http://www.slf4j.org/api/org/slf4j/Logger.html">org.slf4j.Logger</a>&nbsp;<span
                    class="staticfield">log</span>&nbsp;=&nbsp;<a
                    href="http://www.slf4j.org/apidocs/org/slf4j/LoggerFactory.html#getLogger(java.lang.Class)">org.slf4j.LoggerFactory.getLogger</a>(LogExample.<span
                    class="keyword">class</span>);</pre></dd>
            <dt><code>@XSlf4j</code></dt>
            <dd>Creates <pre><span class="keyword">private&nbsp;static&nbsp;final&nbsp;</span><a
                    href="http://www.slf4j.org/api/org/slf4j/ext/XLogger.html">org.slf4j.ext.XLogger</a>&nbsp;<span
                    class="staticfield">log</span>&nbsp;=&nbsp;<a
                    href="http://www.slf4j.org/apidocs/org/slf4j/ext/XLoggerFactory.html#getXLogger(java.lang.Class)">org.slf4j.ext.XLoggerFactory.getXLogger</a>(LogExample.<span
                    class="keyword">class</span>);</pre></dd>
        </dl>
        <p>
            By default, the topic (or name) of the logger will be the class name of the class annotated with the
            <code>@Log</code> annotation. This can be customised by specifying the <code>topic</code> parameter. For
            example: <code>@XSlf4j(topic="reporting")</code>.
        </p>
    </div>
    <@u.comparison />
    <div class="row">
        <h3>Supported configuration keys:</h3>
        <dl>
            <dt><code>lombok.log.fieldName</code> = <em>an identifier</em> (default: <code>log</code>)</dt>
            <dd>The generated logger fieldname is by default '<code>log</code>', but you can change it to a different
                name with this setting.
            </dd>
            <dt><code>lombok.log.fieldIsStatic</code> = [<code>true</code> | <code>false</code>] (default: true)</dt>
            <dd>Normally the generated logger is a <code>static</code> field. By setting this key to <code>false</code>,
                the generated field will be an instance field instead.
            </dd>
            <dt><code>lombok.log.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not set)</dt>
            <dd>Lombok will flag any usage of any of the various log annotations as a warning or error if configured.
            </dd>
            <dt><code>lombok.log.apacheCommons.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default:
                not set)
            </dt>
            <dd>Lombok will flag any usage of <code>@lombok.extern.apachecommons.CommonsLog</code> as a warning or error
                if configured.
            </dd>
            <dt><code>lombok.log.javaUtilLogging.flagUsage</code> = [<code>warning</code> | <code>error</code>]
                (default: not set)
            </dt>
            <dd>Lombok will flag any usage of <code>@lombok.extern.java.Log</code> as a warning or error if configured.
            </dd>
            <dt><code>lombok.log.log4j.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not set)
            </dt>
            <dd>Lombok will flag any usage of <code>@lombok.extern.log4j.Log4j</code> as a warning or error if
                configured.
            </dd>
            <dt><code>lombok.log.log4j2.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not
                set)
            </dt>
            <dd>Lombok will flag any usage of <code>@lombok.extern.log4j.Log4j2</code> as a warning or error if
                configured.
            </dd>
            <dt><code>lombok.log.slf4j.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not set)
            </dt>
            <dd>Lombok will flag any usage of <code>@lombok.extern.slf4j.Slf4j</code> as a warning or error if
                configured.
            </dd>
            <dt><code>lombok.log.xslf4j.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not
                set)
            </dt>
            <dd>Lombok will flag any usage of <code>@lombok.extern.slf4j.XSlf4j</code> as a warning or error if
                configured.
            </dd>
        </dl>
    </div>
    <div class="row">
        <h3>Small print</h3>

        <div class="smallprint">
            <p>
                If a field called <code>log</code> already exists, a warning will be emitted and no code will be
                generated.
            </p>

            <p>
                A future feature of lombok's diverse log annotations is to find calls to the logger field and, if the
                chosen logging framework supports it and the log level can be compile-time determined from the log call,
                guard it with an <code>if</code> statement. This way if the log statement ends up being ignored, the
                potentially expensive calculation of the log string is avoided entirely. This does mean that you should
                <em>NOT</em> put any side-effects in the expression that you log.
            </p>
        </div>
    </div>
</div>
</@u.page>
