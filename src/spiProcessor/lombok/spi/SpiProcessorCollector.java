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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

class SpiProcessorCollector {
	private final Map<String, SpiProcessorService> services = new HashMap<String, SpiProcessorService>();
	private final List<String> removed = new ArrayList<String>();
	
	private final Elements elements;
	private final Messager logger;
	private final Filer filer;
	
	SpiProcessorCollector(ProcessingEnvironment processingEnv) {
		this.elements = processingEnv.getElementUtils();
		this.logger = processingEnv.getMessager();
		this.filer = processingEnv.getFiler();
	}
	
	SpiProcessorCollector(Elements elements, Messager logger, Filer filer) {
		if (elements == null) throw new NullPointerException("elements");
		if (logger == null) throw new NullPointerException("logger");
		if (filer == null) throw new NullPointerException("filer");
		this.elements = elements;
		this.logger = logger;
		this.filer = filer;
	}
	
	SpiProcessorService getService(String serviceName) {
		if (serviceName == null) throw new NullPointerException("serviceName");
		if (!services.containsKey(serviceName)) {
			SpiProcessorService newService = new SpiProcessorService(serviceName);
			CharSequence initialData = readInitialData(serviceName);
			if (initialData != null) {
				newService.addAllFromProvidersNameList(initialData.toString());
				for (String provider : removed) newService.removeProvider(provider);
			}
			services.put(serviceName, newService);
		}
		return services.get(serviceName);
	}
	
	Collection<SpiProcessorService> services() {
		return Collections.unmodifiableMap(services).values();
	}
	
	void removeProvider(String provider) {
		if (provider == null) throw new NullPointerException("provider");
		removed.add(provider);
		for (SpiProcessorService service : services.values()) service.removeProvider(provider);
	}
	
	@Override public String toString() {
		return services.values().toString();
	}
	
	private CharSequence readInitialData(String serviceName) {
		String pathName = SpiProcessor.getRootPathOfServiceFiles() + serviceName;
		FileObject resource;
		
		try {
			resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", pathName);
		} catch (Exception e) {
			logger.printMessage(Kind.ERROR, SpiProcessor.toErrorMsg(e, pathName));
			return null;
		}
		
		return SpiProcessorPersistence.readFilerResource(resource, logger, pathName);
	}
	
	void stripProvidersWithoutSourceFile() {
		for (SpiProcessorService s : services.values()) s.stripProvidersWithoutSourceFile(elements);
	}
}
