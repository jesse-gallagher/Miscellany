package frostillicus.wrapbootstrap.ace1_3.library;

import org.eclipse.core.runtime.Plugin;

import com.ibm.xsp.extlib.minifier.ExtLibLoaderExtension;

import frostillicus.wrapbootstrap.ace1_3.minifier.AceLoader;

public class Activator extends Plugin {
	public static final boolean _debug = false;

	public static Activator instance;

	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
	                                                    value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
	                                                    justification="This is an intentional pattern")
	public Activator() {
		instance = this;

		ExtLibLoaderExtension.getExtensions().add(new AceLoader());
	}
}