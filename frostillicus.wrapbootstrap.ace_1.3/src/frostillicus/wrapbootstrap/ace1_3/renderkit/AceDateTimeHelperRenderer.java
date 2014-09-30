package frostillicus.wrapbootstrap.ace1_3.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.xsp.component.xp.XspInputText;
import com.ibm.xsp.convert.DateTimeConverter;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.renderkit.dojo.DateTimeHelperRenderer;

public class AceDateTimeHelperRenderer extends DateTimeHelperRenderer {

	@Override
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException {


		if(component instanceof XspInputText) {
			String styleClass = ExtLibUtil.concatStyleClasses(((XspInputText)component).getStyleClass(), getPickerStyleClass(context, component));
			((XspInputText)component).setStyleClass(styleClass);
		}

		ResponseWriter w = context.getResponseWriter();
		w.startElement("div", component);
		w.writeAttribute("class", "input-group", null);

		super.encodeBegin(context, component);
	}

	@Override
	public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException {
		super.encodeEnd(context, component);

		ResponseWriter w = context.getResponseWriter();

		w.startElement("span", null);
		w.writeAttribute("class", "input-group-addon", null);
		w.startElement("i", null);
		w.writeAttribute("class", "fa " + getPickerIconStyleClass(context, component) + " bigger-110", null);
		w.endElement("i");
		w.endElement("span");


		w.endElement("div");
	}

	@Override
	protected boolean isOutputDateTimeHelper(final FacesContext context, final UIComponent component) {
		return false;
	}

	@Override
	protected void writeDateTimeAttributes(final FacesContext context, final UIComponent component, final ResponseWriter w, final String paramString) throws IOException {
		DateTimeConverter converter = (DateTimeConverter)((UIInput)component).getConverter();
		switch(PickerType.valueFor(context, component)) {
		case DATE:
			w.writeAttribute("data-date-format", convertDojoPatternForJQuery(converter.getDatePattern(context)), null);
			break;
		case TIME:
			w.writeAttribute("data-time-format", convertDojoPatternForJQuery(converter.getTimePattern(context)), null);
			break;
		case DATETIME:
			w.writeAttribute("data-date-format", convertDojoPatternForJQuery(converter.getBothDatePattern(context)), null);
			w.writeAttribute("data-time-format", convertDojoPatternForJQuery(converter.getBothTimePattern(context)), null);
			break;
		}


		super.writeDateTimeAttributes(context, component, w, paramString);
	}

	protected String convertDojoPatternForJQuery(final String pattern) {
		return pattern.replaceAll("\\bMMM\\b", "MM");
	}

	protected String getPickerStyleClass(final FacesContext context, final UIComponent component) {
		switch(PickerType.valueFor(context, component)) {
		case DATE:
			return "date-picker";
		case TIME:
			return "time-picker";
		case DATETIME:
			return "datetime-picker";
		}
		return "";
	}

	protected String getPickerIconStyleClass(final FacesContext context, final UIComponent component) {
		switch(PickerType.valueFor(context, component)) {
		case DATE:
			return "fa-calendar";
		case TIME:
			return "fa-clock-o";
		case DATETIME:
			return "fa-calendar";
		}
		return "";
	}



	protected static enum PickerType {
		DATE, TIME, DATETIME;

		public static PickerType valueFor(final FacesContext context, final UIComponent component) {
			DateTimeConverter converter = (DateTimeConverter)((UIInput)component).getConverter();

			String type = converter.getType();
			if("date".equals(type)) {
				return DATE;
			} else if("time".equals(type)) {
				return TIME;
			} else {
				return DATETIME;
			}
		}
	}
}