/**
 *
 */
package lombok.javac.handlers;

import lombok.core.LombokNode;
import lombok.util.LombokGeneratorHelper;
import lombok.util.LombokGeneratorHelper.ImplementationDetails;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * @author Alexandru Bledea
 * @since Oct 5, 2014
 */
enum JavacImplementationDetails implements ImplementationDetails {

	INSTANCE;

	/* (non-Javadoc)
	 * @see lombok.util.LombokGeneratorHelper.ImplementationDetails#isStandard(lombok.core.LombokNode)
	 */
	@Override public boolean isStandard(final LombokNode<?, ?, ?> node) {
		return isFlagSet(Flags.STATIC, node);
	}

	/* (non-Javadoc)
	 * @see lombok.util.LombokGenerator.ImplementationDetails#isTransient(lombok.core.LombokNode)
	 */
	@Override public boolean isTransient(final LombokNode<?, ?, ?> node) {
		return isFlagSet(Flags.TRANSIENT, node);
	}

	/**
	 * @param flag
	 * @param child
	 * @return
	 */
	private static boolean isFlagSet(final int flag, final LombokNode<?, ?, ?> child) {
		return LombokGeneratorHelper.isFlagSet(flag, ((JCVariableDecl) child.get()).mods.flags);
	}

}
