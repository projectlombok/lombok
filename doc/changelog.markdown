Lombok Changelog
----------------

### v1.16.1 "Edgy Guinea Pig"
* FEATURE: The config key `lombok.extern.findbugs.addSuppressFBWarnings` can now be used to add findbugs suppress warnings annotations to all code lombok generates. This addresses feature request [Issue #702](https://code.google.com/p/projectlombok/issues/detail?id=702).
* FEATURE: New lombok annotation: `@UtilityClass`, for making utility classes (not instantiable, contains only static 'function' methods). See the [feature documentation](http://projectlombok.org/features/experimental/UtilityClass.html) for more information.
* BUGFIX: The ant `delombok` task was broken starting with v1.16.0. Note that the task def class has been changed; taskdef `lombok.delombok.ant.Tasks$Delombok` instead of the old `lombok.delombok.ant.DelombokTask`. [Issue #775](https://code.google.com/p/projectlombok/issues/detail?id=775).
* BUGFIX: `val` in javac would occasionally fail if used inside inner classes. This is (probably) fixed. [Issue #694](https://code.google.com/p/projectlombok/issues/detail?id=694).
* BUGFIX: Starting with v1.16.0, lombok would fail to execute as an executable jar if it was in a path was spaced in it. [Issue #777](https://code.google.com/p/projectlombok/issues/detail?id=777).
* BUGFIX: v1.16.0 did not work in old eclipse versions (such as eclipse indigo).

### v1.16.0 "Candid Duck" (January 26th, 2015)
* BUGFIX: `@ExtensionMethod` was broken in Eclipse using java 8. [Issue #742](https://code.google.com/p/projectlombok/issues/detail?id=742), [Issue #747](https://code.google.com/p/projectlombok/issues/detail?id=747)
* BUGFIX: delombok: Using exotic characters in your source files would overzealously backslash-u escape them. Now, all characters are printed unescaped, assuming your chosen encoding can support them. Otherwise, they are escaped. [Issue #759](https://code.google.com/p/projectlombok/issues/detail?id=759)
* PROMOTION: `@Builder` has graduated from experimental to the main package with a few changes (addition of `@Singular`, removal of the `fluent` and `chain` options). The old one still exists and has been deprecated.
* FEATURE: `@Builder` now supports adding the `@Singular` annotation to any field/parameter that represents a collection, which results in a method in the generated builder that takes in one element of that collection and adds it. Lombok takes care of generating the appropriate code to produce a compacted immutable version of the appropriate type. In this version, java.util collections and guava's ImmutableCollections are supported. See the [feature documentation](http://projectlombok.org/features/Builder.html) for more information.
* FEATURE: Added a launcher to the lombok boot process which removes the need for `-Xbootclasspath` to be in your `eclipse.ini` file, and removes all non-public API and third party dependencies (such as ASM) from the lombok jar, thus removing them from your IDE's auto complete offerings in any project that uses lombok. For those debugging lombok, the launcher enables hot code replace which makes debugging a lot easier, as previously one was required to shut down the IDE, rebuild the jar, and relaunch. Add `-Dshadow.override.lombok=/path/to/lombok/bin` to the launch target for hot code replace.

### v1.14.8 (September 15th, 2014)
* PERFORMANCE: The configuration system typically hit the filesystem twice per read configuration key instead of hardly ever. This is a continuation of [Issue #682](https://code.google.com/p/projectlombok/issues/detail?id=682).

### v1.14.6 (September 2nd, 2014)
* BUGFIX: Usage of `val` would break starting with JDK8 release `1.8.0_20`. [Issue #731](https://code.google.com/p/projectlombok/issues/detail?id=731)
* BUGFIX: Depending on your eclipse project setup, releases v1.14.0 through v1.14.4 could noticably slow down your eclipse. [Issue #682](https://code.google.com/p/projectlombok/issues/detail?id=682).


### v1.14.4 (July 1st, 2014)
* BUGFIX: GWT produces errors in handlers on line 1 in any source files that use lombok; this has been fixed. [Issue #699](https://code.google.com/p/projectlombok/issues/detail?id=699)
* BUGFIX-IN-PROGRESS: Many pathfinder issues in eclipse (see the bugfix in progress in v1.14.2) have now been fixed. [Issue #682](https://code.google.com/p/projectlombok/issues/detail?id=682)

### v1.14.2  (June 10th, 2014)
* BUGFIX: syntax highlighting in eclipse will become weird and auto-complete may stop working amongst other eclipse features in v1.14.0 (regression from v1.12.6). [Issue #688](https://code.google.com/p/projectlombok/issues/detail?id=688)
* FEATURE: Added `@Tolerate`; put this annotation on any method or constructor and lombok will skip it when considering whether or not to generate a method or constructor. This is useful if the types of the parameters of your method do not clash with what lombok would generate.
* FEATURE: Added config key `lombok.getter.noIsPrefix`, which lets you disable use and generation of `isFoo()`, instead going with `getFoo()`, for {@code boolean} fields.
* BUGFIX: Errors in the eclipse log with `IndexOutOfBound: 2` in `ASTConverter.convertType`. [Issue #686](https://code.google.com/p/projectlombok/issues/detail?id=686)
* BUGFIX-IN-PROGRESS: As yet unknown conditions in eclipse result in lots of `IllegalArgumentException` in the log with message "Path must include project and resource name". Also, 'invalid URL' or 'URI not absolute' errors can occur when using exotic file system abstractions such as Jazz. These bugs haven't been fixed, but instead of catastrophic failure, warning logs will be emitted instead. [Issue #682](https://code.google.com/p/projectlombok/issues/detail?id=682)
* BUGFIX: mvn builds fail with a 'URI not absolute' exception. [Issue #683](https://code.google.com/p/projectlombok/issues/detail?id=683)

### v1.14.0 "Branching Cobra" (May 27th, 2014)
* FEATURE: You can now configure aspects of lombok project wide (or even workspace wide, or just for a single package) via the [configuration system](http://projectlombok.org/features/configuration.html). You can configure many things; run `java -jar lombok.jar config -gv` for the complete list.
* DEPRECATION: `@Delegate` has been moved to `lombok.experimental.Delegate`, and corner cases such as recursive delegation (delegating a type that itself has fields or methods annotated with `@Delegate`) are now error conditions. See the [feature documentation](http://projectlombok.org/features/experimental/Delegate.html) for more information.
* FEATURE: It is now possible to put annotations, such as `@Nullable`, on the one parameter of generated `equals()` methods by specifying the `onParam=` option on `@EqualsAndHashCode`, similar to how that feature already exists for `@Setter`. [Issue #674](https://code.google.com/p/projectlombok/issues/detail?id=674)
* CHANGE: suppressConstructorProperties should now be configured via lombok configuration. [Issue #659](https://code.google.com/p/projectlombok/issues/detail?id=659)
* CHANGE: The `canEqual` method generated by `@EqualsAndHashCode`, `@Value` and `@Data` is now `protected` instead of `public`.  [Issue #660](https://code.google.com/p/projectlombok/issues/detail?id=660)
* BUGFIX: Major work on improving support for JDK8, both for javac and eclipse.
* BUGFIX: Deadlocks would occasionally occur in eclipse when using lazy getters [Issue #590](https://code.google.com/p/projectlombok/issues/detail?id=590)
* BUGFIX: Usage of `@SneakyThrows` with a javac from JDK8 with `-target 1.8` would result in a post compiler error. [Issue #655](https://code.google.com/p/projectlombok/issues/detail?id=655)
* BUGFIX: Switching workspace on some versions of eclipse resulted in a 'duplicate field' error. [Issue #666](https://code.google.com/p/projectlombok/issues/detail?id=666)

### v1.12.6 (March 6th, 2014)
* BUGFIX: Deadlocks would occasionally occur in eclipse during project builds, especially if using the gradle plugin. [Issue #645](https://code.google.com/p/projectlombok/issues/detail?id=645)
* PLATFORM: Added support for Eclipse Luna. [Issue #609](https://code.google.com/p/projectlombok/issues/detail?id=609)
* PLATFORM: Initial JDK8 support for eclipse's alpha support in kepler. [Issue #597](https://code.google.com/p/projectlombok/issues/detail?id=597)
* FEATURE: The various `@Log` annotations now support the `topic` parameter, which sets the logger's name. The default remains the fully qualified type name of the class itself. [Issue #632](https://code.google.com/p/projectlombok/issues/detail?id=632).
* BUGFIX: Using lombok with IntelliJ and the IBM JDK would result in NPEs during initialization. [Issue #648](https://code.google.com/p/projectlombok/issues/detail?id=648), [IntelliJ plugin issue #74](https://code.google.com/p/lombok-intellij-plugin/issues/detail?id=74).
* BUGFIX: Eclipse quickfix _Surround with try/catch block_ didn't work inside `@SneakyThrows` annotated methods [Issue #438](https://code.google.com/p/projectlombok/issues/detail?id=438).
* BUGFIX: Eclipse refactoring _Extract Local Variable_ didn't work inside `@SneakyThrows` annotated methods [Issue #633](https://code.google.com/p/projectlombok/issues/detail?id=633).
* BUGFIX: {Netbeans} @SneakyThrows would lead to unused import and break refactorings [Issue #471](https://code.google.com/p/projectlombok/issues/detail?id=471).
* BUGFIX: Eclipse Organize Imports would generate error: AST must not be null [Issue #631](https://code.google.com/p/projectlombok/issues/detail?id=631).
* BUGFIX: Copying javadoc to getters / setters / withers would copy non-relevant sections too. [Issue #585](https://code.google.com/p/projectlombok/issues/detail?id=585).
* ENHANCEMENT: Lombok used to ship with [JNA](http://en.wikipedia.org/wiki/Java_Native_Access). It added over 800k to the size of lombok.jar and could mess with usage of JNA in your local environment, especially in eclipse. [Issue #647](https://code.google.com/p/projectlombok/issues/detail?id=647)
* DETAIL: {Delombok} Inside enum bodies the delombok formatter didn't respect the emptyLines directive [Issue #629](https://code.google.com/p/projectlombok/issues/detail?id=629).
* DETAIL: Use smaller primes (<127) for generating hashcodes [Issue #625](https://code.google.com/p/projectlombok/issues/detail?id=625)

### v1.12.4 (January 15th, 2014)
* BUGFIX: v1.12.2's delombok turns all operator+assignments into just assignment. Fixed. [Issue #598](https://code.google.com/p/projectlombok/issues/detail?id=598)
* BUGFIX: {Netbeans} v1.12.2 doesn't well with netbeans. [Issue #591](https://code.google.com/p/projectlombok/issues/detail?id=591)
* ENHANCEMENT: Delombok now supports varied options for how it formats the resulting source files. This includes scanning the source for things like the preferred indent. Use option `--format-help` for more information. [Issue #608](http://code.google.com/p/projectlombok/issues/detail?id=608)
* DETAIL: The primes lombok generates for use in generated hashCode() methods used to be direct copies from Effective Java. It turns out these particular primes are used so much, they tend to be a bit more collision-prone, so we switched them. Now, '277' is used instead of '31'. The primes for booleans have also been changed. [Issue #625](https://code.google.com/p/projectlombok/issues/detail?id=625)

### v1.12.2 (October 10th, 2013)
* PLATFORM: Initial JDK8 support, without affecting existing support for JDK6 and 7. [Issue #451](https://code.google.com/p/projectlombok/issues/detail?id=451). While lombok will now work on JDK8 / javac8, and netbeans 7.4 and up, lombok does not (yet) support new language features introduced with java8, such as lambda expressions. Support for these features will be added in a future version.
* PLATFORM: Running javac on IBM J9 VM would cause NullPointerExceptions when compiling with lombok. These issues should be fixed. [Issue #554](https://code.google.com/p/projectlombok/issues/detail?id=554).
* CHANGE: [JDK8-related] The canonical way to write onMethod / onParameter / onConstructor annotation now uses a double underscore instead of a single underscore, so, now, the proper way to use this feature is `@RequiredArgsConstructor(onConstructor=@__(@Inject))`. The old way (single underscore) still works, but generates warnings on javac 8.
* BUGFIX: Using `@NonNull` on an abstract method used to cause exceptions during compilation. [Issue #559](https://code.google.com/p/projectlombok/issues/detail?id=559).
* BUGFIX: Using `@NonNull` on methods that also have `@SneakyThrows` or `@Synchronized` caused arbitrary behaviour. [Issue #588](https://code.google.com/p/projectlombok/issues/detail?id=588).
* GERMANY: Major version bumped from 0 to 1, because allegedly this is important. Rest assured, this change is nevertheless backwards compatible.

### v0.12.0 "Angry Butterfly" (July 16th, 2013)
* FEATURE: javadoc on fields will now be copied to generated getters / setters / withers. There are ways to specify separate javadoc for the field, the setter, and the getter, and `@param` and `@return` are handled appropriately. Addresses feature request [Issue #59](https://code.google.com/p/projectlombok/issues/detail?id=59). [@Getter and @Setter documentation](http://projectlombok.org/features/GetterSetter.html). [@Wither documentation](http://projectlombok.org/features/experimental/Wither.html).
* CHANGE: The desugaring of @Getter(lazy=true) is now less object creation intensive. Documentation has been updated to reflect what the new desugaring looks like. [@Getter(lazy=true) documentation](http://projectlombok.org/features/GetterLazy.html).
* PROMOTION: `@Value` has been promoted from experimental to the main package with no changes. The 'old' experimental one is still around but is deprecated, and is an alias for the new main package one. [@Value documentation](http://projectlombok.org/features/Value.html).
* FEATURE: {Experimental} `@Builder` support. One of our earliest feature request issues, [Issue #16](https://code.google.com/p/projectlombok/issues/detail?id=16), has finally been addressed. [@Builder documentation](http://projectlombok.org/features/experimental/Builder.html).
* FEATURE: `@NonNull` on a method or constructor parameter now generates a null-check statement at the start of your method. This nullcheck will throw a `NullPointerException` with the name of the parameter as the message. [Issue #514](https://code.google.com/p/projectlombok/issues/detail?id=514)
* BUGFIX: Usage of `Lombok.sneakyThrow()` or `@SneakyThrows` would sometimes result in invalid classes (classes which fail with `VerifyError`). [Issue #470](https://code.google.com/p/projectlombok/issues/detail?id=470)
* BUGFIX: Using `val` in try-with-resources did not work for javac. [Issue #520](https://code.google.com/p/projectlombok/issues/detail?id=520)
* BUGFIX: When using `@Data`, warnings are not generated if certain aspects are not generated because you wrote explicit versions of them. However, this gets confusing with `equals` / `hashCode` / `canEqual`, as nothing is generated if any one of those methods is present. Now, if one of `equals` or `hashCode` is present but not the other one (or `canEqual` is present but `equals` and/or `hashCode` is missing), a warning is emitted to explain that lombok will not generate any of the equals / hashCode methods, and that you should either write them all yourself or remove them all. [Issue #513](https://code.google.com/p/projectlombok/issues/detail?id=513)
* BUGFIX: Possibly fixed a race condition in patcher [Issue #531](https://code.google.com/p/projectlombok/issues/detail?id=531).

### v0.11.8 (April 23rd, 2013)
* FEATURE: Major performance improvements in eclipse by profiling the project clean process.
* CHANGE: {Experimental} The experimental `@Value` feature no longer implies the also experimental `@Wither`. If you like your `@Value` classes to make withers, add `@Wither` to the class right next to `@Value`.
* FEATURE: {Experimental} Reintroduced `onMethod`, `onConstructor` and `onParam` to `@Getter`, `@Setter`, `@Wither`, and `@XArgsConstructor`. These parameters allow you to add annotations to the methods/constructors that lombok will generate. This is a workaround feature: The stability of the feature on future versions of javac is not guaranteed, and if a better way to implement this feature is found, this feature's current incarnation will be removed without a reasonable period of deprecation. [Documentation on the onX feature](http://projectlombok.org/features/experimental/onX.html)
* FEATURE: Added support for Log4j v2.0 via `@Log4j2` [Issue #432](http://code.google.com/p/projectlombok/issues/detail?id=432)
* ENHANCEMENT: The Lombok installer can now find and install lombok into [JBoss Developer Studio](http://www.redhat.com/products/jbossenterprisemiddleware/developer-studio/). The installer will now also look for eclipse and eclipse variants in your home directory. [Issue #434](http://code.google.com/p/projectlombok/issues/detail?id=432)
* BUGFIX: `@ExtensionMethods` no longer causes `VerifyError` exceptions when running eclipse-compiled code if extension methods are called on expressions which are method calls whose return type is a type variable. For example, `someList.get(i).extensionMethod()` would fail that way. [Issue #436](http://code.google.com/p/projectlombok/issues/detail?id=436)
* BUGFIX: java 7's try-with-resources statement did not delombok correctly. [Issue #459](http://code.google.com/p/projectlombok/issues/detail?id=459)

### v0.11.6 (October 30th, 2012)
* FEATURE: Lombok can be disabled entirely for any given compile run by using JVM switch `-Dlombok.disable`. This might be useful for code style checkers and such.
* FEATURE: Added support for Slf4j extended logger [Issue #421](http://code.google.com/p/projectlombok/issues/detail?id=421)
* BUGFIX: {Delombok} Running delombok has been causing VerifyError errors when used with javac 1.7 since 0.11.0. [Issue #422](http://code.google.com/p/projectlombok/issues/detail?id=422)
* BUGFIX: A conflict between lombok and certain eclipse plugins would result in NullPointerExceptions in the log when using `@Delegate`.
* BUGFIX: `NullPointerException in lombok.javac.handlers.JavacHandlerUtil.upToTypeNode(JavacHandlerUtil.java:978)` when compiling with `@ExtensionMethod` in javac and generated constructors are involved. [Issue #423](http://code.google.com/p/projectlombok/issues/detail?id=423)
* BUGFIX: `@Deprecated` on a field that gets a generated setter in eclipse would result in `IllegalArgumentException`, which you wouldn't see unless you have the error log open. If you have save actions defined, you'd get a popup box with the exception. Now fixed. [Issue #408](http://code.google.com/p/projectlombok/issues/detail?id=408)

### v0.11.4 (August 13th, 2012)
* FEATURE: {Experimental} `@Value`, `@Wither` and `@FieldDefaults` are now available. These are a lot like `@Data` but geared towards immutable classes. [Documentation on @Value](http://projectlombok.org/features/experimental/Value.html), [Documentation on @Wither](http://projectlombok.org/features/experimental/Wither.html) and [Documentation on @FieldDefaults](http://projectlombok.org/features/experimental/FieldDefaults.html).
* BUGFIX: Eclipse would throw an OOME if using `@ExtensionMethod`. [Issue #390](http://code.google.com/p/projectlombok/issues/detail?id=390)
* BUGFIX: {Netbeans} `@Cleanup` and `@Synchronized` cause far fewer issues in the netbeans editor. [Issue #393](http://code.google.com/p/projectlombok/issues/detail?id=393)
* BUGFIX: {Installer} Erroneous messages about the installer complaining about needing root access when installing or removing lombok from eclipse installs has been fixed. The installer edge of this problem was actually already fixed in v0.11.2. [Issue #363](http://code.google.com/p/projectlombok/issues/detail?id=363)
* BUGFIX: `@ExtensionMethod` had all sorts of issues in javac. [Issue #399](http://code.google.com/p/projectlombok/issues/detail?id=399)
* BUGFIX: Generating static constructors with javac when you have fields with generics, i.e. `Class<T>`, caused errors. [Issue #396](http://code.google.com/p/projectlombok/issues/detail?id=396)
* BUGFIX: Minor `@ExtensionMethod` issues in eclipse, such as the ability to call extension methods on a `super` reference which is now no longer possible. [Issue #406](http://code.google.com/p/projectlombok/issues/detail?id=406)

### v0.11.2 "Dashing Kakapo" (July 3rd, 2012)
* FEATURE: {Experimental} `@ExtensionMethod` is now available to add extensions to
any type in the form of static methods that take as first parameter an object of that type. [Documentation on @ExtensionMethod](http://projectlombok.org/features/experimental/ExtensionMethod.html)
* FEATURE: ONGOING: Fix for using lombok together with gwt-designer.
* ENHANCEMENT: Small performance enhancements in `equals` and `hashCode`. [Issue #366](http://code.google.com/p/projectlombok/issues/detail?id=366)
* BUGFIX: Eclipse would display an error message regarding an invalid super constructor in the wrong location. [Issue #336](http://code.google.com/p/projectlombok/issues/detail?id=336)
* BUGFIX: Eclipse refactor script 'rename method arguments' should work more often with lombok-affected methods.
* BUGFIX: Using `val` in an enhanced for loop did not work if the iterable was a raw type.
* BUGFIX: Using `@Getter(lazy=true)` when the data type is boolean, int, array, or some other type that requires special treatment for hashCode/equals, now works properly with `@Data`, `@EqualsHashCode` and `@ToString`. [Issue #376](http://code.google.com/p/projectlombok/issues/detail?id=376)
* BUGFIX: `SneakyThrows` in constructor should not wrap this/super call in try-block [Issue #381](http://code.google.com/p/projectlombok/issues/detail?id=381)
* BUGFIX: Setting breakpoints on code above the first generated method was not possible. [Issue #377](http://code.google.com/p/projectlombok/issues/detail?id=377)

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
