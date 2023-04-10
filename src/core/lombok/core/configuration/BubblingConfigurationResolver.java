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

		ConfigurationFile currentLevel = start;
		Collection<ConfigurationFile> visited = new HashSet<>();
		boolean stopBubbling = false;

		outer:
		while (!stopBubbling && currentLevel != null) {
			Deque<ConfigurationFile> round = new ArrayDeque<>();
			round.push(currentLevel);

			while (!round.isEmpty()) {
				ConfigurationFile currentFile = round.pop();
				if (currentFile == null || !visited.add(currentFile)) continue;

				ConfigurationSource source = fileMapper.parsed(currentFile);
				if (source == null) continue;

				round.addAll(source.imports());

				stopBubbling = updateStopBubbling(stopBubbling, source);

				Result result = source.resolve(key);
				if (result != null) {
					listModificationsList = processResult(result, isList, listModificationsList);
					if (result.isAuthoritative()) {
						if (isList) break outer;
						return (T) result.getValue();
					}
				}
			}
			currentLevel = currentLevel.parent();
		}

		return processFinalResult(isList, listModificationsList);
	}

	private boolean updateStopBubbling(boolean stopBubbling, ConfigurationSource source) {
		Result stop = source.resolve(ConfigurationKeys.STOP_BUBBLING);
		return stopBubbling || (stop != null && Boolean.TRUE.equals(stop.getValue()));
	}

	private <T> List<List<ListModification>> processResult(Result result, boolean isList, List<List<ListModification>> listModificationsList) {
		if (isList) {
			if (listModificationsList == null) listModificationsList = new ArrayList<>();
			listModificationsList.add((List<ListModification>) result.getValue());
		}
		return listModificationsList;
	}

	private <T> T processFinalResult(boolean isList, List<List<ListModification>> listModificationsList) {
		if (!isList) return null;
		if (listModificationsList == null) return (T) Collections.emptyList();

		List<Object> listValues = new ArrayList<>();
		Collections.reverse(listModificationsList);

		for (List<ListModification> listModifications : listModificationsList) {
			if (listModifications != null) {
				for (ListModification modification : listModifications) {
					listValues.remove(modification.getValue());
					if (modification.isAdded()) listValues.add(modification.getValue());
				}
			}
		}
		return (T) listValues;
	}
}
