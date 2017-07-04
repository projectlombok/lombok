Lombok Changelog
----------------

### v1.16.18 (July 3rd, 2017)
* PLATFORM: JDK9 support much improved since v1.16.6; [Issue #985](https://github.com/rzwitserloot/lombok/issues/985)
* BUGFIX: Lombok now works with [Bazel](https://bazel.build/) and [Error Prone](https://error-prone.info/). [Issue #1290](https://github.com/rzwitserloot/lombok/issues/1290)
* FEATURE: Lombok has a new [website](https://projectlombok.org/)! A few very minor changes to the code to be more consistent with it have been added, mostly to the javadoc.

### v1.16.16 "Dancing Elephant" (March 23rd, 2017)
* FEATURE: `@Builder.Default` lets you configure default values for your fields when using `@Builder`. See the [Builder feature page](https://projectlombok.org/features/Builder.html) for more information. [Issue #1201](https://github.com/rzwitserloot/lombok/issues/1201)
* PLATFORM: JDK9 now supported for compilation (delomboking with java9 not yet possible). Note, you'll have to do some command line wrangling. See [Issue #985](https://github.com/rzwitserloot/lombok/issues/985)
* BUGFIX: The `onX` feature (which lets you add annotations to generated methods) did not work if the annotation you added contained named parameters, and you are compiling with JDK8's javac. We can't fix this (it's a bug in javac), but we have provided an alternate, prettier way to do `onX` on javac8+. [Issue #778](https://github.com/rzwitserloot/lombok/issues/778) [onX documentation](https://projectlombok.org/features/experimental/onX.html)
* BUGFIX: `@Data` and `@Value` now respect the configuration for field access when generating equals, hashCode and toString [Issue #1329](https://github.com/rzwitserloot/lombok/issues/1329)
* BUGFIX: `@Builder` now marks generated builder 'setters' as `@Deprecated` if the source field is deprecated. [Issue #1342](https://github.com/rzwitserloot/lombok/issues/1342)
* CHANGE: `@ConstructorProperties` will now also be generated for private and package private constructors. This is useful for Jackson [Issue #1180](https://github.com/rzwitserloot/lombok/issues/1180)

### v1.16.14 (February 10th, 2017)
* FEATURE: Generated classes, methods and fields can now also annotated with `@lombok.Generated` [Issue #1014](https://github.com/rzwitserloot/lombok/issues/1014)
* PLATFORM: Lombok can now be used together with other annotation processors that are looking for lombok-generated methods, but only if lombok is the first annotation processor executed. The most commonly used annotation processor affected by this change is [MapStruct](http://mapstruct.org/); we've worked with the mapstruct team specifically to allow any order. Other annotation processors might follow the framework we've built to make this possible; point the authors of any such processor to us and we'll get it sorted [MapStruct issue #510](https://github.com/mapstruct/mapstruct/issues/510) [Lombok issue #973](https://github.com/rzwitserloot/lombok/issues/973)
* PLATFORM: Eclipse: Refactor script 'rename field' when lombok has also generated getters and/or setters for this field is nicer now [Issue #210](https://github.com/rzwitserloot/lombok/issues/210)
* BUGFIX: Something you never encountered. [Issue #1274](https://github.com/rzwitserloot/lombok/issues/1274)

### v1.16.12 (December 5th, 2016)
* FEATURE: `var` is the mutable sister of `val`. For now experimental, and opt-in using `ALLOW` in the flagUsage configuration key. Thanks for the contribution, Bulgakov Alexander. 
* CHANGE: `@Value` and `@FieldDefaults` no longer touch static fields [Issue #1254](https://github.com/rzwitserloot/lombok/issues/1254)
* BUGFIX: `val` in lambda expressions now work as expected [Issue #911](https://github.com/rzwitserloot/lombok/issues/911)
* BUGFIX: `Getter(lazy=true)` now emits an error message when used on a transient field [Issue #1236](https://github.com/rzwitserloot/lombok/issues/1236)
* BUGFIX: Annotation Processors that use ecj internally (dagger) no longer give linkage errors [Issue #1218](https://github.com/rzwitserloot/lombok/issues/1218)
* PLATFORM: Red Hat JBoss Developer Studio is now correctly identified by the installer [Issue #1164](https://github.com/rzwitserloot/lombok/issues/1164)
* BUGFIX: delombok: for-loops with initializers that are not local variables would be generated incorrectly [Issue #1076](https://github.com/rzwitserloot/lombok/issues/1076)

### v1.16.10 (July 15th, 2016)
* FEATURE: Added support for JBoss logger [Issue #1103](https://github.com/rzwitserloot/lombok/issues/1103)
* ENHANCEMENT: Running `javac -Xlint:all` would generate a warning about unclaimed annotations [Issue #1117](https://github.com/rzwitserloot/lombok/issues/1117)
* BUGFIX: Eclipse Mars would sometimes throw a NullPointerException when using `@Delegate` [Issue #913](https://github.com/rzwitserloot/lombok/issues/913)
* ENHANCEMENT: Add support for older maven-compiler-plugin [Issue #1138](https://github.com/rzwitserloot/lombok/issues/1138)

### v1.16.8 (March 7th, 2016)
* PLATFORM: Starting jdk9 support: No more error message regarding `pid`
* FEATURE: `@Builder` updates: It now generates `clearFieldName()` methods if `@Singular` is used. [Issue #967](https://github.com/rzwitserloot/lombok/issues/967).
* FEATURE: `@Builder` updates: The annotation can now be put on instance methods. [Issue #63](https://github.com/rzwitserloot/lombok/issues/63).
* FEATURE: `@Builder` updates: `@Singular` now supports guava's ImmutableTable [Issue #937](https://github.com/rzwitserloot/lombok/issues/937).
* FEATURE: A `lombok.config` key can now be used to make your fields `final` and/or `private`... __everywhere__. We'll be monitoring the performance impact of this for a while. We'll touch every source file if you turn these on, and even if you don't, we have to call into the lombok config system for every file.
* FEATURE: A `lombok.config` key can now be used to set the default behaviour of `@EqualsAndHashCode` when generating methods for a class that extends something in regards to calling the superclass implementations of `equals` and `hashCode` or not. [Issue #965](https://github.com/rzwitserloot/lombok/issues/965).
* FEATURE: Putting `@Wither` on abstract classes now generates something slightly more useful: An abstract wither method. [Issue #945](https://github.com/rzwitserloot/lombok/issues/945).
* BUGFIX: `@Helper` used to only be be legal in pretty specific places; now it works just about everywhere.
* BUGFIX: lambdas with 1 argument that has an explicit type did not pretty print correctly. [Issue #972](https://github.com/rzwitserloot/lombok/issues/972).
* BUGFIX: When using delombok, a source file with only `@NonNull` annotations on parameters as lombok feature would not get properly delomboked.  [Issue #950](https://github.com/rzwitserloot/lombok/issues/950).
* BUGFIX: `@Delegate` in javac would generate arrays instead of varargs parameters. [Issue #932](https://github.com/rzwitserloot/lombok/issues/932).
* BUGFIX: `@Value` and `@FieldDefaults` no longer make uninitialized static fields final. [Issue #928](https://github.com/rzwitserloot/lombok/issues/928).
* ENHANCEMENT: `@Builder.ObtainVia` now has `@Retention(SOURCE)` [Issue #986](https://github.com/rzwitserloot/lombok/issues/986).
* ENHANCEMENT: Putting `@NonNull` on a parameter of an abstract method no longer generates a warning, to allow you to use this annotation to document intended behaviour [Issue #807](https://github.com/rzwitserloot/lombok/issues/807).

### v1.16.6 (August 18th, 2015)
* FEATURE: `@Helper` can be placed on method-local inner classes to make all methods in the class accessible to the rest of the method. [Full documentation](https://projectlombok.org/features/experimental/Helper.html).
* FEATURE: `@Builder(toBuilder = true)` is now available. It produces an instance method that creates a new builder, initialized with all the values of that instance. For more, read the [Feature page on Builder](https://projectlombok.org/features/Builder.html).
* FEATURE: the `hashCode()` method generated by lombok via `@EqualsAndHashCode`, `@Data`, and `@Value` is now smarter about nulls; they are treated as if they hash to a magic prime instead of 0, which reduces hash collisions.
* FEATURE: `@NoArgsConstructor(force = true)` can be used to create no args constructors even if final fields are present.
* BUGFIX: Parameterized static methods with `@Builder` would produce compiler errors in javac. [Issue #828](https://github.com/rzwitserloot/lombok/issues/828).
* BUGFIX: The new annotations-on-types feature introduced in JDK8 did not delombok correctly. [Issue #855](https://github.com/rzwitserloot/lombok/issues/855).
* PERFORMANCE: the config system caused significant slowdowns in eclipse if the filesystem is very slow (network file system) or has a slow authentication system.
* BUGFIX: Various quickfixes in Eclipse Mars were broken. [Issue #861](https://github.com/rzwitserloot/lombok/issues/861) [Issue #866](https://github.com/rzwitserloot/lombok/issues/866) [Issue #870](https://github.com/rzwitserloot/lombok/issues/870).

### v1.16.4 (April 14th, 2015)
* BUGFIX: Lombok now works with Eclipse Mars.
* BUGFIX: @UtilityClass could result in uninitialized static variables if compiled with ecj/eclipse. [Issue #839](https://github.com/rzwitserloot/lombok/issues/839)
* BUGFIX: This version of lombok has a refactored launcher (the one introduced in v1.16.0), which fixes various bugs related to errors in eclipse concerning loading classes, failure to find lombok classes, and errors on ClassLoaders. Probably impacts issues [Issue #767](https://github.com/rzwitserloot/lombok/issues/767) and [Issue #826](https://github.com/rzwitserloot/lombok/issues/826).

### v1.16.2 (February 10th, 2015)
* FEATURE: The config key `lombok.extern.findbugs.addSuppressFBWarnings` can now be used to add findbugs suppress warnings annotations to all code lombok generates. This addresses feature request [Issue #737](https://github.com/rzwitserloot/lombok/issues/737).
* FEATURE: New lombok annotation: `@UtilityClass`, for making utility classes (not instantiable, contains only static 'function' methods). See the [feature documentation](https://projectlombok.org/features/experimental/UtilityClass.html) for more information.
* BUGFIX: The ant `delombok` task was broken starting with v1.16.0. Note that the task def class has been changed; taskdef `lombok.delombok.ant.Tasks$Delombok` instead of the old `lombok.delombok.ant.DelombokTask`. [Issue #810](https://github.com/rzwitserloot/lombok/issues/810).
* BUGFIX: `val` in javac would occasionally fail if used inside inner classes. This is (probably) fixed. [Issue #729](https://github.com/rzwitserloot/lombok/issues/729) and [Issue #616](https://github.com/rzwitserloot/lombok/issues/616).
* BUGFIX: Starting with v1.16.0, lombok would fail to execute as an executable jar if it was in a path with spaces in it. [Issue #812](https://github.com/rzwitserloot/lombok/issues/812).
* BUGFIX: v1.16.0 did not work in old eclipse versions (such as eclipse indigo). [Issue #818](https://github.com/rzwitserloot/lombok/issues/818).

### v1.16.0 "Candid Duck" (January 26th, 2015)
* BUGFIX: `@ExtensionMethod` was broken in Eclipse using java 8. [Issue #777](https://github.com/rzwitserloot/lombok/issues/777), [Issue #782](https://github.com/rzwitserloot/lombok/issues/782)
* BUGFIX: delombok: Using exotic characters in your source files would overzealously backslash-u escape them. Now, all characters are printed unescaped, assuming your chosen encoding can support them. Otherwise, they are escaped. [Issue #794](https://github.com/rzwitserloot/lombok/issues/794)
* PROMOTION: `@Builder` has graduated from experimental to the main package with a few changes (addition of `@Singular`, removal of the `fluent` and `chain` options). The old one still exists and has been deprecated.
* FEATURE: `@Builder` now supports adding the `@Singular` annotation to any field/parameter that represents a collection, which results in a method in the generated builder that takes in one element of that collection and adds it. Lombok takes care of generating the appropriate code to produce a compacted immutable version of the appropriate type. In this version, java.util collections and guava's ImmutableCollections are supported. See the [feature documentation](https://projectlombok.org/features/Builder.html) for more information.
* FEATURE: Added a launcher to the lombok boot process which removes the need for `-Xbootclasspath` to be in your `eclipse.ini` file, and removes all non-public API and third party dependencies (such as ASM) from the lombok jar, thus removing them from your IDE's auto complete offerings in any project that uses lombok. For those debugging lombok, the launcher enables hot code replace which makes debugging a lot easier, as previously one was required to shut down the IDE, rebuild the jar, and relaunch. Add `-Dshadow.override.lombok=/path/to/lombok/bin` to the launch target for hot code replace.

### v1.14.8 (September 15th, 2014)
* PERFORMANCE: The configuration system typically hit the filesystem twice per read configuration key instead of hardly ever. This is a continuation of [Issue #717](https://github.com/rzwitserloot/lombok/issues/717).

### v1.14.6 (September 2nd, 2014)
* BUGFIX: Usage of `val` would break starting with JDK8 release `1.8.0_20`. [Issue #766](https://github.com/rzwitserloot/lombok/issues/766)
* BUGFIX: Depending on your eclipse project setup, releases v1.14.0 through v1.14.4 could noticably slow down your eclipse. [Issue #717](https://github.com/rzwitserloot/lombok/issues/717).


### v1.14.4 (July 1st, 2014)
* BUGFIX: GWT produces errors in handlers on line 1 in any source files that use lombok; this has been fixed. [Issue #734](https://github.com/rzwitserloot/lombok/issues/734)
* BUGFIX-IN-PROGRESS: Many pathfinder issues in eclipse (see the bugfix in progress in v1.14.2) have now been fixed. [Issue #717](https://github.com/rzwitserloot/lombok/issues/717)

### v1.14.2  (June 10th, 2014)
* BUGFIX: syntax highlighting in eclipse will become weird and auto-complete may stop working amongst other eclipse features in v1.14.0 (regression from v1.12.6). [Issue #723](https://github.com/rzwitserloot/lombok/issues/723)
* FEATURE: Added `@Tolerate`; put this annotation on any method or constructor and lombok will skip it when considering whether or not to generate a method or constructor. This is useful if the types of the parameters of your method do not clash with what lombok would generate.
* FEATURE: Added config key `lombok.getter.noIsPrefix`, which lets you disable use and generation of `isFoo()`, instead going with `getFoo()`, for {@code boolean} fields.
* BUGFIX: Errors in the eclipse log with `IndexOutOfBound: 2` in `ASTConverter.convertType`. [Issue #721](https://github.com/rzwitserloot/lombok/issues/721)
* BUGFIX-IN-PROGRESS: As yet unknown conditions in eclipse result in lots of `IllegalArgumentException` in the log with message "Path must include project and resource name". Also, 'invalid URL' or 'URI not absolute' errors can occur when using exotic file system abstractions such as Jazz. These bugs haven't been fixed, but instead of catastrophic failure, warning logs will be emitted instead. [Issue #717](https://github.com/rzwitserloot/lombok/issues/717)
* BUGFIX: mvn builds fail with a 'URI not absolute' exception. [Issue #718](https://github.com/rzwitserloot/lombok/issues/718)

### v1.14.0 "Branching Cobra" (May 27th, 2014)
* FEATURE: You can now configure aspects of lombok project wide (or even workspace wide, or just for a single package) via the [configuration system](https://projectlombok.org/features/configuration.html). You can configure many things; run `java -jar lombok.jar config -gv` for the complete list.
* DEPRECATION: `@Delegate` has been moved to `lombok.experimental.Delegate`, and corner cases such as recursive delegation (delegating a type that itself has fields or methods annotated with `@Delegate`) are now error conditions. See the [feature documentation](https://projectlombok.org/features/experimental/Delegate.html) for more information.
* FEATURE: It is now possible to put annotations, such as `@Nullable`, on the one parameter of generated `equals()` methods by specifying the `onParam=` option on `@EqualsAndHashCode`, similar to how that feature already exists for `@Setter`. [Issue #709](https://github.com/rzwitserloot/lombok/issues/709)
* CHANGE: suppressConstructorProperties should now be configured via lombok configuration. [Issue #694](https://github.com/rzwitserloot/lombok/issues/694)
* CHANGE: The `canEqual` method generated by `@EqualsAndHashCode`, `@Value` and `@Data` is now `protected` instead of `public`.  [Issue #695](https://github.com/rzwitserloot/lombok/issues/695)
* BUGFIX: Major work on improving support for JDK8, both for javac and eclipse.
* BUGFIX: Deadlocks would occasionally occur in eclipse when using lazy getters [Issue #625](https://github.com/rzwitserloot/lombok/issues/625)
* BUGFIX: Usage of `@SneakyThrows` with a javac from JDK8 with `-target 1.8` would result in a post compiler error. [Issue #690](https://github.com/rzwitserloot/lombok/issues/690)
* BUGFIX: Switching workspace on some versions of eclipse resulted in a 'duplicate field' error. [Issue #701](https://github.com/rzwitserloot/lombok/issues/701)

### v1.12.6 (March 6th, 2014)
* BUGFIX: Deadlocks would occasionally occur in eclipse during project builds, especially if using the gradle plugin. [Issue #680](https://github.com/rzwitserloot/lombok/issues/680)
* PLATFORM: Added support for Eclipse Luna. [Issue #644](https://github.com/rzwitserloot/lombok/issues/644)
* PLATFORM: Initial JDK8 support for eclipse's alpha support in kepler. [Issue #632](https://github.com/rzwitserloot/lombok/issues/632)
* FEATURE: The various `@Log` annotations now support the `topic` parameter, which sets the logger's name. The default remains the fully qualified type name of the class itself. [Issue #667](https://github.com/rzwitserloot/lombok/issues/667).
* BUGFIX: Using lombok with IntelliJ and the IBM JDK would result in NPEs during initialization. [Issue #683](https://github.com/rzwitserloot/lombok/issues/683).
* BUGFIX: Eclipse quickfix _Surround with try/catch block_ didn't work inside `@SneakyThrows` annotated methods [Issue #511](https://github.com/rzwitserloot/lombok/issues/511).
* BUGFIX: Eclipse refactoring _Extract Local Variable_ didn't work inside `@SneakyThrows` annotated methods [Issue #668](https://github.com/rzwitserloot/lombok/issues/668).
* BUGFIX: {Netbeans} @SneakyThrows would lead to unused import and break refactorings [Issue #544](https://github.com/rzwitserloot/lombok/issues/544).
* BUGFIX: Eclipse Organize Imports would generate error: AST must not be null [Issue #666](https://github.com/rzwitserloot/lombok/issues/666).
* BUGFIX: Copying javadoc to getters / setters / withers would copy non-relevant sections too. [Issue #620](https://github.com/rzwitserloot/lombok/issues/620).
* ENHANCEMENT: Lombok used to ship with [JNA](http://en.wikipedia.org/wiki/Java_Native_Access). It added over 800k to the size of lombok.jar and could mess with usage of JNA in your local environment, especially in eclipse. [Issue #682](https://github.com/rzwitserloot/lombok/issues/682)
* DETAIL: {Delombok} Inside enum bodies the delombok formatter didn't respect the emptyLines directive [Issue #664](https://github.com/rzwitserloot/lombok/issues/664).
* DETAIL: Use smaller primes (<127) for generating hashcodes [Issue #660](https://github.com/rzwitserloot/lombok/issues/660)

### v1.12.4 (January 15th, 2014)
* BUGFIX: v1.12.2's delombok turns all operator+assignments into just assignment. Fixed. [Issue #633](https://github.com/rzwitserloot/lombok/issues/633)
* BUGFIX: {Netbeans} v1.12.2 doesn't well with netbeans. [Issue #626](https://github.com/rzwitserloot/lombok/issues/626)
* ENHANCEMENT: Delombok now supports varied options for how it formats the resulting source files. This includes scanning the source for things like the preferred indent. Use option `--format-help` for more information. [Issue #643](https://github.com/rzwitserloot/lombok/issues/643)
* DETAIL: The primes lombok generates for use in generated hashCode() methods used to be direct copies from Effective Java. It turns out these particular primes are used so much, they tend to be a bit more collision-prone, so we switched them. Now, '277' is used instead of '31'. The primes for booleans have also been changed. [Issue #660](https://github.com/rzwitserloot/lombok/issues/660)

### v1.12.2 (October 10th, 2013)
* PLATFORM: Initial JDK8 support, without affecting existing support for JDK6 and 7. [Issue #524](https://github.com/rzwitserloot/lombok/issues/524). While lombok will now work on JDK8 / javac8, and netbeans 7.4 and up, lombok does not (yet) support new language features introduced with java8, such as lambda expressions. Support for these features will be added in a future version.
* PLATFORM: Running javac on IBM J9 VM would cause NullPointerExceptions when compiling with lombok. These issues should be fixed. [Issue #589](https://github.com/rzwitserloot/lombok/issues/589).
* CHANGE: [JDK8-related] The canonical way to write onMethod / onParameter / onConstructor annotation now uses a double underscore instead of a single underscore, so, now, the proper way to use this feature is `@RequiredArgsConstructor(onConstructor=@__(@Inject))`. The old way (single underscore) still works, but generates warnings on javac 8.
* BUGFIX: Using `@NonNull` on an abstract method used to cause exceptions during compilation. [Issue #594](https://github.com/rzwitserloot/lombok/issues/594).
* BUGFIX: Using `@NonNull` on methods that also have `@SneakyThrows` or `@Synchronized` caused arbitrary behaviour. [Issue #623](https://github.com/rzwitserloot/lombok/issues/623).
* GERMANY: Major version bumped from 0 to 1, because allegedly this is important. Rest assured, this change is nevertheless backwards compatible.

### v0.12.0 "Angry Butterfly" (July 16th, 2013)
* FEATURE: javadoc on fields will now be copied to generated getters / setters / withers. There are ways to specify separate javadoc for the field, the setter, and the getter, and `@param` and `@return` are handled appropriately. Addresses feature request [Issue #132](https://github.com/rzwitserloot/lombok/issues/132). [@Getter and @Setter documentation](https://projectlombok.org/features/GetterSetter.html). [@Wither documentation](https://projectlombok.org/features/experimental/Wither.html).
* CHANGE: The desugaring of @Getter(lazy=true) is now less object creation intensive. Documentation has been updated to reflect what the new desugaring looks like. [@Getter(lazy=true) documentation](https://projectlombok.org/features/GetterLazy.html).
* PROMOTION: `@Value` has been promoted from experimental to the main package with no changes. The 'old' experimental one is still around but is deprecated, and is an alias for the new main package one. [@Value documentation](https://projectlombok.org/features/Value.html).
* FEATURE: {Experimental} `@Builder` support. One of our earliest feature request issues, [Issue #89](https://github.com/rzwitserloot/lombok/issues/89), has finally been addressed. [@Builder documentation](https://projectlombok.org/features/experimental/Builder.html).
* FEATURE: `@NonNull` on a method or constructor parameter now generates a null-check statement at the start of your method. This nullcheck will throw a `NullPointerException` with the name of the parameter as the message. [Issue #549](https://github.com/rzwitserloot/lombok/issues/549)
* BUGFIX: Usage of `Lombok.sneakyThrow()` or `@SneakyThrows` would sometimes result in invalid classes (classes which fail with `VerifyError`). [Issue #543](https://github.com/rzwitserloot/lombok/issues/543)
* BUGFIX: Using `val` in try-with-resources did not work for javac. [Issue #555](https://github.com/rzwitserloot/lombok/issues/555)
* BUGFIX: When using `@Data`, warnings are not generated if certain aspects are not generated because you wrote explicit versions of them. However, this gets confusing with `equals` / `hashCode` / `canEqual`, as nothing is generated if any one of those methods is present. Now, if one of `equals` or `hashCode` is present but not the other one (or `canEqual` is present but `equals` and/or `hashCode` is missing), a warning is emitted to explain that lombok will not generate any of the equals / hashCode methods, and that you should either write them all yourself or remove them all. [Issue #548](https://github.com/rzwitserloot/lombok/issues/548)
* BUGFIX: Possibly fixed a race condition in patcher [Issue #566](https://github.com/rzwitserloot/lombok/issues/566).

### v0.11.8 (April 23rd, 2013)
* FEATURE: Major performance improvements in eclipse by profiling the project clean process.
* CHANGE: {Experimental} The experimental `@Value` feature no longer implies the also experimental `@Wither`. If you like your `@Value` classes to make withers, add `@Wither` to the class right next to `@Value`.
* FEATURE: {Experimental} Reintroduced `onMethod`, `onConstructor` and `onParam` to `@Getter`, `@Setter`, `@Wither`, and `@XArgsConstructor`. These parameters allow you to add annotations to the methods/constructors that lombok will generate. This is a workaround feature: The stability of the feature on future versions of javac is not guaranteed, and if a better way to implement this feature is found, this feature's current incarnation will be removed without a reasonable period of deprecation. [Documentation on the onX feature](https://projectlombok.org/features/experimental/onX.html)
* FEATURE: Added support for Log4j v2.0 via `@Log4j2` [Issue #505](https://github.com/rzwitserloot/lombok/issues/505)
* ENHANCEMENT: The Lombok installer can now find and install lombok into [JBoss Developer Studio](http://www.redhat.com/products/jbossenterprisemiddleware/developer-studio/). The installer will now also look for eclipse and eclipse variants in your home directory. [Issue #507](https://github.com/rzwitserloot/lombok/issues/507)
* BUGFIX: `@ExtensionMethods` no longer causes `VerifyError` exceptions when running eclipse-compiled code if extension methods are called on expressions which are method calls whose return type is a type variable. For example, `someList.get(i).extensionMethod()` would fail that way. [Issue #509](https://github.com/rzwitserloot/lombok/issues/509)
* BUGFIX: java 7's try-with-resources statement did not delombok correctly. [Issue #532](https://github.com/rzwitserloot/lombok/issues/532)

### v0.11.6 (October 30th, 2012)
* FEATURE: Lombok can be disabled entirely for any given compile run by using JVM switch `-Dlombok.disable`. This might be useful for code style checkers and such.
* FEATURE: Added support for Slf4j extended logger [Issue #494](https://github.com/rzwitserloot/lombok/issues/494)
* BUGFIX: {Delombok} Running delombok has been causing VerifyError errors when used with javac 1.7 since 0.11.0. [Issue #495](https://github.com/rzwitserloot/lombok/issues/495)
* BUGFIX: A conflict between lombok and certain eclipse plugins would result in NullPointerExceptions in the log when using `@Delegate`.
* BUGFIX: `NullPointerException in lombok.&#8203;javac.&#8203;handlers.&#8203;JavacHandlerUtil.&#8203;upToTypeNode&#8203;(JavacHandlerUtil.java:978)` when compiling with `@ExtensionMethod` in javac and generated constructors are involved. [Issue #496](https://github.com/rzwitserloot/lombok/issues/496)
* BUGFIX: `@Deprecated` on a field that gets a generated setter in eclipse would result in `IllegalArgumentException`, which you wouldn't see unless you have the error log open. If you have save actions defined, you'd get a popup box with the exception. Now fixed. [Issue #481](https://github.com/rzwitserloot/lombok/issues/481)

### v0.11.4 (August 13th, 2012)
* FEATURE: {Experimental} `@Value`, `@Wither` and `@FieldDefaults` are now available. These are a lot like `@Data` but geared towards immutable classes. [Documentation on @Value](https://projectlombok.org/features/experimental/Value.html), [Documentation on @Wither](https://projectlombok.org/features/experimental/Wither.html) and [Documentation on @FieldDefaults](https://projectlombok.org/features/experimental/FieldDefaults.html).
* BUGFIX: Eclipse would throw an OOME if using `@ExtensionMethod`. [Issue #463](https://github.com/rzwitserloot/lombok/issues/463)
* BUGFIX: {Netbeans} `@Cleanup` and `@Synchronized` cause far fewer issues in the netbeans editor. [Issue #466](https://github.com/rzwitserloot/lombok/issues/466)
* BUGFIX: {Installer} Erroneous messages about the installer complaining about needing root access when installing or removing lombok from eclipse installs has been fixed. The installer edge of this problem was actually already fixed in v0.11.2. [Issue #436](https://github.com/rzwitserloot/lombok/issues/436)
* BUGFIX: `@ExtensionMethod` had all sorts of issues in javac. [Issue #472](https://github.com/rzwitserloot/lombok/issues/472)
* BUGFIX: Generating static constructors with javac when you have fields with generics, i.e. `Class<T>`, caused errors. [Issue #469](https://github.com/rzwitserloot/lombok/issues/469)
* BUGFIX: Minor `@ExtensionMethod` issues in eclipse, such as the ability to call extension methods on a `super` reference which is now no longer possible. [Issue #479](https://github.com/rzwitserloot/lombok/issues/479)

### v0.11.2 "Dashing Kakapo" (July 3rd, 2012)
* FEATURE: {Experimental} `@ExtensionMethod` is now available to add extensions to
any type in the form of static methods that take as first parameter an object of that type. [Documentation on @ExtensionMethod](https://projectlombok.org/features/experimental/ExtensionMethod.html)
* FEATURE: ONGOING: Fix for using lombok together with gwt-designer.
* ENHANCEMENT: Small performance enhancements in `equals` and `hashCode`. [Issue #439](https://github.com/rzwitserloot/lombok/issues/439)
* BUGFIX: Eclipse would display an error message regarding an invalid super constructor in the wrong location. [Issue #409](https://github.com/rzwitserloot/lombok/issues/409)
* BUGFIX: Eclipse refactor script 'rename method arguments' should work more often with lombok-affected methods.
* BUGFIX: Using `val` in an enhanced for loop did not work if the iterable was a raw type.
* BUGFIX: Using `@Getter(lazy=true)` when the data type is boolean, int, array, or some other type that requires special treatment for hashCode/equals, now works properly with `@Data`, `@EqualsHashCode` and `@ToString`. [Issue #449](https://github.com/rzwitserloot/lombok/issues/449)
* BUGFIX: `SneakyThrows` in constructor should not wrap this/super call in try-block [Issue #454](https://github.com/rzwitserloot/lombok/issues/454)
* BUGFIX: Setting breakpoints on code above the first generated method was not possible. [Issue #450](https://github.com/rzwitserloot/lombok/issues/450)

### v0.11.0 (March 26th, 2012)
* FEATURE: {Experimental} 'fluent' getters and setters (using just `fieldName` as methodname instead of `getFieldName`), setters that return `this` instead of `void`, and support for fields with prefixes is introduced with this lombok version. Also, the number of parameters of any existing methods with the same name that lombok would generate are now taken into account; previously if you had any method named `setX` regardless of how many parameters it has, lombok would avoid generating a `setX` method. Now lombok generates the method if all present `setX` methods have a number of parameters other than 1. [documentation](https://projectlombok.org/features/experimental/Accessors.html).
* FEATURE: The combination of `@Delegate` and `@Getter` or `@Data` will now delegate to the result of a generated getter. [Issue #401](https://github.com/rzwitserloot/lombok/issues/401)
* FEATURE: Developing android apps on eclipse with lombok is now possible by running `java -jar lombok.jar publicApi` and including the generated jar in your project. [Documentation on using lombok for android development](https://projectlombok.org/setup/android.html).
* BUGFIX: In NetBeans the generated default constructor would still be generated even if Lombok also generated constructors. [Issue #399](https://github.com/rzwitserloot/lombok/issues/399)
* BUGFIX: Some classes that contain @SneakyThrows would not compile (throw ClassFormatError). [Issue #412](https://github.com/rzwitserloot/lombok/issues/412)
* BUGFIX: delombok: When `@Delegate` would generate a method with type parameters of the type `T extends package.Class`, a dot would be prepended to the type name. [Issue #414](https://github.com/rzwitserloot/lombok/issues/414)
* BUGFIX: @Getter and @Setter now generate deprecated methods for deprecated fields. Fixes [Issue #415](https://github.com/rzwitserloot/lombok/issues/415)
* BUGFIX: @Delegate would not generate @Deprecated on methods marked deprecated in javadoc. Fixes [Issue #421](https://github.com/rzwitserloot/lombok/issues/421)
* BUGFIX: Using `val` with a type like `Outer<TypeArgs>.Inner` now works. [Issue #416](https://github.com/rzwitserloot/lombok/issues/416)
* BUGFIX: `@Getter(lazy=true)` where the variable type is a primitive and the initializing expression is of a different primitive type that would type coerce implicitly, i.e. ints can be assigned to longs without a cast, didn't work before. [Issue #418](https://github.com/rzwitserloot/lombok/issues/418)
* BUGFIX: `val` is no longer legal inside basic for loops (the old kind, not the foreach kind). These variables should rarely be final, and in practice it wasn't possible to delombok this code properly. [Issue #419](https://github.com/rzwitserloot/lombok/issues/419)
* BUGFIX: PrettyCommentsPrinter now prints default clause of annotation methods. Fixes [Issue #423](https://github.com/rzwitserloot/lombok/issues/423)

### v0.10.8 (January 19th, 2012)
* FEATURE: `@Delegate` can now be used on a no-argument method, which works similarly to adding it to fields. See [documentation](https://projectlombok.org/features/Delegate.html).
* BUGFIX: Eclipse refactoring Extract Interface was broken when using lombok annotation to generate methods. [Issue #159](https://github.com/rzwitserloot/lombok/issues/159)
* BUGFIX: Eclipse action Sort Members was broken when using lombok annotations to generate methods or fields. [Issue #338](https://github.com/rzwitserloot/lombok/issues/338)
* BUGFIX: Eclipse action Refactor/Rename on an inner type was broken when using lombok annotations. [Issue #389](https://github.com/rzwitserloot/lombok/issues/389)
* BUGFIX: 0.10.6 causes ClassNotFoundErrors when using ecj (and thus, play framework, gwt, etc). [Issue #393](https://github.com/rzwitserloot/lombok/issues/393)
* BUGFIX: Eclipse parsing was broken when using lombok annotations with parentheses. [Issue #398](https://github.com/rzwitserloot/lombok/issues/398)
* ENHANCEMENT: Lombok now adds a line to the Eclipse About dialog about itself.

### v0.10.6 (December 19th, 2011)
* PERFORMANCE: Performance issues (memory leaks) when using lombok in netbeans, introduced in 0.10, have been fixed. [Issue #315](https://github.com/rzwitserloot/lombok/issues/315)
* BUGFIX: Eclipse quickfix "Add unimplemented methods" would sometimes insert the new method stubs in strange places, especially if `@Data` was present. [Issue #124](https://github.com/rzwitserloot/lombok/issues/124)
* BUGFIX: Eclipse quickfix "Assign parameter to new field" would insert it outside the class body if `@Data` was present. [Issue #295](https://github.com/rzwitserloot/lombok/issues/295)
* BUGFIX: Renaming a @Data-annotated class in eclipse using Alt+Shift+R no longer mangles the data annotation. [Issue #359](https://github.com/rzwitserloot/lombok/issues/359)
* BUGFIX: Using save action 'Use this qualifier for field accesses, only if necessary' did not work together with `@Data` in certain cases. [Issue #374](https://github.com/rzwitserloot/lombok/issues/374)
* BUGFIX: Organize imports, either run manually or as save action, would throw an exception. [Issue #381](https://github.com/rzwitserloot/lombok/issues/381)
* BUGFIX: Extracted constants would be placed outside the class body when a logging annotation was present. [Issue #388](https://github.com/rzwitserloot/lombok/issues/388)

### v0.10.4 (November 21st, 2011)
* BUGFIX: Using the `log` field from `@Log`, etc, now works in static initializers. [Issue #368](https://github.com/rzwitserloot/lombok/issues/368)
* BUGFIX: Auto-formatting code containing lombok on eclipse, even via an auto-save action, now works. [Issue #163](https://github.com/rzwitserloot/lombok/issues/163)
* BUGFIX: Letting eclipse generate various methods when a lombok annotation is present now works. [Issue #211](https://github.com/rzwitserloot/lombok/issues/211)
* BUGFIX: Renaming a @Data-annotated class in eclipse no longer mangles the data annotation. [Issue #359](https://github.com/rzwitserloot/lombok/issues/359)
* BUGFIX: Eclipse save action *Add final modifier to private fields* no longer adds final keyword to `@Setter` fields. [Issue #336](https://github.com/rzwitserloot/lombok/issues/336)
* BUGFIX: Mixing labels and `lombok.val` would cause NPEs in javac. [Issue #372](https://github.com/rzwitserloot/lombok/issues/372)
* BUGFIX: Writing `lombok.val` out in full (vs. using an import statement) did not work in eclipse. [Issue #373](https://github.com/rzwitserloot/lombok/issues/373)

### v0.10.2 (November 1st, 2011)
* BUGFIX: Delombok will no longer jumble up comments from different files when using -sourcepath option. [Issue #357](https://github.com/rzwitserloot/lombok/issues/357)
* BUGFIX: Turns out treating `@NotNull` as an annotation that indicates lombok should generate nullcheck guards causes all sorts of problems. This has been removed again, and documentation has been updated to reflect this. [Issue #360](https://github.com/rzwitserloot/lombok/issues/360)
* BUGFIX: `@EqualsAndHashCode` or `@Data` did not work on non-static inner classes whose outer class has a type variable. It does now. [Issue #362](https://github.com/rzwitserloot/lombok/issues/362)

### v0.10.1 (October 3rd, 2011)
* BUGFIX: `@Delegate` in eclipse could cause memory leaks in 0.10.0. [Issue #337](https://github.com/rzwitserloot/lombok/issues/337)
* BUGFIX: Annotations on enum values were being deleted by delombok. [Issue #342](https://github.com/rzwitserloot/lombok/issues/342)
* BUGFIX: `@AllArgsConstructor` was erroneously generating a parameter and an assignment for final variables already assigned in their declaration. [Issue #351](https://github.com/rzwitserloot/lombok/issues/351)
* ENHANCEMENT: `@NotNull` is now also recognized as an annotation indicating that lombok should generate nullcheck guards in generated constructors and setters. [Issue #344](https://github.com/rzwitserloot/lombok/issues/344)

### v0.10.0 "Burning Emu" (August 19th, 2011)
* FEATURE: New annotation: @Delegate. This annotation lets lombok generate delegation methods for a given field. [More&hellip;](https://projectlombok.org/features/Delegate.html)
* FEATURE: Added support for 'val'. Val is an immutable variable that infers its type from the right hand side of the initializing expression. [More&hellip;](https://projectlombok.org/features/val.html)
* FEATURE: Added support for several logging frameworks via the `@Log`, `@Slf4j`, etc. annotation. [More&hellip;](https://projectlombok.org/features/Log.html)
* FEATURE: Lombok now supports post-compile transformers. [Issue #217](https://github.com/rzwitserloot/lombok/issues/217)
* FEATURE: Using `@SneakyThrows` no longer requires a runtime dependency on lombok.jar. In fact, any call to `Lombok.sneakyThrows(ex)` is optimized at the bytecode level and no longer requires you to actually have lombok.jar or lombok-runtime.jar on the classpath.
* FEATURE: @*X*ArgsConstructor, @Getter, and @ToString can now be used on enum declarations. Previously, behaviour of these annotations on enums was undefined.
* FEATURE: @Getter/@Setter (and by extension, @Data) in v0.9.3 and earlier would generate getter and setter method names that did not conform to the beanspec, primarily when faced with boolean properties. This has been fixed. In practice this won't affect you unless you have properties named `isFoo` or `hasFoo`. Now the setter generated for this will be called `setFoo` (as the property name is `foo`) and not `setIsFoo`. Also, `hasFoo` is now no longer special; the names would be `isHasFoo` and `setHasFoo`. The java bean spec does not give `has` special meaning.
* FEATURE: `@EqualsAndHashCode` (and by extension, `@Data`) now add a `canEqual` method which improves the sanity of equality amongst a hierarchy of classes. [More&hellip;](https://projectlombok.org/features/EqualsAndHashCode.html)
* FEATURE: `@Getter` now supports a `lazy=true` attribute. [More&hellip;](https://projectlombok.org/features/GetterLazy.html)
* ENHANCEMENT: The installer will now find Eclipse installations when they are located in a subdirectory of a directory containing the word 'eclipse' . [Issue #283](https://github.com/rzwitserloot/lombok/issues/283)
* ENHANCEMENT: Add null check for `@Cleanup` [Issue #227](https://github.com/rzwitserloot/lombok/issues/227)
* BUGFIX: Lombok is now compatible with javac 7.
* BUGFIX: Hard to reproduce `NullPointerException` in Eclipse on the `getTypeBinding` method in the error log has been fixed. [Issue #237](https://github.com/rzwitserloot/lombok/issues/237)
* BUGFIX: `@Setter` and `@Getter` can now be applied to static fields again (was broken in v0.9.3 only). [Issue #209](https://github.com/rzwitserloot/lombok/issues/209)
* BUGFIX: delombok added type parameters to constructors that mirror the type's own type parameters. This resulted in delombok turning any generated constructor that takes at least 1 parameter of type 'T' into something that didn't compile, and to boot, a confusing error message ('T is not compatible with T'). This is now fixed. [Issue #213](https://github.com/rzwitserloot/lombok/issues/213)
* BUGFIX: The Eclipse source generator would place the generated code outside the class [Issue #228](https://github.com/rzwitserloot/lombok/issues/228)
* BUGFIX: When using m2eclipse, occasionally you'd see a ClassNotFoundError on JavacProcessingEnvironment. This has been fixed. [Issue #250](https://github.com/rzwitserloot/lombok/issues/250)
* BUGFIX: Either all or none of `equals`, `hashCode` and `canEqual` will be generated. [Issue #313](https://github.com/rzwitserloot/lombok/issues/313)
* BUGFIX: Delombok in output-to-directory mode was generating very long paths on mac and linux. [Issue #322](https://github.com/rzwitserloot/lombok/issues/322)
* BUGFIX: Various refactor scripts and save actions bugs have been fixed in eclipse, though most remain.

### v0.9.3 "Burrowing Whale" (July 25th, 2010)
* FEATURE: Adding `@Getter` or `@Setter` to a class is now legal and is like adding those annotations to every non-static field in it. [Issue #202](https://github.com/rzwitserloot/lombok/issues/202)
* FEATURE: Three new annotations, `@NoArgsConstructor`, `@RequiredArgsConstructor` and `@AllArgsConstructor` have been added. These split off `@Data`'s ability to generate constructors, and also allow you to finetune what kind of constructor you want. In addition, by using these annotations, you can force generation of constructors even if you have your own. [Issue #152](https://github.com/rzwitserloot/lombok/issues/152)
* FEATURE: Constructors generated by lombok now include a `@java.beans.ConstructorProperties` annotation. This does mean these constructors no longer work in java 1.5, as this is a java 1.6 feature. The annotation can be suppressed by setting `suppressConstructorProperties` to `true` in a `@RequiredArgsConstructor` or `@AllArgsConstructor` annotation. [Issue #195](https://github.com/rzwitserloot/lombok/issues/195)
* FEATURE: generated `toString`, `equals` and `hashCode` methods will now use `this.getX()` and `other.getX()` instead of `this.x` and `other.x` if a suitable getter is available. This behaviour is useful for proxied classes, such as the POJOs that hibernate makes. Usage of the getters can be suppressed with `@ToString/@EqualsAndHashCode(doNotUseGetters = true)`. [Issue #183](https://github.com/rzwitserloot/lombok/issues/183)
* ENHANCEMENT: FindBugs' `@CheckForNull` is now copied from a field to a setter's parameter and the getter method just like `@Nullable`. [Issue #201](https://github.com/rzwitserloot/lombok/issues/201)
* ENHANCEMENT: plugins and `@SneakyThrows`: Resolving types in annotations now works better especially for classes that aren't in the core java libraries. [Issue #161](https://github.com/rzwitserloot/lombok/issues/161)
* ENHANCEMENT: If `tools.jar` isn't found (required when running _delombok_), now a useful error message is generated. The search for `tools.jar` now also looks in `JAVA_HOME`.
* ENHANCEMENT: toString() on inner classes now lists the class name as `Outer.Inner` instead of just `Inner`. [Issue #206](https://github.com/rzwitserloot/lombok/issues/206)
* ENHANCEMENT: All field accesses generated by lombok are now qualified (like so: `this.fieldName`). For those who have a warning configured for unqualified field access, those should no longer occur. [Issue #121](https://github.com/rzwitserloot/lombok/issues/121)
* ENHANCEMENT: All fields and methods generated by lombok now get `@SuppressWarnings("all")` attached to avoid such warnings as missing javadoc, for those of you who have that warning enabled. [Issue #120](https://github.com/rzwitserloot/lombok/issues/120)
* PLATFORMS: Lombok should now run in stand-alone ecj (Eclipse Compiler for Java). This isn't just useful for the few souls actually using this compiler day to day, but various eclipse build tools such as the RCP builder run ecj internally as well. [Issue #145](https://github.com/rzwitserloot/lombok/issues/145)
* BUGFIX: Eclipse: `@Data` and other annotations now don't throw errors when you include fields with bounded wildcard generics, such as `List<? extends Number>`. [Issue #157](https://github.com/rzwitserloot/lombok/issues/157)
* BUGFIX: complex enums didn't get delomboked properly. [Issue #169](https://github.com/rzwitserloot/lombok/issues/169)
* BUGFIX: delombok now no longer forgets to remove `import lombok.AccessLevel;`. In netbeans, that import will no longer be flagged erroneously as being unused. [Issue #173](https://github.com/rzwitserloot/lombok/issues/173) and [Issue #176](https://github.com/rzwitserloot/lombok/issues/176)
* BUGFIX: While its discouraged, `import lombok.*;` is supposed to work in the vast majority of cases. In eclipse, however, it didn't. Now it does. [Issue #175](https://github.com/rzwitserloot/lombok/issues/175)
* BUGFIX: When `@Getter` or `@Setter` is applied to a multiple field declaration, such as `@Getter int x, y;`, the annotation now applies to all fields, not just the first. [Issue #127](https://github.com/rzwitserloot/lombok/issues/127)
* BUGFIX: delombok on most javacs would quit with a NoSuchFieldError if it contains `<?>` style wildcards anywhere in the source, as well as at least 1 lombok annotation. No longer. [Issue #207](https://github.com/rzwitserloot/lombok/issues/207)
* BUILD: dependencies are now fetched automatically via ivy, and most dependencies now include sources by default, which is particularly handy for those working on the lombok sources themselves.

### v0.9.2 "Hailbunny" (December 15th, 2009)
* preliminary support for lombok on NetBeans! - thanks go to Jan Lahoda from NetBeans. [Issue #93](https://github.com/rzwitserloot/lombok/issues/93)
* lombok now ships with the delombok tool, which copies an entire directory filled with sources to a new directory, desugaring any java files to what it would look like without lombok's transformations. Compiling the sources in this new directory without lombok support should result in the same class files as compiling the original with lombok support. Great to double check on what lombok is doing, and for chaining the delombok-ed sources to source-based java tools such as Google Web Toolkit or javadoc. lombok.jar itself also provides an ant task for delombok. [Full documentation of delombok](https://projectlombok.org/features/delombok.html).
* Lombok now works on openjdk7 (tested with JDK7m5)! For all the folks on the cutting edge, this should be very good news. [Issue #134](https://github.com/rzwitserloot/lombok/issues/134) - thanks go to Jan Lahoda from NetBeans.
* lombok now has various command-line accessible utilities bundled with it. Run `java -jar lombok.jar --help` to see them. Included (aside from the already mentioned delombok):
* Ability to create a tiny jar named lombok-runtime.jar with runtime dependencies. The lombok transformations that have a runtime dependency on this jar can be listed as well. Run `java -jar lombok.jar createRuntime --help` for more information.
* Scriptable command line install and uninstall options. Run `java -jar lombok.jar install --help` (or `uninstall`, of course) for more information. Technically this support has been there in earlier versions, but the command line options are now much more lenient, not to mention more visible.
* Lombok now works on Springsource Tool Suite. [Issue #95](https://github.com/rzwitserloot/lombok/issues/95)
* Lombok now works on JDK 1.6.0_0, for those of us who have really old JDK1.6's installed on their system. [Issue #156](https://github.com/rzwitserloot/lombok/issues/156)
* Erroneous use of lombok in Eclipse (adding it to a project as an annotation processor, which is not how lombok is to be used on Eclipse) now generates a useful warning message with helpful information, instead of a confusing error hidden in the logs. [Issue #126](https://github.com/rzwitserloot/lombok/issues/126)
* FIXED: Regression bug where you would occasionally see errors with the gist 'loader constraint violation: when resolving...', such as when opening the help system, starting the diff editor, or, rarely, opening any java source file. [Issue #141](https://github.com/rzwitserloot/lombok/issues/141)
* FIXED: @SneakyThrows without any parameters should default to `Throwable.class` but it didn't do anything in javac. [Issue #146](https://github.com/rzwitserloot/lombok/issues/146)
* FIXED: Capitalization is now ignored when scanning for existing methods, so if `setURL` already exists, then a `@Data` annotation on a class with a field named `url` will no longer _also_ generate `setUrl`. [Issue #148](https://github.com/rzwitserloot/lombok/issues/148)

### v0.9.1 (November 9th, 2009)

* The installer now works much better on linux, in that it auto-finds eclipse in most locations linux users tend to put their eclipse installs, and it can now handle apt-get installed eclipses, which previously didn't work well at all. There's also a hidden feature where the installer can work as a command-line only tool (`java -jar lombok.jar install eclipse path/to/eclipse`) which also supports `uninstall` of course. You can now also point at `eclipse.ini` in case you have a really odd eclipse install, which should always work.
* For lombok developers, the eclipse launch target now works out-of-the-box on snow leopard. [Issue #139](https://github.com/rzwitserloot/lombok/issues/139)

### v0.9.0 (November 2nd, 2009)

* The lombok class patching system has been completely revamped; the core business of patching class files has been offloaded in an independent project called 'lombok.patcher', which is now used to patch lombok into eclipse.
* Many behind-the-scenes changes to improve lombok's stability and flexibility on eclipse.
* Changes to the lombok core API which aren't backwards compatible with lombok series v0.8 but which were necessary to make writing third party processors for lombok a lot easier.
* Minor version number bumped due to the above 3 issues.
* Eclipse's "rename" refactor script, invoked by pressing CMD/CTRL+SHIFT+R, now works on `@Data` annotated classes.
* The windows installer would fail on boot if you have unformatted drives. [Issue #138](https://github.com/rzwitserloot/lombok/issues/138)
* The static constructor that `@Data` can make was being generated as package private when compiling with javac. [Issue #136](https://github.com/rzwitserloot/lombok/issues/136)

### v0.8.5 (September 3rd, 2009)

* There's now an `AccessLevel.NONE` that you can use for your `@Getter` and `@Setter` annotations to suppress generating setters and getters when you're using the `@Data` annotation. Address [Issue #110](https://github.com/rzwitserloot/lombok/issues/110)
* Both `@EqualsAndHashCode` and `@ToString` now support explicitly specifying the fields to use, via the new 'of' parameter. Fields that begin with a '$' are now also excluded by default from equals, hashCode, and toString generation, unless of course you explicitly mention them in the 'of' parameter. Addresses [Issue #105](https://github.com/rzwitserloot/lombok/issues/105)
* There's a commonly used `@NotNull` annotation, from javax.validation (and in earlier versions of hibernate, which is the origin of javax.validation) which does not quite mean what we want it to mean: It is not legal on parameters, and it is checked at runtime after an explicit request for validation. As a workaround, we've removed checking for any annotation named `NotNull` from the nonnull support of lombok's generated Getters, Setters, and constructors. [Issue #116](https://github.com/rzwitserloot/lombok/issues/116)
* Fixed yet another issue with `@SneakyThrows`. This was reported fixed in v0.8.4. but it still didn't work quite as it should. Still falls under the bailiwick of
[Issue #103](https://github.com/rzwitserloot/lombok/issues/103)

### v0.8.4 (September 2nd, 2009)

* Fixed many issues with `@SneakyThrows` - in previous versions, using it would sometimes confuse the syntax colouring, and various constructs in the annotated method would cause outright eclipse errors, such as beginning the method with a try block. This also fixes [Issue #103](https://github.com/rzwitserloot/lombok/issues/103)
* Fixed the David Lynch bug - in eclipse, classes with lombok features used in them would sometimes appear invisible from other source files. It's described in more detail on [Issue #114](https://github.com/rzwitserloot/lombok/issues/114). If you suffered from it, you'll know what this is about.
* Fixed the problem where eclipse's help system did not start up on lombokized eclipses. [Issue #99](https://github.com/rzwitserloot/lombok/issues/99)
* All generated methods now make their parameters (if they have any) final. This should help avoid problems with the 'make all parameters final' save action in eclipse. [Issue #113](https://github.com/rzwitserloot/lombok/issues/113)
* Okay, this time _really_ added support for @NonNull and @NotNull annotations. It was reported for v0.8.3 but it wasn't actually in that release. @Nullable annotations are now also copied over to the getter's return type and the setter and constructor's parameters (but, obviously, no check is added). Any @NonNull annotated non-final fields that are not initialized are now also added to the generated constructor by @Data in order to ensure via an explicit null check that they contain a legal value.
* @ToString (and hence, @Data) now default to includeFieldNames=true. [Issue #108](https://github.com/rzwitserloot/lombok/issues/108)

### v0.8.3 (August 21st, 2009)

* @EqualsAndHashCode (and, indirectly, @Data) generate a warning when overriding a class other than java.lang.Object but not setting EqualsAndHashCode's callSuper to true. There are, however, legitimate reasons to do this, so this warning is now no longer generated if you explicitly set callSuper to false. The warning text now also refers to this action if not calling super is intentional.
* If your fields have @NonNull or @NotNull annotations, then generated setters are generated with a null check, and the
annotation is copied to the setter's parameter, and the getter's method.
* An annoying bug that usually showed up if you had package-info.java files has been fixed. It would cause a `NullPointerException` at lombok.javac.apt.Processor.toUnit(Processor.java:143)

### v0.8.2 (July 29th, 2009)

* @EqualsAndHashCode and @ToString created; these are subsets of what @Data does (namely: generate toString(), and generate equals() and hashCode() implementations). @Data will still generate these methods, but you can now generate them separately if you wish. As part of this split off, you can now specify for toString generation to include the field names in the produced toString method, and for all 3 methods: You can choose to involve the implementation of the superclass, and you can choose to exclude certain fields. [Issue #81](https://github.com/rzwitserloot/lombok/issues/81)
* when compiling with javac: warnings on specific entries of an annotation parameter (such as non-existent fields in a @EqualsAndHashCode exclude parameter) now show up on the problematic parameter and not on the entire annotation. [Issue #84](https://github.com/rzwitserloot/lombok/issues/84)

### v0.8.1 (July 26th, 2009)

* Changelog tracking from this version on.
* Using eclipse's 'find callers' on a @Data annotation will now find callers of the static constructor if you generated it. If not, it still finds callers to hashCode() as before (it's not possible to make eclipse find callers to the normal constructor, though you can just use 'find callers' on the class name, which works fine). [Issue #78](https://github.com/rzwitserloot/lombok/issues/78)
* If your field is called 'hasFoo' and its a boolean, and you use @Getter or @Data to generate a getter for it, that getter will now be called 'hasFoo' and not 'isHasFoo' as before. This rule holds for any field prefixed with 'has', 'is', or 'get', AND the character following the prefix is not lowercase (so that 'hashCodeGenerated' is not erroneously identified as already having a prefix!). Similar logic has been added to not generate a getter at all for a field named 'foo' or 'hasFoo' if there is already a method named 'isFoo'. [Issue #77](https://github.com/rzwitserloot/lombok/issues/77)
* Starting the lombok installer on mac os X using soylatte instead of apple's JVM now correctly detects being on a mac, and using mac-specific code for finding and installing eclipses. [Issue #80](https://github.com/rzwitserloot/lombok/issues/80)
* For non-mac, non-windows installations, the jar file in the `-javaagent` parameter is now written as an absolute path in `eclipse.ini` instead of a relative one. For some reason, on at least 1 linux installation, an absolute path is required to make javaagent work. This 'fix' has the unfortunate side-effect of making it impossible to move your eclipse installation around without breaking the pointer to the lombok java agent, so this change has only been introduced for non-windows, non-mac. Thanks to WouterS for spotting this one and helping us out with some research on fixing it. [Issue #79](https://github.com/rzwitserloot/lombok/issues/79)

### v0.8

* Initial release before announcements
* (note: There are a few different editions of lombok out there, all tagged with v0.8.)
