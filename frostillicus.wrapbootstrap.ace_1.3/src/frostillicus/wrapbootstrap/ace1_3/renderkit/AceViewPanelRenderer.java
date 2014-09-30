package frostillicus.wrapbootstrap.ace1_3.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.ajax.AjaxUtil;
import com.ibm.xsp.component.xp.XspViewPanel;
import com.ibm.xsp.component.xp.XspViewTitle;
import com.ibm.xsp.renderkit.FacesRenderer;
import com.ibm.xsp.renderkit.html_extended.RenderUtil;
//import com.ibm.xsp.renderkit.html_extended.ViewPanelRenderer;
import com.ibm.xsp.util.FacesUtil;
import com.ibm.xsp.util.JSUtil;

public class AceViewPanelRenderer extends FacesRenderer {
	@Override
	public boolean getRendersChildren() { return true; }

	@Override
	public void decode(final FacesContext facesContext, final UIComponent component) {
		super.decode(facesContext, component);
		RenderKit localRenderKit = facesContext.getRenderKit();
		Renderer localRenderer = localRenderKit.getRenderer("javax.faces.Data", "com.ibm.xsp.TableEx");

		localRenderer.decode(facesContext, component);
	}

	@Override
	public void encodeBegin(final FacesContext facesContext, final UIComponent component) throws IOException
	{
		if ((facesContext == null) || (component == null)) {
			throw new IOException();
		}
		if (!(component.isRendered())) {
			return;
		}

		XspViewPanel viewPanel = null;
		if (component instanceof XspViewPanel) {
			viewPanel = (XspViewPanel)component;
		}

		String clientId = null;
		if (viewPanel != null)
			clientId = viewPanel.getOuterTableClientId(facesContext);
		else {
			clientId = component.getClientId(facesContext);
		}

		boolean isAjaxRendering = false;
		if (AjaxUtil.isAjaxPartialRefresh(facesContext)) {
			isAjaxRendering = AjaxUtil.isRendering(facesContext);
			if(!isAjaxRendering) {
				if(AjaxUtil.getAjaxComponentId(facesContext).equals(clientId)) {
					AjaxUtil.setRendering(facesContext, true);
				}
			}
		}

		super.encodeBegin(facesContext, component);

		ResponseWriter w = facesContext.getResponseWriter();

		String role = null;
		if (viewPanel != null) {
			role = viewPanel.getRole();
		} else {
			role = (String)component.getAttributes().get("role");
		}

		// Write the Ajax refresher div
		JSUtil.writeTextln(w);
		w.startElement("div", component);
		w.writeAttribute("class", "dataTable_refresher", null);
		w.writeAttribute("id", component.getClientId(facesContext) + "_OUTER_TABLE", null);

		// Write out the caption, which is a div.table-header
		JSUtil.writeTextln(w);
		String caption;
		String captionStyleClass;
		if (viewPanel != null) {
			caption = viewPanel.getCaption();
			captionStyleClass = viewPanel.getCaptionStyleClass();
		} else {
			caption = (String)component.getAttributes().get("caption");
			captionStyleClass = (String)component.getAttributes().get("captionStyleClass");
		}
		if(StringUtil.isNotEmpty(caption)) {
			w.startElement("div", component);
			if (captionStyleClass != null) {
				w.writeAttribute("class", captionStyleClass, null);
			} else {
				w.writeAttribute("class", "table-header", null);
			}
			w.writeText(caption, null);
			w.endElement("div");
		}

		// Write the surrounding divs
		JSUtil.writeTextln(w);
		w.startElement("div", component);
		w.writeAttribute("class", "table-responsive", null);
		w.startElement("div", component);
		if (role != null)
			w.writeAttribute("role", role, null);
		else {
			w.writeAttribute("role", "grid", null);
		}

		if (viewPanel != null) {
			RenderUtil.writeAttribute(w, "style", viewPanel.getViewStyle());
			RenderUtil.writeAttribute(w, "class", "dataTables_wrapper " + viewPanel.getViewStyleClass());
		}
		else {
			RenderUtil.writeAttribute(w, "style", component.getAttributes().get("viewStyle"));
			RenderUtil.writeAttribute(w, "class", "dataTables_wrapper " + component.getAttributes().get("viewStyleClass"));
		}

		String headerStyle = getStringValue(component.getAttributes().get("headerStyle"));
		String headerStyleClass = getStringValue(component.getAttributes().get("headerStyleClass"));


		// Write header here
		UIComponent header = component.getFacet("header");
		UIComponent headerPager = component.getFacet("headerPager");
		UIComponent viewTitle = component.getFacet("viewTitle");
		writeHeaderFooter(w, facesContext, component, headerStyle, headerStyleClass, viewTitle, header, headerPager);


		// Write out the table
		JSUtil.writeTextln(w);
		RenderKit renderKit = facesContext.getRenderKit();
		Renderer dataTableRenderer = renderKit.getRenderer("javax.faces.Data", "com.ibm.xsp.TableEx");

		dataTableRenderer.encodeBegin(facesContext, component);
		dataTableRenderer.encodeChildren(facesContext, component);
		dataTableRenderer.encodeEnd(facesContext, component);

		JSUtil.writeTextln(w);


		// Write the footer here
		UIComponent footer = component.getFacet("footer");
		UIComponent footerPager = component.getFacet("footerPager");
		String footerStyle = getStringValue(component.getAttributes().get("footerStyle"));
		String footerStyleClass = getStringValue(component.getAttributes().get("footerStyleClass"));
		writeHeaderFooter(w, facesContext, component, footerStyle, footerStyleClass, null, footer, footerPager);

		w.endElement("div"); // .dataTables_wrapper
		w.endElement("div"); // .table-responsive
		w.endElement("div"); // .dataTable_refresher

		if (AjaxUtil.isAjaxPartialRefresh(facesContext))
			AjaxUtil.setRendering(facesContext, isAjaxRendering);
	}

	private void writeHeaderFooter(final ResponseWriter w, final FacesContext facesContext, final UIComponent component, final String style, final String styleClass, final UIComponent title, final UIComponent body, final UIComponent pager) throws IOException {
		JSUtil.writeTextln(w);

		// Give the title its own row
		if(title != null && title.isRendered()) {

			w.startElement("div", component);
			if(title instanceof XspViewTitle) {
				XspViewTitle viewTitle = (XspViewTitle)title;
				if(StringUtil.isNotEmpty(viewTitle.getStyleClass())) {
					w.writeAttribute("class", viewTitle.getStyleClass(), null);
				} else {
					w.writeAttribute("class", "row", null);
				}
				if(StringUtil.isNotEmpty(viewTitle.getStyle())) {
					w.writeAttribute("style", viewTitle.getStyle(), null);
				}
			} else {
				w.writeAttribute("class", "row", null);
			}
			FacesUtil.renderComponent(facesContext, title);

			w.endElement("div");
		}

		boolean renderBody = body != null && body.isRendered();
		boolean renderPager = pager != null && pager.isRendered();
		if(renderBody || renderPager) {
			w.startElement("div", component);
			if(StringUtil.isNotEmpty(styleClass)) {
				w.writeAttribute("class", styleClass, null);
			} else {
				w.writeAttribute("class", "row", null);
			}
			if(StringUtil.isNotEmpty(style)) {
				w.writeAttribute("style", style, null);
			}


			int cellWidth = !renderBody || !renderPager ? 12 : 6;

			if(renderBody) {
				w.startElement("div", component);
				w.writeAttribute("class", "col-sm-" + cellWidth, null);

				FacesUtil.renderComponent(facesContext, body);

				w.endElement("div");
			}
			if(renderPager) {
				w.startElement("div", component);
				w.writeAttribute("class", "col-sm-" + cellWidth, null);
				w.startElement("div", component);
				w.writeAttribute("class", "dataTables_paginate paging_bootstrap", null);

				FacesUtil.renderComponent(facesContext, pager);

				w.endElement("div");
				w.endElement("div");
			}

			w.endElement("div"); // .row
		}
	}

	private String getStringValue(final Object object) {
		if (object == null) {
			return null;
		}
		return object.toString();
	}

	@Override
	public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException {
		super.encodeChildren(context, component);
	}
	@Override
	public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException {
		super.encodeEnd(context, component);
	}
}
