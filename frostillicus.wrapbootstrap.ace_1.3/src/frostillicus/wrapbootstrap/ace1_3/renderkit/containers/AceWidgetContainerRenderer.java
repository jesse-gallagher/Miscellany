package frostillicus.wrapbootstrap.ace1_3.renderkit.containers;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.component.containers.UIWidgetContainer;
import com.ibm.xsp.extlib.renderkit.html_extended.containers.WidgetContainerRenderer;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.util.FacesUtil;
import com.ibm.xsp.util.JSUtil;

public class AceWidgetContainerRenderer extends WidgetContainerRenderer {
	public AceWidgetContainerRenderer() {

	}

	@Override
	protected Object getProperty(final int prop) {
		switch(prop) {
		case PROP_CONTAINER_STYLE_DEFAULT:
			return "";
		case PROP_CSSWIDGETSIDEBAR:
			return "widget-box transparent";
		case PROP_CSSWIDGETBASIC:
		case PROP_CSSWIDGETPLAIN:
			return "widget-box";

		case PROP_CSSTITLEBAR:
			return "widget-header widget-header-small";
		case PROP_TAGTITLETEXT:
			return "h5";
		case PROP_CSSTITLETEXT:
			return "widget-title";
		case PROP_STYLETITLEBAR:
			return "";

		case PROP_CSSBODY:
			return "widget-body";
		}
		return super.getProperty(prop);
	}

	@Override
	protected void writeMainFrame(final FacesContext facesContext, final ResponseWriter w, final UIWidgetContainer c) throws IOException {
		String id = c.getClientId(facesContext);

		// Start the main frame
		w.startElement("div",c); // $NON-NLS-1$
		w.writeAttribute("id",id,null); // $NON-NLS-1$

		w.writeAttribute("role", "region", null); // $NON-NLS-1$ $NON-NLS-2$
		if (c.isTitleBar()) {
			w.writeAttribute("aria-labelledby", id + "_title", null); // $NON-NLS-1$
		}

		boolean closed = c.isClosed();
		w.writeAttribute("aria-expanded", Boolean.toString(!closed), null); // $NON-NLS-1$

		String type = c.getType();
		String cls = c.getStyleClass();
		// Strip out any widget-header-specific styles
		if(StringUtil.isNotEmpty(cls)) {
			cls = cls.replaceAll("\\bwidget-header-\\w+", "");
		}
		if(StringUtil.equals(type, UIWidgetContainer.TYPE_SIDEBAR)) {
			cls = ExtLibUtil.concatStyleClasses((String)getProperty(PROP_CSSWIDGETSIDEBAR), cls);
		} else if(StringUtil.equals(type, UIWidgetContainer.TYPE_PLAIN)) {
			cls = ExtLibUtil.concatStyleClasses((String)getProperty(PROP_CSSWIDGETPLAIN), cls);
		} else {
			cls = ExtLibUtil.concatStyleClasses((String)getProperty(PROP_CSSWIDGETBASIC), cls);
		}


		if(StringUtil.isNotEmpty(cls)) {
			w.writeAttribute("class",cls,null); // $NON-NLS-1$
		}
		String style = c.getStyle();
		if( StringUtil.isEmpty(style) ){
			// note, this default style is not concat'd
			style = (String) getProperty(PROP_CONTAINER_STYLE_DEFAULT);
		}
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style",style,null); // $NON-NLS-1$
		}
		newLine(w);


		// Write out some stub open/close markers that XSP's scripts look for
		w.startElement("div", c);
		w.writeAttribute("style", "display: none", null);
		w.startElement("div", c);
		w.writeAttribute("id", id + "_open", null);
		w.endElement("div");
		w.startElement("div", c);
		w.writeAttribute("id", id + "_lk_open", null);
		w.endElement("div");
		w.startElement("div", c);
		w.writeAttribute("id", id + "_close", null);
		w.endElement("div");
		w.startElement("div", c);
		w.writeAttribute("id", id + "_lk_close", null);
		w.endElement("div");
		w.endElement("div");

		// Write the title bar
		if(c.isTitleBar()) {
			writeTitleBar(facesContext, w, c);
		}

		// Write the widget content (the children)
		writeContent(facesContext, w, c);

		// Close the main frame
		w.endElement("div"); // $NON-NLS-1$
		newLine(w);
	}

	@Override
	protected void writeTitleBar(final FacesContext facesContext, final ResponseWriter w, final UIWidgetContainer c) throws IOException {
		String tag = (String)getProperty(PROP_TAGTITLE);
		w.startElement(tag,c);

		String id = c.getClientId(facesContext);
		w.writeAttribute("id", id + "_title", null);

		String headerStyleClass = (String)getProperty(PROP_CSSTITLEBAR);

		// There's only one styleClass, so we have to abuse it
		// Find any widget-header-* classes
		String styleClass = c.getStyleClass();
		if(StringUtil.isNotEmpty(styleClass)) {
			Pattern pattern = Pattern.compile("\\bwidget-header-\\w+\\b");
			Matcher matcher = pattern.matcher(styleClass);
			while(matcher.find()) {
				headerStyleClass = ExtLibUtil.concatStyleClasses(headerStyleClass, matcher.group());
			}
		}

		if(StringUtil.isNotEmpty(headerStyleClass)) {
			w.writeAttribute("class", headerStyleClass, null);
		}
		String style = (String)getProperty(PROP_STYLETITLEBAR);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null);
		}

		// Write the title
		writeTitle(facesContext, w, c);

		// Write the collapsible arrow
		writeCollapsible(facesContext, w, c);

		// Write the dropdown menu
		if(c.isDropDownRendered()) {
			writeDropDown(facesContext, w, c);
		}

		w.endElement(tag);

		writeCollapsibleInput(facesContext, w, c);
	}

	@Override
	protected void writeBodyContent(final FacesContext facesContext, final ResponseWriter w, final UIWidgetContainer c) throws IOException {

		// Since there's only one styleClass, we have to store lots in there
		// In this area, search for no-padding and use it if present
		w.startElement("div", c);
		String styleClass = c.getStyleClass();
		if(styleClass != null && styleClass.contains("no-padding")) {
			w.writeAttribute("class", "widget-main no-padding", null);
		} else {
			w.writeAttribute("class", "widget-main", null);
		}

		FacesUtil.renderChildren(facesContext, c);

		if( c.getChildCount() == 0 ){
			boolean isBodyPreventBlank = (Boolean) getProperty(PROP_BODY_PREVENT_BLANK);
			if( isBodyPreventBlank ){
				JSUtil.writeTextBlank(w); // &nbsp;
			}
		}

		w.endElement("div");
	}

	@Override
	protected void writeCollapsible(final FacesContext facesContext, final ResponseWriter w, final UIWidgetContainer c) throws IOException {
		if(c.isCollapsible()) {
			newLine(w);
			w.startElement("div", null);
			w.writeAttribute("class", "widget-toolbar", null);
			w.startElement("a", null);
			w.writeAttribute("href", "#", null);
			w.writeAttribute("data-action", "collapse", null);
			w.startElement("i", null);
			w.writeAttribute("class", "ace-icon fa fa-chevron-up", null);
			w.endElement("i");
			w.endElement("a");

			w.endElement("div");
		}
	}
}
