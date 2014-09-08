package de.varylab.varylab.plugin;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SkinChangeListener;

import de.jreality.plugin.basic.InfoOverlayPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.job.JobMonitorTooBar;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.ui.AppearanceInspector;
import de.jreality.ui.TextureInspector;
import de.jtem.halfedgetools.adapter.generic.UndirectedEdgeIndex;
import de.jtem.halfedgetools.plugin.AlgorithmDropdownToolbar;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.adapter.ConicalNormalAdapter;
import de.varylab.varylab.halfedge.adapter.GeodesicLabelAdapter;
import de.varylab.varylab.halfedge.adapter.NodeWeigthAdapter;
import de.varylab.varylab.halfedge.adapter.SingularityAdapter;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.halfedge.adapter.VTexturePositionAdapter;
import de.varylab.varylab.plugin.optimization.IterationProtocolPanel;

public class VarylabMain extends Plugin {

	@Override
	public void install(final Controller c) throws Exception {
		super.install(c);
		configureJReality(c);
		configureHalfedgeInterface(c);
		hideInfoOverlayWorkaround(c);
		firstStartConfig(c);
		
		SubstanceLookAndFeel.registerSkinChangeListener(new SkinChangeListener() {
			@Override
			public void skinChanged() {
				IterationProtocolPanel ipp = c.getPlugin(IterationProtocolPanel.class);
				ipp.updateBackgroundColors();
				BackgroundColor viewBackground = c.getPlugin(BackgroundColor.class);
				if (viewBackground.getColor().equals("UI Background")) {
					viewBackground.setColor("UI Background");
				}
			}
		
	    });
	}

	
	protected void firstStartConfig(Controller c) {
		String flag = c.getProperty(VarylabMain.class, "firstStartFlag", "true");
		c.storeProperty(VarylabMain.class, "firstStartFlag", "false");
		if (flag.equals("false")) return;

		// set background color
		BackgroundColor bgColorPlugin = c.getPlugin(BackgroundColor.class);
		bgColorPlugin.setColor("UI Background");
		
		// default textures
		ContentAppearance ca = c.getPlugin(ContentAppearance.class);
		AppearanceInspector ai = ca.getAppearanceInspector();
		TextureInspector ti = ai.getTextureInspector();
		Map<String, String> texMap = new HashMap<String, String>();
		texMap.put("Quads", "de/varylab/varylab/texture/quads01.png");
		texMap.put("Checker", "de/varylab/varylab/texture/checker03.png");
		texMap.put("Hex", "de/varylab/varylab/texture/hex_pattern.png");
		texMap.put("Tri", "de/varylab/varylab/texture/triangle_pattern.png");
		ti.setTextures(texMap);
		ti.setTexture("Quads");
		
		View view = c.getPlugin(View.class);
		view.getCenterComponent().setPreferredSize(new Dimension(1280, 800));
	}
	


	protected void configureHalfedgeInterface(Controller c) {
		HalfedgeInterface hif = c.getPlugin(HalfedgeInterface.class);
		hif.addAdapter(new VPositionAdapter(), true);
		hif.addAdapter(new VTexturePositionAdapter(), true);
		hif.addAdapter(new NodeWeigthAdapter(), true);
		hif.addAdapter(new GeodesicLabelAdapter(), true);
		hif.addAdapter(new SingularityAdapter(), true);
		hif.addAdapter(new UndirectedEdgeIndex(), true);
		hif.addAdapter(new ConicalNormalAdapter(), true);
		hif.setTemplateHDS(new VHDS());
		hif.set(new VHDS());
	}


	protected void hideInfoOverlayWorkaround(Controller c) {
		c.getPlugin(View.class);
		InfoOverlayPlugin iol = c.getPlugin(InfoOverlayPlugin.class);
		iol.getInfoOverlay().setVisible(false);
		ViewToolBar toolbar = c.getPlugin(ViewToolBar.class);
		JobMonitorTooBar jobToolbar = c.getPlugin(JobMonitorTooBar.class);
		AlgorithmDropdownToolbar algoToolbar = c.getPlugin(AlgorithmDropdownToolbar.class);
		jobToolbar.setFloatable(false);
		toolbar.setFloatable(false);
		algoToolbar.setFloatable(false);
	}
	
	protected void configureJReality(Controller c) {
		c.getPlugin(ContentTools.class).setRotateAnimationEnabled(false);
		Logger.getLogger("de.jreality").setUseParentHandlers(true);
	}
	
}
