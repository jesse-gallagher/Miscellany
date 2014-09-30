package frostillicus.wrapbootstrap.ace1_3.renderkit.util;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.renderkit.html_extended.outline.tree.HtmlListRenderer;
import com.ibm.xsp.extlib.tree.ITreeNode;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.renderkit.html_extended.RenderUtil;

public class AceNavRenderer extends HtmlListRenderer {
	private static final long serialVersionUID = 1L;

	public static final int PROP_MENUPREFIX = 100;

	public AceNavRenderer() { }
	public AceNavRenderer(final UIComponent component) {
		super(component);
	}

	@Override
	protected Object getProperty(final int prop) {
		switch(prop) {
		case PROP_MENUPREFIX: return "al";
		}
		return super.getProperty(prop);
	}

	@Override
	public String getContainerStyleClass(final TreeContextImpl node) {
		if(node.getDepth() > 1) {
			return "user-menu dropdown-menu-right dropdown-menu dropdown-yellow dropdown-caret dropdown-close";
		}
		return null;
	}

	@Override
	protected String getItemStyleClass(final TreeContextImpl tree, final boolean enabled, final boolean selected) {
		String clazz = tree.getNode().getStyleClass();
		if(tree.getNode().getType() == ITreeNode.NODE_SEPARATOR) {
			return "divider";
		}
		if(tree.getNode().getType() == ITreeNode.NODE_CONTAINER) {
			if(tree.getDepth() > 2) {
				clazz = ExtLibUtil.concatStyleClasses(clazz, "dropdown-submenu");
			}
		}
		if(!enabled) {
			clazz = ExtLibUtil.concatStyleClasses(clazz, "disabled");
		}
		if(selected && makeSelectedActive(tree)) {
			clazz = ExtLibUtil.concatStyleClasses(clazz, "active");
		}
		return clazz;
	}
	protected boolean makeSelectedActive(final TreeContextImpl tree) {
		return tree.getDepth() <= 2;
	}

	@Override
	protected void renderEntrySeparator(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree) throws IOException {
		writer.startElement("li", null);
		writer.writeAttribute("class", "divider", null);
		writer.endElement("li");
	}

	@Override
	protected void renderEntryItemLinkAttributes(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree, final boolean enabled, final boolean selected) throws IOException {
		if(tree.getNode().getType() == ITreeNode.NODE_CONTAINER && tree.getDepth() <= 2) {
			writer.writeAttribute("class", "dropdown-toggle", null);
			writer.writeAttribute("data-toggle", "dropdown", null);
			writer.writeAttribute("href", "#", null);
		}
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
					writer.writeAttribute("role", role, null);
				}
				RenderUtil.writeLinkAttribute(context,writer,href);
				hasLink = true;
			} else {
				String onclick = findNodeOnClick(tree);
				if(StringUtil.isNotEmpty(onclick)) {
					writer.startElement("a",null);
					writer.writeAttribute("href","javascript:;",null);
					writer.writeAttribute("onclick", "javascript:"+onclick, null);
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
			//writer.writeText(label, "label");
			// Don't tell anyone I did this
			writer.write(label);
		}

		// Render a popup image, if any
		writePopupImage(context, writer, tree);

		if(hasLink || alwaysRenderLinks) {
			writer.endElement("a");
			tree.markCurrentAsAction();
		}
	}

	@Override
	protected void writePopupImage(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree) throws IOException {
		if(tree.getNode().getType() == ITreeNode.NODE_CONTAINER) {
			if(tree.getDepth() == 2) {
				writer.startElement("i", null);
				writer.writeAttribute("class", "ace-icon fa fa-caret-down", null);
				writer.endElement("i");
			}
		}
	}
}
