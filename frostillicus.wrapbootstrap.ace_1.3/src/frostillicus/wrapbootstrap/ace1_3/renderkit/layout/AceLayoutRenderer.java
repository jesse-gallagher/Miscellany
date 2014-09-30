// This steals heavily from https://github.com/OpenNTF/Bootstrap4XPages/blob/master/eclipse/plugins/org.openntf.xsp.bootstrap/src/org/openntf/xsp/bootstrap/renderkit/html_extended/extlib/layout/BootstrapApplicationLayoutRenderer.java

package frostillicus.wrapbootstrap.ace1_3.renderkit.layout;

import java.io.IOException;
import java.util.*;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UICallback;
import com.ibm.xsp.component.UIInputText;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.component.xp.XspCallback;
import com.ibm.xsp.component.xp.XspEventHandler;
import com.ibm.xsp.extlib.component.layout.ApplicationConfiguration;
import com.ibm.xsp.extlib.component.layout.UIApplicationLayout;
import com.ibm.xsp.extlib.component.layout.impl.BasicApplicationConfigurationImpl;
import com.ibm.xsp.extlib.component.layout.impl.SearchBar;
import com.ibm.xsp.extlib.renderkit.html_extended.FacesRendererEx;
import com.ibm.xsp.extlib.renderkit.html_extended.outline.tree.AbstractTreeRenderer;
import com.ibm.xsp.extlib.tree.ITree;
import com.ibm.xsp.extlib.tree.ITreeNode;
import com.ibm.xsp.extlib.tree.impl.TreeImpl;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.renderkit.html_basic.HtmlRendererUtil;
import com.ibm.xsp.util.FacesUtil;
import com.ibm.xsp.util.HtmlUtil;
import com.ibm.xsp.util.JSUtil;
import com.ibm.xsp.util.JavaScriptUtil;

import frostillicus.wrapbootstrap.ace1_3.renderkit.layout.tree.AceApplicationLinksRenderer;
import frostillicus.wrapbootstrap.ace1_3.renderkit.layout.tree.AcePlaceBarLinksRenderer;

public class AceLayoutRenderer extends FacesRendererEx {
	public static final boolean DEBUG = false;
	private static final String DEBUG_PREFIX = ">>>> Test Renderer >> ";

	protected void writeMainFrame(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		if(config != null && config.isMastHeader()) {
			writeMastHeader(context, w, layout, config);
		}

		// The banner (top nav bar) goes outside the main container
		if(config != null && config.isBanner()) {
			writeBanner(context, w, layout, config);
		}

		// Start main container
		newLine(w);
		w.startElement("div", layout);
		w.writeAttribute("class", "main-container", null);
		if(HtmlUtil.isUserId(layout.getId())) {
			w.writeAttribute("id", layout.getClientId(context), null);
		}
		newLine(w);

		if(config != null) {
			writeLeftColumn(context, w, layout, config);

			// Start main content
			newLine(w);
			w.startElement("div", layout);
			w.writeAttribute("class", "main-content", null);

			writeBreadCrumbBar(context, w, layout, config);

			// Start page content
			newLine(w);
			w.startElement("div", layout);
			w.writeAttribute("class", "page-content", null);

			// The title bar and place bar are cojoined
			if(config.isTitleBar() || config.isPlaceBar()) {
				writeTitleBar(context, w, layout, config);
			}

			writeMainContent(context, w, layout, config);


			// End page content
			newLine(w);
			w.endElement("div");
			w.writeComment("/.page-content");

			// End main content
			newLine(w);
			w.endElement("div");
			w.writeComment("/.main-content");

			writeFooter(context, w, layout, config);
		}

		// End main container
		newLine(w);
		w.endElement("div");
		w.writeComment("/.main-container");
		newLine(w);
	}

	protected void writeMastHeader(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		// This does not support a mast header, so ignore
	}

	protected void writeLeftColumn(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		UIComponent sidebar = layout.getLeftColumn();
		if(!isEmptyComponent(sidebar)) {
			newLine(w);
			w.startElement("div", layout);
			w.writeAttribute("id", "sidebar", null);
			w.writeAttribute("class", "sidebar responsive" + (isFixedLayout(layout) ? " sidebar-fixed" : ""), null);

			newLine(w);
			FacesUtil.renderComponent(context, sidebar);

			// Write the collapser
			newLine(w);
			w.startElement("div", layout);
			w.writeAttribute("id", "sidebar-collapse", null);
			w.writeAttribute("class", "sidebar-collapse", null);
			w.startElement("i", layout);
			w.writeAttribute("class", "ace-icon fa fa-double-angle-left", null);
			w.writeAttribute("data-icon2", "ace-icon fa fa-double-angle-right", null);
			w.writeAttribute("data-icon1", "ace-icon fa fa-double-angle-left", null);
			w.endElement("i");
			w.endElement("div");

			w.startElement("script", layout);
			w.writeAttribute("type", "text/javascript", null);
			w.writeText("try{ace.settings.check('sidebar', 'collapsed')}catch(e){}", null);
			w.endElement("script");

			w.endElement("div");
			newLine(w);
		}
	}

	protected void writeBanner(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		// Start navbar
		newLine(w);
		w.startElement("div", layout);
		w.writeAttribute("class", "navbar navbar-default" + (isFixedLayout(layout) ? " navbar-fixed-top" : ""), null);
		w.writeAttribute("id", "navbar", null);
		newLine(w);
		w.startElement("div", layout);
		w.writeAttribute("class", "navbar-container", null);
		w.writeAttribute("id", "navbar-container", null);

		// Sidebar toggle button
		w.startElement("button", null);
		w.writeAttribute("class", "navbar-toggle menu-toggler pull-left", null);
		w.writeAttribute("id", "menu-toggler", null);

		w.startElement("span", null);
		w.writeAttribute("class", "sr-only", null);
		w.writeText("Toggle sidebar", null);
		w.endElement("span");

		for(int i = 0; i < 3; i++) {
			w.startElement("span", null);
			w.writeAttribute("class", "icon-bar", null);
			w.endElement("span");
		}

		w.endElement("button");

		// Write the actual banner content and links
		writeBannerContent(context, w, layout, config);

		// End navbar
		newLine(w);
		w.endElement("div");
		w.writeComment("/.navbar-container");
		newLine(w);
		w.endElement("div");
		w.writeComment("/.navbar");
	}

	protected void writeBannerContent(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		writeBannerProductLogo(context, w, layout, config);
		writeBannerLink(context, w, layout, config);
		writeBannerUtilityLinks(context, w, layout, config);
	}

	protected void writeBannerProductLogo(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		newLine(w);
		w.startElement("div", layout);
		w.writeAttribute("class", "navbar-header pull-left", null);

		newLine(w);
		w.startElement("a", layout);
		w.writeURIAttribute("href", HtmlRendererUtil.getImageURL(context, "/"), null);
		w.writeAttribute("class", "navbar-brand", null);
		// Add the style here
		String style = config.getProductLogoStyle();
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null);
		}

		newLine(w);
		w.startElement("small", layout);

		String clazz = config.getProductLogoClass();
		String logoAlt = config.getProductLogoAlt();
		// If there's a specified image, use that - otherwise, use i with the class
		String logoImg = config.getProductLogo();
		if(StringUtil.isNotEmpty(logoImg)) {
			String imgSrc = HtmlRendererUtil.getImageURL(context, logoImg);
			w.startElement("img", layout);
			w.writeURIAttribute("src", imgSrc, null);
			if(StringUtil.isNotEmpty(clazz)) {
				w.writeAttribute("class", clazz, null);
			}

			w.writeAttribute("alt", logoAlt == null ? "Application Logo" : logoAlt, null);

			String width = config.getProductLogoWidth();
			if(StringUtil.isNotEmpty(width)) {
				w.writeAttribute("width", width, null);
			} else {
				w.writeAttribute("width", "23", null);
			}
			String height = config.getProductLogoHeight();
			if(StringUtil.isNotEmpty(height)) {
				w.writeAttribute("height", height, null);
			} else {
				w.writeAttribute("height", "23", null);
			}

			w.endElement("img");
			newLine(w);
		} else if(StringUtil.isNotEmpty(clazz)) {
			w.startElement("i", layout);
			w.writeAttribute("class", clazz, null);

			w.endElement("i");
			newLine(w);
		}

		// Use the alt text from the image for the app name
		if(StringUtil.isNotEmpty(logoAlt)) {
			w.writeText(logoAlt, null);
		}

		newLine(w);
		w.endElement("small");

		newLine(w);
		w.endElement("a");
		w.writeComment("/.navbar-brand");

		newLine(w);
		w.endElement("div");
		w.writeComment("/.navbar-header.pull-left");

		newLine(w);
	}
	protected void writeBannerLink(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		// Not supported - noop
	}
	protected void writeBannerUtilityLinks(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		ITree tree = TreeImpl.get(config.getBannerUtilityLinks());
		if(tree != null) {
			newLine(w);
			w.startElement("div", layout);
			w.writeAttribute("class", "navbar-buttons navbar-header pull-right", null);
			w.writeAttribute("role", "navigation", null);

			newLine(w);
			AbstractTreeRenderer renderer = new AceApplicationLinksRenderer();
			renderer.render(context, layout, "al", tree, w);

			newLine(w);
			w.endElement("div");
			w.writeComment("/.navbar-header.pull-right");
		}
	}

	protected void writeBreadCrumbBar(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		// This should render if breadcrumbs or search are present
		SearchBar searchBar = config.getSearchBar();
		UIComponent anyOldSearchBar = layout.getFacet("SearchBar");
		UIComponent breadCrumbs = layout.getFacet("BreadCrumbs");

		boolean rendered = searchBar != null || anyOldSearchBar != null || breadCrumbs != null;
		if(rendered) {
			newLine(w);
			w.startElement("div", layout);
			w.writeAttribute("class", "breadcrumbs" + (isFixedLayout(layout) ? " breadcrumbs-fixed" : ""), null);
			w.writeAttribute("id", "breadcrumbs", null);

			if(breadCrumbs != null) {
				writeBreadCrumbs(context, w, layout, config, breadCrumbs);
			}

			if(anyOldSearchBar != null) {
				writeAnyOldSearchBar(context, w, layout, config, anyOldSearchBar);
			} else if(searchBar != null) {
				writeSearchBar(context, w, layout, config, searchBar);
			}

			newLine(w);
			w.endElement("div");
			w.writeComment("/.breadcrumbs");
		}
	}
	protected void writeBreadCrumbs(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config, final UIComponent breadCrumbs) throws IOException {
		if(!isEmptyComponent(breadCrumbs)) {
			FacesUtil.renderComponent(context, breadCrumbs);
		}
	}
	protected void writeSearchBar(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config, final SearchBar searchBar) throws IOException {
		newLine(w);
		w.startElement("div", layout);
		w.writeAttribute("class", "nav-search", null);
		w.writeAttribute("id", "nav-search", null);

		newLine(w);
		w.startElement("div", layout);
		w.writeAttribute("class", "form-search", null);

		boolean searchOptions = false;
		ITree tree = TreeImpl.get(searchBar.getOptions());
		if(tree!=null) {
			searchOptions = true;
		}
		writeSearchBox(context, w, layout, config, searchOptions, searchBar);

		newLine(w);
		w.endElement("div");
		w.writeComment("/.form-search");
		newLine(w);
		w.endElement("div");
		w.writeComment("/.nav-search");
	}
	protected void writeSearchBox(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config, final boolean options, final SearchBar searchBar) throws IOException {
		String clientId = layout.getClientId(context) + "_search";

		w.startElement("span", layout);
		w.writeAttribute("class", "input-icon", null);

		// The input box
		w.startElement("input", layout);
		w.writeAttribute("type", "search", null);
		w.writeAttribute("class", "nav-search-input", null);
		w.writeAttribute("id", clientId, null);
		w.writeAttribute("autocomplete", "off", null);
		w.writeAttribute("name", clientId, null);

		String placeholder = searchBar.getInactiveText();
		if(StringUtil.isNotEmpty(placeholder)) {
			w.writeAttribute("placeholder", placeholder, null);
		}
		String title = searchBar.getInputTitle();
		if(StringUtil.isNotEmpty(title)) {
			w.writeAttribute("title", title, null);
		}

		String submitSearch = "_xspAppSearchSubmit";
		w.writeAttribute("onkeypress", "javascript:var kc=event.keyCode?event.keyCode:event.which;if(kc==13){"+submitSearch+"(); return false}", null);

		w.endElement("input");

		w.startElement("i", layout);
		w.writeAttribute("class", "ace-icon fa fa-search nav-search-icon", null);
		w.endElement("i");

		// The assistance script
		String searchPageName = searchBar.getPageName();
		if( StringUtil.isEmpty(searchPageName) ){
			searchPageName = "/";
		}else{
			// append .xsp if needed
			searchPageName = ExtLibUtil.getPageXspUrl(searchPageName);
		}
		// "/apps/XPagesExt.nsf/search.xsp"
		String path = context.getApplication().getViewHandler().getResourceURL(context, searchPageName);
		path = context.getExternalContext().encodeActionURL(path);

		// Compose the script function
		w.startElement("script", layout);
		if(DEBUG) { newLine(w); }
		StringBuilder sb = new StringBuilder();
		sb.append("function ");
		sb.append(submitSearch);
		sb.append("(){");
		if(DEBUG) { sb.append('\n'); }
		//sb.append("var val=XSP.getElementById('"); sb.append(cid); sb.append("').value;");
		sb.append("var val=XSP.getFieldValue(XSP.getElementById("); // $NON-NLS-1$
		JavaScriptUtil.addString(sb, clientId);
		sb.append("));"); // $NON-NLS-1$
		if(DEBUG) { sb.append('\n'); }
		if(options) {
			String oid = layout.getClientId(context)+"_searchopt"; // $NON-NLS-1$
			sb.append("var opt=XSP.getFieldValue(XSP.getElementById("); // $NON-NLS-1$
			JavaScriptUtil.addString(sb, oid);
			sb.append("));"); // $NON-NLS-1$
			if(DEBUG) { sb.append('\n'); }
		}
		sb.append("if(val){var loc="); // $NON-NLS-1$
		StringBuilder locStart = new StringBuilder();
		locStart.append(path).append("?");
		String queryParam = searchBar.getQueryParam();
		if(StringUtil.isEmpty(queryParam)) {
			queryParam = "search"; // $NON-NLS-1$
		}
		locStart.append(queryParam).append("="); // $NON-NLS-1$
		JSUtil.addString(sb, locStart.toString());
		sb.append("+encodeURIComponent(val)"); // $NON-NLS-1$
		if(options) {
			sb.append("+"); // $NON-NLS-1$
			StringBuilder optionKeyValue = new StringBuilder();
			optionKeyValue.append("&"); // $NON-NLS-1$
			String optionsParam = searchBar.getOptionsParam();
			if(StringUtil.isEmpty(optionsParam)) {
				optionsParam = "option"; // $NON-NLS-1$
			}
			optionKeyValue.append(optionsParam);
			optionKeyValue.append("="); // $NON-NLS-1$
			JSUtil.addString(sb, optionKeyValue.toString());
			sb.append("+encodeURIComponent(opt)"); // $NON-NLS-1$
		}
		sb.append(";");
		if(DEBUG) { sb.append('\n'); }
		sb.append("window.location.href=loc;}}"); // $NON-NLS-1$
		w.writeText(sb.toString(),null);
		if(DEBUG) { newLine(w); }

		w.endElement("script"); // $NON-NLS-1$

		w.endElement("span");
	}
	protected void writeAnyOldSearchBar(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config, final UIComponent anyOldSearchBar) throws IOException {
		if(!isEmptyComponent(anyOldSearchBar)) {
			UIComponent searchComponent = anyOldSearchBar;
			if(searchComponent instanceof XspCallback) {
				searchComponent = (UIComponent)((XspCallback)searchComponent).getChildren().get(0);
			}

			newLine(w);
			w.startElement("div", layout);
			w.writeAttribute("class", "nav-search", null);
			w.writeAttribute("id", "nav-search", null);

			newLine(w);
			w.startElement("div", layout);
			w.writeAttribute("class", "form-search", null);

			// Unwrap callbacks

			if(searchComponent instanceof UIInputText) {
				// If we're passed an input control directly, make some assumptions
				w.startElement("span", layout);
				w.writeAttribute("class", "input-icon", null);
				FacesUtil.renderComponent(context, searchComponent);
				w.startElement("i", layout);
				w.writeAttribute("class", "ace-icon fa fa-search nav-search-icon", null);
				w.endElement("i");
				w.endElement("span");
			} else {
				// Otherwise, let the control handle the doodads
				FacesUtil.renderComponent(context, searchComponent);
			}


			newLine(w);
			w.endElement("div");
			w.writeComment("/.form-search");
			newLine(w);
			w.endElement("div");
			w.writeComment("/.nav-search");
		}
	}

	protected void writeTitleBar(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		List<ITreeNode> placeBarActions = config.getPlaceBarActions();
		String title = config.getTitleBarName();
		String subtitle = config.getPlaceBarName();

		if(!(placeBarActions == null || placeBarActions.isEmpty()) || StringUtil.isNotEmpty(title) || StringUtil.isNotEmpty(subtitle)) {
			w.startElement("div", layout);
			w.writeAttribute("class", "page-header", null);

			// Write out any placebar actions
			ITree tree = TreeImpl.get(placeBarActions);
			if(tree != null) {
				newLine(w);
				w.startElement("div", layout);
				w.writeAttribute("class", "pull-right", null);

				newLine(w);
				AbstractTreeRenderer renderer = new AcePlaceBarLinksRenderer();
				renderer.render(context, layout, "al", tree, w);

				newLine(w);
				w.endElement("div");
			}

			// Write out the titles
			if(StringUtil.isNotEmpty(title) || StringUtil.isNotEmpty(subtitle)) {
				w.startElement("h1", layout);
				if(StringUtil.isNotEmpty(title)) {
					w.writeText(title, null);
				}

				if(StringUtil.isNotEmpty(subtitle)) {
					newLine(w);
					w.startElement("small", null);
					newLine(w);
					w.startElement("i", null);
					w.writeAttribute("class", "ace-icon fa fa-double-angle-right", null);
					w.endElement("i");
					newLine(w);
					w.writeText(subtitle, null);
					w.endElement("small");
				}

				w.endElement("h1");
			}

			w.endElement("div");
		}
	}

	protected void writeMainContent(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		newLine(w);
		w.startElement("div", layout);
		w.writeAttribute("class", "row", null);
		newLine(w);
		w.startElement("div", layout);
		w.writeAttribute("class", "col-xs-12", null);

		newLine(w);
		newLine(w);
		renderChildren(context, layout);

		newLine(w);
		newLine(w);
		w.endElement("div");
		w.writeComment("/.col-xs-12");
		newLine(w);
		w.endElement("div");
		w.writeComment("/.row");
	}


	protected void writeFooter(final FacesContext context, final ResponseWriter w, final UIApplicationLayout layout, final BasicApplicationConfigurationImpl config) throws IOException {
		UIComponent footer = layout.getMastFooter();
		if(!isEmptyComponent(footer)) {
			w.startElement("div", null);
			w.writeAttribute("class", "footer", null);
			newLine(w);

			w.startElement("div", null);
			w.writeAttribute("class", "footer-inner", null);

			w.startElement("div", null);
			w.writeAttribute("class", "footer-content", null);

			FacesUtil.renderComponent(context, footer);

			w.endElement("div"); // div.footer-content

			w.endElement("div"); // div.footer-inner

			w.endElement("div"); // div.footer
		}
	}


	/* ************************************************************************************************
	 * Rendering utilities
	 **************************************************************************************************/

	// See the above URL for why this is here
	protected void renderChildren(final FacesContext context, final UIComponent component) throws IOException {
		// encode component and children
		int count = component.getChildCount();
		if (count > 0) {
			List<?> children = component.getChildren();
			for (int i = 0; i < count; i++) {
				UIComponent child = (UIComponent) children.get(i);
				if (isRenderChild(context, child)) {
					FacesUtil.renderComponent(context, child);
				}
			}
		}
	}
	protected boolean isRenderChild(final FacesContext context, final UIComponent child) throws IOException {
		// Only render the non event handler components
		if (!(child instanceof XspEventHandler)) {
			return true;
		}
		return false;
	}


	/* ************************************************************************************************
	 * JSF basics
	 **************************************************************************************************/
	@Override
	public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException { }

	@Override
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException {
		ResponseWriter w = context.getResponseWriter();

		UIApplicationLayout layout = (UIApplicationLayout)component;
		if(!layout.isRendered()) { return; }

		ApplicationConfiguration conf = layout.findConfiguration();
		if(!(conf instanceof BasicApplicationConfigurationImpl)) { return; }

		BasicApplicationConfigurationImpl config = (BasicApplicationConfigurationImpl)conf;

		writeMainFrame(context, w, layout, config);
	}

	@Override
	public boolean getRendersChildren() { return true; }
	protected boolean isEmptyComponent(final UIComponent c) {
		// If the component is null, then it is considered as empty
		if (c == null) {
			return true;
		}
		// If it is not rendered, then it is empty as well
		if (!c.isRendered()) {
			return true;
		}
		// Else, if it is a UICallback, then we should check it content
		// a UICallback without anything in it should be considered as
		// and empty component.
		if (c instanceof UICallback) {
			if (c.getChildCount() > 0) {
				for (Object child : c.getChildren()) {
					if (!isEmptyComponent((UIComponent) child)) {
						return false;
					}
				}
			}
			if (c.getFacetCount() > 0) {
				for (Object child : c.getFacets().values()) {
					if (!isEmptyComponent((UIComponent) child)) {
						return false;
					}
				}
			}
			return true;
		}
		// Ok, the component exists so it is not considered as empty
		return false;
	}

	protected boolean isFixedLayout(final UIComponent layout) {
		UIViewRootEx2 view = (UIViewRootEx2)FacesUtil.getViewRoot(layout);
		String styleClass = view.getStyleClass();
		if(StringUtil.isNotEmpty(styleClass) && styleClass.contains("fixed-layout")) {
			return true;
		}
		return false;
	}


	/* ************************************************************************************************
	 * Misc. internals
	 **************************************************************************************************/

	public static void debug(final Object text) {
		if(DEBUG) {
			System.out.println(DEBUG_PREFIX + text);
		}
	}
}
