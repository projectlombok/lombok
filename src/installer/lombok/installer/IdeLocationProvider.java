/*
 * Copyright (C) 2009-2016 The Project Lombok Authors.
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
package lombok.installer;

import java.util.List;
import java.util.regex.Pattern;

public interface IdeLocationProvider {
	/**
	 * @throws CorruptedIdeLocationException
	 *    Only throw this exception if the location seems like a proper installation except there's something wrong with it.
	 *    Do not throw it (just return {@code null}) if there's nothing there or it looks absolutely nothing like your IDE.
	 */
	IdeLocation create(String path) throws CorruptedIdeLocationException;
	
	/**
	 * Return the usual name of the IDE executable or other obvious marker of an IDE installation on the current platform.
	 */
	Pattern getLocationSelectors();
	
	/**
	 * Look for installations of your IDE in the usual places.
	 * 
	 * @param locations Add to this list any valid locations that you found.
	 * @param problems
	 *     Add to this list any locations that look like installations,
	 *     but have problems that prevent you from installing/uninstalling from them. DONT add to this list
	 *     any common locations that have no installation at all - only add near misses.
	 */
	void findIdes(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems);
}
