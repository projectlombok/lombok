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
package lombok.installer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Represents an installation location problem.
 * You should throw it upon creation of a {@code IdeLocation} class
 * if the provided location looks like your kind of IDE but there's something wrong with it.
 */
public class CorruptedIdeLocationException extends Exception {
	private final String ideType;
	
	public CorruptedIdeLocationException(String message, String ideType, Throwable cause) {
		super(message, cause);
		this.ideType = ideType;
	}
	
	public String getIdeType() {
		return ideType;
	}
	
	void showDialog(JFrame appWindow) {
		JOptionPane.showMessageDialog(appWindow, getMessage(), "Cannot configure " + ideType + " installation", JOptionPane.WARNING_MESSAGE);
	}
}
