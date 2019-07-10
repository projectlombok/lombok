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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;
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
				if (annotation != null) annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		
		if (includes != null && !includes.isEmpty()) {
			for (int i : createListOfNonExistentFields(includes, type, false, false)) {
				if (annotation != null) annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}
	
	public static class Included<L, I extends Annotation> {
		private final L node;
		private final I inc;
		private final boolean defaultInclude;
		
		public Included(L node, I inc, boolean defaultInclude) {
			this.node = node;
			this.inc = inc;
			this.defaultInclude = defaultInclude;
		}
		
		public L getNode() {
			return node;
		}
		
		public I getInc() {
			return inc;
		}
		
		public boolean isDefaultInclude() {
			return defaultInclude;
		}
	}
	
	private static String innerAnnName(Class<? extends Annotation> type) {
		String name = type.getSimpleName();
		Class<?> c = type.getEnclosingClass();
		while (c != null) {
			name = c.getSimpleName() + "." + name;
			c = c.getEnclosingClass();
		}
		return name;
	}
	
	public static <A extends AST<A, L, N>, L extends LombokNode<A, L, N>, N, I extends Annotation> List<Included<L, I>> handleIncludeExcludeMarking(Class<I> inclType, String replaceName, Class<? extends Annotation> exclType, LombokNode<A, L, N> typeNode, AnnotationValues<?> annotation, LombokNode<A, L, N> annotationNode, boolean includeTransient) {
		List<String> oldExcludes = (annotation != null && annotation.isExplicit("exclude")) ? annotation.getAsStringList("exclude") : null;
		List<String> oldIncludes = (annotation != null && annotation.isExplicit("of")) ? annotation.getAsStringList("of") : null;
		
		boolean onlyExplicitlyIncluded = annotation != null ? annotation.getAsBoolean("onlyExplicitlyIncluded") : false;
		boolean memberAnnotationMode = onlyExplicitlyIncluded;
		List<Included<L, I>> members = new ArrayList<Included<L, I>>();
		List<String> namesToAutoExclude = new ArrayList<String>();
		
		if (typeNode == null || typeNode.getKind() != Kind.TYPE) return null;
		
		checkForBogusFieldNames(typeNode, annotation, oldExcludes, oldIncludes);
		String inclTypeName = innerAnnName(inclType);
		String exclTypeName = innerAnnName(exclType);
		
		if (oldExcludes != null && oldIncludes != null) {
			oldExcludes = null;
			if (annotation != null) annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		
		for (L child : typeNode.down()) {
			boolean markExclude = child.getKind() == Kind.FIELD && child.hasAnnotation(exclType);
			AnnotationValues<I> markInclude = null;
			if (child.getKind() == Kind.FIELD || child.getKind() == Kind.METHOD) markInclude = child.findAnnotation(inclType);
			
			if (markExclude || markInclude != null) memberAnnotationMode = true;
			
			if (markInclude != null && markExclude) {
				child.addError("@" + exclTypeName + " and @" + inclTypeName + " are mutually exclusive; the @Include annotation will be ignored");
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
				I inc = markInclude.getInstance();
				if (child.getKind() == Kind.METHOD) {
					if (child.countMethodParameters() > 0) {
						child.addError("Methods included with @" + inclTypeName + " must have no arguments; it will not be included");
						continue;
					}
					String n = replaceName != null ?  markInclude.getAsString(replaceName) : "";
					if (n.isEmpty()) n = name;
					namesToAutoExclude.add(n);
				}
				members.add(new Included<L, I>(child, inc, false));
				continue;
			}
			
			if (onlyExplicitlyIncluded) continue;
			if (oldIncludes != null) {
				if (child.getKind() == Kind.FIELD && oldIncludes.contains(name)) members.add(new Included<L, I>(child, null, false));
				continue;
			}
			if (child.getKind() != Kind.FIELD) continue;
			if (child.isStatic()) continue;
			if (child.isTransient() && !includeTransient) continue;
			if (name.startsWith("$")) continue;
			if (child.isEnumMember()) continue;
			members.add(new Included<L, I>(child, null, true));
		}
		
		/* delete default-included fields with the same name as an explicit inclusion */ {
			Iterator<Included<L, I>> it = members.iterator();
			while (it.hasNext()) {
				Included<L, I> m = it.next();
				if (m.isDefaultInclude() && namesToAutoExclude.contains(m.getNode().getName())) it.remove();
			}
		}
		
		if (annotation == null || !annotation.isExplicit("exclude")) oldExcludes = null;
		if (annotation == null || !annotation.isExplicit("of")) oldIncludes = null;
		
		if (memberAnnotationMode && (oldExcludes != null || oldIncludes != null)) {
			annotationNode.addError("The old-style 'exclude/of' parameter cannot be used together with the new-style @Include / @Exclude annotations.");
			return null;
		}
		
		return members;
	}
	
	public static <A extends AST<A, L, N>, L extends LombokNode<A, L, N>, N> List<Included<L, ToString.Include>> handleToStringMarking(LombokNode<A, L, N> typeNode, AnnotationValues<ToString> annotation, LombokNode<A, L, N> annotationNode) {
		List<Included<L, ToString.Include>> members = handleIncludeExcludeMarking(ToString.Include.class, "name", ToString.Exclude.class, typeNode, annotation, annotationNode, true);
		
		Collections.sort(members, new Comparator<Included<L, ToString.Include>>() {
			@Override public int compare(Included<L, ToString.Include> a, Included<L, ToString.Include> b) {
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
	
	public static <A extends AST<A, L, N>, L extends LombokNode<A, L, N>, N> List<Included<L, EqualsAndHashCode.Include>> handleEqualsAndHashCodeMarking(LombokNode<A, L, N> typeNode, AnnotationValues<EqualsAndHashCode> annotation, LombokNode<A, L, N> annotationNode) {
		return handleIncludeExcludeMarking(EqualsAndHashCode.Include.class, "replaces", EqualsAndHashCode.Exclude.class, typeNode, annotation, annotationNode, false);
	}
}
