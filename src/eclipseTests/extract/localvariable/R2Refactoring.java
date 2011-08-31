package R2;

public class R2Refactoring {
	private int oldName=0;

	public void someMethod() {
		int oldName;
		/* 1: ExtractLocalVariable(newName) :1 */
		oldName = this.oldName;
		/* :1: */
	}
}
