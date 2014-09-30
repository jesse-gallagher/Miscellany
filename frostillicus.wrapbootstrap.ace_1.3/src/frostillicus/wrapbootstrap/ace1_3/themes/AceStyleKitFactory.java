package frostillicus.wrapbootstrap.ace1_3.themes;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.ibm.xsp.stylekit.StyleKitFactory;

import frostillicus.wrapbootstrap.ace1_3.library.AceLibrary;

public class AceStyleKitFactory implements StyleKitFactory {
	private static final String FOLDER_PATH = AceStyleKitFactory.class.getPackage().getName().replace(".", "/");
	private static final List<String> KNOWN_THEMES = Arrays.asList(new String[] {
			AceLibrary.LIBRARY_RESOURCE_NAMESPACE,
			AceLibrary.LIBRARY_RESOURCE_NAMESPACE + "-skin1",
			AceLibrary.LIBRARY_RESOURCE_NAMESPACE + "-skin2",
			AceLibrary.LIBRARY_RESOURCE_NAMESPACE + "-skin3"
	});

	public InputStream getThemeAsStream(final String themeId, final int scope) {
		if(scope == StyleKitFactory.STYLEKIT_GLOBAL) {
			if(KNOWN_THEMES.contains(themeId)) {
				String variant = themeId.substring(AceLibrary.LIBRARY_RESOURCE_NAMESPACE.length());
				return getThemeFromBundle(FOLDER_PATH + "/wrapbootstrap-ace" + variant + ".theme");
			}
		}
		return null;
	}

	public InputStream getThemeFragmentAsStream(final String arg0, final int arg1) {
		return null;
	}

	private InputStream getThemeFromBundle(final String fileName) {
		ClassLoader cl = getClass().getClassLoader();
		return cl.getResourceAsStream(fileName);
	}
}
