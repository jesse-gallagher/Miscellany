package frostillicus.wrapbootstrap.ace1_3.renderkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.ajax.AjaxUtil;
import com.ibm.xsp.component.xp.XspMessages;
import com.sun.faces.renderkit.html_basic.MessagesRenderer;

public class AceMessagesRenderer extends MessagesRenderer {

	@SuppressWarnings("unchecked")
	@Override
	public void encodeEnd(final FacesContext facesContext, final UIComponent component) throws IOException {
		ResponseWriter w = facesContext.getResponseWriter();
		if(AjaxUtil.isAjaxNullResponseWriter(w)) {
			return;
		}

		XspMessages c = (XspMessages)component;
		// TODO look into whether there's a "for" to use
		String clientId = c.isGlobalOnly() ? "" : null;
		Iterator<FacesMessage> iterator = getMessageIter(facesContext, clientId, c);

		Map<Severity, List<FacesMessage>> messagesBySeverity = new HashMap<Severity, List<FacesMessage>>();

		// Drop the messages into appropriate bins
		while(iterator.hasNext()) {
			FacesMessage message = iterator.next();
			Severity severity = message.getSeverity();
			if(!messagesBySeverity.containsKey(severity)) {
				messagesBySeverity.put(severity, new ArrayList<FacesMessage>());
			}
			messagesBySeverity.get(severity).add(message);
		}

		// Loop through the bins to display related messages together
		for(Map.Entry<Severity, List<FacesMessage>> entry : messagesBySeverity.entrySet()) {
			Severity severity = entry.getKey();
			List<FacesMessage> messages = entry.getValue();

			String styleClass = "alert";
			String icon = null;
			boolean block = messages.size() > 1;
			if(severity.equals(FacesMessage.SEVERITY_ERROR) || severity.equals(FacesMessage.SEVERITY_FATAL)) {
				styleClass += " alert-danger";
				icon = "ace-icon fa fa-remove";
			} else if(severity.equals(FacesMessage.SEVERITY_INFO)) {
				styleClass += " alert-info";
			} else if(severity.equals(FacesMessage.SEVERITY_WARN)) {
				styleClass += " alert-warning";
			} else {
				styleClass += " alert-success";
				icon = "ace-icon fa fa-ok";
			}
			if(block) {
				styleClass += " alert-block";
			}

			w.startElement("div", c);
			w.writeAttribute("class", styleClass, null);

			for(FacesMessage message : messages) {

				if(block) {
					w.startElement("p", null);
				}

				String summary = message.getSummary();
				if(icon != null || StringUtil.isNotEmpty(summary)) {
					w.startElement("strong", null);
				}
				if(icon != null) {
					w.startElement("i", null);
					w.writeAttribute("class", icon, null);
					w.endElement("i");
				}
				if(StringUtil.isNotEmpty(summary)) {
					w.writeText(summary, null);
				}
				if(icon != null || StringUtil.isNotEmpty(summary)) {
					w.endElement("strong");
				}

				String detail = message.getDetail();
				if(StringUtil.isNotEmpty(detail) && !StringUtil.equals(detail, summary)) {
					w.writeText(" ", null);
					w.writeText(detail, null);
				}

				if(block) {
					w.endElement("p");
				}
			}

			w.endElement("div");

		}
	}

	@Override
	protected void encodeRecursive(final FacesContext context, final UIComponent component) throws IOException {

	}
}
