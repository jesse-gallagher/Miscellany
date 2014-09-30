package frostillicus.wrapbootstrap.ace1_3.renderkit.outline.tree;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.extlib.renderkit.html_extended.outline.tree.HtmlListRenderer;
import com.ibm.xsp.extlib.resources.ExtLibResources;
import com.ibm.xsp.extlib.tree.ITreeNode;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.renderkit.html_extended.RenderUtil;

import frostillicus.wrapbootstrap.ace1_3.renderkit.util.IconUtil;

public class AceMenuRenderer extends HtmlListRenderer {
	public static final int TYPE_PILL	= 0;
	public static final int TYPE_LIST	= 1;

	private static final long serialVersionUID = 1L;

	protected static final int PROP_MENU_SELECTED	= 6;
	protected static final int PROP_MENU_EXPANDED	= 7;
	protected static final int PROP_MENU_COLLAPSED	= 8;

	@Override
	protected Object getProperty(final int prop) {
		switch(prop) {
		case PROP_MENU_SELECTED: 	return "active"; // $NON-NLS-1$
		case PROP_MENU_EXPANDED: 	return "fa fa-minus-sign"; // $NON-NLS-1$
		case PROP_MENU_COLLAPSED: 	return "fa fa-plus-sign"; // $NON-NLS-1$
		}
		return super.getProperty(prop);
	}

	private boolean expandable;
	private String expandEffect;
	private int expandLevel;

	public AceMenuRenderer() {
	}

	public AceMenuRenderer(final UIComponent component, final int type) {
		super(component);
	}

	public boolean isExpandable() {
		return expandable;
	}

	public void setExpandable(final boolean expandable) {
		this.expandable = expandable;
	}

	public String getExpandEffect() {
		return expandEffect;
	}

	public void setExpandEffect(final String expandEffect) {
		this.expandEffect = expandEffect;
	}

	public int getExpandLevel() {
		return expandLevel;
	}

	public void setExpandLevel(final int expandLevel) {
		this.expandLevel = expandLevel;
	}

	@Override
	protected boolean alwaysRenderItemLink(final TreeContextImpl tree, final boolean enabled, final boolean selected) {
		return true;
	}

	@Override
	protected String getContainerStyleClass(final TreeContextImpl node) {
		if(node.getDepth() > 1) {
			return "submenu";
		}
		return "nav nav-list";
	}

	@Override
	protected String getItemStyleClass(final TreeContextImpl tree, final boolean enabled, final boolean selected) {
		String clazz = null;
		if(tree.getNode().getType() == ITreeNode.NODE_SEPARATOR) {
			clazz = "divider";
		}
		if(!enabled) {
			clazz = ExtLibUtil.concatStyleClasses(clazz, "disabled");
		}
		if(selected) {
			// TODO make this look for children
			clazz = ExtLibUtil.concatStyleClasses(clazz, (String)getProperty(PROP_MENU_SELECTED));
			if(tree.getNode().getType() == ITreeNode.NODE_CONTAINER) {
				clazz = ExtLibUtil.concatStyleClasses(clazz, "open");
			}
		}
		return clazz;
	}


	@Override
	protected void preRenderTree(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree) throws IOException {
		// Add the JS support if necessary
		if(isExpandable()) {
			UIViewRootEx rootEx = (UIViewRootEx) context.getViewRoot();
			rootEx.setDojoTheme(true);
			//ExtLibResources.addEncodeResource(rootEx, BootstrapResources.bootstrapNavigator);
			// Specific dojo effects
			String effect = getExpandEffect();
			if(StringUtil.isNotEmpty(effect)) {
				rootEx.addEncodeResource(ExtLibResources.dojoFx);
				ExtLibResources.addEncodeResource(rootEx, ExtLibResources.dojoFx);
			}
		}
		super.preRenderTree(context, writer, tree);
	}

	@Override
	protected void renderEntryItemContent(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree, final boolean enabled, final boolean selected) throws IOException {
		newLine(writer);

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
				RenderUtil.writeLinkAttribute(context, writer, href);
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

		// Apparently base depth is 2, so anything below that gets arrow icons instead
		if(tree.getDepth() < 3) {
			IconUtil.renderIcon(context, writer, tree.getNode(), "menu");
		} else {
			writer.startElement("i", null);
			writer.writeAttribute("class", "menu-icon fa fa-caret-right", null);
			writer.endElement("i");
		}

		// Write the label and, if present, badge
		writer.startElement("span", null);
		writer.writeAttribute("class", "menu-text", null);

		String label = tree.getNode().getLabel();
		if(StringUtil.isNotEmpty(label)) {
			writer.writeText(label, "label");
		}

		// Check for a title and use that as a badge
		String title = tree.getNode().getTitle();
		if(StringUtil.isNotEmpty(title)) {
			writer.startElement("span", null);
			writer.writeAttribute("class", "badge badge-primary", null);
			writer.writeText(title, null);
			writer.endElement("span");
		}

		writer.endElement("span");

		// Render a popup image, if any
		writePopupImage(context, writer, tree);

		if(hasLink || alwaysRenderLinks) {
			writer.endElement("a");
			tree.markCurrentAsAction();
		}
		newLine(writer);
	}

	@Override
	protected void renderEntryItemLinkAttributes(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree, final boolean enabled, final boolean selected) throws IOException {
		boolean section = tree.getNode().getType() != ITreeNode.NODE_LEAF;
		String styleClass = tree.getNode().getStyleClass();
		if(section) {
			writer.writeAttribute("class", ExtLibUtil.concatStyleClasses("dropdown-toggle", styleClass), null);
			writer.writeAttribute("href", "#", null);
		} else if(StringUtil.isNotEmpty(styleClass)) {
			writer.writeAttribute("class", styleClass, null);
		}
	}

	@Override
	protected boolean isChildrenSeparate() {
		// We need the children to be generated in a separate <li>, else the lotusSelected class
		// applies to the entire hierarchy
		return false;
	}

	@Override
	protected void renderEntryNodeChildAttributes(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree, final boolean enabled, final boolean selected) throws IOException {
		if(tree.getNodeContext().isHidden()) {
			writer.writeAttribute("style", "display:none", null);
		}
	}

	@Override
	protected String getItemRole(final TreeContextImpl tree, final boolean enabled, final boolean selected) {
		if(tree.getNode().getType()==ITreeNode.NODE_LEAF) {
			return "menuitem";
		}
		return null;
	}

	@Override
	protected void writePopupImage(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree) throws IOException {
		boolean section = tree.getNode().getType() != ITreeNode.NODE_LEAF;
		if(section) {
			writer.startElement("b", null);
			writer.writeAttribute("class", "arrow fa fa-angle-down", null);
			writer.endElement("b");
		}
	}
}
