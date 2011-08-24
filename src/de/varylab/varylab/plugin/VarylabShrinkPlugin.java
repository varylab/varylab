package de.varylab.varylab.plugin;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public abstract class VarylabShrinkPlugin extends ShrinkPanelPlugin {

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
