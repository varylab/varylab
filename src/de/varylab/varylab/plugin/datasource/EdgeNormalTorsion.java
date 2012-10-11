package de.varylab.varylab.plugin.datasource;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.hds.adapter.EdgeNormalTorsionAdapter;

public class EdgeNormalTorsion extends Plugin implements DataSourceProvider {

	private EdgeNormalTorsionAdapter
		adapter = new EdgeNormalTorsionAdapter();
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}

}
