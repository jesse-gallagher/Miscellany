package frostillicus.wrapbootstrap.ace1_3.renderkit.layout.tree;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.renderkit.html_extended.outline.tree.HtmlTagsRenderer;
import com.ibm.xsp.renderkit.html_extended.RenderUtil;

import frostillicus.wrapbootstrap.ace1_3.renderkit.util.IconUtil;

public class AcePlaceBarLinksRenderer extends HtmlTagsRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getContainerTag() {
		return "";
	}
	@Override
	protected String getItemTag() {
		return "";
	}
	@Override
	protected void renderEntryItemLinkAttributes(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree, final boolean selected, final boolean enabled) throws IOException {

	}
	@Override
	protected void renderEntryItemContent(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree, final boolean enabled, final boolean selected) throws IOException {
		boolean hasLink = false;
		boolean alwaysRenderLinks = alwaysRenderItemLink(tree, enabled, selected);
		if(enabled) {
			String href = tree.getNode().getHref();
			if(StringUtil.isNotEmpty(href)) {
				writer.startElement("a",null);
				String role = getItemRole(tree, enabled, selected);
				if (StringUtil.isNotEmpty(role)) {
					writer.writeAttribute("role", role, null); // $NON-NLS-1$
				}

				// Since the parent node isn't rendered, write the CSS info here
				String styleClass = tree.getNode().getStyleClass();
				if(StringUtil.isNotEmpty(styleClass)) {
					writer.writeAttribute("class", styleClass, null);
				}
				String style = tree.getNode().getStyle();
				if(StringUtil.isNotEmpty(style)) {
					writer.writeAttribute("style", style, null);
				}

				RenderUtil.writeLinkAttribute(context,writer,href);
				hasLink = true;
			} else {
				String onclick = findNodeOnClick(tree);
				if(StringUtil.isNotEmpty(onclick)) {
					writer.startElement("a",null);
					writer.writeAttribute("href","javascript:;",null); // $NON-NLS-1$ $NON-NLS-2$
					writer.writeAttribute("onclick", "javascript:"+onclick, null); // $NON-NLS-1$ $NON-NLS-2$
					hasLink = true;
				}
			}
		}
		if(!hasLink && alwaysRenderLinks) {
			// Render an empty link...
			writer.startElement("a",null);
			hasLink = true;
		}
		if(hasLink) {
			renderEntryItemLinkAttributes(context, writer, tree, enabled, selected);
		}

		IconUtil.renderIcon(context, writer, tree.getNode(), "ace");

		// Generate a regular node
		String label = tree.getNode().getLabel();
		if(StringUtil.isNotEmpty(label)) {
			writer.writeText(label, "label"); // $NON-NLS-1$
		}

		// Render a popup image, if any
		writePopupImage(context, writer, tree);

		if(hasLink || alwaysRenderLinks) {
			writer.endElement("a");
			tree.markCurrentAsAction();
		}
	}
}
