/**
 *
 */
package lombok.eclipse.handlers;

import lombok.core.LombokNode;
import lombok.util.LombokGeneratorHelper;
import lombok.util.LombokGeneratorHelper.ImplementationDetails;

import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * @author Alexandru Bledea
 * @since Oct 5, 2014
 */
enum EclipseImplementationDetails implements ImplementationDetails {

	INSTANCE;

	/* (non-Javadoc)
	 * @see lombok.util.LombokGeneratorHelper.ImplementationDetails#isStandard(lombok.core.LombokNode)
	 */
	@Override public boolean isStandard(final LombokNode<?, ?, ?> node) {
		return isFlagSet(ClassFileConstants.AccStatic, node);
	}

	/* (non-Javadoc)
	 * @see lombok.util.LombokGenerator.ImplementationDetails#isTransient(lombok.core.LombokNode)
	 */
	@Override public boolean isTransient(final LombokNode<?, ?, ?> node) {
		return isFlagSet(ClassFileConstants.AccTransient, node);
	}

	/**
	 * @param flag
	 * @param child
	 * @return
	 */
	private static boolean isFlagSet(final int flag, final LombokNode<?, ?, ?> child) {
		return LombokGeneratorHelper.isFlagSet(flag, ((FieldDeclaration) child.get()).modifiers);
	}

}
