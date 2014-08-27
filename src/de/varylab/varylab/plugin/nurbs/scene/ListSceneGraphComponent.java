package de.varylab.varylab.plugin.nurbs.scene;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;



public class ListSceneGraphComponent<E,F extends SceneGraphComponentProvider<E>> {

	private SceneGraphComponent 
		root = new SceneGraphComponent();
	
	private Map<E,SceneGraphComponent>
		elementComponentMap = Collections.synchronizedMap(new LinkedHashMap<E,SceneGraphComponent>());
	
	private SceneGraphComponentProvider<E>
		sgcProvider = null;
	
	public ListSceneGraphComponent(String name, F provider) {
		root.setName(name);
		sgcProvider = provider;
	}
	
	public boolean remove(E e) {
		SceneGraphComponent c = elementComponentMap.get(e);
		if(c == null) {
			return false;
		} else {
			synchronized(root) {
				root.removeChild(c);
			}
			elementComponentMap.remove(e);
			return true;
		}
	}
	
	public void add(E e) {
		SceneGraphComponent c = elementComponentMap.get(e);
		if(c == null) {
			c = sgcProvider.createSceneGraphComponent(e);
			elementComponentMap.put(e, c);
			synchronized(root) {
				root.addChild(c);
			}
		}
	}
	
	public void addAll(Collection<E> elts) {
		List<SceneGraphComponent> cpts = new LinkedList<>();
		for(E e: elts) {
			SceneGraphComponent c = elementComponentMap.get(e);
			if(c == null) {
				c = sgcProvider.createSceneGraphComponent(e);
				elementComponentMap.put(e, c);
				cpts.add(c);
			}
		}
		synchronized(root) {
			root.addChildren(cpts.toArray(new SceneGraphComponent[cpts.size()]));
		}
	}
	
	public void clear() {
		synchronized(root) {
			root.removeAllChildren();
		}
		elementComponentMap.clear();
	}

	public void setAppearance(Appearance app) {
		synchronized(root) {
			root.setAppearance(app);
		}
	}

	public SceneGraphComponent getComponent() {
		return root;
	}
	
	public void setVisible(E e, boolean visible) {
		SceneGraphComponent c = elementComponentMap.get(e);
		if(c != null) {
			c.setVisible(visible);
		}
	}

	public void retain(Collection<E> list) {
		Set<E> keys = new LinkedHashSet<>(elementComponentMap.keySet());
		keys.removeAll(list);
		removeAll(keys);
		addAll(list);
	}

	public void removeAll(Collection<E> list) {
		List<SceneGraphComponent> cpts = new LinkedList<>();
		for(E e: list) {
			SceneGraphComponent c = elementComponentMap.get(e);
			if(c != null) {
				cpts.add(c);
			}
			elementComponentMap.remove(e);
		}
		synchronized(root) {
			root.removeChildren(cpts.toArray(new SceneGraphComponent[cpts.size()]));
		}

	}
}
