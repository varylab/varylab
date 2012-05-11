package de.varylab.varylab.plugin.generator;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.util.NativePathUtility;
import de.jtem.ellipticFunctions.Jacobi;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.subdivision.MedialGraphLinear;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.varylab.plugin.mathematica.MathematicaConsole;
import de.varylab.plugin.mathematica.MathematicaLinkPlugin;

public class TschebyscheffSphereGenerator extends AlgorithmDialogPlugin {

	private MathematicaLinkPlugin 
		link = null;
	private ConverterJR2Heds
		converter = new ConverterJR2Heds();
	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		kModel = new SpinnerNumberModel(0.7, 0, 1, 0.1),
		n1Model = new SpinnerNumberModel(20, 2, 10000, 1),
		n2Model = new SpinnerNumberModel(20, 2, 10000, 1),
		sModel = new SpinnerNumberModel(1, 0.01, 100.0, 0.1),
		tuModel = new SpinnerNumberModel(0.0, -1000.0, 1000.0, 0.1),
		tvModel = new SpinnerNumberModel(0.0, -1000.0, 1000.0, 0.1);
	private JSpinner
		kSpinner = new JSpinner(kModel),
		n1Spinner = new JSpinner(n1Model),
		n2Spinner = new JSpinner(n2Model),
		sSpinner = new JSpinner(sModel),
		tuSpinner = new JSpinner(tuModel),
		tvSpinner = new JSpinner(tvModel);
		
	
	public TschebyscheffSphereGenerator() {
		GridBagConstraints c1 = new GridBagConstraints();
		GridBagConstraints c2 = new GridBagConstraints();
		c1.weightx = 1.0;
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.insets = new Insets(2, 2, 2, 2);
		c2.weightx = 1.0;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.insets = new Insets(2, 2, 2, 2);
		panel.setLayout(new GridBagLayout());
		panel.add(new JLabel("k"), c1);
		panel.add(kSpinner, c2);
		panel.add(new JLabel("n1"), c1);
		panel.add(n1Spinner, c2);
		panel.add(new JLabel("n2"), c1);
		panel.add(n2Spinner, c2);
		panel.add(new JLabel("scale"), c1);
		panel.add(sSpinner, c2);
		panel.add(new JLabel("u translation"), c1);
		panel.add(tuSpinner, c2);
		panel.add(new JLabel("v translation"), c1);
		panel.add(tvSpinner, c2);
	}
	
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
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
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		QuadMeshFactory qmf = new QuadMeshFactory();
		double r = 1;
		double k = kModel.getNumber().doubleValue();
		int n1 = n1Model.getNumber().intValue();
		int n2 = n2Model.getNumber().intValue();
		double s = sModel.getNumber().doubleValue();
		double tu = tuModel.getNumber().doubleValue();
		double tv = tvModel.getNumber().doubleValue();
		
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.scale(s);
		mb.rotate(Math.PI/4, 0, 0, 1);
		mb.translate(tu, tv, 0);
		Matrix T = mb.getMatrix();
		
		double[][][] verts = new double[n1][n2][];
		for (int i = 0; i < n1; i++) {
			for (int j = 0; j < n2; j++) {
				double u = i / (double)(n1 - 1);
				double v = j / (double)(n1 - 1);
				double[] uvVec = {u, v, 0, 1};
				T.transformVector(uvVec);
				u = uvVec[0];
				v = uvVec[1];
				verts[i][j] = new double[] {u,v,0,1};
//				double[][] D = getDerivative(u, v, k, r);
//				double l0 = Rn.euclideanNormSquared(D[0]);
//				double l1 = Rn.euclideanNormSquared(D[1]);
//				double rV = Rn.euclideanNorm(verts[i][j]);
//				System.out.println("R = " + rV);
//				System.out.println("D(" + u + ", " + v + ") = " + l0 + ";  " + l1);
			}
		}
		qmf.setVLineCount(n1);
		qmf.setULineCount(n2);
		qmf.setVertexCoordinates(verts);
		qmf.update();
		IndexedFaceSet ifs = qmf.getIndexedFaceSet();
		converter.ifs2heds(ifs, hds, a);
		MedialGraphLinear l = new MedialGraphLinear();
		HDS newHDS = hi.createEmpty(hds);
		
		Map<V, F> vertexFaceMap = new HashMap<V, F>();
		Map<E, V> edgeVertexMap = new HashMap<E, V>();
		Map<F, F> faceFaceMap = new HashMap<F, F>();
		Map<E, E> edgeEdgeMap1 = new HashMap<E, E>();
		
		l.execute(hds, newHDS, vertexFaceMap, edgeVertexMap, faceFaceMap, edgeEdgeMap1, a);
		
		for (V v : newHDS.getVertices()) {
			double[] uv = a.getD(Position3d.class, v);
			double[] xyz = getSurfacePoint(uv[0], uv[1], k, r);
			a.set(Position.class, v, xyz);
		}
		
		hi.set(newHDS);
	}
	
	
	protected double[] getSurfacePoint(double u, double v, double k, double r) {
		double x = sn((u+v)/r, k) * cos(k*(u-v)/r);
		double y = sn((u+v)/r, k) * sin(k*(u-v)/r);
		double z = cn((u+v)/r, k);
		return new double[] {x, y, z};
	}
	
	
	protected double[][] getDerivative(double u, double v, double k, double r) {
		double[][] d = {{0,0,0},{0,0,0}};
		double eps = 1E-8;
		double[] uPlus = getSurfacePoint(u + eps, v, k, r);
		double[] uMinus = getSurfacePoint(u - eps, v, k, r);
		double[] vPlus = getSurfacePoint(u, v + eps, k, r);
		double[] vMinus = getSurfacePoint(u, v - eps, k, r);
		Rn.subtract(d[0], vPlus, vMinus);
		Rn.times(d[0], 1/(2*eps), d[0]);
		Rn.subtract(d[1], uPlus, uMinus);
		Rn.times(d[1], 1/(2*eps), d[1]);
		return d;
	}
		

	protected double Jacobi(String fun, double u, double m) {
		KernelLink l = null;
		try {
			l = link.getLink();
			l.putFunction("EvaluatePacket", 1);
			l.putFunction(fun, 2);
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
	
	
	private double sn(double u, double m) {
		return Jacobi.sn(u, m);
//		return Jacobi("JacobiSN", u, m);
	}
	
	private double cn(double u, double m) {
		return Jacobi.cn(u, m);
//		return Jacobi("JacobiCN", u, m);
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
