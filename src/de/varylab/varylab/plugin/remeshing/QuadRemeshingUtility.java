package de.varylab.varylab.plugin.remeshing;


public class QuadRemeshingUtility {

//	public static void fitToBoundary(CoHDS hds, VHDS r, int xRes, int yRes) {
//		Vector<VVertex> boundaryCorners = new Vector<VVertex>();
//		LinkedList<VVertex> boundaryVertices = new LinkedList<VVertex>();
//		
//		findGridPolygon(hds, r, xRes, yRes, boundaryCorners, boundaryVertices);
//		LinkedList<VEdge> polygon = createInducedPolygon(r,boundaryVertices);
//		removeOutside(r,polygon);
//		
//		SpringRemeshingUtility.straightenBoundary(polygon,boundaryCorners);
//		
//		SpringRemeshingUtility.relaxInterior(r, boundaryCorners, true, false);
//
//		RemeshingUtility.projectOntoBoundary(r, hds);
//	}

//	private static LinkedList<VEdge> createInducedPolygon(VHDS r, LinkedList<VVertex> boundaryVertices) {
//		LinkedList<VEdge> bdPolygon = new LinkedList<VEdge>();
//		for(int i = 0; i < boundaryVertices.size(); ++i) {
//			VEdge e = HalfEdgeUtils.findEdgeBetweenVertices(boundaryVertices.get(i),boundaryVertices.get((i+1)%boundaryVertices.size()));
//			if(e == null) {
//				for(VFace f : HalfEdgeUtils.facesIncidentWithVertex(boundaryVertices.get(i))) {
//					if(HalfEdgeUtils.boundaryVertices(f).contains(boundaryVertices.get((i+1)%boundaryVertices.size()))){
//						RemeshingUtility.splitFaceAt(f,boundaryVertices.get(i),boundaryVertices.get((i+1)%boundaryVertices.size()));
//						e = HalfEdgeUtils.findEdgeBetweenVertices(boundaryVertices.get(i),boundaryVertices.get((i+1)%boundaryVertices.size()));
//						break;
//					} 
//				}
//			}
//			bdPolygon.add(e);
//		}
//		return bdPolygon;
//	}
//
//	private static void findGridPolygon(CoHDS hds, VHDS r, int xRes, int yRes,
//			Vector<VVertex> boundaryCorners,
//			LinkedList<VVertex> boundaryVertices) {
//		
//		KdTree<VVertex> kdTree = new KdTree<VVertex>(r.getVertices(), 1000, true);
//		
//		CoEdge be = HalfEdgeUtils.boundaryEdges(hds).iterator().next();
//		be = TextureUtility.findNextTextureCorner(be);
//		CoEdge e = be;
//		VVertex gridVertex = kdTree.collectKNearest(e.getStartVertex(), 1).firstElement(); 
//		//FIXME: there seems to be something wrong with the kdTree
//		gridVertex = RemeshingUtility.bruteForceNearest(e.getStartVertex().getTextureCoord(),r);
//		gridVertex.setPosition(new geom3d.Point(e.getStartVertex().getTextureCoord()));
//		
//		boundaryCorners.add(gridVertex);
//		boundaryVertices.add(gridVertex);
//		Point dir = new geom3d.Point(e.getTargetVertex().getTextureCoord());
//		dir.subtract(e.getStartVertex().getTextureCoord());
//		int step = getStep(xRes,yRes,dir);
//		do {
//			e = TextureUtility.findNextTextureCorner(e);
//			CoVertex pv = e.getStartVertex();
//			VVertex previousCorner = boundaryCorners.lastElement();
//			
//			double distance = previousCorner.getPosition().distanceTo(pv.getTextureCoord());
//			int i = 1;
//			while(distance > r.getVertex(previousCorner.getIndex()+i*step).getPosition().distanceTo(pv.getTextureCoord())) {
//				boundaryVertices.add(r.getVertex(previousCorner.getIndex()+i*step));
//				distance = boundaryVertices.getLast().getPosition().distanceTo(pv.getTextureCoord());
//				++i;
//			}	
//			gridVertex = boundaryVertices.getLast(); 
//			gridVertex.setPosition(pv.getTextureCoord());
//			boundaryCorners.add(gridVertex);
//			dir = new geom3d.Point(e.getTargetVertex().getTextureCoord());
//			dir.subtract(e.getStartVertex().getTextureCoord());
//			step = getStep(xRes,yRes,dir);
//		} while (e != be);
//		boundaryCorners.remove(boundaryCorners.size()-1);
//		if(boundaryVertices.getFirst() != boundaryVertices.getLast()) {
//			System.err.println("polygon didn't close!"); // FIXME
//		}
//		boundaryVertices.removeLast();
//	}
//
//	public static void removeExteriorVertices(VHDS hds, List<VEdge> polygon) {
//		VFace intFace = polygon.get(0).getRightFace();
//		LinkedList<VFace> queue = new LinkedList<VFace>();
//		HashSet<VFace> visited = new HashSet<VFace>();
//		queue.add(intFace);
//		while (!queue.isEmpty()){
//			VFace actFace = queue.poll();
//			List<VFace> star = new LinkedList<VFace>();
//			for(VEdge e : HalfEdgeUtils.boundaryEdges(actFace)) {
//				if(e.getRightFace() != null) {
//					star.add(e.getRightFace());
//				}
//			}
//			for (VFace f : star){
//				if (!visited.contains(f)){
//					visited.add(f);
//					queue.offer(f);
//				}
//			}
//		}
//		Set<VVertex> outsideVerts = new HashSet<VVertex>();
//		outsideVerts.addAll(hds.getVertices());
//		for(VFace f : visited) {
//			outsideVerts.removeAll(HalfEdgeUtils.boundaryVertices(f));
//		}
//		for(VVertex v : outsideVerts) {
//			TopologyAlgorithms.removeVertex(v);
//		}
//	}
//	
//	public static void removeOutside(VHDS r, LinkedList<VEdge> polygon) {
//		for(VEdge e : polygon) {
//			r.removeFace(e.getLeftFace());
//		}
//		removeExteriorVertices(r, polygon);
//	}
//
//	public static int getStep(int xRes, int yRes, geom3d.Vector dir) {
//		int step = 0;
//		step += Math.signum(Math.round(dir.x()*1E2))*yRes;
//		step += Math.signum(Math.round(dir.y()*1E2))*1;
//		return step;
//	}
//	
}
