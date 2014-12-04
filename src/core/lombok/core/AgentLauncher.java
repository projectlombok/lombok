/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
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

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AgentLauncher {
	public interface AgentLaunchable {
		void runAgent(String agentArgs, Instrumentation instrumentation, boolean injected, Class<?> launchingContext) throws Exception;
	}
	
	public static void runAgents(String agentArgs, Instrumentation instrumentation, boolean injected, Class<?> launchingContext) throws Throwable {
		for (AgentInfo info : AGENTS) {
			try {
				Class<?> agentClass = Class.forName(info.className());
				AgentLaunchable agent = (AgentLaunchable) agentClass.newInstance();
				agent.runAgent(agentArgs, instrumentation, injected, launchingContext);
			} catch (Throwable t) {
				info.problem(t, instrumentation);
			}
		}
	}
	
	private static final List<AgentInfo> AGENTS = Collections.unmodifiableList(Arrays.<AgentInfo>asList(
			new EclipsePatcherInfo()
	));
	
	private static abstract class AgentInfo {
		abstract String className();
		
		/**
		 * Called if an exception occurs while loading the agent represented by this AgentInfo object.
		 * 
		 * @param t The throwable.
		 * @param instrumentation In case you want to take an alternative action.
		 */
		void problem(Throwable t, Instrumentation instrumentation) throws Throwable {
			if (t instanceof ClassNotFoundException) {
				//That's okay - this lombok evidently is a version with support for something stripped out.
				return;
			}
			
			if (t instanceof ClassCastException) {
				throw new InternalError("Lombok bug. Class: " + className() + " is not an implementation of lombok.core.Agent");
			}
			
			if (t instanceof IllegalAccessError) {
				throw new InternalError("Lombok bug. Class: " + className() + " is not public");
			}
			
			if (t instanceof InstantiationException) {
				throw new InternalError("Lombok bug. Class: " + className() + " is not concrete or has no public no-args constructor");
			}
			
			throw t;
		}
	}
	
	private static class EclipsePatcherInfo extends AgentInfo {
		@Override String className() {
			return "lombok.eclipse.agent.EclipsePatcher";
		}
	}
}
