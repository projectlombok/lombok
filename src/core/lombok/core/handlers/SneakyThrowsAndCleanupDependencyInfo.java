/*
 * Copyright (C) 2009-2010 The Project Lombok Authors.
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
package lombok.core.handlers;

import java.util.Arrays;
import java.util.List;

import lombok.core.runtimeDependencies.RuntimeDependencyInfo;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(RuntimeDependencyInfo.class)
public class SneakyThrowsAndCleanupDependencyInfo implements RuntimeDependencyInfo {
	@Override public List<String> getRuntimeDependencies() {
		return Arrays.asList(
				"lombok/Lombok.class"
		);
	}
	
	@Override public List<String> getRuntimeDependentsDescriptions() {
		return Arrays.asList(
			"@SneakyThrows (only when delomboking - using @SneakyThrows in code that is compiled with lombok on the classpath does not create the dependency)",
			"@Cleanup (only when delomboking - using @Cleanup in code that is compiled with lombok on the classpath does not create the dependency)"
		);
	}
}
