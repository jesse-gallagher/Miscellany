package controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import javax.faces.context.FacesContext;

import model.ViewState;

import com.ibm.xsp.application.IComponentNode;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.controller.BasicXPageController;

public class viewStateRevision extends BasicXPageController {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	@Override
	public void beforePageLoad() throws Exception {
//		super.afterPageLoad();
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Map<String, String> param = (Map<String, String>)ExtLibUtil.resolveVariable(facesContext, "param");

		ViewState state = (ViewState)ExtLibUtil.resolveVariable(facesContext, "state");
		byte[] bytes = (byte[])state.document().getItemValue("State" + param.get("index"), byte[].class);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		//ObjectInputStream ois = new ObjectInputStream(bis);
		Class<? extends ObjectInputStream> inputClass = (Class<? extends ObjectInputStream>)Class.forName("com.ibm.xsp.application.AbstractSerializingStateManager$FastObjectInputStream");
		Constructor<?> inputConstructor = inputClass.getConstructor(FacesContext.class, InputStream.class);
		ObjectInputStream ois = (ObjectInputStream)inputConstructor.newInstance(facesContext, bis);

		// Read the components in
		Object treeStructure = ois.readObject();
		//Object componentStructure = ois.readObject();
		Method readObjectEx = inputClass.getMethod("readObjectEx");
		Object componentStructure = readObjectEx.invoke(ois);
		
//		IComponentNode localIComponentNode = (IComponentNode)treeStructure;
//		UIViewRootEx view = (UIViewRootEx)localIComponentNode.restore(facesContext);
//		Object viewState = componentStructure;
//		view.processRestoreState(facesContext, viewState);
//		System.out.println("view is " + view);
		
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("contextTreeStructure", treeStructure);
		viewScope.put("contextComponentStructure", componentStructure);
	}

}
