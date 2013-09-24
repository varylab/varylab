package de.varylab.varylab.plugin.remeshing;

import java.awt.geom.Rectangle2D;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class TriangleRemeshingUtility {


	private static double ystep = 0.5;
	private static double xstep = 1.0/6.0;

//	private static ConverterHeds2JR converter = new ConverterHeds2JR();
//	
//	public static void createSkewTriangleMesh(VHDS hds, Rectangle2D bbox) {
//		double xSpan = bbox.getWidth();
//		double ySpan = bbox.getHeight();
//		int yRes = (int)Math.ceil(ySpan / ystep);
//		int xRes = (int)Math.ceil(xSpan / xstep)+(yRes-1)/2;
//		
//		for (int i = 0; i < xRes; i++) {
//			for (int j = 0; j < yRes; j++) {
//				VVertex v = hds.addNewVertex();
//				double xPos = bbox.getMinX() - ((yRes-1)/2)*2*xstep + (2*i+j)*xstep ;
//				double yPos = bbox.getMinY() + j * ystep;
//				v.position = new double[]{xPos,yPos,1.0};
//				v.texcoord = new double[]{xPos,yPos,1.0};
//			}
//		}
//		for (int i = 0; i < xRes  - 1; i++) {
//			for (int j = 0; j < yRes - 1; j++) {
//				VVertex v1 = hds.getVertex(i*yRes + j); 
//				VVertex v2 = hds.getVertex((i+1)*yRes + j); 
//				VVertex v3 = hds.getVertex((i+1)*yRes + j + 1); 
//				VVertex v4 = hds.getVertex(i*yRes + j + 1); 
//				HalfEdgeUtils.constructFaceByVertices(hds, v1, v2, v4);
//				HalfEdgeUtils.constructFaceByVertices(hds, v2, v3, v4);
//			}
//		}
//	}
//	
//
//	public static void fitToBoundary(CoHDS hds, VHDS r, int xRes, int yRes, Matrix texInvMatrix) throws RemeshingException {
//		Vector<VVertex> boundaryCorners = new Vector<VVertex>();
//		LinkedList<VVertex> boundaryVertices = new LinkedList<VVertex>();
//		
//		SceneGraphComponent root = new SceneGraphComponent();
//		SceneGraphComponent sgc1 = new SceneGraphComponent();
//		sgc1.setGeometry(converter.heds2ifs(r, new AdapterSet(new VPositionAdapter())));
//		root.addChild(sgc1);
//		
//		System.out.println("find lattice polygon...");
//		findLatticePolygon(hds, r, xRes, yRes, texInvMatrix, boundaryCorners, boundaryVertices);
//		System.out.println("create induced polygon...");
//		SceneGraphComponent sgc2 = new SceneGraphComponent();
//		sgc2.setGeometry(converter.heds2ifs(r, new AdapterSet(new VPositionAdapter())));
//		root.addChild(sgc2);
//		
//		LinkedList<VEdge> polygon = createInducedPolygon(r,boundaryVertices,true);
//		System.out.println("remove outside...");
//		SceneGraphComponent sgc3 = new SceneGraphComponent();
//		sgc3.setGeometry(converter.heds2ifs(r, new AdapterSet(new VPositionAdapter())));
//		root.addChild(sgc3);
//		
//		QuadRemeshingUtility.removeOutside(r,polygon);
//		System.out.println("straighten boundary...");
//		SceneGraphComponent sgc4 = new SceneGraphComponent();
//		sgc4.setGeometry(converter.heds2ifs(r, new AdapterSet(new VPositionAdapter())));
//		root.addChild(sgc4);
//		
//		SpringRemeshingUtility.straightenBoundary(polygon, boundaryCorners);
//		SceneGraphComponent sgc5 = new SceneGraphComponent();
//		sgc5.setGeometry(converter.heds2ifs(r, new AdapterSet(new VPositionAdapter())));
//		root.addChild(sgc5);
//		
//		System.out.println("relax interior...");
//		SpringRemeshingUtility.relaxInterior(r, boundaryCorners, true, false);
//		SceneGraphComponent sgc6 = new SceneGraphComponent();
//		sgc6.setGeometry(converter.heds2ifs(r, new AdapterSet(new VPositionAdapter())));
//		root.addChild(sgc6);
//
//		System.out.println("project onto boundary...");
//		RemeshingUtility.projectOntoBoundary(r, hds);
//		SceneGraphComponent sgc7 = new SceneGraphComponent();
//		sgc7.setGeometry(converter.heds2ifs(r, new AdapterSet(new VPositionAdapter())));
//		root.addChild(sgc7);
//		
////		JRViewer.display(root);
//		
//}
//
//	private static LinkedList<VEdge> createInducedPolygon(VHDS r, LinkedList<VVertex> bdVerts, boolean newVertices) throws RemeshingException {
//		LinkedList<VEdge> polygon = new LinkedList<VEdge>();
//		for(int i = 0; i < bdVerts.size(); ++i) {
//			VVertex nextVertex = bdVerts.get((i+1)%bdVerts.size());
//			VVertex actVertex = bdVerts.get(i);
//			VEdge e = HalfEdgeUtils.findEdgeBetweenVertices(actVertex,nextVertex);
//			if(e == null) {
//				for(VEdge re: HalfEdgeUtilsExtra.get1Ring(actVertex)) {
//					VFace rf = re.getRightFace();
//					if(rf != null && HalfEdgeUtils.boundaryVertices(rf).contains(nextVertex)) {
//						if(newVertices) {
//							VFace f = TopologyAlgorithms.removeEdgeFill(re);
//							VVertex v = TopologyAlgorithms.splitFace(f);
//							v.position = Rn.times(null, 0.5, Rn.add(null, actVertex.position, nextVertex.position));
//							v.texcoord =  Rn.times(null, 0.5, Rn.add(null, actVertex.texcoord, nextVertex.texcoord));
//							polygon.add(HalfEdgeUtils.findEdgeBetweenVertices(actVertex,v));
//							e = HalfEdgeUtils.findEdgeBetweenVertices(v,nextVertex);
//						} else {
//							TopologyAlgorithms.flipEdge(re);
//							if(re.getStartVertex() == actVertex) {
//								e = re;
//							} else {
//								e = re.getOppositeEdge();
//							}
//							break;
//						}
//					}
//				}
//			}
//			if(e == null) {
//				throw new RemeshingException("Could not create induced polygon");
//			}
//			polygon.add(e);
//		}
//		return polygon;
//		
//	}
//
//	private static void findLatticePolygon(
//			CoHDS hds, VHDS r, 
//			int xRes, int yRes, Matrix texInvMatrix,
//			Vector<VVertex> boundaryCorners,
//			LinkedList<VVertex> boundaryVertices) throws RemeshingException 
//	{
//		
//		CoEdge be = HalfEdgeUtils.boundaryEdges(hds).iterator().next();
//		be = TextureUtility.findNextTextureCorner(be);
//		CoEdge e = be;
//		VVertex gridVertex = RemeshingUtility.bruteForceNearest(e.getStartVertex().getTextureCoord(), r);
//		gridVertex.setPosition(new geom3d.Point(e.getStartVertex().getTextureCoord()));
//		
//		boundaryCorners.add(gridVertex);
//		boundaryVertices.add(gridVertex);
//		Point dir = new geom3d.Point(e.getTargetVertex().getTextureCoord());
//		dir.subtract(e.getStartVertex().getTextureCoord());
//
//		int step = getStep(xRes,yRes,TextureUtility.transformCoord(dir,texInvMatrix));
//		do {
//			e = TextureUtility.findNextTextureCorner(e);
//			
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
//			step = getStep(xRes,yRes,TextureUtility.transformCoord(dir,texInvMatrix));
//		} while (e != be);
//		boundaryCorners.remove(boundaryCorners.size()-1);
//		if(boundaryVertices.getFirst() != boundaryVertices.getLast()) { //FIXME!
//			throw new RemeshingException("Could not create lattice polygon: " +
//					"polygon did not close " + 
//					boundaryVertices.getFirst() + "!=" + boundaryVertices.getLast());
//		}
//		boundaryVertices.removeLast();
//	}
//	
//	private static int getStep(int xRes, int yRes, Point dir) {
//		double theta = Math.atan2(dir.y(),dir.x());  
//		int i = (int) Math.round(6*theta/Math.PI);
//		int[] steps = new int[] { 
//	        yRes, yRes+1, 1, -yRes + 2, -yRes+1, -2*yRes+1,
//	       -yRes, -yRes-1, -1, yRes-2, yRes-1, 2*yRes-1};
//		return steps[(12+i)%12];
//	}

	
	public static void createRectangularTriangleMesh(VHDS hds, Rectangle2D bbox) {
		double xSpan = bbox.getWidth();
		double ySpan = bbox.getHeight();
		int xRes = (int)Math.ceil(xSpan / xstep);
		int yRes = (int)Math.ceil(ySpan / ystep); 
		
		
		for (int i = 0; i < xRes; i++) {
			for (int j = 0; j < yRes; j++) {
				VVertex v = hds.addNewVertex();
				double move = j % 2 == 0 ? 0.0 : xstep;
				double xPos = bbox.getMinX() + 2*i*xstep + move;
				double yPos = bbox.getMinY() + j * ystep;
				v.setP(new double[]{xPos, yPos, 0.0, 1.0});
				v.setT(new double[]{xPos, yPos, 0.0, 1.0});
			}
		}
		for (int i = 0; i < xRes  - 1; i++) {
			for (int j = 0; j < yRes - 1; j++) {
				VVertex v1 = hds.getVertex(i*yRes + j); 
				VVertex v2 = hds.getVertex((i+1)*yRes + j); 
				VVertex v3 = hds.getVertex((i+1)*yRes + j + 1); 
				VVertex v4 = hds.getVertex(i*yRes + j + 1); 
				if (j % 2 == 0) {
					HalfEdgeUtils.constructFaceByVertices(hds, v1, v4, v2);
					HalfEdgeUtils.constructFaceByVertices(hds, v2, v4, v3);
				} else {
					HalfEdgeUtils.constructFaceByVertices(hds, v1, v3, v2);
					HalfEdgeUtils.constructFaceByVertices(hds, v1, v4, v3);
				}
			}
		}
	}
}
