/*
 * Copyright (C) 2024 The Project Lombok Authors.
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

import java.util.LinkedHashSet;

import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

public class TransformationResult {
	private boolean changed;
	private boolean success;
	private LinkedHashSet<CompilerMessage> messages = new LinkedHashSet<CompilerMessage>();
	private String output;
	
	public void addMessage(CompilerMessage message) {
		getMessages().add(message);
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public String getOutput() {
		return output;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public LinkedHashSet<CompilerMessage> getMessages() {
		return messages;
	}
}
