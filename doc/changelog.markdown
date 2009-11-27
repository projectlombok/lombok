Lombok Changelog
----------------

### v0.9.2
* lombok now ships with the delombok tool, which copies an entire directory filled with sources to a new directory, desugaring any java files to what it would look like without lombok's transformations. Compiling the sources in this new directory without lombok support should result in the same class files as compiling the original with lombok support. Great to double check on what lombok is doing, and for chaining the delombok-ed sources to source-based java tools such as Google Web Toolkit or javadoc.
* Lombok now works on openjdk7 (tested with JDK7m5)! For all the folks on the cutting edge, this should be very good news. [Issue #61](http://code.google.com/p/projectlombok/issues/detail?id=61)
* The installer command-line mode now supports installing/uninstalling all eclipses that can be found via auto-discovery. Just use 'auto' instead of a path.
* Erroneous use of lombok in eclipse (adding it to a project as an annotation processor, which is not how lombok is to be used on eclipse) now generates a useful warning message with helpful information, instead of a confusing error hidden in the logs. [Issue #53](http://code.google.com/p/projectlombok/issues/detail?id=53)
* Solved a regression bug where you would occasionally see errors with the gist 'loader constraint violation: when resolving...', such as when opening the help system, starting the diff editor, or, rarely, opening any java source file. [Issue #68](http://code.google.com/p/projectlombok/issues/detail?id=68)

### v0.9.1

* The installer now works much better on linux, in that it auto-finds eclipse in most locations linux users tend to put their eclipse installs, and it can now handle apt-get installed eclipses, which previously didn't work well at all. There's also a hidden feature where the installer can work as a command-line only tool (`java -jar lombok.jar install eclipse path/to/eclipse`) which also supports `uninstall` of course. You can now also point at `eclipse.ini` in case you have a really odd eclipse install, which should always work.
* For lombok developers, the eclipse launch target now works out-of-the-box on snow leopard. [Issue #66](http://code.google.com/p/projectlombok/issues/detail?id=66)

### v0.9.0

* The lombok class patching system has been completely revamped; the core business of patching class files has been offloaded in an independent project called 'lombok.patcher', which is now used to patch lombok into eclipse.
* Many behind-the-scenes changes to improve lombok's stability and flexibility on eclipse.
* Changes to the lombok core API which aren't backwards compatible with lombok series v0.8 but which were necessary to make writing third party processors for lombok a lot easier.
* Minor version number bumped due to the above 3 issues.
* Eclipse's "rename" refactor script, invoked by pressing CMD/CTRL+SHIFT+R, now works on `@Data` annotated classes.
* The windows installer would fail on boot if you have unformatted drives. [Issue #65](http://code.google.com/p/projectlombok/issues/detail?id=65)
* The static constructor that `@Data` can make was being generated as package private when compiling with javac. [Issue #63](http://code.google.com/p/projectlombok/issues/detail?id=63)

### v0.8.5

* There's now an `AccessLevel.NONE` that you can use for your `@Getter` and `@Setter` annotations to suppress generating setters and getters when you're using the `@Data` annotation. Address [Issue #37](http://code.google.com/p/projectlombok/issues/detail?id=37)
* Both `@EqualsAndHashCode` and `@ToString` now support explicitly specifying the fields to use, via the new 'of' parameter. Fields that begin with a '$' are now also excluded by default from equals, hashCode, and toString generation, unless of course you explicitly mention them in the 'of' parameter. Addresses [Issue #32](http://code.google.com/p/projectlombok/issues/detail?id=32)
* There's a commonly used `@NotNull` annotation, from javax.validation (and in earlier versions of hibernate, which is the origin of javax.validation) which does not quite mean what we want it to mean: It is not legal on parameters, and it is checked at runtime after an explicit request for validation. As a workaround, we've removed checking for any annotation named `NotNull` from the nonnull support of lombok's generated Getters, Setters, and constructors. [Issue #43](http://code.google.com/p/projectlombok/issues/detail?id=43)
* Fixed yet another issue with `@SneakyThrows`. This was reported fixed in v0.8.4. but it still didn't work quite as it should. Still falls under the bailiwick of
[Issue #30](http://code.google.com/p/projectlombok/issues/detail?id=30)

### v0.8.4

* Fixed many issues with `@SneakyThrows` - in previous versions, using it would sometimes confuse the syntax colouring, and various constructs in the annotated method would cause outright eclipse errors, such as beginning the method with a try block. This also fixes [Issue #30](http://code.google.com/p/projectlombok/issues/detail?id=30)
* Fixed the David Lynch bug - in eclipse, classes with lombok features used in them would sometimes appear invisible from other source files. It's described in more detail on [Issue #41](http://code.google.com/p/projectlombok/issues/detail?id=41). If you suffered from it, you'll know what this is about.
* Fixed the problem where eclipse's help system did not start up on lombokized eclipses. [Issue #26](http://code.google.com/p/projectlombok/issues/detail?id=26)
* All generated methods now make their parameters (if they have any) final. This should help avoid problems with the 'make all parameters final' save action in eclipse. [Issue #40](http://code.google.com/p/projectlombok/issues/detail?id=40)
* Okay, this time _really_ added support for @NonNull and @NotNull annotations. It was reported for v0.8.3 but it wasn't actually in that release. @Nullable annotations are now also copied over to the getter's return type and the setter and constructor's parameters (but, obviously, no check is added). Any @NonNull annotated non-final fields that are not initialized are now also added to the generated constructor by @Data in order to ensure via an explicit null check that they contain a legal value.
* @ToString (and hence, @Data) now default to includeFieldNames=true. [Issue #35](http://code.google.com/p/projectlombok/issues/detail?id=35)

### v0.8.3

* @EqualsAndHashCode (and, indirectly, @Data) generate a warning when overriding a class other than java.lang.Object but not setting EqualsAndHashCode's callSuper to true. There are, however, legitimate reasons to do this, so this warning is now no longer generated if you explicitly set callSuper to false. The warning text now also refers to this action if not calling super is intentional.
* If your fields have @NonNull or @NotNull annotations, then generated setters are generated with a null check, and the
annotation is copied to the setter's parameter, and the getter's method.
* An annoying bug that usually showed up if you had package-info.java files has been fixed. It would cause a `NullPointerException` at lombok.javac.apt.Processor.toUnit(Processor.java:143)

### v0.8.2

* @EqualsAndHashCode and @ToString created; these are subsets of what @Data does (namely: generate toString(), and generate equals() and hashCode() implementations). @Data will still generate these methods, but you can now generate them separately if you wish. As part of this split off, you can now specify for toString generation to include the field names in the produced toString method, and for all 3 methods: You can choose to involve the implementation of the superclass, and you can choose to exclude certain fields. [Issue #8](http://code.google.com/p/projectlombok/issues/detail?id=8)
* when compiling with javac: warnings on specific entries of an annotation parameter (such as non-existent fields in a @EqualsAndHashCode exclude parameter) now show up on the problematic parameter and not on the entire annotation. [Issue #11](http://code.google.com/p/projectlombok/issues/detail?id=11)

### v0.8.1

* Changelog tracking from this version on.
* Using eclipse's 'find callers' on a @Data annotation will now find callers of the static constructor if you generated it. If not, it still finds callers to hashCode() as before (it's not possible to make eclipse find callers to the normal constructor, though you can just use 'find callers' on the class name, which works fine). [Issue #5](http://code.google.com/p/projectlombok/issues/detail?id=5)
* If your field is called 'hasFoo' and its a boolean, and you use @Getter or @Data to generate a getter for it, that getter will now be called 'hasFoo' and not 'isHasFoo' as before. This rule holds for any field prefixed with 'has', 'is', or 'get', AND the character following the prefix is not lowercase (so that 'hashCodeGenerated' is not erroneously identified as already having a prefix!). Similar logic has been added to not generate a getter at all for a field named 'foo' or 'hasFoo' if there is already a method named 'isFoo'. [Issue #4](http://code.google.com/p/projectlombok/issues/detail?id=4)
* Starting the lombok installer on mac os X using soylatte instead of apple's JVM now correctly detects being on a mac, and using mac-specific code for finding and installing eclipses. [Issue #7](http://code.google.com/p/projectlombok/issues/detail?id=7)
* For non-mac, non-windows installations, the jar file in the `-javaagent` parameter is now written as an absolute path in `eclipse.ini` instead of a relative one. For some reason, on at least 1 linux installation, an absolute path is required to make javaagent work. This 'fix' has the unfortunate side-effect of making it impossible to move your eclipse installation around without breaking the pointer to the lombok java agent, so this change has only been introduced for non-windows, non-mac. Thanks to WouterS for spotting this one and helping us out with some research on fixing it. [Issue #6](http://code.google.com/p/projectlombok/issues/detail?id=6)

### v0.8

* Initial release before announcements
* (note: There are a few different editions of lombok out there, all tagged with v0.8.)