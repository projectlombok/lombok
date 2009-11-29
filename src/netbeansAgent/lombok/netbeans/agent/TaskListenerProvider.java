package lombok.netbeans.agent;

import org.netbeans.api.java.source.ClasspathInfo;

import com.sun.source.util.TaskListener;
import com.sun.tools.javac.util.Context;

public interface TaskListenerProvider {
	public TaskListener create(Context c, ClasspathInfo info);
}
