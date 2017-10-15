/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.file;

import javax.tools.JavaFileManager;
import java.nio.charset.Charset;

public abstract class BaseFileManager implements JavaFileManager {
    protected BaseFileManager(Charset charset) {}
}
