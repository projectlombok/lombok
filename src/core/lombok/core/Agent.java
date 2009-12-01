/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

public abstract class Agent {
	protected abstract void runAgent(String agentArgs, Instrumentation instrumentation, boolean injected) throws Exception;
	
	public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Exception {
		runAgents(agentArgs, instrumentation, true);
	}
	
	public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
		runAgents(agentArgs, instrumentation, false);
	}
	
	private static final List<String> AGENT_NAMES = Collections.unmodifiableList(Arrays.asList(
			"lombok.netbeans.agent.NetbeansPatcher",
			"lombok.eclipse.agent.EclipsePatcher"
			));
	
	private static void runAgents(String agentArgs, Instrumentation instrumentation, boolean injected) throws Exception {
		for (String agentName : AGENT_NAMES) {
			try {
				Class<?> agentClass = Class.forName(agentName);
				Agent agent = (Agent) agentClass.newInstance();
				agent.runAgent(agentArgs, instrumentation, injected);
			} catch (ClassNotFoundException e) {
				//That's okay - this lombok evidently is a version with support for something stripped out.
			} catch (ClassCastException e) {
				throw new InternalError("Lombok bug. Class: " + agentName + " is not an implementation of lombok.core.Agent");
			} catch (IllegalAccessException e) {
				throw new InternalError("Lombok bug. Class: " + agentName + " is not public");
			} catch (InstantiationException e) {
				throw new InternalError("Lombok bug. Class: " + agentName + " is not concrete or has no public no-args constructor");
			}
		}
	}
}
