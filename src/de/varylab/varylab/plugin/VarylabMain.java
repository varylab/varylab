package de.varylab.varylab.plugin;

import de.jreality.plugin.menu.BackgroundColor;
import de.jtem.halfedgetools.adapter.generic.UndirectedEdgeIndex;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.adapter.GeodesicLabelAdapter;
import de.varylab.varylab.hds.adapter.NodeWeigthAdapter;
import de.varylab.varylab.hds.adapter.SingularityAdapter;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.hds.adapter.VTexturePositionAdapter;

public class VarylabMain extends Plugin {

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		HalfedgeInterface hif = c.getPlugin(HalfedgeInterface.class);
		hif.addAdapter(new VPositionAdapter(), true);
		hif.addAdapter(new VTexturePositionAdapter(), true);
		hif.addAdapter(new NodeWeigthAdapter(), true);
		hif.addAdapter(new GeodesicLabelAdapter(), true);
		hif.addAdapter(new SingularityAdapter(), true);
		hif.addAdapter(new UndirectedEdgeIndex(), true);
		hif.setTemplateHDS(new VHDS());
		hif.set(new VHDS());
		String color = c.getProperty(BackgroundColor.class, "color", "");
		if (color.equals("")) {
			BackgroundColor bgColorPlugin = c.getPlugin(BackgroundColor.class);
			bgColorPlugin.setColor("UI Background");
		}
	}
	
	
}
