package frostillicus;

import java.io.*;
import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.application.StateManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import com.ibm.xsp.application.ComponentNode;
import com.ibm.xsp.application.IComponentNode;
import com.ibm.xsp.application.StateManagerImpl;
import com.ibm.xsp.application.UniqueViewIdManager;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.util.Delegation;
import com.ibm.xsp.util.FacesUtil;

import org.openntf.domino.*;

import util.JSFUtil;

public class DominoStateManager extends StateManagerImpl {
	private final StateManager delegate_;
	private final static boolean debug_ = false;

	public DominoStateManager(final StateManager delegate) {
		super(delegate);
		delegate_ = delegate;
		print("created using " + (delegate_ != null ? delegate_.getClass().getName() : "null"));
	}

	public DominoStateManager() throws FacesException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		StateManager priorStateManager = (StateManager)Delegation.getImplementation("state-manager");
		this.delegate_ = priorStateManager;

		print("created empty using " + (delegate_ != null ? delegate_.getClass().getName() : "null"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public SerializedView saveSerializedView(final FacesContext facesContext) {
		print("--------------------------");
		print("running saveSerializedView");

		try {
			UIViewRootEx view = (UIViewRootEx)facesContext.getViewRoot();
			view._xspCleanTransientData();
			String key = view.getUniqueViewId();

			Database database = (Database)JSFUtil.getDatabase();
			key = database.getReplicaID() + key;
			print("storage key is " + key);


			Object treeStructure = getTreeStructureToSave(facesContext, view);
			Object componentStructure = getComponentStateToSave(facesContext, view);
			SerializedView serializedView = new SerializedView(treeStructure, componentStructure);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Class<? extends ObjectOutputStream> outputClass = (Class<? extends ObjectOutputStream>)Class.forName("com.ibm.xsp.application.AbstractSerializingStateManager$FastObjectOutputStream");
			//ObjectOutputStream oos = new ObjectOutputStream(bos);
			Constructor<?> outputConstructor = outputClass.getConstructor(OutputStream.class);
			ObjectOutputStream oos = (ObjectOutputStream)outputConstructor.newInstance(bos);

			oos.writeObject(serializedView.getStructure());
			//oos.writeObject(serializedView.getState());
			Method writeObjectEx = outputClass.getMethod("writeObjectEx", Object.class);
			writeObjectEx.invoke(oos, serializedView.getState());

			oos.flush();
			bos.flush();

			byte[] bytes = bos.toByteArray();

			Database storage = getStorageDatabase();
			Document doc = storage.getDocumentByKey(key, true);
			int index = 0;
			if(doc.hasItem("StateIndex")) {
				index = doc.getItemValueInteger("StateIndex") + 1;
			}
			doc.put("State" + index, bytes);
			doc.put("StateTime" + index, new Date());
			doc.put("StateIndex", index);
			doc.put("User", database.getAncestorSession().getEffectiveUserName());
			doc.put("PageName", view.getPageName());

			Map<String, Object> applicationScope = ExtLibUtil.getApplicationScope();
			
			// Build or create a tree
			DefaultMutableTreeNode tree = null;
			
			Map<String, Object> infoNode = new HashMap<String, Object>();
			infoNode.put("index", index);
			infoNode.put("time", new Date());
			
			if(doc.hasItem("RevisionTree")) {
				tree = doc.getItemValue("RevisionTree", DefaultMutableTreeNode.class);
			} else {
				tree = new DefaultMutableTreeNode(infoNode);
			}
			if(index > 0) {
				String branchedKey = "branchedrevision" + view.getUniqueViewId();
				DefaultMutableTreeNode parent = null;
				if(applicationScope.containsKey(branchedKey)) {
					// Then we want to find the indexed revision
					Integer requestedIndex = (Integer)applicationScope.get(branchedKey);
					parent = findIndexedNode(tree, requestedIndex);
//					System.out.println("adding to tree node at requested index " + requestedIndex + ": " + parent);
					applicationScope.remove(branchedKey);
				} else {
					// Then we want to find the previous revision
					parent = findIndexedNode(tree, index-1);
//					System.out.println("adding to tree node at previous index " + (index-1) + ": " + parent);
				}
				parent.add(new DefaultMutableTreeNode(infoNode));
			}
			doc.put("RevisionTree", tree);
			
			doc.save();
			
			Map<String, Integer> stateHistories = null;
			if(!applicationScope.containsKey("stateHistories")) {
				stateHistories = new HashMap<String, Integer>();
				applicationScope.put("stateHistories", stateHistories);
			} else {
				stateHistories = (Map<String, Integer>)applicationScope.get("stateHistories");
			}
			stateHistories.put(view.getUniqueViewId(), index);

			//			print("storage size was " + bytes.length);
			print("added view to holder");
			print("--------------------------");

			return serializedView;
		} catch(Exception e) { e.printStackTrace(); throw new RuntimeException(e); }
	}

	@SuppressWarnings("unchecked")
	@Override
	public UIViewRoot restoreView(final FacesContext facesContext, final String viewId, final String renderKitId) {
		long startTime = System.nanoTime();

		print("--------------------------");
		print("reading view " + viewId + ", " + renderKitId);

		try {
			String uniqueViewId = UniqueViewIdManager.getRequestUniqueViewId(facesContext);
			Database database = (Database)JSFUtil.getDatabase();
			String key = database.getReplicaID() + uniqueViewId;
			print("loading key is " + key);

			Database storage = getStorageDatabase();
			Document doc = storage.getDocumentByKey(key, false);
			//			byte[] bytes = (byte[])doc.get("State");
			//			Object[] result = (Object[])doc.get("State");

			//			byte[] bytes = (byte[])client.get(key);
			//			if(bytes != null) {
			if(doc != null) {
				
				Map<String, Object> applicationScope = ExtLibUtil.getApplicationScope();
				int index = -1;
				String revisionKey = "revision" + uniqueViewId;
				if(applicationScope.containsKey(revisionKey)) {
					index = (Integer)applicationScope.get(revisionKey);
					applicationScope.remove(revisionKey);
					applicationScope.put("branched" + revisionKey, index);
				} else {
					index = doc.getItemValueInteger("StateIndex");
				}
				
				byte[] bytes = doc.getItemValue("State" + index, byte[].class);

				print("bytes length in is " + bytes.length);
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

				print("fetched from Domino");
				long endTime = System.nanoTime();
				print("fetch took " + ((endTime - startTime) / 1000.0 / 1000) + "ms");
				print("--------------------------");

				SerializedView serializedView = new SerializedView(treeStructure, componentStructure);

				// Reconstruct the view
				IComponentNode localIComponentNode = (IComponentNode)serializedView.getStructure();
				UIViewRootEx view = (UIViewRootEx)localIComponentNode.restore(facesContext);
				Object viewState = serializedView.getState();
				FacesUtil.setRestoreRoot(facesContext, view);
				//				view = (UIViewRootEx)facesContext.getViewRoot();
				view.processRestoreState(facesContext, viewState);
				FacesUtil.setRestoreRoot(facesContext, null);

				print("view is " + view);
				view.setUniqueViewId(uniqueViewId);
				return view;
			} else {
				print("view not found in DB");
				print("--------------------------");
				return null;
			}
		} catch(Exception e) { e.printStackTrace(); throw new RuntimeException(e); }
	}


	private void print(final Object message) {
		if(debug_) System.out.println("StateManager>> " + message);
	}

	private Database getStorageDatabase() {
		XSPContext context = ExtLibUtil.getXspContext();
		String databaseName = context.getProperty("frostillicus.dominostatemanager.database");
		Session session = (Session)JSFUtil.getSession();
		return session.getDatabase(databaseName);
	}
	
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode findIndexedNode(DefaultMutableTreeNode tree, Integer index) {
		Enumeration<DefaultMutableTreeNode> enumeration = tree.breadthFirstEnumeration();
		while(enumeration.hasMoreElements()) {
			DefaultMutableTreeNode node = enumeration.nextElement();
			Map<String, Object> infoNode = (Map<String, Object>)node.getUserObject();
//			System.out.println("comparing index " + index + " to node containing " + node);
			if(infoNode.get("index").equals(index)) {
				return node;
			}
		}
		return null;
	}

	/* ******************************************************************************************
	 * From AbstractSerializngStateManager
	 ********************************************************************************************/

	protected Object getTreeStructureToSave(final FacesContext facesContext, final UIViewRoot view) {
		if (view.isTransient()) {
			return null;
		}
		IComponentNode localIComponentNode = createComponentNode(facesContext, view);
		return localIComponentNode;
	}
	protected IComponentNode createComponentNode(final FacesContext facesContext, final UIViewRoot view)
	{
		Class<ComponentNode> holderClass = ComponentNode.class;
		Constructor<?> cons = holderClass.getDeclaredConstructors()[0];
		cons.setAccessible(true);
		try {
			return (ComponentNode)cons.newInstance(view);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	protected Object getComponentStateToSave(final FacesContext facesContext, final UIViewRoot view) {
		return view.processSaveState(facesContext);
	}
}