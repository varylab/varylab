package de.varylab.varylab.hds;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.jreality.Bundle;
import de.jtem.halfedgetools.jreality.Bundle.BundleType;
import de.jtem.halfedgetools.jreality.Bundle.DisplayType;
import de.jtem.halfedgetools.jreality.node.JREdge;

public class VEdge extends JREdge<VVertex, VEdge, VFace> {
	
	@Bundle(dimension=1,type=BundleType.Value, display=DisplayType.Debug, name="")
	public double getEdgeLength() {
		return Rn.euclideanDistance(getTargetVertex().position, getStartVertex().position);
	}

}
