package frostillicus.wrapbootstrap.ace1_3.library;

import com.ibm.xsp.library.AbstractXspLibrary;

public class AceLibrary extends AbstractXspLibrary {
	public final static String LIBRARY_RESOURCE_NAMESPACE = "wrapbootstrap-ace-1.3";

	public AceLibrary() {
	}

	public String getLibraryId() {
		return "frostillicus.wrapbootstrap.ace1_3.library";
	}

	public boolean isDefault() { return true; }

	@Override
	public String getPluginId() {
		return "frostillicus.wrapbootstrap.ace_1.3";
	}

	@Override
	public String[] getDependencies() {
		return new String[] {
				"com.ibm.xsp.core.library",
				"com.ibm.xsp.extsn.library",
				"com.ibm.xsp.domino.library",
				"com.ibm.xsp.extlib.library"
		};
	}

	@Override
	public String[] getFacesConfigFiles() {
		return new String[] {
				"frostillicus/wrapbootstrap/ace1_3/config/ace1_3-faces-config.xml",
				"src/frostillicus/wrapbootstrap/ace1_3/config/ace1_3-faces-config.xml"
		};
	}
}
