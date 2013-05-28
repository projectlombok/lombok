package lombok.eclipse;


import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

public class EclipseAstProblemView {
	/**
	 * Adds a problem to the provided CompilationResult object so that it will show up
	 * in the Problems/Warnings view.
	 */
	public static void addProblemToCompilationResult(char[] fileNameArray, CompilationResult result,
			boolean isWarning, String message, int sourceStart, int sourceEnd) {
		if (result == null) return;
		if (fileNameArray == null) fileNameArray = "(unknown).java".toCharArray();
		int lineNumber = 0;
		int columnNumber = 1;
		int[] lineEnds = null;
		lineNumber = sourceStart >= 0
				? Util.getLineNumber(sourceStart, lineEnds = result.getLineSeparatorPositions(), 0, lineEnds.length-1)
				: 0;
		columnNumber = sourceStart >= 0
				? Util.searchColumnNumber(result.getLineSeparatorPositions(), lineNumber,sourceStart)
				: 0;
		
		CategorizedProblem ecProblem = new LombokProblem(
				fileNameArray, message, 0, new String[0],
				isWarning ? ProblemSeverities.Warning : ProblemSeverities.Error,
				sourceStart, sourceEnd, lineNumber, columnNumber);
		result.record(ecProblem, null);
	}
	
	private static class LombokProblem extends DefaultProblem {
		private static final String MARKER_ID = "org.eclipse.jdt.apt.pluggable.core.compileProblem";  //$NON-NLS-1$
		
		public LombokProblem(char[] originatingFileName, String message, int id,
				String[] stringArguments, int severity,
				int startPosition, int endPosition, int line, int column) {
			super(originatingFileName, message, id, stringArguments, severity, startPosition, endPosition, line, column);
		}
		
		@Override public int getCategoryID() {
			return CAT_UNSPECIFIED;
		}
		
		@Override public String getMarkerType() {
			return MARKER_ID;
		}
	}
}
