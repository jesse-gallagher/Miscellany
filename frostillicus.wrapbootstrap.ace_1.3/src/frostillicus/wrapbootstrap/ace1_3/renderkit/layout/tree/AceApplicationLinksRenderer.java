package frostillicus.wrapbootstrap.ace1_3.renderkit.layout.tree;

import frostillicus.wrapbootstrap.ace1_3.renderkit.util.AceNavRenderer;

public class AceApplicationLinksRenderer extends AceNavRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	protected boolean makeSelectedActive(final TreeContextImpl tree) {
		return false;
	}

	@Override
	public String getContainerStyleClass(final TreeContextImpl node) {
		if(node.getDepth() == 1) {
			return "nav ace-nav";
		}
		return super.getContainerStyleClass(node);
	}
}