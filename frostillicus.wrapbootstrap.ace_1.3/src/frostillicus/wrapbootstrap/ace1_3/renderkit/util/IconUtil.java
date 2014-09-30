package frostillicus.wrapbootstrap.ace1_3.renderkit.util;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.tree.ITreeNode;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.renderkit.html_basic.HtmlRendererUtil;

public enum IconUtil {
	;

	public static void renderIcon(final FacesContext context, final ResponseWriter writer, final ITreeNode node, final String iconType) throws IOException {
		String image = node.getImage();
		if(StringUtil.isNotEmpty(image)) {
			// Assume any image URL just consisting of letters, numbers, and - is a Bootstrap icon
			boolean bootstrapIcon = Pattern.matches("^(\\w|\\d|-)+$", image);
			if(!bootstrapIcon) {
				writer.startElement("img",null);
				image = HtmlRendererUtil.getImageURL(context, image);
				writer.writeAttribute("src",image,null);
				String imageAlt = node.getImageAlt();
				if (StringUtil.isNotEmpty(imageAlt)) {
					writer.writeAttribute("alt", imageAlt, null);
					writer.writeAttribute("class", imageAlt, null);
				}
				String imageHeight = node.getImageHeight();
				if (StringUtil.isNotEmpty(imageHeight)) {
					writer.writeAttribute("height", imageHeight, null);
				}
				String imageWidth = node.getImageWidth();
				if (StringUtil.isNotEmpty(imageWidth)) {
					writer.writeAttribute("width", imageWidth, null);
				}
				writer.endElement("img");
			} else {
				writer.startElement("i", null);
				String imageClass;
				if(image.contains("glyphicon")) {
					imageClass = ExtLibUtil.concatStyleClasses(iconType + "-icon", image);
				} else if(image.contains("fa-")) {
					imageClass = ExtLibUtil.concatStyleClasses(iconType + "-icon", image);
				} else {
					imageClass = ExtLibUtil.concatStyleClasses(iconType + "-icon fa", "fa-" + image);
				}
				writer.writeAttribute("class", imageClass, null);
				writer.endElement("i");
			}
		}
	}
}
