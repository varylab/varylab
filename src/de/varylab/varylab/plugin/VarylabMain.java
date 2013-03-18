package de.varylab.varylab.plugin;

import de.jreality.plugin.basic.InfoOverlayPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.ui.AppearanceInspector;
import de.jreality.ui.TextureInspector;
import de.jtem.halfedgetools.adapter.generic.UndirectedEdgeIndex;
import de.jtem.halfedgetools.plugin.AlgorithmDropdownToolbar;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.discreteconformal.plugin.DomainVisualisationPlugin;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.adapter.GeodesicLabelAdapter;
import de.varylab.varylab.halfedge.adapter.NodeWeigthAdapter;
import de.varylab.varylab.halfedge.adapter.SingularityAdapter;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.halfedge.adapter.VTexturePositionAdapter;

public class VarylabMain extends Plugin {

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		configureHalfedgeInterface(c);
		adapterViewToUI(c);
		hideInfoOverlayWorkaround(c);
		
		ContentAppearance ca = c.getPlugin(ContentAppearance.class);
		AppearanceInspector ai = ca.getAppearanceInspector();
		TextureInspector ti = ai.getTextureInspector();
		ti.getTextures().remove("2 Metal Grid");
		ti.getTextures().remove("3 Metal Floor");
		ti.getTextures().remove("4 Chain-Link Fence");
		ti.getTextures().put("Quads", "de/varylab/varylab/texture/quads01.png");
		ti.getTextures().put("Checker", "de/varylab/varylab/texture/checker03.png");
		ti.getTextures().put("Hex", "de/varylab/varylab/texture/hex_pattern.png");
		ti.getTextures().put("Tri", "de/varylab/varylab/texture/triangle_pattern.png");
		String selectedTex = ti.getTexture();
		ti.setTexture("Quads");
		ti.setTexture(selectedTex);
	}


	public void adapterViewToUI(Controller c) {
		String color = c.getProperty(BackgroundColor.class, "color", "");
		if (color.equals("")) {
			BackgroundColor bgColorPlugin = c.getPlugin(BackgroundColor.class);
			bgColorPlugin.setColor("UI Background");
		}
	}


	public void configureHalfedgeInterface(Controller c) {
		HalfedgeInterface hif = c.getPlugin(HalfedgeInterface.class);
		hif.addAdapter(new VPositionAdapter(), true);
		hif.addAdapter(new VTexturePositionAdapter(), true);
		hif.addAdapter(new NodeWeigthAdapter(), true);
		hif.addAdapter(new GeodesicLabelAdapter(), true);
		hif.addAdapter(new SingularityAdapter(), true);
		hif.addAdapter(new UndirectedEdgeIndex(), true);
		hif.setTemplateHDS(new VHDS());
		hif.set(new VHDS());
	}


	public void hideInfoOverlayWorkaround(Controller c) {
		c.getPlugin(View.class);
		c.getPlugin(DomainVisualisationPlugin.class);
		InfoOverlayPlugin iol = c.getPlugin(InfoOverlayPlugin.class);
		iol.getInfoOverlay().setVisible(false);
		ViewToolBar toolbar = c.getPlugin(ViewToolBar.class);
		toolbar.setFloatable(false);
		AlgorithmDropdownToolbar algoToolbar = c.getPlugin(AlgorithmDropdownToolbar.class);
		algoToolbar.setFloatable(false);
	}
	
	
	protected void replaceDefaultTextures(ContentAppearance ca) {
		
	}
	
	
}
