package lombok.eclipse.agent;

public interface EclipseTransformer {
	/** slash and not dot separated */
	String getTargetClassName();
	
	byte[] transform(byte[] in);
}
