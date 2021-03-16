/*
 * Copyright (C) 2021 The Project Lombok Authors.
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
package lombok.spi;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.lang.model.util.Elements;

final class SpiProcessorService {
	private final String name;
	private final Set<String> providers = new TreeSet<String>();
	
	SpiProcessorService(String name) {
		if (name == null) throw new NullPointerException("name");
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	void addProvider(String className) {
		if (className == null) throw new NullPointerException("className");
		providers.add(className);
	}
	
	boolean removeProvider(String provider) {
		return providers.remove(provider);
	}
	
	String toProvidersListFormat() {
		return providers.stream().collect(Collectors.joining("\n"));
	}
	
	void addAllFromProvidersNameList(String in) {
		for (String line : in.split("\\n")) {
			String[] content = line.split("#", 2);
			if (content.length == 0) continue;
			String trimmed = content[0].trim();
			String[] elems = trimmed.split("\\s+", 2);
			if (elems.length == 0 || elems[0].isEmpty()) continue;
			String cn = elems[0];
			addProvider(cn);
		}
	}
	
	void stripProvidersWithoutSourceFile(Elements elements) {
		Iterator<String> it = providers.iterator();
		while (it.hasNext()) {
			if (!sourceExists(elements, it.next())) it.remove();
		}
	}
	
	private boolean sourceExists(Elements elements, String typeName) {
		return elements.getTypeElement(typeName) != null;
	}
	
	@Override public String toString() {
		return name + " = " + providers;
	}
}
