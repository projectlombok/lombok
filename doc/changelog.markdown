Lombok Changelog
----------------

### v0.8.4

* All generated methods now make their parameters (if they have any) final. This should help avoid problems with the 'make all parameters final' save action in eclipse. [Issue #40](http://code.google.com/p/projectlombok/issues/detail?id=40)
* Okay, this time _really_ added support for @NonNull and @NotNull annotations. It was reported for v0.8.3 but it wasn't actually in that release. @Nullable annotations are now also copied over to the getter's return type and the setter and constructor's parameters (but, obviously, no check is added).

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