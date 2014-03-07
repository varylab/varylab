package de.varylab.varylab.plugin.datasource;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;

import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.shader.CommonAttributes;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.numericalMethods.util.Arrays;
import de.varylab.varylab.halfedge.adapter.type.ConicalNormal;
import de.varylab.varylab.utilities.MathUtility;

public class SCConicalConeDataSource extends Plugin implements DataSourceProvider {

	private static IndexedFaceSet
		coneGeometry = Primitives.cone(80);
	private static double[]
		e4 = {0,0,0,1};
	private SceneGraphComponent
		cone01 = new SceneGraphComponent(),
		cone02 = new SceneGraphComponent();
	
	public SCConicalConeDataSource() {
		Appearance coneAppearance = new Appearance();
		coneAppearance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		coneAppearance.setAttribute(CommonAttributes.EDGE_DRAW, false);
		coneAppearance.setAttribute(CommonAttributes.FACE_DRAW, true);
		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, new Color(60, 140, 200));
		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SPECULAR_COLOR, Color.RED);
		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SPECULAR_COEFFICIENT, 1.0);
		cone01.setAppearance(coneAppearance);
		cone02.setAppearance(coneAppearance);
		cone01.setGeometry(coneGeometry);
		cone02.setGeometry(coneGeometry);
		MatrixBuilder mb01 = MatrixBuilder.euclidean();
		MatrixBuilder mb02 = MatrixBuilder.euclidean();
		mb01.rotate(Math.PI, 1, 0, 0);
		mb01.translate(0, 0, -1);
		mb02.translate(0, 0, -1);
		mb01.assignTo(cone01);
		mb02.assignTo(cone02);
	}
	
	
	private class SCConicalConesAdapter01 extends AbstractAdapter<SceneGraphNode> {

		public SCConicalConesAdapter01() {
			super(SceneGraphNode.class, true, false);
		}
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nc) {
			return Vertex.class.isAssignableFrom(nc);
		}
		@Override
		public String toString() {
			return "Vertex Cones 01";
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
			SceneGraphComponent root = createConeRootAtVertex(v, a);
			root.addChild(cone01);
			return root;
		}

	}
	
	private class SCConicalConesAdapter02 extends AbstractAdapter<SceneGraphNode> {

		public SCConicalConesAdapter02() {
			super(SceneGraphNode.class, true, false);
		}
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nc) {
			return Vertex.class.isAssignableFrom(nc);
		}
		@Override
		public String toString() {
			return "Vertex Cones 02";
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
			SceneGraphComponent root = createConeRootAtVertex(v, a);
			root.addChild(cone02);
			return root;
		}

	}
	
	private class SCConicalDiagonalIntersectionAdapter extends AbstractAdapter<SceneGraphNode> {

		public SCConicalDiagonalIntersectionAdapter() {
			super(SceneGraphNode.class, true, false);
		}
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nc) {
			return Face.class.isAssignableFrom(nc);
		}
		@Override
		public String toString() {
			return "Vertex Cones Diagonal Intersections";
		}
		@Override
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> SceneGraphNode getF(F f, AdapterSet a) {
			double[] diagX = MathUtility.calculateDiagonalIntersection(f, a);
			SceneGraphComponent c = new SceneGraphComponent();
			c.setGeometry(Primitives.point(diagX));
			return c;
		}

	}
	
	
	protected <
		F extends Face<V, E, F>, 
		E extends Edge<V, E, F>, 
		V extends Vertex<V, E, F>
	> SceneGraphComponent createConeRootAtVertex(V v, AdapterSet a) {
		E refEdge = null;
		for (E e : HalfEdgeUtils.incomingEdges(v)) {
			if (e.getLeftFace() != null) {
				refEdge = e;
				break;
			}
		}
		F refFace = refEdge.getLeftFace();
		double[] vPoint = a.getD(Position4d.class, v);
		double[] diagX = MathUtility.calculateDiagonalIntersection(refFace, a);
		double[] fNormal = a.getD(Normal.class, refFace);
		double[] conicalNormal = a.getD(ConicalNormal.class, v);
		double[] dir = Rn.projectOntoComplement(null, conicalNormal, fNormal);
		double len = Rn.euclideanNorm(Rn.subtract(null, diagX, vPoint));
		Rn.setToLength(dir, dir, len);
		
		conicalNormal = Arrays.resize(conicalNormal, 4);
		dir = Arrays.resize(dir, 4);
		Matrix T = getConeMatrix(vPoint, conicalNormal, dir);
		SceneGraphComponent c = new SceneGraphComponent();
		T.assignTo(c);
		return c;
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
		return new AdapterSet(new SCConicalConesAdapter01(), new SCConicalConesAdapter02(), new SCConicalDiagonalIntersectionAdapter());
	}

}
