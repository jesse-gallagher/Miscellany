package controller;

import java.util.Date;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.controller.BasicXPageController;

public class home extends BasicXPageController {
	private static final long serialVersionUID = 1L;
	private int refreshCount_ = 0;
	private Date refreshUpdated_ = null;

	public int getRefreshCount() {
		return refreshCount_;
	}
	public Date getRefreshUpdated() {
		// I am aware that this is not immutable.
		return refreshUpdated_;
	}

	public void incCount() {
		refreshCount_++;
		refreshUpdated_ = new Date();
	}
	
	public void switchRevision() throws Exception {
		UIViewRootEx2 view = (UIViewRootEx2)FacesContext.getCurrentInstance().getViewRoot();
		int index = (Integer)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "index");
		System.out.println("asked to switch to revision " + index);
		Map<String, Object> applicationScope = ExtLibUtil.getApplicationScope();
		applicationScope.put("revision" + view.getUniqueViewId(), index);
		System.out.println("Stored revision as 'revision" + view.getUniqueViewId() + "'");
	}

}
