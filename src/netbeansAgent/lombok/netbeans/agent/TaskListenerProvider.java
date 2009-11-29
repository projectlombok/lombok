package lombok.netbeans.agent;

public interface TaskListenerProvider {
	public TaskListener create(Context c, ClasspathInfo info);
}
