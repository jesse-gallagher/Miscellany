package frostillicus.wrapbootstrap.ace1_3.renderkit.data;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.component.data.UIForumPost;
import com.ibm.xsp.extlib.renderkit.html_extended.data.ForumPostRenderer;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.util.FacesUtil;

public class AceForumPostRenderer extends ForumPostRenderer {

	@Override
	protected Object getProperty(final int prop) {
		switch(prop) {
		case PROP_MAINCLASS:
			return "itemdiv dialogdiv";
		case PROP_MAINSTYLE:
			return "";
		case PROP_AUTHORCLASS:
			return "user";
		case PROP_AUTHORSTYLE:
			return "";
		case PROP_POSTCLASS:
			return "body";
		case PROP_POSTSTYLE:
			return "";
		case PROP_AUTHORNAMECLASS:
			return "name";
		case PROP_AUTHORNAMESTYLE:
			return "";
		case PROP_AUTHORMETACLASS:
			return "time";
		case PROP_AUTHORMETASTYLE:
			return "";
		case PROP_POSTDETAILSCLASS:
			return "text";
		case PROP_POSTDETAILSSTYLE:
			return "";
		}
		return super.getProperty(prop);
	}

	@Override
	protected void writeForumPost(final FacesContext context, final ResponseWriter w, final UIForumPost c) throws IOException {
		w.startElement("div", c);
		// styleClass
		String userStyleClass = c.getStyleClass();
		String defaultStyleClass = (String)getProperty(PROP_MAINCLASS);
		String styleClass = ExtLibUtil.concatStyleClasses(userStyleClass, defaultStyleClass);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null);
		}
		// style
		String userStyle = c.getStyle();
		String defaultStyle = (String)getProperty(PROP_MAINSTYLE);
		String style = ExtLibUtil.concatStyles(userStyle, defaultStyle);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null);
		}

		newLine(w);

		writeAuthor(context, w, c);
		writePost(context, w, c);

		w.endElement("div"); // div.itemdiv.dialogdiv
		newLine(w);
	}

	/**
	 * Due to the nature of the theme's structure where user info is in both "user" and "body", this opens a div that is later closed by the content. It's weird.
	 */
	@Override
	protected void writeAuthor(final FacesContext context, final ResponseWriter w, final UIForumPost c) throws IOException {
		UIComponent avatar = c.getFacet(UIForumPost.FACET_AUTHAVATAR);
		UIComponent name = c.getFacet(UIForumPost.FACET_AUTHNAME);
		UIComponent meta = c.getFacet(UIForumPost.FACET_POSTMETA); // Like I said: it's weird

		if(avatar != null) {
			w.startElement("div", null); // div.user
			String styleClass = (String)getProperty(PROP_AUTHORCLASS);
			if(StringUtil.isNotEmpty(styleClass)) {
				w.writeAttribute("class", styleClass, null);
			}
			String style = (String)getProperty(PROP_AUTHORSTYLE);
			if(StringUtil.isNotEmpty(style)) {
				w.writeAttribute("style", style, null);
			}

			writeAuthorAvatar(context, w, c, avatar);

			w.endElement("div");
			newLine(w);
		}

		w.startElement("div", null); // div.body
		String styleClass = (String)getProperty(PROP_POSTCLASS);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null);
		}
		String style = (String)getProperty(PROP_POSTSTYLE);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null);
		}

		if(meta != null) {
			writeAuthorMeta(context, w, c, meta);
		}
		if(name != null) {
			writeAuthorName(context, w, c, name);
		}
	}

	@Override
	protected void writeAuthorAvatar(final FacesContext context, final ResponseWriter w, final UIForumPost c, final UIComponent facet) throws IOException {
		FacesUtil.renderComponent(context, facet);
	}

	@Override
	protected void writeAuthorName(final FacesContext context, final ResponseWriter w, final UIForumPost c, final UIComponent facet) throws IOException {
		w.startElement("div", null); // div.name
		String styleClass = (String)getProperty(PROP_AUTHORNAMECLASS);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null);
		}
		String style = (String)getProperty(PROP_AUTHORNAMESTYLE);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null);
		}

		FacesUtil.renderComponent(context, facet);

		w.endElement("div");
	}
	@Override
	protected void writeAuthorMeta(final FacesContext context, final ResponseWriter w, final UIForumPost c, final UIComponent facet) throws IOException {
		w.startElement("div", null); // div.name
		String styleClass = (String)getProperty(PROP_AUTHORMETACLASS);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null);
		}
		String style = (String)getProperty(PROP_AUTHORMETASTYLE);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null);
		}

		FacesUtil.renderComponent(context, facet);

		w.endElement("div");
	}

	@Override
	protected void writePost(final FacesContext context, final ResponseWriter w, final UIForumPost c) throws IOException {
		UIComponent details = c.getFacet(UIForumPost.FACET_POSTDETAILS);
		if(details == null) {
			return;
		}
		writePostDetails(context, w, c, details);

		w.endElement("div"); // overarching div
	}

	@Override
	protected void writePostDetails(final FacesContext context, final ResponseWriter w, final UIForumPost c, final UIComponent facet) throws IOException {
		w.startElement("div", null); // div.name
		String styleClass = (String)getProperty(PROP_POSTDETAILSCLASS);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null);
		}
		String style = (String)getProperty(PROP_POSTDETAILSSTYLE);
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null);
		}

		FacesUtil.renderComponent(context, facet);

		w.endElement("div");
	}
}