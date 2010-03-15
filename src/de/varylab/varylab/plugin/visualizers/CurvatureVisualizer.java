package de.varylab.varylab.plugin.visualizers;

import static de.jreality.shader.CommonAttributes.DEPTH_FUDGE_FACTOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.*;

import java.awt.Color;
import java.util.LinkedList;

import no.uib.cipr.matrix.EVD;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.bsp.KdTree;
import de.varylab.varylab.math.geom3d.Basis;
import de.varylab.varylab.math.geom3d.GeometryUtility;
import de.varylab.varylab.math.geom3d.Point;
import de.varylab.varylab.math.geom3d.Vector;
import de.varylab.varylab.math.mesh.MeshUtility;

public class CurvatureVisualizer extends VisualizerPlugin {

	private double 
		minCircleScale = 6.0;
	private SceneGraphComponent
		root = new SceneGraphComponent("Principal Curvatures"),
		k1Root = new SceneGraphComponent("k1"),
		k2Root = new SceneGraphComponent("k2"),
		nRoot = new SceneGraphComponent("n");
	
	public CurvatureVisualizer() {
		createScene();
	}
	
	private void createScene() {
		root.addChild(k1Root);
		root.addChild(k2Root);
		root.addChild(nRoot);
		Appearance app1 = new Appearance();
		app1.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		app1.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR, 0.99999);
		app1.setAttribute(EDGE_DRAW, true);
		app1.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		app1.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 1.0);
		k1Root.setAppearance(app1);
		Appearance app2 = new Appearance();
		app2.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.BLUE);
		app2.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR, 0.99999);
		app2.setAttribute(EDGE_DRAW, true);
		app2.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		app2.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 1.0);
		k2Root.setAppearance(app2);	
		Appearance app3 = new Appearance();
		app3.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.ORANGE);
		app3.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR, 0.99999);
		app3.setAttribute(EDGE_DRAW, true);
		app3.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		app3.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 1.0);
		nRoot.setAppearance(app3);
	}
	
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		VHDS mesh = (VHDS)hds;
		VVertex[] kdVertices = mesh.getVertices().toArray(new VVertex[0]);
		KdTree<VVertex> kd = new KdTree<VVertex>(kdVertices, 10, false);
		double scale = MeshUtility.meanEdgeLength(mesh);
		LinkedList<Basis> basisList = new LinkedList<Basis>();
		LinkedList<Point> centerList = new LinkedList<Point>();
		EVD evd = null;
		for (VFace f : mesh.getFaces()) {
			Point p = MeshUtility.toTriangle(f).getBaryCenter();
			Basis B = new Basis();
			try {
				evd = MeshUtility.getTensorInformation(p, scale*minCircleScale, kd);
				Vector normal = MeshUtility.getNormal(f);
				Vector[] vecs = MeshUtility.getSortedEigenVectors(evd);
				B.setX(vecs[0]);
				B.setY(vecs[1]);
				B.setZ(normal);
				B.getX().makeOrthogonalTo(normal).normalize();
				B.getY().makeOrthogonalTo(normal).normalize();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (B == null)
				continue;
			basisList.add(B);
			centerList.add(p);
		}
		LinkedList<Vector> k1VecList = new LinkedList<Vector>();
		LinkedList<Vector> k2VecList = new LinkedList<Vector>();
		LinkedList<Vector> nVecList = new LinkedList<Vector>();
		for (Basis B : basisList) {
			k1VecList.add(B.getX());
			k2VecList.add(B.getY());
			nVecList.add(B.getZ());
			
		}
		double vecScale = MeshUtility.meanEdgeLength(mesh) * 0.7;
		IndexedLineSet k1Set = GeometryUtility.createVectorSet(k1VecList, centerList, vecScale);
		IndexedLineSet k2Set = GeometryUtility.createVectorSet(k2VecList, centerList, vecScale);
		IndexedLineSet nSet  = GeometryUtility.createVectorSet(nVecList, centerList, vecScale);
		k1Root.setGeometry(k1Set);
		k2Root.setGeometry(k2Set);
		nRoot.setGeometry(nSet);
	};
	
	
	@Override
	public SceneGraphComponent getComponent() {
		return root;
	}
	
	@Override
	public String getName() {
		return "Principal Curvatures";
	}

}
