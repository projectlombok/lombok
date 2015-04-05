<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>@NonNull</h1>

            <h3>or: How I learned to stop worrying and love the NullPointerException.</h3>
        </div>
    </div>
    <div class="row">
        <h3>Overview</h3>

        <p>
            <em>NEW in Lombok 0.11.10: </em>You can use <code>@NonNull</code> on the parameter of a method or
            constructor to have lombok generate a null-check statement for you.
        </p>

        <p>
            Lombok has always treated any annotation named <code>@NonNull</code> on a field as a signal to generate
            a null-check if lombok generates an entire method or constructor for you, via
            for example <a href="/features/details/data.html"><code>@Data</code></a>. Now, however, using lombok's own <code>
            @lombok.NonNull</code>
            on a parameter results in the insertion of just the null-check
            statement inside your own method or constructor.
        </p>

        <p>
            The null-check looks like <pre>if (param == null) throw new NullPointerException("param");</pre> and
            will be inserted at the very top of your method. For constructors, the null-check
            will be inserted immediately following any explicit <code>this()</code> or <code>super()</code> calls.
        </p>

        <p>
            If a null-check is already present at the top, no additional null-check will be generated.
        </p>
    </div>
    <@u.comparison />
    <div class="row">
        <h3>Supported configuration keys:</h3>
        <dl>
            <dt><code>lombok.nonNull.exceptionType</code> = [<code>NullPointerException</code> |
                <code>IllegalArgumentException</code>]
                (default: <code>NullPointerException</code>).
            <dd>When lombok generates a null-check <code>if</code> statement, by default, a
                <code>java.lang.NullPointerException</code>
                will be thrown with the field name as the exception message.
                However, you can use <code>IllegalArgumentException</code> in this configuration key to have lombok
                throw that exception, with '<em>fieldName</em> is null' as exception message.
            </dd>
            <dt><code>lombok.nonNull.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not
                set)
            </dt>
            <dd>Lombok will flag any usage of <code>@NonNull</code> as a warning or error if configured.</dd>
        </dl>
    </div>
    <div class="overview">
        <h3>Small print</h3>

        <div class="smallprint">
            <p>
                Lombok's detection scheme for already existing null-checks consists of scanning for if statements
                that look just like lombok's own. Any 'throws' statement as
                the 'then' part of the if statement, whether in braces or not, counts. The conditional of the if
                statement <em>must</em> look exactly like <code>PARAMNAME == null</code>.
                The first statement in your method that is not such a null-check stops the process of inspecting for
                null-checks.
            </p>

            <p>
                While <code>@Data</code> and other method-generating lombok annotations will trigger on any
                annotation named <code>@NonNull</code> regardless of casing or package name,
                this feature only triggers on lombok's own <code>@NonNull</code> annotation from the
                <code>lombok</code> package.
            </p>

            <p>
                A <code>@NonNull</code> on a primitive parameter results in a warning. No null-check will be
                generated.
            </p>
        </div>
    </div>
</div>
</@u.page>
