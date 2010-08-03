package lombok.core;

public interface PostCompilerTransformation {
	byte[] applyTransformations(byte[] original, String className, DiagnosticsReceiver diagnostics);
}
