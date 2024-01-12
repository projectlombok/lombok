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

import java.util.Map;

public class TestParameters {
	private Long sourceVersion;
	private Long targetVersion;
	
	// Javac only
	private Integer minVersion;
	private String encoding;
	private Map<String, String> formatPreferences;
	private boolean checkPositions;
	
	// Eclipse/Ecj only
	private boolean verifyDiet;
	
	public Long getSourceVersion() {
		return sourceVersion;
	}
	
	public void setSourceVersion(Long sourceVersion) {
		this.sourceVersion = sourceVersion;
	}
	
	public Long getTargetVersion() {
		return targetVersion;
	}
	
	public void setTargetVersion(Long targetVersion) {
		this.targetVersion = targetVersion;
	}
	
	public Integer getMinVersion() {
		return minVersion;
	}
	
	public void setMinVersion(Integer minVersion) {
		this.minVersion = minVersion;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public Map<String, String> getFormatPreferences() {
		return formatPreferences;
	}
	
	public void setFormatPreferences(Map<String, String> formatPreferences) {
		this.formatPreferences = formatPreferences;
	}
	
	public boolean isCheckPositions() {
		return checkPositions;
	}
	
	public void setCheckPositions(boolean checkPositions) {
		this.checkPositions = checkPositions;
	}
	
	public boolean isVerifyDiet() {
		return verifyDiet;
	}
	
	public void setVerifyDiet(boolean verifyDiet) {
		this.verifyDiet = verifyDiet;
	}
}
