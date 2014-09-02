package de.varylab.varylab.plugin.nurbs.scene;

import de.jreality.scene.SceneGraphComponent;

public class DefaultComponentProvider<E extends SceneGraphComponent> implements SceneGraphComponentProvider<E>{
	
	public SceneGraphComponent createSceneGraphComponent(E e) {
		return e;
	}
}
