package de.varylab.varylab.math.bsp;

import static de.jtem.halfedge.util.HalfEdgeUtils.facesIncidentWithVertex;
import static de.jtem.halfedge.util.HalfEdgeUtils.incomingEdges;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import de.jtem.halfedge.Node;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

public final class KdUtility {

	public static Vector<VFace> collectFacesInRadius(KdTree<VVertex> kdTree, HasKdTreePosition p, double radius) {
		Vector<VVertex> vertresult = kdTree.collectInRadius(p, radius);
		Set<VFace> faceSet = new TreeSet<VFace>(new NodeComparator());
		
		for (VVertex v : vertresult)
			faceSet.addAll(facesIncidentWithVertex(v));
		
		Vector<VFace> result = new Vector<VFace>();
		result.addAll(faceSet);
		return result;
	}
	
	
	public static Vector<VEdge> collectEdgesInRadius(KdTree<VVertex> kdTree, HasKdTreePosition p, double radius) {
		Vector<VVertex> vertresult = kdTree.collectInRadius(p, radius);
		Set<VEdge> edgeSet = new TreeSet<VEdge>(new NodeComparator());
		
		for (VVertex v : vertresult)
			edgeSet.addAll(incomingEdges(v));
		
		Vector<VEdge> result = new Vector<VEdge>();
		result.addAll(edgeSet);
		return result;
	}
	
	
	private static class NodeComparator implements Comparator<Node<?, ?, ?>> {

		@Override
		public int compare(Node<?, ?, ?> o1, Node<?, ?, ?> o2) {
			return o1.getIndex() - o2.getIndex();
		}
		
	}
	
	
}
