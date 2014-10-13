package de.varylab.varylab.plugin.nurbs.scene;

import de.jreality.scene.SceneGraphComponent;

public class DefaultComponentProvider<E extends SceneGraphComponent> implements SceneGraphComponentProvider<E>{
	
	@Override
	public SceneGraphComponent createSceneGraphComponent(E e) {
		return e;
	}
}
