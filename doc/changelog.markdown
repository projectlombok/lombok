Lombok Changelog
----------------

### v0.11.1 (EDGE)
* BUGFIX: Eclipse refactor script 'rename method arguments' should work more often with lombok-affected methods.
* BUGFIX: Using 'val' in an enhanced for loop did not work if the iterable was a raw type.
* FEATURE: ONGOING: Fix for using lombok together with gwt-designer.

### v0.11.0 (March 26th, 2012)
* FEATURE: {Experimental} 'fluent' getters and setters (using just `fieldName` as methodname instead of `getFieldName`), setters that return `this` instead of `void`, and support for fields with prefixes is introduced with this lombok version. Also, the number of parameters of any existing methods with the same name that lombok would generate are now taken into account; previously if you had any method named `setX` regardless of how many parameters it has, lombok would avoid generating a `setX` method. Now lombok generates the method if all present `setX` methods have a number of parameters other than 1. [documentation](http://projectlombok.org/features/experimental/Accessors.html).
* FEATURE: The combination of `@Delegate` and `@Getter` or `@Data` will now delegate to the result of a generated getter. [Issue #328](http://code.google.com/p/projectlombok/issues/detail?id=328)
* FEATURE: Developing android apps on eclipse with lombok is now possible by running `java -jar lombok.jar publicApi` and including the generated jar in your project. [Documentation on using lombok for android development](http://projectlombok.org/setup/android.html).
* BUGFIX: In NetBeans the generated default constructor would still be generated even if Lombok also generated constructors. [Issue #326](http://code.google.com/p/projectlombok/issues/detail?id=326)
* BUGFIX: Some classes that contain @SneakyThrows would not compile (throw ClassFormatError). [Issue 339](http://code.google.com/p/projectlombok/issues/detail?id=339)
* BUGFIX: delombok: When `@Delegate` would generate a method with type parameters of the type `T extends package.Class`, a dot would be prepended to the type name. [Issue #341](http://code.google.com/p/projectlombok/issues/detail?id=341)
* BUGFIX: @Getter and @Setter now generate deprecated methods for deprecated fields. Fixes [Issue #342](http://code.google.com/p/projectlombok/issues/detail?id=342)
* BUGFIX: @Delegate would not generate @Deprecated on methods marked deprecated in javadoc. Fixes [Issue #348](http://code.google.com/p/projectlombok/issues/detail?id=348)
* BUGFIX: Using `val` with a type like `Outer<TypeArgs>.Inner` now works. [Issue #343](http://code.google.com/p/projectlombok/issues/detail?id=343)
* BUGFIX: `@Getter(lazy=true)` where the variable type is a primitive and the initializing expression is of a different primitive type that would type coerce implicitly, i.e. ints can be assigned to longs without a cast, didn't work before. [Issue #345](http://code.google.com/p/projectlombok/issues/detail?id=345)
* BUGFIX: `val` is no longer legal inside basic for loops (the old kind, not the foreach kind). These variables should rarely be final, and in practice it wasn't possible to delombok this code properly. [Issue #346](http://code.google.com/p/projectlombok/issues/detail?id=346)
* BUGFIX: PrettyCommentsPrinter now prints default clause of annotation methods. Fixes [Issue #350](http://code.google.com/p/projectlombok/issues/detail?id=350)

### v0.10.8 (January 19th, 2012)
* FEATURE: `@Delegate` can now be used on a no-argument method, which works similarly to adding it to fields. See [documentation](http://projectlombok.org/features/Delegate.html).
* BUGFIX: Eclipse refactoring Extract Interface was broken when using lombok annotation to generate methods. [Issue #86](http://code.google.com/p/projectlombok/issues/detail?id=86)
* BUGFIX: Eclipse action Sort Members was broken when using lombok annotations to generate methods or fields. [Issue #265](http://code.google.com/p/projectlombok/issues/detail?id=265)
* BUGFIX: Eclipse action Refactor/Rename on an inner type was broken when using lombok annotations. [Issue #316](http://code.google.com/p/projectlombok/issues/detail?id=316)
* BUGFIX: 0.10.6 causes ClassNotFoundErrors when using ecj (and thus, play framework, gwt, etc). [Issue #320](http://code.google.com/p/projectlombok/issues/detail?id=320)
* BUGFIX: Eclipse parsing was broken when using lombok annotations with parentheses. [Issue #325](http://code.google.com/p/projectlombok/issues/detail?id=325)
* ENHANCEMENT: Lombok now adds a line to the Eclipse About dialog about itself.

### v0.10.6 (December 19th, 2011)
* PERFORMANCE: Performance issues (memory leaks) when using lombok in netbeans, introduced in 0.10, have been fixed. [Issue #242](http://code.google.com/p/projectlombok/issues/detail?id=242)
* BUGFIX: Eclipse quickfix "Add unimplemented methods" would sometimes insert the new method stubs in strange places, especially if `@Data` was present. [Issue #51](http://code.google.com/p/projectlombok/issues/detail?id=51)
* BUGFIX: Eclipse quickfix "Assign parameter to new field" would insert it outside the class body if `@Data` was present. [Issue #222](http://code.google.com/p/projectlombok/issues/detail?id=222)
* BUGFIX: Renaming a @Data-annotated class in eclipse using Alt+Shift+R no longer mangles the data annotation. [Issue #286](http://code.google.com/p/projectlombok/issues/detail?id=286)
* BUGFIX: Using save action 'Use this qualifier for field accesses, only if necessary' did not work together with `@Data` in certain cases. [Issue #301](http://code.google.com/p/projectlombok/issues/detail?id=301)
* BUGFIX: Organize imports, either run manually or as save action, would throw an exception. [Issue #308](http://code.google.com/p/projectlombok/issues/detail?id=308)
* BUGFIX: Extracted constants would be placed outside the class body when a logging annotation was present. [Issue #315](http://code.google.com/p/projectlombok/issues/detail?id=315)

### v0.10.4 (November 21st, 2011)
* BUGFIX: Using the `log` field from `@Log`, etc, now works in static initializers. [Issue #295](http://code.google.com/p/projectlombok/issues/detail?id=295)
* BUGFIX: Auto-formatting code containing lombok on eclipse, even via an auto-save action, now works. [Issue #90](http://code.google.com/p/projectlombok/issues/detail?id=90)
* BUGFIX: Letting eclipse generate various methods when a lombok annotation is present now works. [Issue #138](http://code.google.com/p/projectlombok/issues/detail?id=138)
* BUGFIX: Renaming a @Data-annotated class in eclipse no longer mangles the data annotation. [Issue #286](http://code.google.com/p/projectlombok/issues/detail?id=286)
* BUGFIX: Eclipse save action *Add final modifier to private fields* no longer adds final keyword to `@Setter` fields. [Issue #263](http://code.google.com/p/projectlombok/issues/detail?id=263)
* BUGFIX: Mixing labels and `lombok.val` would cause NPEs in javac. [Issue #299](http://code.google.com/p/projectlombok/issues/detail?id=299)
* BUGFIX: Writing `lombok.val` out in full (vs. using an import statement) did not work in eclipse. [Issue #300](http://code.google.com/p/projectlombok/issues/detail?id=300)

### v0.10.2 (November 1st, 2011)
* BUGFIX: Delombok will no longer jumble up comments from different files when using -sourcepath option. [Issue #284](http://code.google.com/p/projectlombok/issues/detail?id=284)
* BUGFIX: Turns out treating `@NotNull` as an annotation that indicates lombok should generate nullcheck guards causes all sorts of problems. This has been removed again, and documentation has been updated to reflect this. [Issue #287](http://code.google.com/p/projectlombok/issues/detail?id=287)
* BUGFIX: `@EqualsAndHashCode` or `@Data` did not work on non-static inner classes whose outer class has a type variable. It does now. [Issue #289](http://code.google.com/p/projectlombok/issues/detail?id=289)

### v0.10.1 (October 3rd, 2011)
* BUGFIX: `@Delegate` in eclipse could cause memory leaks in 0.10.0. [Issue #264](http://code.google.com/p/projectlombok/issues/detail?id=264)
* BUGFIX: Annotations on enum values were being deleted by delombok. [Issue #269](http://code.google.com/p/projectlombok/issues/detail?id=269)
* BUGFIX: `@AllArgsConstructor` was erroneously generating a parameter and an assignment for final variables already assigned in their declaration. [Issue #278](http://code.google.com/p/projectlombok/issues/detail?id=278)
* ENHANCEMENT: `@NotNull` is now also recognized as an annotation indicating that lombok should generate nullcheck guards in generated constructors and setters. [Issue #271](http://code.google.com/p/projectlombok/issues/detail?id=271)

### v0.10.0 "Burning Emu" (August 19th, 2011)
* FEATURE: New annotation: @Delegate. This annotation lets lombok generate delegation methods for a given field. [More&hellip;](http://projectlombok.org/features/Delegate.html)
* FEATURE: Added support for 'val'. Val is an immutable variable that infers its type from the right hand side of the initializing expression. [More&hellip;](http://projectlombok.org/features/val.html)
* FEATURE: Added support for several logging frameworks via the `@Log`, `@Slf4j`, etc. annotation. [More&hellip;](http://projectlombok.org/features/Log.html)
* FEATURE: Lombok now supports post-compile transformers. [Issue #144](http://code.google.com/p/projectlombok/issues/detail?id=144)
* FEATURE: Using `@SneakyThrows` no longer requires a runtime dependency on lombok.jar. In fact, any call to `Lombok.sneakyThrows(ex)` is optimized at the bytecode level and no longer requires you to actually have lombok.jar or lombok-runtime.jar on the classpath.
* FEATURE: @*X*ArgsConstructor, @Getter, and @ToString can now be used on enum declarations. Previously, behaviour of these annotations on enums was undefined.
* FEATURE: @Getter/@Setter (and by extension, @Data) in v0.9.3 and earlier would generate getter and setter method names that did not conform to the beanspec, primarily when faced with boolean properties. This has been fixed. In practice this won't affect you unless you have properties named `isFoo` or `hasFoo`. Now the setter generated for this will be called `setFoo` (as the property name is `foo`) and not `setIsFoo`. Also, `hasFoo` is now no longer special; the names would be `isHasFoo` and `setHasFoo`. The java bean spec does not give `has` special meaning.
* FEATURE: `@EqualsAndHashCode` (and by extension, `@Data`) now add a `canEqual` method which improves the sanity of equality amongst a hierarchy of classes. [More&hellip;](http://projectlombok.org/features/EqualsAndHashCode.html)
* FEATURE: `@Getter` now supports a `lazy=true` attribute. [More&hellip;](http://projectlombok.org/features/GetterLazy.html)
* ENHANCEMENT: The installer will now find Eclipse installations when they are located in a subdirectory of a directory containing the word 'eclipse' . [Issue #210](http://code.google.com/p/projectlombok/issues/detail?id=210)
* ENHANCEMENT: Add null check for `@Cleanup` [Issue #154](http://code.google.com/p/projectlombok/issues/detail?id=154)
* BUGFIX: Lombok is now compatible with javac 7.
* BUGFIX: Hard to reproduce `NullPointerException` in Eclipse on the `getTypeBinding` method in the error log has been fixed. [Issue #164](http://code.google.com/p/projectlombok/issues/detail?id=164)
* BUGFIX: `@Setter` and `@Getter` can now be applied to static fields again (was broken in v0.9.3 only). [Issue #136](http://code.google.com/p/projectlombok/issues/detail?id=136)
* BUGFIX: delombok added type parameters to constructors that mirror the type's own type parameters. This resulted in delombok turning any generated constructor that takes at least 1 parameter of type 'T' into something that didn't compile, and to boot, a confusing error message ('T is not compatible with T'). This is now fixed. [Issue #140](http://code.google.com/p/projectlombok/issues/detail?id=140)
* BUGFIX: The Eclipse source generator would place the generated code outside the class [Issue #155](http://code.google.com/p/projectlombok/issues/detail?id=155)
* BUGFIX: When using m2eclipse, occasionally you'd see a ClassNotFoundError on JavacProcessingEnvironment. This has been fixed. [Issue #177](http://code.google.com/p/projectlombok/issues/detail?id=177)
* BUGFIX: Either all or none of `equals`, `hashCode` and `canEqual` will be generated. [Issue #240](http://code.google.com/p/projectlombok/issues/detail?id=240)
* BUGFIX: Delombok in output-to-directory mode was generating very long paths on mac and linux. [Issue #249](http://code.google.com/p/projectlombok/issues/detail?id=249)
* BUGFIX: Various refactor scripts and save actions bugs have been fixed in eclipse, though most remain.

### v0.9.3 "Burrowing Whale" (July 25th, 2010)
* FEATURE: Adding `@Getter` or `@Setter` to a class is now legal and is like adding those annotations to every non-static field in it. [Issue #129](http://code.google.com/p/projectlombok/issues/detail?id=129)
* FEATURE: Three new annotations, `@NoArgsConstructor`, `@RequiredArgsConstructor` and `@AllArgsConstructor` have been added. These split off `@Data`'s ability to generate constructors, and also allow you to finetune what kind of constructor you want. In addition, by using these annotations, you can force generation of constructors even if you have your own. [Issue #79](http://code.google.com/p/projectlombok/issues/detail?id=79)
* FEATURE: Constructors generated by lombok now include a `@java.beans.ConstructorProperties` annotation. This does mean these constructors no longer work in java 1.5, as this is a java 1.6 feature. The annotation can be suppressed by setting `suppressConstructorProperties` to `true` in a `@RequiredArgsConstructor` or `@AllArgsConstructor` annotation. [Issue #122](http://code.google.com/p/projectlombok/issues/detail?id=122)
* FEATURE: generated `toString`, `equals` and `hashCode` methods will now use `this.getX()` and `other.getX()` instead of `this.x` and `other.x` if a suitable getter is available. This behaviour is useful for proxied classes, such as the POJOs that hibernate makes. Usage of the getters can be suppressed with `@ToString/@EqualsAndHashCode(doNotUseGetters = true)`. [Issue #110](http://code.google.com/p/projectlombok/issues/detail?id=110)
* ENHANCEMENT: FindBugs' `@CheckForNull` is now copied from a field to a setter's parameter and the getter method just like `@Nullable`. [Issue #128](http://code.google.com/p/projectlombok/issues/detail?id=128)
* ENHANCEMENT: plugins and `@SneakyThrows`: Resolving types in annotations now works better especially for classes that aren't in the core java libraries. [Issue #88](http://code.google.com/p/projectlombok/issues/detail?id=88)
* ENHANCEMENT: If `tools.jar` isn't found (required when running _delombok_), now a useful error message is generated. The search for `tools.jar` now also looks in `JAVA_HOME`.
* ENHANCEMENT: toString() on inner classes now lists the class name as `Outer.Inner` instead of just `Inner`. [Issue #133](http://code.google.com/p/projectlombok/issues/detail?id=133)
* ENHANCEMENT: All field accesses generated by lombok are now qualified (like so: `this.fieldName`). For those who have a warning configured for unqualified field access, those should no longer occur. [Issue #48](http://code.google.com/p/projectlombok/issues/detail?id=48)
* ENHANCEMENT: All fields and methods generated by lombok now get `@SuppressWarnings("all")` attached to avoid such warnings as missing javadoc, for those of you who have that warning enabled. [Issue #47](http://code.google.com/p/projectlombok/issues/detail?id=47)
* PLATFORMS: Lombok should now run in stand-alone ecj (Eclipse Compiler for Java). This isn't just useful for the few souls actually using this compiler day to day, but various eclipse build tools such as the RCP builder run ecj internally as well. [Issue #72](http://code.google.com/p/projectlombok/issues/detail?id=72)
* BUGFIX: Eclipse: `@Data` and other annotations now don't throw errors when you include fields with bounded wildcard generics, such as `List<? extends Number>`. [Issue #84](http://code.google.com/p/projectlombok/issues/detail?id=84)
* BUGFIX: complex enums didn't get delomboked properly. [Issue #96](http://code.google.com/p/projectlombok/issues/detail?id=96)
* BUGFIX: delombok now no longer forgets to remove `import lombok.AccessLevel;`. In netbeans, that import will no longer be flagged erroneously as being unused. [Issue #100](http://code.google.com/p/projectlombok/issues/detail?id=100) and [Issue #103](http://code.google.com/p/projectlombok/issues/detail?id=103)
* BUGFIX: While its discouraged, `import lombok.*;` is supposed to work in the vast majority of cases. In eclipse, however, it didn't. Now it does. [Issue #102](http://code.google.com/p/projectlombok/issues/detail?id=102)
* BUGFIX: When `@Getter` or `@Setter` is applied to a multiple field declaration, such as `@Getter int x, y;`, the annotation now applies to all fields, not just the first. [Issue #54](http://code.google.com/p/projectlombok/issues/detail?id=54)
* BUGFIX: delombok on most javacs would quit with a NoSuchFieldError if it contains `<?>` style wildcards anywhere in the source, as well as at least 1 lombok annotation. No longer. [Issue #134](http://code.google.com/p/projectlombok/issues/detail?id=134)
* BUILD: dependencies are now fetched automatically via ivy, and most dependencies now include sources by default, which is particularly handy for those working on the lombok sources themselves.

### v0.9.2 "Hailbunny" (December 15th, 2009)
* preliminary support for lombok on NetBeans! - thanks go to Jan Lahoda from NetBeans. [Issue #20](http://code.google.com/p/projectlombok/issues/detail?id=20)
* lombok now ships with the delombok tool, which copies an entire directory filled with sources to a new directory, desugaring any java files to what it would look like without lombok's transformations. Compiling the sources in this new directory without lombok support should result in the same class files as compiling the original with lombok support. Great to double check on what lombok is doing, and for chaining the delombok-ed sources to source-based java tools such as Google Web Toolkit or javadoc. lombok.jar itself also provides an ant task for delombok. [Full documentation of delombok](http://projectlombok.org/features/delombok.html).
* Lombok now works on openjdk7 (tested with JDK7m5)! For all the folks on the cutting edge, this should be very good news. [Issue #61](http://code.google.com/p/projectlombok/issues/detail?id=61) - thanks go to Jan Lahoda from NetBeans.
* lombok now has various command-line accessible utilities bundled with it. Run `java -jar lombok.jar --help` to see them. Included (aside from the already mentioned delombok):
* Ability to create a tiny jar named lombok-runtime.jar with runtime dependencies. The lombok transformations that have a runtime dependency on this jar can be listed as well. Run `java -jar lombok.jar createRuntime --help` for more information.
* Scriptable command line install and uninstall options. Run `java -jar lombok.jar install --help` (or `uninstall`, of course) for more information. Technically this support has been there in earlier versions, but the command line options are now much more lenient, not to mention more visible.
* Lombok now works on Springsource Tool Suite. [Issue #22](http://code.google.com/p/projectlombok/issues/detail?id=22)
* Lombok now works on JDK 1.6.0_0, for those of us who have really old JDK1.6's installed on their system. [Issue #83](http://code.google.com/p/projectlombok/issues/detail?id=83)
* Erroneous use of lombok in Eclipse (adding it to a project as an annotation processor, which is not how lombok is to be used on Eclipse) now generates a useful warning message with helpful information, instead of a confusing error hidden in the logs. [Issue #53](http://code.google.com/p/projectlombok/issues/detail?id=53)
* FIXED: Regression bug where you would occasionally see errors with the gist 'loader constraint violation: when resolving...', such as when opening the help system, starting the diff editor, or, rarely, opening any java source file. [Issue #68](http://code.google.com/p/projectlombok/issues/detail?id=68)
* FIXED: @SneakyThrows without any parameters should default to `Throwable.class` but it didn't do anything in javac. [Issue #73](http://code.google.com/p/projectlombok/issues/detail?id=73)
* FIXED: Capitalization is now ignored when scanning for existing methods, so if `setURL` already exists, then a `@Data` annotation on a class with a field named `url` will no longer _also_ generate `setUrl`. [Issue #75](http://code.google.com/p/projectlombok/issues/detail?id=75)

### v0.9.1 (November 9th, 2009)

* The installer now works much better on linux, in that it auto-finds eclipse in most locations linux users tend to put their eclipse installs, and it can now handle apt-get installed eclipses, which previously didn't work well at all. There's also a hidden feature where the installer can work as a command-line only tool (`java -jar lombok.jar install eclipse path/to/eclipse`) which also supports `uninstall` of course. You can now also point at `eclipse.ini` in case you have a really odd eclipse install, which should always work.
* For lombok developers, the eclipse launch target now works out-of-the-box on snow leopard. [Issue #66](http://code.google.com/p/projectlombok/issues/detail?id=66)

### v0.9.0 (November 2nd, 2009)

* The lombok class patching system has been completely revamped; the core business of patching class files has been offloaded in an independent project called 'lombok.patcher', which is now used to patch lombok into eclipse.
* Many behind-the-scenes changes to improve lombok's stability and flexibility on eclipse.
* Changes to the lombok core API which aren't backwards compatible with lombok series v0.8 but which were necessary to make writing third party processors for lombok a lot easier.
* Minor version number bumped due to the above 3 issues.
* Eclipse's "rename" refactor script, invoked by pressing CMD/CTRL+SHIFT+R, now works on `@Data` annotated classes.
* The windows installer would fail on boot if you have unformatted drives. [Issue #65](http://code.google.com/p/projectlombok/issues/detail?id=65)
* The static constructor that `@Data` can make was being generated as package private when compiling with javac. [Issue #63](http://code.google.com/p/projectlombok/issues/detail?id=63)

### v0.8.5 (September 3rd, 2009)

* There's now an `AccessLevel.NONE` that you can use for your `@Getter` and `@Setter` annotations to suppress generating setters and getters when you're using the `@Data` annotation. Address [Issue #37](http://code.google.com/p/projectlombok/issues/detail?id=37)
* Both `@EqualsAndHashCode` and `@ToString` now support explicitly specifying the fields to use, via the new 'of' parameter. Fields that begin with a '$' are now also excluded by default from equals, hashCode, and toString generation, unless of course you explicitly mention them in the 'of' parameter. Addresses [Issue #32](http://code.google.com/p/projectlombok/issues/detail?id=32)
* There's a commonly used `@NotNull` annotation, from javax.validation (and in earlier versions of hibernate, which is the origin of javax.validation) which does not quite mean what we want it to mean: It is not legal on parameters, and it is checked at runtime after an explicit request for validation. As a workaround, we've removed checking for any annotation named `NotNull` from the nonnull support of lombok's generated Getters, Setters, and constructors. [Issue #43](http://code.google.com/p/projectlombok/issues/detail?id=43)
* Fixed yet another issue with `@SneakyThrows`. This was reported fixed in v0.8.4. but it still didn't work quite as it should. Still falls under the bailiwick of
[Issue #30](http://code.google.com/p/projectlombok/issues/detail?id=30)

### v0.8.4 (September 2nd, 2009)

* Fixed many issues with `@SneakyThrows` - in previous versions, using it would sometimes confuse the syntax colouring, and various constructs in the annotated method would cause outright eclipse errors, such as beginning the method with a try block. This also fixes [Issue #30](http://code.google.com/p/projectlombok/issues/detail?id=30)
* Fixed the David Lynch bug - in eclipse, classes with lombok features used in them would sometimes appear invisible from other source files. It's described in more detail on [Issue #41](http://code.google.com/p/projectlombok/issues/detail?id=41). If you suffered from it, you'll know what this is about.
* Fixed the problem where eclipse's help system did not start up on lombokized eclipses. [Issue #26](http://code.google.com/p/projectlombok/issues/detail?id=26)
* All generated methods now make their parameters (if they have any) final. This should help avoid problems with the 'make all parameters final' save action in eclipse. [Issue #40](http://code.google.com/p/projectlombok/issues/detail?id=40)
* Okay, this time _really_ added support for @NonNull and @NotNull annotations. It was reported for v0.8.3 but it wasn't actually in that release. @Nullable annotations are now also copied over to the getter's return type and the setter and constructor's parameters (but, obviously, no check is added). Any @NonNull annotated non-final fields that are not initialized are now also added to the generated constructor by @Data in order to ensure via an explicit null check that they contain a legal value.
* @ToString (and hence, @Data) now default to includeFieldNames=true. [Issue #35](http://code.google.com/p/projectlombok/issues/detail?id=35)

### v0.8.3 (August 21st, 2009)

* @EqualsAndHashCode (and, indirectly, @Data) generate a warning when overriding a class other than java.lang.Object but not setting EqualsAndHashCode's callSuper to true. There are, however, legitimate reasons to do this, so this warning is now no longer generated if you explicitly set callSuper to false. The warning text now also refers to this action if not calling super is intentional.
* If your fields have @NonNull or @NotNull annotations, then generated setters are generated with a null check, and the
annotation is copied to the setter's parameter, and the getter's method.
* An annoying bug that usually showed up if you had package-info.java files has been fixed. It would cause a `NullPointerException` at lombok.javac.apt.Processor.toUnit(Processor.java:143)

### v0.8.2 (July 29th, 2009)

* @EqualsAndHashCode and @ToString created; these are subsets of what @Data does (namely: generate toString(), and generate equals() and hashCode() implementations). @Data will still generate these methods, but you can now generate them separately if you wish. As part of this split off, you can now specify for toString generation to include the field names in the produced toString method, and for all 3 methods: You can choose to involve the implementation of the superclass, and you can choose to exclude certain fields. [Issue #8](http://code.google.com/p/projectlombok/issues/detail?id=8)
* when compiling with javac: warnings on specific entries of an annotation parameter (such as non-existent fields in a @EqualsAndHashCode exclude parameter) now show up on the problematic parameter and not on the entire annotation. [Issue #11](http://code.google.com/p/projectlombok/issues/detail?id=11)

### v0.8.1 (July 26th, 2009)

* Changelog tracking from this version on.
* Using eclipse's 'find callers' on a @Data annotation will now find callers of the static constructor if you generated it. If not, it still finds callers to hashCode() as before (it's not possible to make eclipse find callers to the normal constructor, though you can just use 'find callers' on the class name, which works fine). [Issue #5](http://code.google.com/p/projectlombok/issues/detail?id=5)
* If your field is called 'hasFoo' and its a boolean, and you use @Getter or @Data to generate a getter for it, that getter will now be called 'hasFoo' and not 'isHasFoo' as before. This rule holds for any field prefixed with 'has', 'is', or 'get', AND the character following the prefix is not lowercase (so that 'hashCodeGenerated' is not erroneously identified as already having a prefix!). Similar logic has been added to not generate a getter at all for a field named 'foo' or 'hasFoo' if there is already a method named 'isFoo'. [Issue #4](http://code.google.com/p/projectlombok/issues/detail?id=4)
* Starting the lombok installer on mac os X using soylatte instead of apple's JVM now correctly detects being on a mac, and using mac-specific code for finding and installing eclipses. [Issue #7](http://code.google.com/p/projectlombok/issues/detail?id=7)
* For non-mac, non-windows installations, the jar file in the `-javaagent` parameter is now written as an absolute path in `eclipse.ini` instead of a relative one. For some reason, on at least 1 linux installation, an absolute path is required to make javaagent work. This 'fix' has the unfortunate side-effect of making it impossible to move your eclipse installation around without breaking the pointer to the lombok java agent, so this change has only been introduced for non-windows, non-mac. Thanks to WouterS for spotting this one and helping us out with some research on fixing it. [Issue #6](http://code.google.com/p/projectlombok/issues/detail?id=6)

### v0.8

* Initial release before announcements
* (note: There are a few different editions of lombok out there, all tagged with v0.8.)
