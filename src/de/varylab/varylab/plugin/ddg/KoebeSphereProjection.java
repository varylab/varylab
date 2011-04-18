package de.varylab.varylab.plugin.ddg;

import static de.varylab.varylab.plugin.ddg.ChristoffelTransform.decomposeEdgeLength;
import static de.varylab.varylab.plugin.ddg.ChristoffelTransform.getIncircle;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.projgeom.PlueckerLineGeometry;

public class KoebeSphereProjection extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.DDG;
	}

	@Override
	public String getAlgorithmName() {
		return "Koebe Sphere Projection";
	}

	
	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] getSphereCenter(E e, AdapterSet a) {
		F fr = e.getRightFace();
		F fl = e.getLeftFace();
		double[] cr = getIncircle(fr, a);
		double[] cl = getIncircle(fl, a);
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
		double[] lineIntersection = PlueckerLineGeometry.intersectionPointUnchecked(null, linel, liner);
		Pn.dehomogenize(lineIntersection, lineIntersection);
		return lineIntersection;
	}
	
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] getCircleTouchPoint(E e, AdapterSet a) {
		double[] r12 = decomposeEdgeLength(e, a);
		double[] pstart = a.getD(Position3d.class, e.getStartVertex());
		double[] pend = a.getD(Position3d.class, e.getTargetVertex());
		double rsum = r12[0] + r12[1];
		double[] p0 = Rn.linearCombination(null, r12[0] / rsum, pstart, r12[1] / rsum, pend);
		return Pn.homogenize(null, p0);	
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		double[] center = {0,0,0,0};
		double radius = 0;
		int count = 0;
		for (E e : hds.getEdges()) {
			if (HalfEdgeUtils.isBoundaryEdge(e)) continue;
			double[] eCenter = getSphereCenter(e, a);
			Rn.add(center, center, eCenter);
			double[] eTouch = getCircleTouchPoint(e, a);
			double r = Pn.distanceBetween(eCenter, eTouch, Pn.EUCLIDEAN);
			radius += r;
			count++;
		}
		radius /= count;
		Pn.dehomogenize(center, center);
		
//		for (V v : hds.getVertices()) {
//			double[] p = a.getD(Position3d.class, v);
//			p[0] -= center[0]; p[1] -= center[1]; p[2] -= center[2];
//			Rn.setToLength(p, p, radius);
//			p[0] += center[0]; p[1] += center[1]; p[2] += center[2];
//			a.set(Position.class, v, p);
//		}
//		hi.update();
		SceneGraphComponent cg = new SceneGraphComponent();
		cg.setGeometry(Primitives.sphere(30));
		MatrixBuilder.euclidean().translate(center).scale(radius).assignTo(cg);
		hi.getActiveLayer().addTemporaryGeometry(cg);
	}

}
