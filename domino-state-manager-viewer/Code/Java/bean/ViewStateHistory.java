package bean;

import java.net.URLEncoder;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;

import model.ViewState;

import com.ibm.xsp.extlib.tree.impl.BasicContainerTreeNode;
import com.ibm.xsp.extlib.tree.impl.BasicLeafTreeNode;
import com.ibm.xsp.extlib.tree.impl.BasicNodeList;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class ViewStateHistory extends BasicNodeList {
	private static final long serialVersionUID = 1L;

	public ViewStateHistory() {
		try {
			ViewState state = (ViewState)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "state");
			DefaultMutableTreeNode tree = state.document().getItemValue("RevisionTree", DefaultMutableTreeNode.class);
			processNode(tree, null);
		} catch(Throwable t) {
			t.printStackTrace();
			BasicLeafTreeNode exceptionChild = new BasicLeafTreeNode();
			exceptionChild.setLabel(t.toString());
			addChild(exceptionChild);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void processNode(DefaultMutableTreeNode node, BasicContainerTreeNode root) throws Exception {
		Map<String, String> param = (Map<String, String>)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "param");
		BasicContainerTreeNode entryChild = new BasicContainerTreeNode();
		Map<String, Object> nodeInfo = (Map<String, Object>)node.getUserObject();
		entryChild.setLabel(String.valueOf(nodeInfo));
		entryChild.setHref("/viewStateRevision.xsp?id=" + URLEncoder.encode(param.get("id"), "UTF-8") + "&index=" + nodeInfo.get("index"));
		if(root == null) {
			addChild(entryChild);
		} else {
			root.addChild(entryChild);
		}
		Enumeration<DefaultMutableTreeNode> children = node.children();
		while(children.hasMoreElements()) {
			processNode(children.nextElement(), entryChild);
		}
	}
}
