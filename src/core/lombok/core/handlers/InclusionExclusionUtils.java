/*
 * Copyright (C) 2009-2018 The Project Lombok Authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lombok.ToString;
import lombok.ToString.Include;
import lombok.core.AST;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.LombokNode;

public class InclusionExclusionUtils {
	private static List<Integer> createListOfNonExistentFields(List<String> list, LombokNode<?, ?, ?> type, boolean excludeStandard, boolean excludeTransient) {
		boolean[] matched = new boolean[list.size()];
		
		for (LombokNode<?, ?, ?> child : type.down()) {
			if (list.isEmpty()) break;
			if (child.getKind() != Kind.FIELD) continue;
			if (excludeStandard) {
				if (child.isStatic()) continue;
				if (child.getName().startsWith("$")) continue;
			}
			if (excludeTransient && child.isTransient()) continue;
			
			int idx = list.indexOf(child.getName());
			if (idx > -1) matched[idx] = true;
		}
		
		List<Integer> problematic = new ArrayList<Integer>();
		for (int i = 0 ; i < list.size() ; i++) if (!matched[i]) problematic.add(i);
		
		return problematic;
	}
	
	public static void checkForBogusFieldNames(LombokNode<?, ?, ?> type, AnnotationValues<?> annotation, List<String> excludes, List<String> includes) {
		if (excludes != null && !excludes.isEmpty()) {
			for (int i : createListOfNonExistentFields(excludes, type, true, false)) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		
		if (includes != null && !includes.isEmpty()) {
			for (int i : createListOfNonExistentFields(includes, type, false, false)) {
				annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}
	
	public static class ToStringMember<L> {
		private final L node;
		private final ToString.Include inc;
		private final boolean defaultInclude;
		
		public ToStringMember(L node, ToString.Include inc, boolean defaultInclude) {
			this.node = node;
			this.inc = inc;
			this.defaultInclude = defaultInclude;
		}
		
		public L getNode() {
			return node;
		}
		
		public ToString.Include getInc() {
			return inc;
		}
		
		public boolean isDefaultInclude() {
			return defaultInclude;
		}
	}
	
	public static <A extends AST<A, L, N>, L extends LombokNode<A, L, N>, N> List<ToStringMember<L>> handleToStringMarking(LombokNode<A, L, N> typeNode, AnnotationValues<ToString> annotation, LombokNode<A, L, N> annotationNode) {
		ToString ann = annotation == null ? null : annotation.getInstance();
		List<String> oldExcludes = (ann != null && annotation.isExplicit("exclude")) ? Arrays.asList(ann.exclude()) : null;
		List<String> oldIncludes = (ann != null && annotation.isExplicit("of")) ? Arrays.asList(ann.of()) : null;
		
		boolean onlyExplicitlyIncluded = ann != null && ann.onlyExplicitlyIncluded();
		boolean memberAnnotationMode = onlyExplicitlyIncluded;
		List<ToStringMember<L>> members = new ArrayList<ToStringMember<L>>();
		List<String> namesToAutoExclude = new ArrayList<String>();
		
		if (typeNode == null || typeNode.getKind() != Kind.TYPE) return null;
		
		checkForBogusFieldNames(typeNode, annotation, oldExcludes, oldIncludes);
		
		if (oldExcludes != null && oldIncludes != null) {
			oldExcludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		
		for (L child : typeNode.down()) {
			boolean markExclude = child.getKind() == Kind.FIELD && child.hasAnnotation(ToString.Exclude.class);
			AnnotationValues<ToString.Include> markInclude = null;
			if (child.getKind() == Kind.FIELD || child.getKind() == Kind.METHOD) markInclude = child.findAnnotation(ToString.Include.class);
			
			if (markExclude || markInclude != null) memberAnnotationMode = true;
			
			if (markInclude != null && markExclude) {
				child.addError("@ToString.Exclude and @ToString.Include are mutually exclusive; the @Include annotation will be ignored");
				markInclude = null;
			}
			
			String name = child.getName();
			
			if (markExclude) {
				if (onlyExplicitlyIncluded) {
					child.addWarning("The @Exclude annotation is not needed; 'onlyExplicitlyIncluded' is set, so this member would be excluded anyway");
				} else if (child.isStatic()) {
					child.addWarning("The @Exclude annotation is not needed; static fields aren't included anyway");
				} else if (name.startsWith("$")) {
					child.addWarning("The @Exclude annotation is not needed; fields that start with $ aren't included anyway");
				}
				continue;
			}
			
			if (oldExcludes != null && oldExcludes.contains(name)) continue;
			
			if (markInclude != null) {
				Include inc = markInclude.getInstance();
				if (child.getKind() == Kind.METHOD) {
					if (child.countMethodParameters() > 0) {
						child.addError("Methods included for @ToString must have no arguments; it will not be included");
						continue;
					}
					String n = inc.name();
					if (n.isEmpty()) n = name;
					namesToAutoExclude.add(n);
				}
				members.add(new ToStringMember<L>(child, inc, false));
				continue;
			}
			
			if (onlyExplicitlyIncluded) continue;
			if (oldIncludes != null) {
				if (child.getKind() == Kind.FIELD && oldIncludes.contains(name)) members.add(new ToStringMember<L>(child, null, false));
				continue;
			}
			if (child.getKind() != Kind.FIELD) continue;
			if (child.isStatic()) continue;
			if (name.startsWith("$")) continue;
			if (child.isEnumMember()) continue;
			members.add(new ToStringMember<L>(child, null, true));
		}
		
		/* delete default-included fields with the same name as an explicit inclusion */ {
			Iterator<ToStringMember<L>> it = members.iterator();
			while (it.hasNext()) {
				ToStringMember<L> m = it.next();
				if (m.isDefaultInclude() && namesToAutoExclude.contains(m.getNode().getName())) it.remove();
			}
		}
		
		if (annotation == null || !annotation.isExplicit("exclude")) oldExcludes = null;
		if (annotation == null || !annotation.isExplicit("of")) oldIncludes = null;
		
		if (memberAnnotationMode && (oldExcludes != null || oldIncludes != null)) {
			annotationNode.addError("The old-style 'exclude/of' parameter cannot be used together with the new-style @Include / @Exclude annotations.");
			return null;
		}
		
		Collections.sort(members, new Comparator<ToStringMember<L>>() {
			@Override public int compare(ToStringMember<L> a, ToStringMember<L> b) {
				int ra = a.getInc() == null ? 0 : a.getInc().rank();
				int rb = b.getInc() == null ? 0 : b.getInc().rank();
				if (ra < rb) return +1;
				if (ra > rb) return -1;
				
				int pa = a.getNode().getStartPos();
				int pb = b.getNode().getStartPos();
				
				if (pa < pb) return -1;
				if (pa > pb) return +1;
				
				return 0;
			}
		});
		return members;
	}
}
