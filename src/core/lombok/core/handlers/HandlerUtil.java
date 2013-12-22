/*
 * Copyright (C) 2013 The Project Lombok Authors.
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

import lombok.core.FlagUsageType;
import lombok.core.JavaIdentifiers;
import lombok.core.LombokNode;
import lombok.core.configuration.ConfigurationKey;

public class HandlerUtil {
	private HandlerUtil() {}
	
	/** Checks if the given name is a valid identifier.
	 * 
	 * If it is, this returns {@code true} and does nothing else.
	 * If it isn't, this returns {@code false} and adds an error message to the supplied node.
	 */
	public static boolean checkName(String nameSpec, String identifier, LombokNode<?, ?, ?> errorNode) {
		if (identifier.isEmpty()) {
			errorNode.addError(nameSpec + " cannot be the empty string.");
			return false;
		}
		
		if (!JavaIdentifiers.isValidJavaIdentifier(identifier)) {
			errorNode.addError(nameSpec + " must be a valid java identifier.");
			return false;
		}
		
		return true;
	}
	
	public static void handleFlagUsage(LombokNode<?, ?, ?> node, ConfigurationKey<FlagUsageType> key, String featureName) {
		FlagUsageType fut = node.getAst().readConfiguration(key);
		
		if (fut != null) {
			String msg = "Use of " + featureName + " is flagged according to lombok configuration.";
			if (fut == FlagUsageType.WARNING) node.addWarning(msg);
			else node.addError(msg);
		}
	}
	
	public static void handleFlagUsage(LombokNode<?, ?, ?> node, ConfigurationKey<FlagUsageType> key1, String featureName1, ConfigurationKey<FlagUsageType> key2, String featureName2) {
		FlagUsageType fut1 = node.getAst().readConfiguration(key1);
		FlagUsageType fut2 = node.getAst().readConfiguration(key2);
		
		FlagUsageType fut = null;
		String featureName = null;
		if (fut1 == FlagUsageType.ERROR) {
			fut = fut1;
			featureName = featureName1;
		} else if (fut2 == FlagUsageType.ERROR) {
			fut = fut2;
			featureName = featureName2;
		} else if (fut1 == FlagUsageType.WARNING) {
			fut = fut1;
			featureName = featureName1;
		} else {
			fut = fut2;
			featureName = featureName2;
		}
		
		if (fut != null) {
			String msg = "Use of " + featureName + " is flagged according to lombok configuration.";
			if (fut == FlagUsageType.WARNING) node.addWarning(msg);
			else node.addError(msg);
		}
	}
}
