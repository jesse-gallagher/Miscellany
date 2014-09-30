package frostillicus.wrapbootstrap.ace1_3.renderkit.outline;

import javax.faces.context.FacesContext;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.component.outline.AbstractOutline;
import com.ibm.xsp.extlib.component.outline.UIOutlineNavigator;
import com.ibm.xsp.extlib.renderkit.html_extended.outline.AbstractOutlineRenderer;
import com.ibm.xsp.extlib.tree.ITreeRenderer;

import frostillicus.wrapbootstrap.ace1_3.renderkit.outline.tree.AceMenuRenderer;

public class AceOutlineMenuRenderer extends AbstractOutlineRenderer {
	@Override
	protected ITreeRenderer findTreeRenderer(final FacesContext context, final AbstractOutline outline) {
		AceMenuRenderer r = createMenuRenderer(context, outline);
		if(outline instanceof UIOutlineNavigator) {
			UIOutlineNavigator nav = (UIOutlineNavigator)outline;
			r.setExpandable(nav.isExpandable());
			r.setExpandEffect(nav.getExpandEffect());
			//r.setKeepState(nav.isKeepState());
			r.setExpandLevel(nav.getExpandLevel());
		}
		return r;
	}

	protected AceMenuRenderer createMenuRenderer(final FacesContext context, final AbstractOutline outline) {
		int type = AceMenuRenderer.TYPE_PILL;
		if(outline!=null) {
			String styleClass = outline.getStyleClass();
			if(StringUtil.isNotEmpty(styleClass) && styleClass.contains("nav-list")) {
				type = AceMenuRenderer.TYPE_LIST;
			}
		}
		return new AceMenuRenderer(outline,type);
	}
}
