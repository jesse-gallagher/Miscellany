package frostillicus.wrapbootstrap.ace1_3.renderkit.data;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.component.data.FormLayout;
import com.ibm.xsp.extlib.component.data.UIFormLayoutRow;
import com.ibm.xsp.extlib.renderkit.html_extended.data.FormTableRenderer;
//import com.ibm.xsp.extlib.renderkit.html_extended.data.FormLayoutRenderer;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class AceFormTableRenderer extends FormTableRenderer {

	@Override
	protected Object getProperty(final int prop) {
		switch(prop) {
		case PROP_TAGFORMTITLE:
			return "h4";
		case PROP_STYLEFORMTITLE:
			return "";
		case PROP_STYLECLASSFORMTITLE:
			return "header";
		}
		return super.getProperty(prop);
	}

	@Override
	protected void writeFormLayout(final FacesContext facesContext, final ResponseWriter w, final FormLayout c) throws IOException {
		ComputedFormData formData = createFormData(facesContext, c);

		newLine(w);

		writeHeader(facesContext, w, c);

		String layoutPosition = c.getLabelPosition();
		String styleClass = c.getStyleClass();
		if(!"above".equalsIgnoreCase(layoutPosition)) {
			styleClass = ExtLibUtil.concatStyleClasses(styleClass, "form-horizontal");
		}
		String style = c.getStyle();

		w.startElement("div", c);
		w.writeAttribute("id", c.getClientId(facesContext), null);
		if(StringUtil.isNotEmpty(styleClass)) {
			w.writeAttribute("class", styleClass, null);
		}
		if(StringUtil.isNotEmpty(style)) {
			w.writeAttribute("style", style, null);
		}

		//		writeErrorSummary(facesContext, w, c);

		writeForm(facesContext, w, c, formData);

		// Assume the footer is the action bar
		writeFooter(facesContext, w, c);

		w.endElement("div"); // Main form div
	}

	@Override
	protected void writeFormRowError(final FacesContext facesContext, final ResponseWriter w, final FormLayout c, final UIFormLayoutRow row, final UIInput input, final FacesMessage msg, final ComputedRowData data) throws IOException {
		//		w.startElement("div", row);
		//		String className = "";
		//		if(msg.getSeverity().equals(FacesMessage.SEVERITY_ERROR)) {
		//			className = "alert-danger";
		//		} else if(msg.getSeverity().equals(FacesMessage.SEVERITY_FATAL)) {
		//			className = "alert-danger";
		//		} else if(msg.getSeverity().equals(FacesMessage.SEVERITY_INFO)) {
		//			className = "alert-info";
		//		} else if(msg.getSeverity().equals(FacesMessage.SEVERITY_WARN)) {
		//			className = "alert-warning";
		//		}
		//		w.writeAttribute("class", "alert " + className, null);
		//		w.writeText(msg.getSummary(), null);
		//
		//		w.endElement("div");
	}

	protected void writeErrorSummary(final FacesContext facesContext, final ResponseWriter w, final FormLayout c) throws IOException {
		if(!c.isDisableErrorSummary()) {
			Iterator<FacesMessage> msg = getMessages(facesContext, c.getClientId(facesContext));
			if(msg.hasNext()) {
				w.startElement("div", c);
				String errorStyleClass = "alert alert-danger";
				String style = (String)getProperty(PROP_STYLEERRORSUMMARY);
				if(StringUtil.isNotEmpty(style)) {
					w.writeAttribute("style", style, null);
				}
				errorStyleClass = ExtLibUtil.concatStyleClasses(errorStyleClass, (String)getProperty(PROP_STYLECLASSERRORSUMMARY));
				if(StringUtil.isNotEmpty(errorStyleClass)) {
					w.writeAttribute("class", errorStyleClass, null);
				}

				writeErrorSummaryContent(facesContext, w, c, msg);
				w.endElement("div");
			}
		}
	}
	@SuppressWarnings("unchecked")
	protected static Iterator<FacesMessage> getMessages(final FacesContext context) {
		return context.getMessages();
	}

	@Override
	protected void writeFooterFacet(final FacesContext facesContext, final ResponseWriter w, final FormLayout c, final UIComponent footer) throws IOException {
		newLine(w);
		w.startElement("div", c);
		w.writeAttribute("class", "form-actions", null);

		writeFooterFacetContent(facesContext, w, c, footer);

		w.endElement("div");
	}

	@Override
	protected void writeMultiColumnRows(final FacesContext facesContext, final ResponseWriter w, final FormLayout c, final UIComponent parent, final ComputedFormData formData) throws IOException {
		super.writeMultiColumnRows(facesContext, w, c, parent, formData);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void writeFormRowData(final FacesContext facesContext, final ResponseWriter w, final FormLayout c, final ComputedFormData formData,
			final UIFormLayoutRow row, final UIInput edit, final ComputedRowData rowData) throws IOException {

		newLine(w);

		boolean hasError = false;
		String editClientId = null;
		Iterator<FacesMessage> messages = null;
		if(edit != null) {
			editClientId = edit.getClientId(facesContext);
			messages = facesContext.getMessages(editClientId);
			hasError = messages.hasNext();
		}

		w.startElement("div", null);
		w.writeAttribute("id", row.getClientId(facesContext), null);
		String labelPosition = c.getLabelPosition();
		w.writeAttribute("class", "form-group" + (hasError ? " has-error" : ""), null);

		if(rowData.hasLabel()) {
			w.startElement("label", null);
			String labelStyleClass = "control-label";
			if(!"above".equalsIgnoreCase(labelPosition)) {
				labelStyleClass = ExtLibUtil.concatStyleClasses(labelStyleClass, "col-sm-3");
			}
			w.writeAttribute("class", labelStyleClass, null);

			String labelWidth = row.getLabelWidth();
			if(StringUtil.isNotEmpty(labelWidth)) {
				w.writeAttribute("style", labelWidth, null);
			}

			if(edit != null) {
				w.writeAttribute("for", edit.getClientId(facesContext), null);
			}

			String label = row.getLabel();
			if(StringUtil.isNotEmpty(label)) {
				w.writeText(row.getLabel(), null);
			}

			w.endElement("label");
		}

		if(!"above".equalsIgnoreCase(labelPosition)) {
			w.startElement("div", null);
			w.writeAttribute("class", "col-sm-9", null);
		}

		writeFormRowDataField(facesContext, w, c, row, edit);

		if(edit != null) {
			while(messages.hasNext()) {
				FacesMessage message = messages.next();
				w.startElement("div", null);
				w.writeAttribute("class", "help-block", null);
				w.writeAttribute("for", editClientId, null);

				w.writeText(message.getDetail(), null);

				w.endElement("div");
			}
		}

		if(!"above".equalsIgnoreCase(labelPosition)) {
			w.endElement("div"); // field wrapper
		}
		w.endElement("div"); // row
	}
}
