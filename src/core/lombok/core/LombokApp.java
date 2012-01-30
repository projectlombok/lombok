/*
 * Copyright (C) 2009 The Project Lombok Authors.
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
package lombok.core;

import java.util.Collections;
import java.util.List;

/**
 * Implement this class, and add yourself as a provider for it, to become an app runnable by running lombok.jar as a jar.
 * 
 * @see lombok.core.Main.VersionApp
 */
public abstract class LombokApp {
	/**
	 * @param args The arguments; analogous to what's passed to {@code public static void main(String[] args)} methods.
	 * @return The return value. Don't call {@code System.exit} yourself.
	 */
	public abstract int runApp(List<String> args) throws Exception;
	
	/**
	 * @return Your app name. For example {@code delombok}.
	 */
	public abstract String getAppName();
	
	/**
	 * @return Description of this app, for the command line.
	 */
	public abstract String getAppDescription();
	
	/**
	 * @return When lombok.jar is executed with any of these strings as first argument, your app will be started.
	 */
	public List<String> getAppAliases() {
		return Collections.emptyList();
	}
	
	/**
	 * @return {@code true} if this app is an internal debugging tool and won't be listed by the default help message.
	 */
	public boolean isDebugTool() {
		return false;
	}
}
