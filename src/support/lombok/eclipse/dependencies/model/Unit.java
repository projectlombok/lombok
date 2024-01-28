/*
 * Copyright (C) 2023-2024 The Project Lombok Authors.
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
package lombok.eclipse.dependencies.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class Unit {
	@XmlAttribute
	public String id;
	@XmlAttribute
	@XmlJavaTypeAdapter(VersionAdapter.class)
	public Version version;
	
	@XmlElementWrapper(name = "provides")
	@XmlElement(name="provided")
	public List<Provided> provides;
	
	@XmlElementWrapper(name = "requires")
	@XmlElement(name="required")
	public List<Required> requires;
	
	public boolean satisfies(Required required) {
		return provides.stream().anyMatch(p -> p.namespace.equals(required.namespace) && p.name.equals(required.name) && required.range.contains(p.version));
	}
	
	@Override
	public String toString() {
		return id + "_" + version;
	}
}
