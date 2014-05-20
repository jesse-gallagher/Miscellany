package model;

import java.util.Collection;
import java.util.List;

import org.openntf.domino.Document;
import org.openntf.domino.ViewEntry;

import frostillicus.model.domino.AbstractDominoModel;
import frostillicus.model.domino.DominoColumnInfo;

public class ViewState extends AbstractDominoModel {
	private static final long serialVersionUID = 1L;

	public ViewState(Document doc) {
		super(doc);
	}

	public ViewState(ViewEntry entry, List<DominoColumnInfo> columnInfo) {
		super(entry, columnInfo);
	}

	@Override
	protected Collection<String> nonSummaryFields() {
		return null;
	}

}
