/*
 * Copyright (C) 2020-2021 The Project Lombok Authors.
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
package lombok.installer.eclipse;

import java.util.Collections;

import lombok.installer.IdeLocationProvider;
import lombok.spi.Provides;

@Provides(IdeLocationProvider.class)
public class RhcrLocationProvider extends EclipseProductLocationProvider {
	private static final EclipseProductDescriptor RHCR = new StandardProductDescriptor(
			"Red Hat CodeReady Studio",
			"codereadystudio",
			"studio",
			RhcrLocationProvider.class.getResource("rhds.png"),
			Collections.<String>emptySet()
	);
	
	public RhcrLocationProvider() {
		super(RHCR);
	}
}
