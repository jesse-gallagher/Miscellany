// Taken near-wholesale from https://raw.github.com/OpenNTF/Bootstrap4XPages/master/eclipse/plugins/org.openntf.xsp.bootstrap/src/org/openntf/xsp/bootstrap/renderkit/html_extended/extlib/BootstrapPagerRenderer.java

package frostillicus.wrapbootstrap.ace1_3.renderkit;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.component.UIPager;
import com.ibm.xsp.component.UIPagerControl;
import com.ibm.xsp.component.xp.XspPager;
import com.ibm.xsp.component.xp.XspPagerControl;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.event.PagerEvent;
import com.ibm.xsp.util.AjaxUtilEx;
import com.ibm.xsp.util.FacesUtil;
import com.ibm.xsp.util.JavaScriptUtil;
import com.ibm.xsp.util.TypedUtil;

public class AcePagerRenderer extends Renderer {

	public static final String VAR_PAGE = "page"; //$NON-NLS-1$

	public AcePagerRenderer() {
	}

	@Override
	public void decode(final FacesContext context, final UIComponent component) {
		super.decode(context, component);

		// check that this component cause the submit
		if (decodeCausedSubmit(context, component)) {
			PagerEvent pagerEvent = new PagerEvent(component);

			String hiddenValue = FacesUtil.getHiddenFieldValue(context);
			if (StringUtil.isNotEmpty(hiddenValue)) {
				int pos = hiddenValue.lastIndexOf('_');
				if (pos > -1) {
					hiddenValue = hiddenValue.substring(pos + 1);
					if (isFirst(hiddenValue)) {
						pagerEvent.setAction(PagerEvent.ACTION_FIRST);
					} else if (isLast(hiddenValue)) {
						pagerEvent.setAction(PagerEvent.ACTION_LAST);
					} else if (isNext(hiddenValue)) {
						pagerEvent.setAction(PagerEvent.ACTION_NEXT);
					} else if (isPrevious(hiddenValue)) {
						pagerEvent.setAction(PagerEvent.ACTION_PREVIOUS);
					} else {
						try {
							int value = Integer.parseInt(hiddenValue);
							pagerEvent.setAction(PagerEvent.ACTION_GOTOPAGE);
							pagerEvent.setPage(value);
						} catch (NumberFormatException nfe) {
							return; // just don't queue the event
						}
					}
				} else {
					return;
				}
			}
			((UIPager) component).queueEvent(pagerEvent);
		}
	}
	private boolean decodeCausedSubmit(final FacesContext context, final UIComponent component) {
		String currentClientId = component.getClientId(context);
		String hiddenValue = FacesUtil.getHiddenFieldValue(context);

		if (currentClientId != null && hiddenValue != null) {
			return StringUtil.indexOfIgnoreCase(hiddenValue, currentClientId) > -1;
		}
		return false;
	}


	@Override
	public boolean getRendersChildren() {
		return true;
	}

	@Override
	public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException {
		if (context == null || component == null) {
			throw new IOException();
		}

		XspPager pager = (XspPager) component;
		UIPager.PagerState st = ((UIPager) component).createPagerState();
		if (st == null) {
			return;
		}

		ResponseWriter writer = context.getResponseWriter();

		encodePagerContent(context, writer, st, pager);
	}
	protected void encodePagerContent(final FacesContext context, final ResponseWriter w, final UIPager.PagerState st, final XspPager pager) throws IOException {
		// Compute the page that should be displayed
		int pageCount = st.getPageCount();

		int start = getStart(st, pageCount);
		int end = getEnd(st, pageCount, start);

		String pagerId = pager.getClientId(context);

		boolean RTL = false;

		w.startElement("div", null);

		String pgClass = pager.getStyleClass();
		if(StringUtil.isNotEmpty(pgClass)) {
			w.writeAttribute("class", pgClass, null);
		}

		if(StringUtil.isNotEmpty(pagerId)) {
			w.writeAttribute("id", pagerId, null);
		}
		w.startElement("ul", null);
		w.writeAttribute("class", "pagination", null);

		List<?> listControls = pager.getChildren();
		if (listControls.isEmpty()) {
			return;
		}
		Iterator<?> it = listControls.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof XspPagerControl) {
				XspPagerControl control = (XspPagerControl) obj;
				String type = control.getType();
				if (StringUtil.isNotEmpty(type)) {
					if (isFirst(type) || isNext(type) || isPrevious(type) || (isLast(type) && pager.isAlwaysCalculateLast())) {
						encodeAction(context, pager, st, w, control, type, start, end, RTL);
						continue;
					} else if (isLast(type) && !pager.isAlwaysCalculateLast()) {
						if (!st.hasMoreRows()) {
							encodeAction(context, pager, st, w, control, type, start, end, RTL);
						} else {
							w.startElement("li",null);
							w.writeAttribute("class","disabled",null);
							w.startElement("a",null);
							w.writeText(getMayBeMorePages(), null);
							w.endElement("li");
							w.endElement("a");
						}
						continue;
					} else if (type.equalsIgnoreCase(UIPagerControl.TYPE_GROUP)) {
						encodeGroup(context, pager, st, w, control, start, end);
						continue;
					} else if (type.equalsIgnoreCase(UIPagerControl.TYPE_STATUS)) {
						encodeStatus(context, st, w, pager, control, start, end);
						continue;
					} else if (isSeparator(type)) {
						encodeSeparator(context, w, control, type);
						continue;
					} else if (type.equalsIgnoreCase(UIPagerControl.TYPE_GOTO)) {
						encodeGoto();
						continue;
					}
				}
				String msg = StringUtil.format("Unknown control type {0}", type); // $NLS-PagerRenderer.Unknowncontroltype0-1$
				throw new FacesExceptionEx(msg);
			}
		}

		w.endElement("ul");
		w.endElement("div");
	}

	private void encodeAction(final FacesContext context, final XspPager pager, final UIPager.PagerState st, final ResponseWriter writer, final XspPagerControl control, final String type, final int start, final int end, final boolean RTL) throws IOException {
		String clientId = pager.getClientId(context);
		String controlId = clientId + "__" + type;

		String defaultText = "";
		boolean renderLink = true;

		if (isFirst(type)) {
			renderLink = st.getCurrentPage() > start;
			defaultText = "\u00AB"; // First
		} else if (isPrevious(type)) {
			renderLink = st.getCurrentPage() > start;
			defaultText = "\u2039";  // Previous
		} else if (isNext(type)) {
			renderLink = st.getCurrentPage() < end - 1;
			defaultText = "\u203A"; // Next
		} else if (isLast(type)) {
			renderLink = st.getCurrentPage() < end - 1;
			defaultText = "\u00BB"; // Last
		}

		writer.startElement("li", null);
		if(isPrevious(type)) {
			if(!renderLink) {
				writer.writeAttribute("class", "prev disabled", null);
			} else {
				writer.writeAttribute("class", "prev", null);
			}
		} else if(isNext(type)) {
			if(!renderLink) {
				writer.writeAttribute("class", "next disabled", null);
			} else {
				writer.writeAttribute("class", "next", null);
			}
		} else if (!renderLink) {
			writer.writeAttribute("class", "active", null);
		}

		// Generate the image link
		String val = (String) control.getValue();
		if (StringUtil.isEmpty(val)) {
			val = defaultText;
		}

		// Generate the text link
		if (StringUtil.isNotEmpty(val)) {
			writer.startElement("a", null);
			writer.writeAttribute("id", controlId+"__lnk", null);
			writer.writeAttribute("href", "#", null);

			// Next/previous get special <i> elements
			if(isPrevious(type)) {
				writer.startElement("i", null);
				writer.writeAttribute("class", "fa fa-angle-left", null);
				writer.endElement("i");
			} else if(isNext(type)) {
				writer.startElement("i", null);
				writer.writeAttribute("class", "fa fa-angle-right", null);
				writer.endElement("i");
			} else {
				writer.writeText(val, null);
			}


			writer.endElement("a");
			if (renderLink) {
				setupSubmitOnClick(context, pager, st, controlId, controlId + "__lnk");
			}
		}

		writer.endElement("li");
	}

	private void encodeGroup(final FacesContext context, final XspPager pager, final UIPager.PagerState st, final ResponseWriter writer, final XspPagerControl control, final int start, final int end) throws IOException {
		// Save the old page value
		Map<String, Object> requestMap = TypedUtil.getRequestMap(context.getExternalContext());
		Object oldPage = requestMap.get(VAR_PAGE);

		String clientId = pager.getClientId(context);
		String controlId = clientId + "__" + control.getType();//$NON-NLS-1$

		// Encode the pages
		for (int i = start; i < end; i++) {
			// Push the page number
			requestMap.put(VAR_PAGE, i + 1);
			boolean renderLink = (i != st.getCurrentPage());

			writer.startElement("li", null);
			if (!renderLink) {
				writer.writeAttribute("class", "active", null);
			}

			String val = (String) control.getValue();
			if (StringUtil.isEmpty(val)) {
				val = Integer.toString(i + 1);
			}

			// Generate the text link
			if (StringUtil.isNotEmpty(val)) {
				writer.startElement("a", control); //$NON-NLS-1$
				writer.writeAttribute("id", controlId+"__lnk__"+i, null);
				writer.writeText(val, null);
				writer.endElement("a");
				if (renderLink) {
					setupSubmitOnClick(context, pager, st, controlId + "__lnk__" + i, controlId + "__lnk__" + i); // $NON-NLS-1$
				}
			}

			writer.endElement("li");
		}

		// Encode after the pages
		if (!pager.isAlwaysCalculateLast()) {
			if (end < st.getLastPage() || st.hasMoreRows()) {
				writer.startElement("li", null);
				writer.writeAttribute("class", "disabled", null);
				writer.startElement("a", control); //$NON-NLS-1$
				writer.writeText(getMayBeMorePages(), null);
				writer.endElement("a");
				writer.endElement("li");
			}
		}

		// Restore the old page value
		if (oldPage != null) {
			requestMap.put(VAR_PAGE, oldPage);
		} else {
			requestMap.remove(VAR_PAGE);
		}

	}

	private void setupSubmitOnClick(final FacesContext context, final XspPager component, final UIPager.PagerState st, final String clientId, final String sourceId) {
		boolean immediate = false;

		UIComponent subTree = ((FacesContextEx) context).getSubTreeComponent();

		boolean partialExec = component.isPartialExecute();
		String execId = null;
		if (partialExec) {
			execId = component.getClientId(context);
			immediate = true;
		} else {
			if (subTree != null) {
				partialExec = true;
				execId = subTree.getClientId(context);
				immediate = true;
			}
		}

		boolean partialRefresh = component.isPartialRefresh();
		String refreshId = null;
		if (partialRefresh) {
			UIComponent refreshComponent = component.findSharedDataPagerParent();
			if (null == refreshComponent) {
				refreshComponent = (UIComponent) st.getDataIterator();
			}
			refreshId = AjaxUtilEx.getRefreshId(context, refreshComponent);
		} else {
			if (subTree != null) {
				partialRefresh = true;
				refreshId = subTree.getClientId(context);
			}
		}

		// call some JavaScript in xspClient.js
		final String event = "onclick"; // $NON-NLS-1$
		// Note, the onClick event is also triggered if the user tabs to the
		// image\link and presses enter (Not just when clicked with a
		// mouse).

		// When the source is clicked, put its id in the hidden field and
		// submit the form.
		StringBuilder buff = new StringBuilder();
		if (partialRefresh) {
			JavaScriptUtil.appendAttachPartialRefreshEvent(buff, clientId, sourceId, execId, event,
			                                               /* clientSideScriptName */null, immediate ? JavaScriptUtil.VALIDATION_NONE : JavaScriptUtil.VALIDATION_FULL,
			                                            		   /* refreshId */refreshId,
			                                            		   /* onstart */getOnStart(component),
			                                            		   /* oncomplete */getOnComplete(component),
			                                            		   /* onerror */getOnError(component));
		} else {
			JavaScriptUtil.appendAttachEvent(buff, clientId, sourceId, execId, event,
			                                 /* clientSideScriptName */null,
			                                 /* submit */true, immediate ? JavaScriptUtil.VALIDATION_NONE : JavaScriptUtil.VALIDATION_FULL);
		}
		String script = buff.toString();

		// Add the script block we just generated.
		JavaScriptUtil.addScriptOnLoad(script);
	}

	protected String getOnStart(final XspPager component) {
		return (String) component.getAttributes().get("onStart"); // $NON-NLS-1$
	}

	protected String getOnComplete(final XspPager component) {
		return (String) component.getAttributes().get("onComplete"); // $NON-NLS-1$
	}

	protected String getOnError(final XspPager component) {
		return (String) component.getAttributes().get("onError"); // $NON-NLS-1$
	}

	private void encodeStatus(final FacesContext context, final UIPager.PagerState st, final ResponseWriter writer, final XspPager pager, final XspPagerControl control, final int start, final int end) throws IOException {
		writer.startElement("li", null);
		writer.writeAttribute("class", "disabled",null);

		String val = (String) control.getValue();
		if (StringUtil.isEmpty(val)) {
			val = "{0}";
		}
		if (StringUtil.isNotEmpty(val) && st.getLastPage() > 0) {
			writer.startElement("a", null);
			val = StringUtil.format(val, st.getCurrentPage() + 1, st.getLastPage(), start, end);
			writer.writeText(val, null);
			writer.endElement("a");
		}

		writer.endElement("li");
	}

	private void encodeSeparator(final FacesContext context, final ResponseWriter writer, final XspPagerControl control, final String type) throws IOException {
		String val = (String) control.getValue();

		writer.startElement("li",null);

		if (StringUtil.isEmpty(val)) {
			String defaultSeparator = "|";
			if (type.equalsIgnoreCase(UIPagerControl.TYPE_SEPARATORPAGE)) {
				defaultSeparator = "Page";
			}
			val = defaultSeparator;
		}

		// Generate the text link
		if (StringUtil.isNotEmpty(val)) {
			writer.startElement("a", null);
			writer.writeText(val, null);
			writer.endElement("a");
		}

		writer.endElement("li");
	}

	private void encodeGoto() {
		// Do not exists in core XPages yet..
	}

	private int getStart(final UIPager.PagerState st, final int pageCount) {
		int start = (st.getFirst() / st.getRows()) - pageCount / 2;
		start = Math.min(Math.max(0, st.getLastPage() - pageCount), Math.max(0, start));
		return start;
	}

	private int getEnd(final UIPager.PagerState st, final int pageCount, final int start) {
		int sizeOfPageRange = Math.min(start + pageCount, st.getLastPage()) - start;
		int end = start + sizeOfPageRange;
		return end;
	}

	private boolean isFirst(final String type) {
		return (type.equalsIgnoreCase(UIPagerControl.TYPE_FIRST) || type.equalsIgnoreCase(UIPagerControl.TYPE_FIRSTARROW) || type.equalsIgnoreCase(UIPagerControl.TYPE_FIRSTIMAGE));
	}

	private boolean isNext(final String type) {
		return (type.equalsIgnoreCase(UIPagerControl.TYPE_NEXT) || type.equalsIgnoreCase(UIPagerControl.TYPE_NEXTARROW) || type.equalsIgnoreCase(UIPagerControl.TYPE_NEXTIMAGE));
	}

	private boolean isLast(final String type) {
		return (type.equalsIgnoreCase(UIPagerControl.TYPE_LAST) || type.equalsIgnoreCase(UIPagerControl.TYPE_LASTARROW) || type.equalsIgnoreCase(UIPagerControl.TYPE_LASTIMAGE));
	}

	private boolean isPrevious(final String type) {
		return (type.equalsIgnoreCase(UIPagerControl.TYPE_PREVIOUS) || type.equalsIgnoreCase(UIPagerControl.TYPE_PREVIOUSARROW) || type.equalsIgnoreCase(UIPagerControl.TYPE_PREVIOUSIMAGE));
	}

	private boolean isSeparator(final String type) {
		return (type.equalsIgnoreCase(UIPagerControl.TYPE_SEPARATOR) || type.equalsIgnoreCase(UIPagerControl.TYPE_SEPARATORPAGE));
	}

	private String getMayBeMorePages() {
		return "..."; // $NLS-PagerRenderer.MayBeMorePages-1$
	}
}