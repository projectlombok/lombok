/*
 * Copyright (C) 2026 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lombok.core.LombokConfiguration;
import lombok.delombok.Delombok;
import lombok.javac.Javac;
import lombok.javac.apt.LombokProcessor;

public class TestModuleImports {

	private static final String COPY_ANNOTATION =
		"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) " +
		"@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) public @interface Copy {}";
	private static final String MODEL =
		"package probe;\n%s\n" +
		"@Getter @Setter @RequiredArgsConstructor class Model {\n" +
		"  @NonNull @Copy String value;\n" +
		"  void call(@NonNull String value) {}\n" +
		"}\n";
	private static File root, lombokJar, modules, automaticJar;
	private static int sequence;

	@BeforeClass public static void prepareModules() throws Exception {
		LombokProcessor.addOpensForLombok();
		assumeTrue(Javac.getJavaCompilerVersion() >= 25);
		root = new File("build/module-import-tests-" + System.nanoTime());
		assertTrue(root.mkdirs());
		write(root, "lombok.config", "config.stopBubbling = true\nlombok.copyableAnnotations += test.copy.Copy\n");

		lombokJar = new File(root, "lombok.jar");
		JarOutputStream jar = new JarOutputStream(new FileOutputStream(lombokJar), manifest("lombok"));
		try {
			for (String name : Arrays.asList("AccessLevel", "Getter", "Getter$AnyAnnotation", "Setter", "Setter$AnyAnnotation",
					"RequiredArgsConstructor", "RequiredArgsConstructor$AnyAnnotation", "NonNull", "Generated", "val")) {
				String path = "lombok/" + name + ".class";
				InputStream in = TestModuleImports.class.getClassLoader().getResourceAsStream(path);
				assertNotNull(path, in);
				addToJar(jar, path, in);
			}
			String standardException = "lombok/experimental/StandardException.class";
			addToJar(jar, standardException, TestModuleImports.class.getClassLoader().getResourceAsStream(standardException));
		} finally {
			jar.close();
		}

		File source = new File(root, "modules-src");
		List<File> files = new ArrayList<File>();
		files.add(write(source, "test.annotations/module-info.java", "module test.annotations { exports test.copy; }"));
		files.add(write(source, "test.annotations/test/copy/Copy.java", "package test.copy; " + COPY_ANNOTATION));
		files.add(write(source, "test.annotations/test/copy/Annotations.java", "package test.copy; public class Annotations { " + COPY_ANNOTATION + " }"));
		files.add(write(source, "test.facade/module-info.java", "module test.facade { requires transitive lombok; requires transitive test.annotations; }"));
		files.add(write(source, "test.decoys/module-info.java", "module test.decoys { exports decoy; }"));
		files.add(write(source, "test.decoys/decoy/Copy.java", "package decoy; " + COPY_ANNOTATION));
		files.add(write(source, "test.decoys/decoy/NonNull.java", "package decoy; @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) public @interface NonNull {}"));
		files.add(write(source, "test.decoys/decoy/Getter.java", "package decoy; public @interface Getter {}"));
		files.add(write(source, "test.visibility/module-info.java", "module test.visibility { exports test.visible; exports test.qualified to test.consumer; requires test.annotations; }"));
		files.add(write(source, "test.visibility/test/visible/Marker.java", "package test.visible; public @interface Marker {}"));
		files.add(write(source, "test.visibility/test/qualified/Copy.java", "package test.qualified; " + COPY_ANNOTATION));
		files.add(write(source, "test.visibility/test/hidden/Getter.java", "package test.hidden; public @interface Getter {}"));
		files.add(write(source, "test.visibility/test/hidden/NonNull.java", "package test.hidden; public @interface NonNull {}"));
		files.add(write(source, "test.visibility/test/hidden/Copy.java", "package test.hidden; " + COPY_ANNOTATION));
		modules = new File(root, "modules");
		compile(files, modules, lombokJar.getPath(), false, "--module-source-path", source.getPath());

		automaticJar = new File(root, "automatic-annotations.jar");
		jar = new JarOutputStream(new FileOutputStream(automaticJar), manifest("test.annotations.automatic"));
		try {
			addToJar(jar, "test/copy/Copy.class", new FileInputStream(new File(modules, "test.annotations/test/copy/Copy.class")));
		} finally {
			jar.close();
		}
	}

	@Before public void useFileSystemConfiguration() {
		LombokConfiguration.overrideConfigurationResolverFactory(null);
	}

	@Test public void ordinaryImportsControl() throws Exception {
		checkModel("import lombok.*; import test.copy.Copy;", modulePath(modules));
	}

	@Test public void directModuleImports() throws Exception {
		checkModel("import module lombok; import module test.annotations;", modulePath(modules));
	}

	@Test public void transitiveAndRepeatedModuleImports() throws Exception {
		checkModel("import module test.facade; import module lombok; import module test.annotations;", modulePath(modules));
	}

	@Test public void automaticModuleImports() throws Exception {
		checkModel("import module lombok; import module test.annotations.automatic;", modulePath(automaticJar));
	}

	@Test public void nonExportedPackagesAreNotImported() throws Exception {
		checkModel("import module lombok; import module test.annotations; import module test.visibility;", modulePath(modules));
	}

	@Test public void delombokUnderstandsModuleImports() throws Exception {
		File source = newSource();
		write(source, "Model.java", String.format(MODEL, "import module test.facade;"));
		write(source, "module-info.java", "module test.consumer { requires test.facade; }");
		StringWriter output = new StringWriter();
		Delombok delombok = new Delombok();
		delombok.setModulepath(modulePath(modules));
		delombok.setWriter(output);
		delombok.addFile(source, "Model.java");
		delombok.addFile(source, "module-info.java");
		assertTrue(delombok.delombok());
		String code = output.toString();
		assertTrue(code, code.contains("public Model("));
		assertTrue(code, code.contains("public String getValue()"));
		assertTrue(code, code.contains("public void setValue("));
		assertTrue(code, code.contains("throw new java.lang.NullPointerException"));
		assertTrue(code, code.contains("@Copy"));
	}

	@Test public void qualifiedExportsAreImportedOnlyIntoTheirTarget() throws Exception {
		File source = newSource();
		List<File> files = new ArrayList<File>();
		files.add(write(source, "module-info.java", "module test.consumer { requires lombok; requires test.visibility; }"));
		files.add(write(source, "probe/Qualified.java", "package probe; import module lombok; import module test.visibility; " +
			"@Getter class Qualified { @Copy String value; }"));
		write(source, "lombok.config", "config.stopBubbling = true\nlombok.copyableAnnotations += test.qualified.Copy\n");
		File out = new File(source, "out");
		compile(files, out, modulePath(modules), true);
		ClassLoader loader = new URLClassLoader(new URL[] {out.toURI().toURL(), new File(modules, "test.visibility").toURI().toURL()}, TestModuleImports.class.getClassLoader());
		Method getter = loader.loadClass("probe.Qualified").getDeclaredMethod("getValue");
		AnnotatedElement returnType = (AnnotatedElement) Method.class.getMethod("getAnnotatedReturnType").invoke(getter);
		boolean found = false;
		for (Annotation annotation : returnType.getAnnotations()) if (annotation.annotationType().getName().equals("test.qualified.Copy")) found = true;
		assertTrue("A qualified module export must support configured annotation copying", found);
	}

	@Test public void nonTransitiveRequiresDoesNotImportDependencyPackages() throws Exception {
		File source = newSource();
		File file = write(source, "probe/NotTransitive.java", "package probe; import module lombok; import module test.visibility; " +
			"@Getter class NotTransitive { @Copy String value; }");
		StringWriter diagnostics = new StringWriter();
		assertFalse(compile(Collections.singletonList(file), new File(source, "out"), modulePath(modules), true, diagnostics, "--add-modules", "ALL-MODULE-PATH"));
		assertTrue(diagnostics.toString(), diagnostics.toString().contains("compiler.err.cant.resolve.location"));
	}

	@Test public void ordinaryStarImportsTakePrecedence() throws Exception {
		checkShadowing("import module lombok; import module test.annotations; import decoy.*;", false);
	}

	@Test public void explicitImportsTakePrecedence() throws Exception {
		checkShadowing("import module lombok; import module test.annotations; import decoy.NonNull; import decoy.Copy; import decoy.Getter;", false);
	}

	@Test public void currentPackageTakesPrecedence() throws Exception {
		checkShadowing("import module lombok; import module test.annotations;", true);
	}

	@Test public void lexicalTypesTakePrecedence() throws Exception {
		File source = newSource();
		File file = write(source, "probe/Lexical.java", "package probe; import module lombok; import module test.annotations; " +
			"class Lexical { @interface NonNull {} @interface Copy {} @interface Getter {} " +
			"@lombok.Getter @lombok.Setter @lombok.RequiredArgsConstructor static class Model { @NonNull @Copy String value; } " +
			"static class GetterShadow { @Getter String value; } }");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		ClassLoader loader = load(out);
		Class<?> type = loader.loadClass("probe.Lexical$Model");
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		Method setter = type.getDeclaredMethod("setValue", String.class);
		setter.setAccessible(true);
		setter.invoke(constructor.newInstance(), new Object[] {null});
		assertCopy((AnnotatedElement) Method.class.getMethod("getAnnotatedReturnType").invoke(type.getDeclaredMethod("getValue")), false);
		assertEquals(0, loader.loadClass("probe.Lexical$GetterShadow").getDeclaredMethods().length);
	}

	@Test public void fullyQualifiedAndNestedAnnotationNames() throws Exception {
		File source = newSource();
		File file = write(source, "probe/Names.java", "package probe; import module lombok; import module test.annotations; " +
			"@RequiredArgsConstructor class Names { @lombok.NonNull String value; @Getter @Annotations.Copy String copied; }");
		write(source, "lombok.config", "config.stopBubbling = true\nlombok.copyableAnnotations += test.copy.Annotations$Copy\n");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		Class<?> type = load(out).loadClass("probe.Names");
		assertNotNull(type.getDeclaredConstructor(String.class));
		AnnotatedElement returnType = (AnnotatedElement) Method.class.getMethod("getAnnotatedReturnType").invoke(type.getDeclaredMethod("getCopied"));
		assertEquals("test.copy.Annotations$Copy", returnType.getAnnotations()[0].annotationType().getName());
	}

	@Test public void inheritedAnnotationTypesTakePrecedence() throws Exception {
		File source = newSource();
		File file = write(source, "probe/Inherited.java", "package probe; import module lombok; " +
			"class Parent { @interface Getter {} @interface NonNull {} } " +
			"class Inherited extends Parent { @Getter int value; void call(@NonNull String value) {} }");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		Class<?> type = load(out).loadClass("probe.Inherited");
		try {
			type.getDeclaredMethod("getValue");
			fail("An inherited unrelated @Getter must not run Lombok's handler");
		} catch (NoSuchMethodException expected) {}
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		Method call = type.getDeclaredMethod("call", String.class);
		call.setAccessible(true);
		call.invoke(constructor.newInstance(), new Object[] {null});
	}

	@Test public void localAndAnonymousClassesUseModuleImports() throws Exception {
		File source = newSource();
		File file = write(source, "probe/Local.java", "package probe; import module lombok; class Local { " +
			"Object local() { class Model { @Getter int value; } return new Model(); } " +
			"Object anonymous() { return new Object() { @Getter int value; }; } }");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		Class<?> outer = load(out).loadClass("probe.Local");
		Constructor<?> constructor = outer.getDeclaredConstructor();
		constructor.setAccessible(true);
		Object instance = constructor.newInstance();
		for (String name : Arrays.asList("local", "anonymous")) {
			Method factory = outer.getDeclaredMethod(name);
			factory.setAccessible(true);
			assertNotNull(factory.invoke(instance).getClass().getDeclaredMethod("getValue"));
		}
	}

	@Test public void inheritedAnnotationInLocalClassTakesPrecedence() throws Exception {
		File source = newSource();
		File file = write(source, "probe/LocalInherited.java", "package probe; import module lombok; " +
			"class LocalParent { @interface Getter {} } " +
			"class LocalInherited { Object create() { class Child extends LocalParent { @Getter int value; } return new Child(); } }");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		Class<?> outer = load(out).loadClass("probe.LocalInherited");
		Constructor<?> constructor = outer.getDeclaredConstructor();
		constructor.setAccessible(true);
		Method create = outer.getDeclaredMethod("create");
		create.setAccessible(true);
		Class<?> local = create.invoke(constructor.newInstance()).getClass();
		assertEquals("An inherited unrelated local-class annotation must not generate a getter", 0, local.getDeclaredMethods().length);
	}

	@Test public void classAnnotationDoesNotSeeInheritedMemberTypes() throws Exception {
		for (String imports : Arrays.asList("import module java.base; import lombok.*;", "import module lombok;")) {
			File source = newSource();
			File file = write(source, "probe/Header.java", "package probe; " + imports +
				" @Getter class Header extends HeaderParent { int value; } class HeaderParent { @interface Getter {} }");
			File out = new File(source, "out");
			compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
			assertNotNull(load(out).loadClass("probe.Header").getDeclaredMethod("getValue"));
		}
	}

	@Test public void classAnnotationDoesNotSeeItsOwnMemberTypes() throws Exception {
		File source = newSource();
		File file = write(source, "probe/OwnHeader.java", "package probe; import module lombok; " +
			"@Getter class OwnHeader { @interface Getter {} int value; }");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		assertNotNull(load(out).loadClass("probe.OwnHeader").getDeclaredMethod("getValue"));
	}

	@Test public void moduleImportedExternalOnParamNonNull() throws Exception {
		File jsr305 = new File(javax.annotation.Nonnull.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		assertTrue(jsr305.isFile());
		File source = newSource();
		File file = write(source, "probe/ExternalOnParam.java", "package probe; import module lombok; import module jsr305; " +
			"class ExternalOnParam { @Setter(onParam_ = @Nonnull) String value; }");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules) + File.pathSeparator + jsr305.getPath(), true, "--add-modules", "ALL-MODULE-PATH");
		Class<?> type = load(out).loadClass("probe.ExternalOnParam");
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		Method setter = type.getDeclaredMethod("setValue", String.class);
		setter.setAccessible(true);
		try {
			setter.invoke(constructor.newInstance(), new Object[] {null});
			fail("Module-imported external onParam @Nonnull must generate a null check without an annotation handler");
		} catch (InvocationTargetException e) {
			assertTrue(e.getCause().toString(), e.getCause() instanceof NullPointerException);
		}
	}

	@Test public void localTypesShadowNonAnnotationTypeReferences() throws Exception {
		File source = newSource();
		File file = write(source, "probe/LocalVal.java", "package probe; import module java.base; import lombok.*; " +
			"class LocalVal { void method() { class val {} val value = new val(); value = new val(); } }");
		compile(Collections.singletonList(file), new File(source, "out"), modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
	}

	@Test public void moduleImportedNonAnnotationAndDetachedReferences() throws Exception {
		File source = newSource();
		File file = write(source, "probe/ModuleReferences.java", "package probe; import module lombok; " +
			"class ModuleReferences { @Setter(onParam_ = @NonNull) String value; " +
			"int infer() { val value = \"value\"; return value.length(); } } " +
			"@lombok.experimental.StandardException class ShortConstructor extends RuntimeException { " +
			"public ShortConstructor(String message) { super(message); } }");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		Class<?> type = load(out).loadClass("probe.ModuleReferences");
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		Object instance = constructor.newInstance();
		Method infer = type.getDeclaredMethod("infer");
		infer.setAccessible(true);
		assertEquals(Integer.valueOf(5), infer.invoke(instance));
		Method setter = type.getDeclaredMethod("setValue", String.class);
		setter.setAccessible(true);
		try {
			setter.invoke(instance, new Object[] {null});
			fail("Module-imported detached onParam @NonNull must still produce a null check");
		} catch (InvocationTargetException e) {
			assertTrue(e.getCause().toString(), e.getCause() instanceof NullPointerException);
		}
	}

	@Test public void fullyQualifiedNonAnnotationTypeReferencesRemainRecognized() throws Exception {
		File source = newSource();
		File file = write(source, "probe/ExceptionWithConstructor.java", "package probe; import module java.base; " +
			"@lombok.experimental.StandardException class ExceptionWithConstructor extends RuntimeException { " +
			"public ExceptionWithConstructor(java.lang.String message) { super(message); } " +
			"@lombok.Setter(onParam_ = @lombok.NonNull) String data; " +
			"int inferredLocal() { lombok.val value = \"value\"; return value.length(); } }");
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		Class<?> type = load(out).loadClass("probe.ExceptionWithConstructor");
		Constructor<?> constructor = type.getDeclaredConstructor(String.class);
		constructor.setAccessible(true);
		Method setter = type.getDeclaredMethod("setData", String.class);
		setter.setAccessible(true);
		try {
			setter.invoke(constructor.newInstance("message"), new Object[] {null});
			fail("Fully-qualified detached onParam @NonNull must still produce a null check");
		} catch (InvocationTargetException e) {
			assertTrue(e.getCause().toString(), e.getCause() instanceof NullPointerException);
		}
	}

	@Test public void moduleImportAmbiguityIsNotResolvedByLombok() throws Exception {
		File source = newSource();
		File file = write(source, "probe/Ambiguous.java", "package probe; import module lombok; import module test.decoys; " +
			"@lombok.Getter class Ambiguous { @NonNull String value; }");
		StringWriter diagnostics = new StringWriter();
		assertFalse(compile(Collections.singletonList(file), new File(source, "out"), modulePath(modules), true, diagnostics, "--add-modules", "ALL-MODULE-PATH"));
		assertTrue(diagnostics.toString(), diagnostics.toString().contains("compiler.err.ref.ambiguous"));
	}

	private static void checkModel(String imports, String modulePath) throws Exception {
		File source = newSource();
		File file = write(source, "probe/Model.java", String.format(MODEL, imports));
		File out = new File(source, "out");
		compile(Collections.singletonList(file), out, modulePath, true, "--add-modules", "ALL-MODULE-PATH");
		Class<?> type = load(out).loadClass("probe.Model");
		Constructor<?> constructor = type.getDeclaredConstructor(String.class);
		constructor.setAccessible(true);
		Object instance = constructor.newInstance("value");
		try {
			constructor.newInstance(new Object[] {null});
			fail("RequiredArgsConstructor must check a module-imported @NonNull field");
		} catch (InvocationTargetException e) {
			assertTrue(e.getCause().toString(), e.getCause() instanceof NullPointerException);
		}
		for (String name : Arrays.asList("setValue", "call")) {
			Method method = type.getDeclaredMethod(name, String.class);
			method.setAccessible(true);
			try {
				method.invoke(instance, new Object[] {null});
				fail(name + " must check a module-imported @NonNull");
			} catch (InvocationTargetException e) {
				assertTrue(e.getCause().toString(), e.getCause() instanceof NullPointerException);
			}
		}
		Method getter = type.getDeclaredMethod("getValue");
		assertCopy((AnnotatedElement) Method.class.getMethod("getAnnotatedReturnType").invoke(getter), true);
		Object[] parameters = (Object[]) Method.class.getMethod("getAnnotatedParameterTypes").invoke(type.getDeclaredMethod("setValue", String.class));
		assertCopy((AnnotatedElement) parameters[0], true);
		parameters = (Object[]) Constructor.class.getMethod("getAnnotatedParameterTypes").invoke(constructor);
		assertCopy((AnnotatedElement) parameters[0], true);
	}

	private static void checkShadowing(String imports, boolean samePackage) throws Exception {
		File source = newSource();
		List<File> files = new ArrayList<File>();
		files.add(write(source, "probe/Shadow.java", "package probe; " + imports +
			" @lombok.Getter @lombok.Setter @lombok.RequiredArgsConstructor class Shadow { @NonNull @Copy String value; }" +
			" class GetterShadow { @Getter String value; }"));
		if (samePackage) {
			files.add(write(source, "probe/NonNull.java", "package probe; @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) public @interface NonNull {}"));
			files.add(write(source, "probe/Copy.java", "package probe; " + COPY_ANNOTATION));
			files.add(write(source, "probe/Getter.java", "package probe; public @interface Getter {}"));
		}
		File out = new File(source, "out");
		compile(files, out, modulePath(modules), true, "--add-modules", "ALL-MODULE-PATH");
		ClassLoader loader = load(out);
		Class<?> type = loader.loadClass("probe.Shadow");
		Constructor<?> constructor = type.getDeclaredConstructor();
		constructor.setAccessible(true);
		Method setter = type.getDeclaredMethod("setValue", String.class);
		setter.setAccessible(true);
		setter.invoke(constructor.newInstance(), new Object[] {null});
		assertCopy((AnnotatedElement) Method.class.getMethod("getAnnotatedReturnType").invoke(type.getDeclaredMethod("getValue")), false);
		try {
			loader.loadClass("probe.GetterShadow").getDeclaredMethod("getValue");
			fail("An unrelated @Getter must not run Lombok's handler");
		} catch (NoSuchMethodException expected) {}
	}

	private static void assertCopy(AnnotatedElement element, boolean expected) {
		boolean found = false;
		for (Annotation annotation : element.getAnnotations()) if (annotation.annotationType().getName().equals("test.copy.Copy")) found = true;
		assertEquals("Configured annotation on generated signature", expected, found);
	}

	private static ClassLoader load(File out) throws Exception {
		return new URLClassLoader(new URL[] {out.toURI().toURL(), new File(modules, "test.annotations").toURI().toURL(), automaticJar.toURI().toURL()}, TestModuleImports.class.getClassLoader());
	}

	private static File newSource() {
		File source = new File(root, "case-" + (++sequence));
		assertTrue(source.mkdirs());
		return source;
	}

	private static String modulePath(File dependency) {
		return lombokJar.getPath() + File.pathSeparator + dependency.getPath();
	}

	private static void compile(List<File> files, File out, String modulePath, boolean lombok, String... extra) throws Exception {
		StringWriter diagnostics = new StringWriter();
		boolean success = compile(files, out, modulePath, lombok, diagnostics, extra);
		assertTrue(diagnostics.toString(), success);
	}

	private static boolean compile(List<File> files, File out, String modulePath, boolean lombok, StringWriter diagnostics, String... extra) throws Exception {
		assertTrue(out.mkdirs());
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assertNotNull("These tests require a JDK compiler", compiler);
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
		try {
			List<String> options = new ArrayList<String>(Arrays.asList("--release", "25", "--module-path", modulePath,
				"-classpath", System.getProperty("java.class.path"), "-d", out.getPath(), "-XDrawDiagnostics", lombok ? "-proc:full" : "-proc:none"));
			options.addAll(Arrays.asList(extra));
			JavaCompiler.CompilationTask task = compiler.getTask(diagnostics, manager, null, options, null, manager.getJavaFileObjectsFromFiles(files));
			task.setProcessors(lombok ? Collections.<Processor>singletonList(new LombokProcessor()) : Collections.<Processor>emptyList());
			return task.call().booleanValue();
		} finally {
			manager.close();
		}
	}

	private static File write(File directory, String path, String content) throws Exception {
		File file = new File(directory, path);
		assertTrue(file.getParentFile().isDirectory() || file.getParentFile().mkdirs());
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		try {
			writer.write(content);
		} finally {
			writer.close();
		}
		return file;
	}

	private static Manifest manifest(String name) {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("Automatic-Module-Name", name);
		return manifest;
	}

	private static void addToJar(JarOutputStream jar, String path, InputStream in) throws Exception {
		try {
			jar.putNextEntry(new JarEntry(path));
			byte[] buffer = new byte[8192];
			for (int n; (n = in.read(buffer)) != -1;) jar.write(buffer, 0, n);
			jar.closeEntry();
		} finally {
			in.close();
		}
	}
}
