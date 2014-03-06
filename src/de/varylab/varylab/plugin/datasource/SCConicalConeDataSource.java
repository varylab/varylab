package de.varylab.varylab.plugin.datasource;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.numericalMethods.util.Arrays;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.type.ConicalNormal;
import de.varylab.varylab.utilities.MathUtility;

public class SCConicalConeDataSource extends Plugin implements DataSourceProvider {

	private static IndexedFaceSet
		coneGeometry = Primitives.cone(80);
	private static double[]
		e4 = {0,0,0,1};
	private Appearance
		coneAppearance = new Appearance();
	
	public SCConicalConeDataSource() {
		coneAppearance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		coneAppearance.setAttribute(CommonAttributes.EDGE_DRAW, false);
		coneAppearance.setAttribute(CommonAttributes.FACE_DRAW, true);
		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, new Color(60, 140, 200));
		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SPECULAR_COLOR, Color.RED);
		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SPECULAR_COEFFICIENT, 1.0);
	}
	
	
	private class SCConicalConesAdapter extends AbstractAdapter<SceneGraphNode> {

		public SCConicalConesAdapter() {
			super(SceneGraphNode.class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nc) {
			return nc.equals(VVertex.class);
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> SceneGraphNode getV(V v, AdapterSet a) {
			if (HalfEdgeUtils.isBoundaryVertex(v)) {
				return null;
			}
			E refEdge = null;
			for (E e : HalfEdgeUtils.incomingEdges(v)) {
				if (e.getLeftFace() != null) {
					refEdge = e;
					break;
				}
			}
			F refFace = refEdge.getLeftFace();
			double[] vPoint = a.getD(Position4d.class, v);
			double[] fNormal = a.getD(Normal.class, refFace);
			
//			double[] koenigsPoint = calculateDiagonalIntersection(refFace, a);
//			double[] koenigsVec = Rn.subtract(null, koenigsPoint, vPoint);
			double[] conicalNormal = a.getD(ConicalNormal.class, v);
			double[] dir = Rn.projectOnto(null, conicalNormal, fNormal);
			conicalNormal = Arrays.resize(conicalNormal, 4);
			dir = Arrays.resize(dir, 4);
			Matrix T = getConeMatrix(vPoint, conicalNormal, dir);
			SceneGraphComponent c = new SceneGraphComponent();
			c.setGeometry(coneGeometry);
			c.setAppearance(coneAppearance);
			T.assignTo(c);
			return c;
		}
		
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> double[] calculateDiagonalIntersection(F f, AdapterSet a) {
//			E e1 = f.getBoundaryEdge();
//			E e2 = e1.getNextEdge();
//			E e3 = e2.getNextEdge();
//			E e4 = e3.getNextEdge();
//			double[] p1 = a.getD(Position4d.class, e1);
//			double[] p2 = a.getD(Position4d.class, e2);
//			double[] p3 = a.getD(Position4d.class, e3);
//			double[] p4 = a.getD(Position4d.class, e4);
//			return p1;
			return a.getD(BaryCenter4d.class, f);
		}
		
	}
	
	public static Matrix getConeMatrix(double[] apex, double[] normal, double[] dir) {
		Rn.normalize(normal, normal);
		double[] ty = Rn.crossProduct(null, normal, dir);
		ty = Arrays.resize(Rn.normalize(ty, ty), 4);
		double[] tx = Rn.crossProduct(null, ty, normal);
		tx = Arrays.resize(Rn.normalize(tx, tx), 4);
		double dot = Rn.innerProduct(tx, dir);
		Rn.times(tx, dot, tx);
		Rn.times(ty, dot, ty);
		double[][] from = {{1,0,0,0}, {0,1,0,0}, {1,0,1,0}, e4};
		double[][] to = {tx, ty, dir, e4};
		Matrix T = MathUtility.makeMappingMatrix(from, to);
		
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.translate(apex);
		mb.times(T);
		mb.rotate(Math.PI, 1, 0, 0);
		mb.translate(0, 0, -1);
		return mb.getMatrix();
	}
	
	public static void main(String[] args) {
		SceneGraphComponent root = new SceneGraphComponent();
		Appearance app = new Appearance();
		app.setAttribute(VERTEX_DRAW, false);
		app.setAttribute(EDGE_DRAW, false);
		root.setAppearance(app);
		SceneGraphComponent zero = new SceneGraphComponent();
		zero.setGeometry(Primitives.coloredCube());
		MatrixBuilder.euclidean().scale(0.05).assignTo(zero);
		SceneGraphComponent c = new SceneGraphComponent();
		c.setGeometry(coneGeometry);
		
		double[] apex = {1,0,0,1};
		double[] normal = {0,2,1,0};
		double[] dir = {2,1,0,0};
		Matrix T = getConeMatrix(apex, normal, dir);
		T.assignTo(c);
		root.addChild(zero);
		root.addChild(c);
		
		JRViewer.display(root);
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(new SCConicalConesAdapter());
	}

}
