package de.varylab.varylab.plugin.nurbs.scene;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;



public class ListSceneGraphComponent<E,F extends SceneGraphComponentProvider<E>> {

	private SceneGraphComponent 
		root = new SceneGraphComponent();
	
	private HashMap<E,SceneGraphComponent>
		elementComponentMap = new LinkedHashMap<>();
	
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
			root.removeChild(c);
			elementComponentMap.remove(e);
			return true;
		}
	}
	
	public void add(E e) {
		SceneGraphComponent c = elementComponentMap.get(e);
		if(c == null) {
			c = sgcProvider.createSceneGraphComponent(e);
			elementComponentMap.put(e, c);
			root.addChild(c);
		}
	}
	
	public void addAll(Collection<E> elts) {
		for(E e: elts) {
			add(e);
		}
	}
	
	public void clear() {
		root.removeAllChildren();
		elementComponentMap.clear();
	}

	public void setAppearance(Appearance app) {
		root.setAppearance(app);
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
		for(E e : keys) {
			remove(e);
		}
		for(E e : list) {
			add(e);
		}
	}
}
