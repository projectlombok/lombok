<#import "../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <h1>Lombok features.</h1>
    </div>
    <div class="row">
        <@u.feature title="val" code="val">
            Finally! Hassle-free final local variables.
        </@u.feature>

        <@u.feature title="@NonNull" code="non-null">
            or: How I learned to stop worrying and love the NullPointerException.
         </@u.feature>

        <@u.feature title="@Cleanup" code="cleanup">
            Automatic resource management: Call your <code>close()</code> methods safely with no hassle.
         </@u.feature>

        <@u.feature title="@Getter/@Setter" code="getter-setter">
            Never write <code>public int getFoo() {return foo;}</code> again.
         </@u.feature>

        <@u.feature title="@ToString" code="to-string">
            No need to start a debugger to see your fields: Just let lombok generate a <code>toString</code> for
            you!
         </@u.feature>

        <@u.feature title="@EqualsAndHashCode" code="equals-and-hashcode">
            Equality made easy: Generates <code>hashCode</code> and <code>equals</code> implementations from the
            fields of your object..
         </@u.feature>

        <@u.feature title="@NoArgsConstructor, @RequiredArgsConstructor and @AllArgsConstructor" code="constructor">
            Constructors made to order: Generates constructors that take no arguments, one argument per final /
            non-nullfield, or one argument for every field.
         </@u.feature>

        <@u.feature title="@Data" code="data">
            All together now: A shortcut for <code>@ToString</code>, <code>@EqualsAndHashCode</code>,
            <code>@Getter</code> on all fields, and <code>@Setter</code> on all non-final fields, and
            <code>@RequiredArgsConstructor</code>!
         </@u.feature>

        <@u.feature title="@Value" code="value">
            Immutable classes made very easy.
         </@u.feature>

        <@u.feature title="@Builder" code="builder">
            ... and Bob's your uncle: No-hassle fancy-pants APIs for object creation!
         </@u.feature>

        <@u.feature title="@SneakyThrows" code="sneaky-throws">
            To boldly throw checked exceptions where no one has thrown them before!
         </@u.feature>

        <@u.feature title="@Synchronized" code="sync">
            <code>synchronized</code> done right: Don't expose your locks.
         </@u.feature>

        <@u.feature title="@Getter(lazy=true)" code="getter-lazy">
            Laziness is a virtue!
         </@u.feature>

        <@u.feature title="@Log" code="log">
            Captain's Log, stardate 24435.7: &quot;What was that line again?&quot;
         </@u.feature>
    </div>

    <div class="row">
        <h1>Configuration system</h1>

        <div class="text-center">
            Lombok, made to order: <a href="/features/details/configuration.html">Configure lombok features</a> in one place for
            your entire project or even
            your workspace.
        </div>
    </div>

    <div class="row">
        <h1 class="text-center">Running delombok</h1>

        <div>Delombok copies your source files to another directory, replacing all lombok annotations with their
            desugared form.
            So, it'll turn <code>@Getter</code>
            back into the actual getter. It then removes the annotation. This is useful for all sorts of reasons;
            you
            can check
            out what's happening under the hood,
            if the unthinkable happens and you want to stop using lombok, you can easily remove all traces of it in
            your
            source,
            and you can use delombok to preprocess
            your source files for source-level tools such as javadoc and GWT. More information about how to run
            delombok,
            including instructions for build tools
            can be found at the <a href="/features/details/delombok.html">delombok page</a>.
        </div>
    </div>
</div>
</@u.page>
