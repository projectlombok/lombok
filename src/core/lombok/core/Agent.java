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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public abstract class Agent {
	protected abstract void runAgent(String agentArgs, Instrumentation instrumentation, boolean injected) throws Exception;
	
	public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Throwable {
		runAgents(agentArgs, instrumentation, true);
	}
	
	public static void premain(String agentArgs, Instrumentation instrumentation) throws Throwable {
		runAgents(agentArgs, instrumentation, false);
	}
	
	private static final List<AgentInfo> AGENTS = Collections.unmodifiableList(Arrays.asList(
			new NetbeansPatcherInfo(),
			new EclipsePatcherInfo()
	));
	
	private static void runAgents(String agentArgs, Instrumentation instrumentation, boolean injected) throws Throwable {
		for (AgentInfo info : AGENTS) {
			try {
				Class<?> agentClass = Class.forName(info.className());
				Agent agent = (Agent) agentClass.newInstance();
				agent.runAgent(agentArgs, instrumentation, injected);
			} catch (Throwable t) {
				info.problem(t, instrumentation);
			}
		}
	}
	
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
	
	private static class NetbeansPatcherInfo extends AgentInfo {
		@Override String className() {
			return "lombok.netbeans.agent.NetbeansPatcher";
		}
		
		@Override void problem(Throwable in, Instrumentation instrumentation) throws Throwable {
			try {
				super.problem(in, instrumentation);
			} catch (InternalError ie) {
				throw ie;
			} catch (Throwable t) {
				final String error;
				
				if (t instanceof UnsupportedClassVersionError) {
					error = "Lombok only works on netbeans if you start netbeans using a 1.6 or higher JVM.\n" +
							"Change your platform's default JVM, or edit etc/netbeans.conf\n" +
							"and explicitly tell netbeans your 1.6 JVM's location.";
				} else {
					error = "Lombok disabled due to error: " + t;
				}
				
				instrumentation.addTransformer(new ClassFileTransformer() {
					@Override public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
						if ("org/netbeans/modules/java/source/parsing/JavacParser".equals(className)) {
							//If that class gets loaded, this is definitely a netbeans(-esque) environment, and thus we SHOULD tell the user that lombok is not in fact loaded.
							SwingUtilities.invokeLater(new Runnable() {
								@Override public void run() {
									JOptionPane.showMessageDialog(null, error, "Lombok Disabled", JOptionPane.ERROR_MESSAGE);
								}
							});
						}
						
						return null;
					}
				});
			}
		}
	}
	
	private static class EclipsePatcherInfo extends AgentInfo {
		@Override String className() {
			return "lombok.eclipse.agent.EclipsePatcher";
		}
	}
}
