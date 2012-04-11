package de.varylab.varylab.plugin.generator;

import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sin;

import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.varylab.plugin.mathematica.MathematicaConsole;
import de.varylab.plugin.mathematica.MathematicaLinkPlugin;

public class TschebyscheffSphereGenerator extends AlgorithmPlugin {

	private MathematicaLinkPlugin 
		link = null;
	private ConverterJR2Heds
		converter = new ConverterJR2Heds();
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}

	@Override
	public String getAlgorithmName() {
		return "Tschebyscheff Sphere";
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		link = c.getPlugin(MathematicaLinkPlugin.class);
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		QuadMeshFactory qmf = new QuadMeshFactory();
		int m = 50;
		int n = 50;
		
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.scale(0.1);
//		mb.rotate(Math.PI/2, 0, 0, 1);
		mb.translate(-1, -1, 0);
		Matrix T = mb.getMatrix();
		
//		double k = 1;
		double[][][] verts = new double[m][n][];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				double u = m / (double)(i+1);
				double v = n / (double)(j+1);
				double[] uvVec = {u, v, 0, 1};
				T.transformVector(uvVec);
				u = uvVec[0];
				v = uvVec[1];
//				double x = Math.sin(am(u+v, k)) * Math.cos(u-v) * k;
//				double y = Math.sin(am(u+v, k)) * Math.sin(u-v) * k;
//				double z = Math.cos(am(u+v, k));
				double x = cos(u-v) * (exp(u+v) - exp(-u-v)) / (exp(u+v) + exp(-u-v));
				double y = sin(u-v) * (exp(u+v) - exp(-u-v)) / (exp(u+v) + exp(-u-v));
				double z = 2 / (exp(u+v) + exp(-u-v));
				verts[i][j] = new double[] {x, y, z};
			}
		}
		qmf.setVLineCount(m);
		qmf.setULineCount(n);
		qmf.setVertexCoordinates(verts);
		qmf.update();
		IndexedFaceSet ifs = qmf.getIndexedFaceSet();
		converter.ifs2heds(ifs, hds, a);
		hi.update();
	}

	@SuppressWarnings("unused")
	private double am(double u, double m) {
		KernelLink l = null;
		try {
			l = link.getLink();
			l.putFunction("EvaluatePacket", 1);
			l.putFunction("JacobiAmplitude", 2);
			l.put(u);
			l.put(m);
			l.endPacket();
			l.waitForAnswer();
			return l.getDouble();
		} catch (Exception e) {
			System.err.println(e.toString());
			try {
				l.discardAnswer();
			} catch (MathLinkException e1) {
				System.out.println(e1);
			}
		}
		return 0.0;
	}
	
	public static void main(String[] args) {
		NativePathUtility.set("native");
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentUI();
		v.registerPlugin(TschebyscheffSphereGenerator.class);
		v.registerPlugin(MathematicaConsole.class);
		v.startup();
	}
	
}
