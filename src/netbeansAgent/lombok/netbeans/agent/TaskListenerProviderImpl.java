package lombok.netbeans.agent;

import org.netbeans.api.java.source.ClasspathInfo;

import com.sun.source.util.TaskListener;
import com.sun.tools.javac.util.Context;

public class TaskListenerProviderImpl implements TaskListenerProvider {
	public TaskListener create(final Context context, ClasspathInfo cpInfo) {
		return new NetbeansEntryPoint(context);
	}
}
