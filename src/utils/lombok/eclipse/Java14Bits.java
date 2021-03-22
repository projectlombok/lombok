package lombok.eclipse;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

public class Java14Bits {
	private Java14Bits() { }
	
	public static final int AccRecord = ASTNode.Bit25;
	public static final int IsCanonicalConstructor = ASTNode.Bit10; // record declaration
	public static final int IsImplicit = ASTNode.Bit11; // record declaration / generated statements in compact constructor
}
