package frostillicus.wrapbootstrap.ace1_3.minifier;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import frostillicus.wrapbootstrap.ace1_3.library.AceLibrary;
import frostillicus.wrapbootstrap.ace1_3.library.Activator;

import org.osgi.framework.Bundle;

import com.ibm.commons.util.DoubleMap;
import com.ibm.xsp.extlib.minifier.ExtLibLoaderExtension;
import com.ibm.xsp.extlib.resources.ExtlibResourceProvider;
import com.ibm.xsp.extlib.util.ExtLibUtil;


/**
 * Resource Loader that loads the resources from the Bootstrap plug-in.
 */
public class AceLoader extends ExtLibLoaderExtension {

	public AceLoader() {
	}

	@Override
	public Bundle getOSGiBundle() {
		return Activator.instance.getBundle();
	}

	// ========================================================
	//  Handling CSS
	// ========================================================



	@Override
	public void loadCSSShortcuts(final DoubleMap<String, String> aliases, final DoubleMap<String, String> prefixes) {
		/// ALIASES
		if(aliases != null) {
			// CAREFULLY MAKE SURE THAT THERE IS NO CONFLICT WITH ANOTHER LIBRARY
			//			aliases.put("@@7a", "/.ibmxspres/.extlib/" + AceLibrary.LIBRARY_RESOURCE_NAMESPACE + "/css/bootstrap.min.css");
		}

		/// PREFIXES
		if(prefixes != null) {
			// CAREFULLY MAKE SURE THAT THERE IS NO CONFLICT WITH ANOTHER LIBRARY
			//			prefixes.put("7T0a", "/.ibmxspres/.extlib/" + AceLibrary.LIBRARY_RESOURCE_NAMESPACE);
			prefixes.put("7T0b", "/.ibmxspres/.extlib/" + AceLibrary.LIBRARY_RESOURCE_NAMESPACE + "/css");
			prefixes.put("7T0c", "/.ibmxspres/.extlib/" + AceLibrary.LIBRARY_RESOURCE_NAMESPACE + "/js");
		}
	}

	// ========================================================
	// Serving resources
	// ========================================================

	@Override
	public URL getResourceURL(final HttpServletRequest request, final String name) {
		if(name.startsWith(AceLibrary.LIBRARY_RESOURCE_NAMESPACE)) {
			String path = ExtlibResourceProvider.BUNDLE_RES_PATH_EXTLIB + name;
			return ExtLibUtil.getResourceURL(Activator.instance.getBundle(), path);
		}
		return null;
	}
}
