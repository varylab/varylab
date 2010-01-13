package de.varylab.varylab.hds.adapter;

import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Ifs;
import de.varylab.varylab.hds.VVertex;

public class VertexPositionAdapter implements CoordinateAdapter2Heds<VVertex>,
		CoordinateAdapter2Ifs<VVertex> {

	@Override
	public void setCoordinate(VVertex node, double[] coord) {
		node.position = coord;
	}

	@Override
	public AdapterType getAdapterType() {
		return AdapterType.VERTEX_ADAPTER;
	}

	@Override
	public double[] getCoordinate(VVertex node) {
		return node.position;
	}

}
