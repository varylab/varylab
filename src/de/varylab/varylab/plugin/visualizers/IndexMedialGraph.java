package de.varylab.varylab.plugin.visualizers;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.hds.adapter.IndexMedialGraphAdapter;

public class IndexMedialGraph extends Plugin implements DataSourceProvider {

	private IndexMedialGraphAdapter adapter = new IndexMedialGraphAdapter();	
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}

}
