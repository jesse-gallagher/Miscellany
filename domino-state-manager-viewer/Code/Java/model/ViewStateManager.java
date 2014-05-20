package model;

import org.openntf.domino.*;

import util.JSFUtil;

import frostillicus.model.domino.AbstractDominoManager;

public class ViewStateManager extends AbstractDominoManager<ViewState> {
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<ViewState> getModelClass() {
		return ViewState.class;
	}

	@Override
	protected String getViewPrefix() {
		return "View States\\";
	}
	
	@Override
	protected Database getDatabase() {
		return ((Session)JSFUtil.getSession()).getDatabase("tests/dcluster-storage");
	}

}
