package de.varylab.varylab.startup.definitions;

import java.util.Set;

import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.basic.Inspector;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgePluginFactory;
import de.jtem.halfedgetools.plugin.HalfedgePreferencePage;
import de.jtem.halfedgetools.plugin.MarqueeSelectionPlugin;
import de.jtem.halfedgetools.plugin.data.VisualizationInterface;
import de.jtem.halfedgetools.plugin.widget.ContextMenuWidget;
import de.jtem.halfedgetools.plugin.widget.ViewSwitchWidget;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.nurbs.plugin.NurbsManagerPlugin;
import de.varylab.varylab.startup.VarylabStartupDefinition;

public class VaryLabNurbs extends VarylabStartupDefinition {

	@Override
	public String getApplicationName() {
		return "VaryLab[NURBS]";
	}
	
	@Override
	public String getPropertyFileName() {
		return "VarylabNurbs.xml";
	}
	
	@Override
	public void getPlugins(Set<Class<? extends Plugin>> pSet, Set<Plugin> instances) {
		pSet.add(VarylabMain.class);
		pSet.add(HalfedgeInterface.class);
		
		instances.addAll(HalfedgePluginFactory.createSelectionPlugins());
		instances.addAll(HalfedgePluginFactory.createSubdivisionPlugins());
		instances.addAll(HalfedgePluginFactory.createEditingPlugins());
		instances.addAll(HalfedgePluginFactory.createDataVisualizationPlugins());
		pSet.add(MarqueeSelectionPlugin.class);
		pSet.add(ViewSwitchWidget.class);
		pSet.add(ContextMenuWidget.class);
		pSet.add(ConsolePlugin.class);
		
		pSet.add(NurbsManagerPlugin.class);
		pSet.add(HalfedgePreferencePage.class);
		pSet.add(VisualizationInterface.class);
		pSet.add(Inspector.class);
	}

	public static void main(String[] args) throws Exception {
		new VaryLabNurbs().startup();
	}

}
