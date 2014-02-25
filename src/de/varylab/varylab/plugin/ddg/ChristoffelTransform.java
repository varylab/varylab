package de.varylab.varylab.plugin.ddg;

import static de.jtem.halfedge.util.HalfEdgeUtils.isBoundaryEdge;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.projgeom.PlueckerLineGeometry;
import de.varylab.discreteconformal.util.NodeIndexComparator;
import de.varylab.discreteconformal.util.Search;
import de.varylab.varylab.utilities.GeometryUtility;

public class ChristoffelTransform extends AlgorithmDialogPlugin {

	public static enum NormalMethod {
		LS_Sphere,
		Face_Average,
		Face_Sphere,
		V_Spheres
	}

	private CentralExtensionSubdivision
		ceSubdivider = null;
	private JPanel
		panel = new JPanel();
	private JCheckBox
		useCentralExtensionChecker = new JCheckBox("Use Central Extension");
	private SpinnerNumberModel
		associateModel = new SpinnerNumberModel(0.0, 0.0, 360.0, 0.1);
	private JSpinner
		associateSpinner = new JSpinner(associateModel);
	private HalfedgeInterface hif = null;
	
	private static double[]
	    lsSphereCoords = null;
	
	public ChristoffelTransform() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		panel.add(useCentralExtensionChecker, cr);
		panel.add(new JLabel("Associaled Family"), cl);
		panel.add(associateSpinner, cr);
	}
	
	@Override
	public JPanel getDialogPanel() {
		return panel;
	}
	
	public  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> getRadomInnerPoints(HDS hds, int mcSamples) {
		Random rnd = new Random();
		rnd.setSeed(hds.numVertices());
		
		Set<V> result = new HashSet<V>();
		for (int i = 0; i < Math.min(mcSamples, hds.numVertices()); i++) {
			int sampleIndex = rnd.nextInt(hds.numVertices());
			V sampleVertex = hds.getVertex(sampleIndex);
			if (!HalfEdgeUtils.isBoundaryVertex(sampleVertex)) {
				result.add(sampleVertex);				
			}
		}
		
		if (result.isEmpty()) {
			result.add(hds.getVertex(rnd.nextInt(hds.numVertices())));
		}
		return result;
	}
	
	
	/**
	 * Implements a heuristic for finding a root vertex for the
	 * hyperbolic layout. Its numerically best to have equal distance to the boundary. 
	 * @param hds
	 * @param lMap
	 * @param mcSamples
	 * @return
	 */
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> V guessRootVertex(
		HDS hds, 
		int mcSamples
	) {
		// deterministic random numbers
		Set<V> mcSet = getRadomInnerPoints(hds, mcSamples);
		Map<V, Double> sMap = new HashMap<V, Double>();
		
		Set<V> boundary = new TreeSet<V>(new NodeIndexComparator<V>());
		boundary.addAll(HalfEdgeUtils.boundaryVertices(hds));
		for (V v : mcSet) {
			double mean = 0;
			Map<V, Double> distMap = new HashMap<V, Double>();
			Map<V, List<E>> pathMap = Search.getAllShortestPaths(v, boundary, null, new HashSet<V>());
			for (V bv : boundary) {
				List<E> path = pathMap.get(bv);
				double length = path.size();
				mean += length;
				distMap.put(bv, length);
			}
			mean /= boundary.size();
			double s = 0.0;
			for (V bv : distMap.keySet()) {
				double dist = distMap.get(bv);
				s += (mean - dist) * (mean - dist);
			}
			s /= boundary.size();
			sMap.put(v, s);
		}
		V root = mcSet.iterator().next();
		double minS = Double.MAX_VALUE; 
		for (V v : sMap.keySet()) {
			double s = sMap.get(v);
			if (s < minS) {
				minS = s;
				root = v;
			}
		}
		return root;
	}
	
	
	
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void dualize(HDS hds, double phi, AdapterSet a, NormalMethod method) {
		if (method == NormalMethod.LS_Sphere) {
			lsSphereCoords = LeastSquaresSphere.getSphereCenter(hds, a);
		}
		List<Map<V, double[]>> newCoordsMaps = new LinkedList<Map<V, double[]>>();
		Set<V> rootSet = getRadomInnerPoints(hds, 10);
		for (V v0 : rootSet) {
			Map<V, double[]> newCoordsMap = new HashMap<V, double[]>();
			newCoordsMaps.add(newCoordsMap);
			HashSet<V> readyVertices = new HashSet<V>();
			LinkedList<V> vertexQueue = new LinkedList<V>();
	//		V v0 = hds.getVertex(0);
	//		V v0 = guessRootVertex(hds, 100);
			vertexQueue.offer(v0);
			//vertex 0 in 0.0;
			double[] v0Pos = a.getD(Position3d.class, v0);
			newCoordsMap.put(v0, v0Pos.clone());
			while (!vertexQueue.isEmpty()){
				V v = vertexQueue.poll();
				double[] startCoord = newCoordsMap.get(v);
				List<E> star = HalfEdgeUtils.incomingEdges(v);
				for (E e : new HashSet<E>(star)) {
					V v2 = e.getStartVertex();
					if (readyVertices.contains(v2))
						continue;
					else {
						vertexQueue.offer(v2);
						readyVertices.add(v2);
					}
	//				double[] pv = a.getD(Position3d.class, v);
	//				double[] pv2 = a.getD(Position3d.class, v2);
	//				double[] vec = Rn.subtract(null, pv2, pv);
					double[] vec = getAssociatedEdgeVector(e, phi, a, method);
					//double factor = Rn.euclideanDistanceSquared(pv, pv2);
					E de = e.getLeftFace() != null ? e : e.getOppositeEdge();
					double factor = 0;
					double[] r12 = decomposeEdgeLength(de, a);
					factor = r12[0] * r12[1];
					Boolean edgeSign = a.get(EdgeSign.class, e, Boolean.class);
					double scale = (edgeSign ? -1 : 1) / factor;
					vec[0] *= scale;
					vec[1] *= scale;
					vec[2] *= scale;
					Rn.add(vec, vec, startCoord);
					vec = Pn.homogenize(null, vec);
					newCoordsMap.put(v2, vec);
				}
			}
		}
		
		Map<V, double[]> newCoordsMap = new HashMap<V, double[]>();
		for (Map<V, double[]> map : newCoordsMaps) {
			for (V v : map.keySet()) {
				if (!newCoordsMap.containsKey(v)) {
					newCoordsMap.put(v, new double[4]);
				}
				double[] p = map.get(v);
				double[] pMean = newCoordsMap.get(v);
				Rn.add(pMean, pMean, p);
			}
		}
		for (V v : newCoordsMap.keySet()) {
			double[] p = newCoordsMap.get(v);
			Pn.dehomogenize(p, p);
		}
		
//		// transfom to have offset edges in the CMC case
//		Matrix T = findOffsetTransfom(hds, newCoordsMap, a);
//		for (double[] p : newCoordsMap.values()) {
//			T.transformVector(p);			
//		}
		
		for (V v : hds.getVertices()){
			double[] p = newCoordsMap.get(v);
			if (p != null) {
				a.set(Position.class, v, p);
			}
		}	
	}
	
	
	
	
	/**
	 * Find a translation and uniform scaling such that the new positions
	 * form a edge offset mesh the CMC case.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param hds1
	 * @param posMap
	 * @param a
	 * @return
	 */
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Matrix findOffsetTransfom(HDS hds1, HashMap<V, double[]> posMap, AdapterSet a) {
		
		
		return new Matrix();
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void transform(HDS hds, AdapterSet a, double phi, boolean useCentralExtension, NormalMethod method) {
		EdgeSignAdapter esa = new EdgeSignAdapter();
		a.add(esa);
		if (useCentralExtension) {
			Map<F, V> oldFnewVMap = new HashMap<F, V>();
			Map<E, V> oldEnewVMap = new HashMap<E, V>();
			Map<V, V> oldVnewVMap = new HashMap<V, V>();
			HDS ce = ceSubdivider.subdivide(hds, a, oldFnewVMap, oldEnewVMap, oldVnewVMap);
//			createEdgeLabels(ce, a);
			dualize(ce, phi, a, method);
			for (V v : hds.getVertices()) {
				V newV = oldVnewVMap.get(v);
				a.set(Position.class, v, a.getD(Position3d.class, newV));
			}
		} else {
//			createEdgeLabels(hds, a);
			dualize(hds, phi, a, method);
			hif.addLayerAdapter(esa, false);
		}
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		double phi = associateModel.getNumber().doubleValue() * PI / 180.0;
		boolean useCentralExtension = useCentralExtensionChecker.isSelected();
		transform(hds, a, phi, useCentralExtension, NormalMethod.Face_Average);
		hif.update();
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.DDG;
	}

	@Override
	public String getAlgorithmName() {
		return "Christoffel Transfom";
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		ceSubdivider = c.getPlugin(CentralExtensionSubdivision.class);
		c.getPlugin(LeastSquaresSphere.class);
		hif = c.getPlugin(HalfedgeInterface.class);
	}

	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] decomposeEdgeLength(E e, AdapterSet a) {
		F f = e.getLeftFace();
		double[] c = GeometryUtility.getIncircle(f, a);
		double r = c[3];
		double[] p0 = a.getD(Position3d.class, e.getStartVertex());
		double[] p1 = a.getD(Position3d.class, e.getTargetVertex());
		double[] p2 = a.getD(Position3d.class, e.getPreviousEdge().getStartVertex());
		double[] p3 = a.getD(Position3d.class, e.getNextEdge().getTargetVertex());
		double[] vec1 = Rn.subtract(null, p1, p0);
		double[] vec2 = Rn.subtract(null, p2, p0);
		double alpha = Rn.euclideanAngle(vec1, vec2);
		double r1 = r / Math.tan(alpha / 2);
		
		vec1 = Rn.subtract(null, p0, p1);
		vec2 = Rn.subtract(null, p3, p1);
		alpha = Rn.euclideanAngle(vec1, vec2);
		double r2 = r / Math.tan(alpha / 2);
		return new double[] {r1, r2};
	}
	
	
	
	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getAssociatedNormal(E e, AdapterSet a) {
		// find an edge that is a non-boundary edge
		if (e.getLeftFace() == null && e.getRightFace() == null) {
			return null;
		}
		if (e.getLeftFace() == null) {
			e = e.getOppositeEdge();
		}
		double[] inter = {0,0,0,0};
		
		// left face parameter line
//		int count = 0;
		E ne = e;
		do {
//			if (count++ % 2 != 0) {
//				ne = ne.getNextEdge();
//				continue;
//			}
			if (isBoundaryEdge(ne)) {
				ne = ne.getNextEdge();
				continue;
			}
			F fr = ne.getRightFace();
			F fl = ne.getLeftFace();
			double[] cr = GeometryUtility.getIncircle(fr, a);
			double[] cl = GeometryUtility.getIncircle(fl, a);
			double[] nr = a.getD(Normal.class, fr);
			double[] nl = a.getD(Normal.class, fl);
			cr[3] = 1;
			cl[3] = 1;
			nr = Pn.homogenize(null, nr);
			nl = Pn.homogenize(null, nl);
			nr[3] = 0;
			nl[3] = 0;
			double[] linel = PlueckerLineGeometry.lineFromPoints(null, cl, nl);
			double[] liner = PlueckerLineGeometry.lineFromPoints(null, cr, nr);
			double[] edgeInter = PlueckerLineGeometry.intersectionPointUnchecked(null, linel, liner);
			Pn.dehomogenize(edgeInter, edgeInter);
			Rn.add(inter, inter, edgeInter);
			ne = ne.getNextEdge();
		} while (ne != e);
		
		// right face parameter line
//		count = 0;
		ne = e.getOppositeEdge();
		do {
//			if (count++ % 2 != 0) {
//				ne = ne.getNextEdge();
//				continue;
//			}
			if (isBoundaryEdge(ne)) {
				ne = ne.getNextEdge();
				continue;
			}
			F fr = ne.getRightFace();
			F fl = ne.getLeftFace();
			double[] cr = GeometryUtility.getIncircle(fr, a);
			double[] cl = GeometryUtility.getIncircle(fl, a);
			double[] nr = a.getD(Normal.class, fr);
			double[] nl = a.getD(Normal.class, fl);
			cr[3] = 1;
			cl[3] = 1;
			nr = Pn.homogenize(null, nr);
			nl = Pn.homogenize(null, nl);
			nr[3] = 0;
			nl[3] = 0;
			double[] linel = PlueckerLineGeometry.lineFromPoints(null, cl, nl);
			double[] liner = PlueckerLineGeometry.lineFromPoints(null, cr, nr);
			double[] edgeInter = PlueckerLineGeometry.intersectionPointUnchecked(null, linel, liner);
			Pn.dehomogenize(edgeInter, edgeInter);
			Rn.add(inter, inter, edgeInter);
			ne = ne.getNextEdge();
		} while (ne != e.getOppositeEdge());
		
		Pn.dehomogenize(inter, inter);
		double[] r12 = decomposeEdgeLength(e, a);
		double[] pstart = a.getD(Position3d.class, e.getStartVertex());
		double[] pend = a.getD(Position3d.class, e.getTargetVertex());
		double rsum = r12[0] + r12[1];
		double[] p0 = Rn.linearCombination(null, r12[0] / rsum, pstart, r12[1] / rsum, pend);
		p0 = Pn.homogenize(null, p0);
		if ( Math.abs(inter[3]) < 1E-10) return inter;
		double[] N = Rn.subtract(null, p0, inter);
		return Rn.normalize(N, N);
	}
	
	
	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getAssociatedEdgeVector(E e, double phi, AdapterSet a, NormalMethod method) {
		if (phi == 0) return a.getD(EdgeVector.class, e);
		double[] ne = a.getD(Normal.class, e);
		double[] vn = null;
		switch (method) {
		case LS_Sphere:
			double[] ctp = KoebeSphereProjection.getCircleTouchPoint((e.getLeftFace()==null)?e.getOppositeEdge():e, a);
			Pn.dehomogenize(ctp, ctp);
			double[] center = new double[]{lsSphereCoords[0],lsSphereCoords[1],lsSphereCoords[2],1.0};
			vn = Rn.subtract(null, ctp, center);
			vn[3] = 1.0;
			break;
		case Face_Sphere:
			vn = getAssociatedNormal(e, a);
			break;
		default:
			vn = ne;
			break;
		}
		Rn.normalize(vn, vn);
		if (Rn.innerProduct(ne, vn) < 0) {
			Rn.times(vn, -1, vn);
		}
		double[] vx = a.getD(EdgeVector.class, e);
		double length = Rn.euclideanNorm(vx);
		Rn.normalize(vx, vx);
		double[] vy = Rn.crossProduct(null, vn, vx);
		Rn.normalize(vy, vy);
		double[] vr = Rn.linearCombination(null, cos(phi), vx, sin(phi), vy);
		return Rn.setToLength(vr, vr, length);
	}
	

}
