package bean;

import java.util.Enumeration;

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
			System.out.println("want to build tree for " + state);
			DefaultMutableTreeNode tree = state.document().getItemValue("RevisionTree", DefaultMutableTreeNode.class);
			System.out.println("tree is " + tree);
			processNode(tree, null);
		} catch(Throwable t) {
			t.printStackTrace();
			BasicLeafTreeNode exceptionChild = new BasicLeafTreeNode();
			exceptionChild.setLabel(t.toString());
			addChild(exceptionChild);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void processNode(DefaultMutableTreeNode node, BasicContainerTreeNode root) {
		BasicContainerTreeNode entryChild = new BasicContainerTreeNode();
		entryChild.setLabel(String.valueOf(node.getUserObject()));
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
