/*
 * Copyright (C) 2019 The Project Lombok Authors.
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
package lombok.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CleanupRegistry {
	private static final class CleanupKey {
		private final String key;
		private final Object target;
		
		CleanupKey(String key, Object target) {
			this.key = key;
			this.target = target;
		}
		
		@Override public boolean equals(Object other) {
			if (other == null) return false;
			if (other == this) return true;
			if (!(other instanceof CleanupKey)) return false;
			CleanupKey o = (CleanupKey) other;
			if (!key.equals(o.key)) return false;
			return target == o.target;
		}
		
		@Override public int hashCode() {
			return 109 * System.identityHashCode(target) + key.hashCode();
		}
	}
	
	private final Map<CleanupKey, CleanupTask> tasks = new ConcurrentHashMap<CleanupKey, CleanupTask>();
	
	public void registerTask(String key, Object target, CleanupTask task) {
		CleanupKey ck = new CleanupKey(key, target);
		tasks.putIfAbsent(ck, task);
	}
	
	public void run() {
		for (CleanupTask task : tasks.values()) {
			task.cleanup();
		}
		tasks.clear();
	}
}
