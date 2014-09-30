package frostillicus.wrapbootstrap.ace1_3.renderkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.context.DojoLibrary;
import com.ibm.xsp.context.DojoLibraryFactory;
import com.ibm.xsp.minifier.CSSDependencyList;
import com.ibm.xsp.minifier.CSSResource;
import com.ibm.xsp.minifier.DojoDependencyList;
import com.ibm.xsp.minifier.DojoResource;
import com.ibm.xsp.minifier.ResourceFactory;
import com.ibm.xsp.renderkit.html_basic.ViewRootRendererEx2;
import com.ibm.xsp.resource.DojoModulePathLoader;
import com.ibm.xsp.resource.DojoModulePathResource;
import com.ibm.xsp.resource.DojoModuleResource;
import com.ibm.xsp.resource.ScriptResource;
import com.ibm.xsp.resource.StyleSheetResource;
import com.ibm.xsp.util.FacesUtil;
import com.ibm.xsp.util.JSUtil;
import com.ibm.xsp.resource.Resource;

public class AceViewRootRenderer extends ViewRootRendererEx2 {
	@Override
	protected void encodeOptimizedResources(final FacesContext facesContext, final UIViewRootEx viewRoot, final ResponseWriter writer, final List<Resource> resources) throws IOException {
		ResourceFactory resourceFactory = ResourceFactory.get();
		String dojoLocale = (String)viewRoot.getEncodeProperty("xsp.dojolocale");

		ArrayList<StyleSheetResource> styleSheetResources = new ArrayList<StyleSheetResource>();
		ArrayList<ScriptResource> scriptResources = new ArrayList<ScriptResource>();

		DojoLibrary dojoLibrary = FacesUtil.getRequestParameters(facesContext).getDojoLibrary();

		DojoConfigurationResource localDojoConfigurationResource = null;
		Resource workingResource = null;
		DojoDependencyList dojoDeps = shouldAggregateDojo(facesContext) ? new DojoDependencyList(resourceFactory, dojoLibrary, dojoLocale) : null;
		CSSDependencyList cssDeps = shouldAggregateCss(facesContext) ? new CSSDependencyList(resourceFactory, dojoLibrary) : null;
		List<Resource> localArrayList3 = null;
		List<Resource> modulePathResources = null;
		for(Resource res : resources) {
			if (!res.isRendered()) {
				continue;
			}
			if (res instanceof DojoModuleResource) {
				DojoModuleResource dojoRes = (DojoModuleResource)res;
				if (dojoDeps != null) {
					boolean hasCondition = StringUtil.isNotEmpty(dojoRes.getCondition());
					if(!(hasCondition)) {
						DojoResource localDojoResource = resourceFactory.getDojoResource(dojoRes.getName(), dojoLibrary);
						if(localDojoResource != null) {
							DojoModulePathResource localObject8 = DojoModulePathLoader.lookupExtensionModulePath(localDojoResource.getName());
							if ((localObject8 != null) && localObject8.isRendered()) {
								if (modulePathResources == null) {
									modulePathResources = new ArrayList<Resource>();
								}
								modulePathResources.add(localObject8);
							}
							dojoDeps.addResource(localDojoResource);
						}

					}

					if (localArrayList3 == null) {
						localArrayList3 = new ArrayList<Resource>();
					}
					localArrayList3.add(dojoRes);
				}
			} else {
				if (res instanceof StyleSheetResource) {
					StyleSheetResource localObject7 = (StyleSheetResource)res;
					String href = (localObject7).getHref();
					if ((StringUtil.isNotEmpty(href)) && (!(FacesUtil.isAbsoluteUrl(href)))) {
						int i1 = ((!(StringUtil.isNotEmpty((localObject7).getMedia()))) &&
								(!(StringUtil.isNotEmpty((localObject7).getCharset()))) &&
								(isListEmpty((localObject7).getAttrs()))) ? 0 : 1;
						if (i1 == 0) {
							if (!(href.startsWith("/.ibmxspres/"))) {
								styleSheetResources.add(localObject7);
								continue;
							}
							if (cssDeps != null) {
								CSSResource localObject8 = resourceFactory.getCSSResource(href, dojoLibrary);
								if (localObject8 != null) {
									cssDeps.addResource(localObject8);
									continue;
								}
							}

						}
						if (localArrayList3 == null) {
							localArrayList3 = new ArrayList<Resource>();
						}
						localArrayList3.add(localObject7);
					}
				} else if (res instanceof ScriptResource) {
					if (res instanceof DojoConfigurationResource) {
						localDojoConfigurationResource = (DojoConfigurationResource)res;
					} else {
						ScriptResource scriptRes = (ScriptResource)res;
						if ((scriptRes).isClientSide()) {
							String src = (scriptRes).getSrc();

							if(StringUtil.isNotEmpty(src) && !FacesUtil.isAbsoluteUrl(src) && !src.startsWith("/.ibmxspres/")) {
								scriptResources.add(scriptRes);
								continue;
							}

							if ((workingResource == null) && (StringUtil.equals((scriptRes).getSrc(), "/.ibmxspres/dojoroot/dojo/dojo.js"))) {
								workingResource = scriptRes;
								continue;
							}

							if (localArrayList3 == null) {
								localArrayList3 = new ArrayList<Resource>();
							}
							localArrayList3.add(scriptRes);
						}
					}
				} else if (res instanceof DojoModulePathResource) {
					if (modulePathResources == null) {
						modulePathResources = new ArrayList<Resource>();
					}
					modulePathResources.add(res);
				} else {
					encodeResource(facesContext, viewRoot, writer, res);
				}
			}
		}
		if ((cssDeps != null) && (!(cssDeps.isEmpty()))) {
			String localObject4 = cssDeps.getUrlParameter();
			if (StringUtil.isNotEmpty(localObject4)) {
				StringBuilder localObject6 = new StringBuilder();
				localObject6.append("/xsp/.ibmxspres/.mini/css");
				if ((dojoLibrary.isUncompressed()) || (dojoLibrary != DojoLibraryFactory.getDefaultLibrary(false))) {
					localObject6.append('-');
					localObject6.append(dojoLibrary.getVersionTag());
				}
				localObject6.append('/');
				localObject6.append(localObject4);
				localObject6.append(".css");
				String localObject7 = localObject6.toString();
				writer.startElement("link", null);
				writer.writeAttribute("rel", "stylesheet", null);
				writer.writeAttribute("type", "text/css", null);
				localObject7 = handleProxyPrefix(localObject7);
				writer.writeAttribute("href", localObject7, null);
				writer.endElement("link");
				JSUtil.writeln(writer);
			}

		}

		if(workingResource != null) {
			boolean bool1 = (Boolean)viewRoot.getEncodeProperty("xsp.dojoconfigattr");
			if (!bool1) {
				encodeResource(facesContext, viewRoot, writer, localDojoConfigurationResource);
			}
			encodeResource(facesContext, viewRoot, writer, workingResource);
		}

		if(modulePathResources != null) {
			for(Resource modulePathRes : modulePathResources) {
				encodeResource(facesContext, viewRoot, writer, modulePathRes);
			}

		}

		if(dojoDeps != null && !(dojoDeps.isEmpty())) {
			String str2 = dojoDeps.getUrlParameter();
			if (StringUtil.isNotEmpty(str2)) {
				StringBuilder localStringBuilder = new StringBuilder();
				localStringBuilder.append("/xsp/.ibmxspres/.mini/dojo");
				if ((dojoLibrary.isUncompressed()) || (dojoLibrary != DojoLibraryFactory.getDefaultLibrary(false))) {
					localStringBuilder.append('-');
					localStringBuilder.append(dojoLibrary.getVersionTag());
				}
				localStringBuilder.append('/');
				localStringBuilder.append(str2);
				localStringBuilder.append(".js");
				String localObject7 = localStringBuilder.toString();
				writer.startElement("script", null);
				writer.writeAttribute("type", "text/javascript", null);
				localObject7 = handleProxyPrefix(localObject7);
				writer.writeAttribute("src", localObject7, null);
				writer.endElement("script");
				JSUtil.writeln(writer);
			}

		}

		if (localArrayList3 != null) {
			int j = localArrayList3.size();
			for (int l = 0; l < j; ++l) {
				encodeResource(facesContext, viewRoot, writer, localArrayList3.get(l));
			}
		}
		String localResUrl;
		if(shouldAggregateAppCss(facesContext) && !styleSheetResources.isEmpty()) {
			localResUrl = getApplicationCSSUrl(facesContext, styleSheetResources);
			writer.startElement("link", null);
			writer.writeAttribute("rel", "stylesheet", null);
			writer.writeAttribute("type", "text/css", null);
			localResUrl = handleProxyPrefix(localResUrl);
			writer.writeAttribute("href", localResUrl, null);
			writer.endElement("link");
			JSUtil.writeln(writer);
		} else if (!(styleSheetResources.isEmpty())) {
			for (Resource styleSheet : styleSheetResources) {
				encodeResource(facesContext, viewRoot, writer, styleSheet);
			}
		}
		//		if(shouldAggregateAppJs(facesContext) && !scriptResources.isEmpty()) {
		//			localResUrl = getApplicationJSUrl(facesContext, scriptResources);
		//			writer.startElement("script", null);
		//			writer.writeAttribute("type", "text/javascript", null);
		//			localResUrl = handleProxyPrefix(localResUrl);
		//			writer.writeAttribute("src", localResUrl, null);
		//			writer.endElement("script");
		//			JSUtil.writeln(writer);
		//		} else if (!(scriptResources.isEmpty())) {
		//			for(Resource script : scriptResources) {
		//				encodeResource(facesContext, viewRoot, writer, script);
		//			}
		//		}
	}

	@Override
	protected void encodeResourcesList(final FacesContext facesContext, final UIViewRootEx viewRoot, final ResponseWriter writer, final List<Resource> resources) throws IOException {
		if(resources != null && !resources.isEmpty()) {
			for(Resource resource : resources) {
				if(!isPageJSResource(resource)) {
					encodeResource(facesContext, viewRoot, writer, resource);
				}
			}
		}
	}

	@Override
	protected void encodeEndPage(final FacesContext facesContext, final ResponseWriter writer, final UIViewRootEx viewRoot) throws IOException {
		List<Resource> resources = buildResourceList(facesContext, viewRoot, true, true, true, true);
		for(Resource resource : resources) {
			if(isPageJSResource(resource)) {
				encodeResource(facesContext, viewRoot, writer, resource);
			}
		}

		super.encodeEndPage(facesContext, writer, viewRoot);
	}

	protected boolean isPageJSResource(final Resource resource) {
		if(resource instanceof ScriptResource && !(resource instanceof DojoConfigurationResource)) {
			ScriptResource scriptRes = (ScriptResource)resource;
			if(scriptRes.isClientSide()) {
				String src = scriptRes.getSrc();

				if(StringUtil.isNotEmpty(src) && !src.contains("dojo")) {
					return true;
				}
			}
		}
		return false;
	}

	protected static boolean isListEmpty(final List<?> list) {
		return list == null || list.isEmpty();
	}
	protected String handleProxyPrefix(String paramString) {
		String str = ApplicationEx.getInstance().getApplicationProperty("xsp.application.context.proxy", null);
		if (StringUtil.isNotEmpty(str)) {
			return (paramString = "/" + str + paramString);
		}
		return paramString;
	}
}
