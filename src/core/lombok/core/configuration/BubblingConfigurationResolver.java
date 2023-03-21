/*
 * Copyright (C) 2014-2020 The Project Lombok Authors.
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
package lombok.core.configuration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;

import lombok.ConfigurationKeys;
import lombok.core.configuration.ConfigurationSource.ListModification;
import lombok.core.configuration.ConfigurationSource.Result;

public class BubblingConfigurationResolver implements ConfigurationResolver {
	
	private final ConfigurationFile start;
	private final ConfigurationFileToSource fileMapper;
	
	public BubblingConfigurationResolver(ConfigurationFile start, ConfigurationFileToSource fileMapper) {
		this.start = start;
		this.fileMapper = fileMapper;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T resolve(ConfigurationKey<T> key) {
		boolean isList = key.getType().isList();
		List<List<ListModification>> listModificationsList = null;
		
		boolean stopBubbling = false;
		ConfigurationFile currentLevel = start;
		Collection<ConfigurationFile> visited = new HashSet<ConfigurationFile>();
		outer:
		while (currentLevel != null) {
			Deque<ConfigurationFile> round = new ArrayDeque<ConfigurationFile>();
			round.push(currentLevel);
			
			while (!round.isEmpty()) {
				ConfigurationFile currentFile = round.pop();
				if (currentFile == null || !visited.add(currentFile)) continue;
				
				ConfigurationSource source = fileMapper.parsed(currentFile);
				if (source == null) continue;
				
				for (ConfigurationFile importFile : source.imports()) round.push(importFile);
				
				Result stop = source.resolve(ConfigurationKeys.STOP_BUBBLING);
				stopBubbling = stopBubbling || (stop != null && Boolean.TRUE.equals(stop.getValue()));
				
				Result result = source.resolve(key);
				if (result == null) continue;
				
				if (isList) {
					if (listModificationsList == null) listModificationsList = new ArrayList<List<ListModification>>();
					listModificationsList.add((List<ListModification>) result.getValue());
				}
				if (result.isAuthoritative()) {
					if (isList) break outer;
					return (T) result.getValue();
				}
			}
			if (stopBubbling) break;
			currentLevel = currentLevel.parent();
		}
		
		if (!isList) return null;
		if (listModificationsList == null) return (T) Collections.emptyList();
		
		List<Object> listValues = new ArrayList<Object>();
		Collections.reverse(listModificationsList);
		for (List<ListModification> listModifications : listModificationsList) {
			if (listModifications != null) for (ListModification modification : listModifications) {
				listValues.remove(modification.getValue());
				if (modification.isAdded()) listValues.add(modification.getValue());
			}
		}
		return (T) listValues;
	}
}
