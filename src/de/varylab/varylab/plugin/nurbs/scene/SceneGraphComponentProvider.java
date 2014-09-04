package de.varylab.varylab.plugin.nurbs.scene;

import de.jreality.scene.SceneGraphComponent;

public interface SceneGraphComponentProvider<E> {

	public SceneGraphComponent createSceneGraphComponent(E e);

}
