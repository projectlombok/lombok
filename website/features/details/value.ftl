<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>@Value</h1>

            <h3>Immutable classes made very easy.</h3>
        </div>
    </div>
    <div class="row">

        <p>
            <code>@Value</code> was introduced as experimental feature in lombok v0.11.4.
        </p>

        <p>
            <code>@Value</code> no longer implies <code>@Wither</code> since lombok v0.11.8.
        </p>

        <p>
            <code>@Value</code> promoted to the main <code>lombok</code> package since lombok v0.12.0.
        </p>
    </div>

    <div class="row">
        <h3>Overview</h3>

        <p>
            <code>@Value</code> is the immutable variant of <a href="/features/details/data.html"><code>@Data</code></a>; all
            fields are
            made <code>private</code> and <code>final</code> by default, and setters are not generated. The class itself
            is also made <code>final</code> by default, because immutability is not something that can be forced onto a
            subclass. Like <code>@Data</code>, useful <code>toString()</code>, <code>equals()</code> and
            <code>hashCode()</code>
            methods are also generated, each field gets a getter method, and a constructor that covers every
            argument (except <code>final</code> fields that are initialized in the field declaration) is also generated.
        </p>

        <p>
            In practice, <code>@Value</code> is shorthand for: <code>final @ToString @EqualsAndHashCode
            @AllArgsConstructor @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE) @Getter</code>, except
            that explicitly including an implementation of any of the relevant methods simply means that part won't be
            generated and no warning will be emitted. For example, if you write your own <code>toString</code>, no error
            occurs, and lombok will not generate a <code>toString</code>. Also, <em>any</em> explicit constructor, no
            matter the arguments list, implies lombok will not generate a constructor. If you do want lombok to generate
            the all-args constructor, add <code>@AllArgsConstructor</code> to the class. You can mark any constructor or
            method with <code>@lombok.experimental.Tolerate</code> to hide them from lombok.
        </p>

        <p>
            It is possible to override the final-by-default and private-by-default behaviour using either an explicit
            access level on a field, or by using the <code>@NonFinal</code> or <code>@PackagePrivate</code>
            annotations.<br/>
            It is possible to override any default behaviour for any of the 'parts' that make up <code>@Value</code> by
            explicitly using that annotation.
        </p>
    </div>
    <@u.comparison />
    <div class="row">
        <h3>Supported configuration keys:</h3>
        <dl>
            <dt><code>lombok.value.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not set)
            </dt>
            <dd>Lombok will flag any usage of <code>@Value</code> as a warning or error if configured.</dd>
        </dl>
    </div>
    <div class="overview">
        <h3>Small print</h3>

        <div class="smallprint">
            <p>
                Look for the documentation on the 'parts' of <code>@Value</code>: <a
                    href="/features/details/to-string.html"><code>@ToString</code></a>,
                <a href="/features/details/equals-and-hashcode.html"><code>@EqualsAndHashCode</code></a>, <a
                    href="/features/details/contructor.html"><code>@AllArgsConstructor</code></a>,
                <a href="/features/details/field-defaults.html"><code>@FieldDefaults</code></a>, and <a
                    href="/features/details/getter-setter.html"><code>@Getter</code></a>.
            </p>

            <p>
                For classes with generics, it's useful to have a static method which serves as a constructor, because
                inference of generic parameters via static methods works in java6 and avoids having to use the diamond
                operator. While you can force this by applying an explicit
                <code>@AllArgsConstructor(staticConstructor="of")</code>
                annotation, there's also the <code>@Value(staticConstructor="of")</code> feature, which will make the
                generated all-arguments constructor private, and generates a public static method named <code>of</code>
                which is a wrapper around this private constructor.
            </p>

            <p>
                <code>@Value</code> was an experimental feature from v0.11.4 to v0.11.9 (as
                <code>@lombok.experimental.Value</code>).
                It has since been moved into the core package. The old annotation is still
                around (and is an alias). It will eventually be removed in a future version, though.
            </p>

            <p>
                It is not possible to use <code>@FieldDefaults</code> to 'undo' the private-by-default and
                final-by-default aspect of fields in the annotated class. Use <code>@NonFinal</code> and
                <code>@PackagePrivate</code>
                on the fields in the class to override this behaviour.
            </p>
        </div>
    </div>
</div>
</@u.page>
