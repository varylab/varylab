package de.varylab.varylab.plugin.affineminimal;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import de.jreality.geometry.IndexedFaceSetUtility;
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
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.utilities.SelectionUtility;

public class DirectingPlanesDataSource extends Plugin implements DataSourceProvider {

	private static IndexedFaceSet
		planeGeometry = IndexedFaceSetUtility.constructPolygon(
				new double[][]{
						{0,-0.25,0,1},{0,0.25,0,1},
						{1,0.25,0,1},{1,-0.25,0,1}
				});
	
	private SceneGraphComponent
		plane = new SceneGraphComponent("Directing plane");
	
	public DirectingPlanesDataSource() {
		Appearance coneAppearance = new Appearance();
		coneAppearance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		coneAppearance.setAttribute(CommonAttributes.EDGE_DRAW, false);
		coneAppearance.setAttribute(CommonAttributes.FACE_DRAW, true);
//		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, new Color(60, 140, 200));
//		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SPECULAR_COLOR, Color.RED);
//		coneAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.SPECULAR_COEFFICIENT, 1.0);
		plane.setAppearance(coneAppearance);
		plane.setGeometry(planeGeometry);
	}
	
	
	private class DirectingPlanesAdapter extends AbstractAdapter<SceneGraphNode> {

		public DirectingPlanesAdapter() {
			super(SceneGraphNode.class, true, false);
		}
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nc) {
			return Edge.class.isAssignableFrom(nc);
		}
		@Override
		public String toString() {
			return "Directing Planes";
		}
		@Override
		public <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> SceneGraphNode getE(E e, AdapterSet a) {
			if (e.getLeftFace()==null) {
				return null;
			}
			SceneGraphComponent root = createRootAtEdge(e, a);
			root.addChild(plane);
			return root;
		}

	}
	
	protected <
		F extends Face<V, E, F>, 
		E extends Edge<V, E, F>, 
		V extends Vertex<V, E, F>
	> SceneGraphComponent createRootAtEdge(E e, AdapterSet a) {
		E oe = SelectionUtility.getOppositeEdgeInFace(e);
		if(oe == null) {
			return null;
		}
		double[]
				ev = a.getD(EdgeVector.class, e),
				oev= a.getD(EdgeVector.class,oe);
		
		
		double[] 
				v = a.getD(Position3d.class, e.getStartVertex());
		
		SceneGraphComponent c = new SceneGraphComponent(e.toString());
		Matrix T = getTransformationMatrix(v, ev, oev);
		T.assignTo(c);
		return c;
	}
	
	private static Matrix getTransformationMatrix(double[] o, double[] v1, double[] v2) {
		double s = Rn.euclideanNorm(v1);
		Rn.times(v1,1.0,Rn.normalize(v1, v1));
		double[] n = Rn.normalize(null, Rn.crossProduct(null, v1, v2));
		Rn.normalize(v2, Rn.crossProduct(v2, n, v1));
		Matrix M = new Matrix(v1[0],v2[0],n[0],0,v1[1],v2[1],n[1],0,v1[2],v2[2],n[2],0,0,0,0,1.0);
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.translate(Rn.times(null, 1.0, o));
		mb.scale(s);
		return Matrix.times(mb.getMatrix(),M);
//		return M;
	}
	
	public static void main(String[] args) {
		SceneGraphComponent root = new SceneGraphComponent();
		Appearance app = new Appearance();
		app.setAttribute(VERTEX_DRAW, false);
		app.setAttribute(EDGE_DRAW, false);
		root.setAppearance(app);
		SceneGraphComponent zero = new SceneGraphComponent();
		double[][] square = new double[][]{
				{0.0,0.0,0.0},{2.0,0.0,1.0},
				{2.0,2.0,0.0},{0.0,2.0,1.0}
		};
		zero.setGeometry(IndexedFaceSetUtility.constructPolygon(square));
		SceneGraphComponent c = new SceneGraphComponent();
		c.setGeometry(planeGeometry);
		
		for(int i = 0; i < 4; ++i) {
			SceneGraphComponent sgc = new SceneGraphComponent("vertex "+i);
			double[] vertex = square[i];
			double[] e1 = Rn.subtract(null, square[i], square[(i+1)%4]);
			double[] e2 = Rn.subtract(null, square[(i+2)%4], square[(i+3)%4]);
			Matrix T = getTransformationMatrix(vertex, e1, e2);
			T.assignTo(sgc);
			sgc.addChild(c);
			root.addChild(sgc);
		}
		root.addChild(zero);
		
		JRViewer.display(root);
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(new DirectingPlanesAdapter());
	}

}
