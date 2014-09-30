package frostillicus.wrapbootstrap.ace1_3.renderkit.outline.tree;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.renderkit.html_extended.outline.tree.BreadCrumbsRenderer;
import com.ibm.xsp.renderkit.html_basic.HtmlRendererUtil;
import com.ibm.xsp.renderkit.html_extended.RenderUtil;

public class AceBreadCrumbsRenderer extends BreadCrumbsRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	protected Object getProperty(final int prop) {
		switch(prop) {
		case PROP_BREADCRUMBS_CONTAINER:   	return "breadcrumb";
		case PROP_BREADCRUMBS_LABEL:   		return "";
		}
		return null;
	}

	public AceBreadCrumbsRenderer() {
	}

	public AceBreadCrumbsRenderer(final UIComponent component) {
		super(component);
	}

	@Override
	protected void renderSeparator(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree) throws IOException { }

	@Override
	protected void preRenderList(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree) throws IOException {
		newLine(writer);
		writer.startElement("ul", null);
		writer.writeAttribute("class", getProperty(PROP_BREADCRUMBS_CONTAINER), null);
		//		writer.writeAttribute("role", "navigation", null);
		newLine(writer);
	}
	@Override
	protected void postRenderList(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree) throws IOException {
		writer.endElement("ul");
	}

	@Override
	protected String getItemTag() {
		return "li";
	}
	@Override
	protected void renderEntryItemLinkAttributes(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree, final boolean enabled, final boolean selected) throws IOException {
		// Suppress the normal style="text-decoration:none"
	}
	@Override
	protected void renderEntryItemContent(final FacesContext context, final ResponseWriter writer, final TreeContextImpl tree, final boolean enabled, final boolean selected) throws IOException {
		newLine(writer);

		String image = tree.getNode().getImage();
		String imageClass = tree.getNode().getImageAlt();
		boolean hasImage = StringUtil.isNotEmpty(image);
		if(hasImage) {
			writer.startElement("img",null);
			image = HtmlRendererUtil.getImageURL(context, image);
			writer.writeAttribute("src",image,null);
			String imageAlt = tree.getNode().getImageAlt();
			if (StringUtil.isNotEmpty(imageAlt)) {
				writer.writeAttribute("alt",imageAlt,null);
			}
			String imageHeight = tree.getNode().getImageHeight();
			if (StringUtil.isNotEmpty(imageHeight)) {
				writer.writeAttribute("height",imageHeight,null);
			}
			String imageWidth = tree.getNode().getImageWidth();
			if (StringUtil.isNotEmpty(imageWidth)) {
				writer.writeAttribute("width",imageWidth,null);
			}
			if(StringUtil.isNotEmpty(imageClass)) {
				writer.writeAttribute("class", imageClass, null);
			}
			writer.endElement("img");
		} else if(StringUtil.isNotEmpty(imageClass)) {
			writer.startElement("i", null);
			writer.writeAttribute("class", imageClass, null);
			writer.endElement("i");
		}

		boolean hasLink = false;
		boolean alwaysRenderLinks = alwaysRenderItemLink(tree, enabled, selected);
		if(enabled) {
			String href = tree.getNode().getHref();
			if(StringUtil.isNotEmpty(href)) {
				newLine(writer);
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

		// Generate a regular node
		String label = tree.getNode().getLabel();
		if(StringUtil.isNotEmpty(label)) {
			writer.writeText(label, "label");
		}

		// Render a popup image, if any
		writePopupImage(context, writer, tree);

		if(hasLink || alwaysRenderLinks) {
			writer.endElement("a");
			tree.markCurrentAsAction();
		}
		newLine(writer);
	}
}