<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>@FieldDefaults</h1>

            <div class="byline">New default field modifiers for the 21st century.</div>
            <p>
                @FieldDefaults was introduced as experimental feature in lombok v0.11.4.
            </p>
        </div>
    </div>
    <div class="row">
        <div class="experimental">
            <h3>Experimental</h3>

            <p>
                Experimental because:
            <ul>
                <li>New feature; unsure if this busts enough boilerplate</li>
                <li>Would be nice if you could stick this on the package-info.java package to set the default for all
                    classes in that package
                </li>
                <li>Part of the work on @Value, which is experimental</li>
            </ul>
            Current status: <em>positive</em> - Currently we feel this feature may move out of experimental status with
            no
            or minor changes soon.
        </div>
        <div class="row">
            <h3>Overview</h3>

            <p>
                The <code>@FieldDefaults</code> annotation can add an access modifier (<code>public</code>,
                <code>private</code>, or <code>protected</code>)
                to each field in the annotated class or enum. It can also add <code>final</code> to each field in the
                annotated class or enum.
            </p>

            <p>
                To add <code>final</code> to each field, use <code>@FieldDefaults(makeFinal=true)</code>. Any non-final
                field
                which must remain nonfinal
                can be annotated with <code>@NonFinal</code> (also in the <code>lombok.experimental</code> package).
            </p>

            <p>
                To add an access modifier to each field, use <code>@FieldDefaults(level=AccessLevel.PRIVATE)</code>. Any
                field that does not already have an
                access modifier (i.e. any field that looks like package private access) is changed to have the
                appropriate
                access modifier. Any package private
                field which must remain package private can be annotated with <code>@PackagePrivate</code> (also in the
                <code>lombok.experimental</code> package).
            </p>
        </div>
        <@u.comparison />
        <div class="row">
            <h3>Supported configuration keys:</h3>
            <dl>
                <dt><code>lombok.fieldDefaults.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default:
                    not
                    set)
                </dt>
                <dd>Lombok will flag any usage of <code>@FieldDefaults</code> as a warning or error if configured.</dd>
            </dl>
        </div>
        <div class="overview">
            <h3>Small print</h3>

            <div class="smallprint">
                <p>
                    Like other lombok handlers that touch fields, any field whose name starts with a dollar
                    (<code>$</code>)
                    symbol is skipped entirely.
                    Such a field will not be modified at all.
                </p>
            </div>
        </div>

    </div>
</div>
</@u.page>
