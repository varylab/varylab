package de.varylab.varylab.plugin.datasource;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.halfedge.adapter.EdgeNormalTorsionAdapter;

public class EdgeNormalTorsion extends Plugin implements DataSourceProvider {

	private EdgeNormalTorsionAdapter
		adapter = new EdgeNormalTorsionAdapter();
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Edge Normal Torsion");
		return info;
	}
}
