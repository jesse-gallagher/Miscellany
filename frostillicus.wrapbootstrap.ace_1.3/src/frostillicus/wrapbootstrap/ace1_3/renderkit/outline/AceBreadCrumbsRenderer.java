package frostillicus.wrapbootstrap.ace1_3.renderkit.outline;

import javax.faces.context.FacesContext;

import com.ibm.xsp.extlib.component.outline.AbstractOutline;
import com.ibm.xsp.extlib.renderkit.html_extended.outline.AbstractOutlineRenderer;
import com.ibm.xsp.extlib.tree.ITreeRenderer;

public class AceBreadCrumbsRenderer extends AbstractOutlineRenderer {
	@Override
	protected ITreeRenderer findTreeRenderer(final FacesContext context, final AbstractOutline outline) {
		return new frostillicus.wrapbootstrap.ace1_3.renderkit.outline.tree.AceBreadCrumbsRenderer(outline);
	}
}
