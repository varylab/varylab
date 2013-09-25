package de.varylab.varylab.plugin.nurbs.algorithm;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import de.jreality.math.Rn;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsWeightAdapter;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;
import de.varylab.varylab.utilities.SelectionUtility;

public class NurbsSurfaceFromMesh extends AlgorithmDialogPlugin {

	private JPanel 
		dPanel = new JPanel(new GridBagLayout()); 	
	
	private SpinnerNumberModel 
		uDegree = new SpinnerNumberModel(3, 1, Integer.MAX_VALUE, 1),
		vDegree = new SpinnerNumberModel(3, 1, Integer.MAX_VALUE, 1),
		uSteps = new SpinnerNumberModel(21, 1, Integer.MAX_VALUE, 1),
		vSteps = new SpinnerNumberModel(21, 1, Integer.MAX_VALUE, 1);
	
	private JSpinner
		uStepSpinner = new JSpinner(uSteps),
		vStepSpinner = new JSpinner(vSteps),
		uSpinner = new JSpinner(uDegree),
		vSpinner = new JSpinner(vDegree);
	
	private int
		m = 0,
		n = 0;

	private double[][][] controlMesh;
	
	public NurbsSurfaceFromMesh() {
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		dPanel.add(new JLabel("Nurbs surface parameters"),rc);
		dPanel.add(new JSeparator(SwingConstants.HORIZONTAL),rc);
		dPanel.add(new JLabel("u-degree"),lc);
		dPanel.add(uSpinner,rc);
		dPanel.add(new JLabel("v-degree"),lc);
		dPanel.add(vSpinner,rc);
		dPanel.add(new JSeparator(SwingConstants.HORIZONTAL),rc);
		dPanel.add(new JLabel("u-steps"),lc);
		dPanel.add(uStepSpinner,rc);
		dPanel.add(new JLabel("v-steps"),lc);
		dPanel.add(vStepSpinner,rc);
		
	}
	
	@Override
	public String getCategory() {
		return "NURBS";
	}

	@Override
	public String getAlgorithmName() {
		return "Create Nurbs surface";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeBeforeDialog(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		if(GridRectangleUtility.isGridRectangle(hds)) {
			controlMesh = createControlMesh(hds,a);
			uDegree.setMaximum(m-1);
			vDegree.setMaximum(n-1);
			uDegree.setValue(Math.min(uDegree.getNumber().intValue(), m-1));
			vDegree.setValue(Math.min(vDegree.getNumber().intValue(), n-1));
		} else {
			throw new RuntimeException("The mesh on the active layer does not have the correct combinatorics.");
		}
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[][][] createControlMesh(HDS hds, AdapterSet as) {
		E startEdge = GridRectangleUtility.getCornerEdge(hds);
		E e = startEdge;
		V v = e.getTargetVertex();
		e = e.getNextEdge();
		m = GridRectangleUtility.nStepsToNextCorner(e)+2;
		e = GridRectangleUtility.goToNextCorner(e).getNextEdge();
		n = GridRectangleUtility.nStepsToNextCorner(e)+2;
		if(m == 0 || n == 0) {
			throw new RuntimeException("The mesh on the active layer does not have the correct combinatorics.");
		}
		double[][][] cm = new double[m][n][4];
		e = startEdge.getNextEdge();
		NurbsWeightAdapter nwa = as.query(NurbsWeightAdapter.class);
		synchronized(nwa) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m - 1; j++) {
					v = e.getStartVertex();
					System.arraycopy(as.getD(Position3d.class, v), 0, cm[j][i], 0, 3);
					cm[j][i][3] = 1.0;
					if((nwa != null)) {
						Rn.times(cm[j][i], nwa.getV(v, null), cm[j][i]);
					} 
					if(j < m-2) {
						e = getNextInRowTraversal(e);
					}
				}
				v = e.getTargetVertex();
				System.arraycopy(as.getD(Position3d.class, v), 0, cm[m-1][i], 0, 3);
				cm[m-1][i][3] = 1.0;
				if((nwa != null)) {
					Rn.times(cm[m-1][i], nwa.getV(v, null), cm[m-1][i]);
				} 
				startEdge = startEdge.getPreviousEdge();
				if(i < n-2) {
					e = startEdge.getOppositeEdge().getPreviousEdge().getOppositeEdge();
				} else { // i == n-1
					e = startEdge.getOppositeEdge();
				}
			}
		}
		return cm;
	}
	
	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> E getNextInRowTraversal(E e) {
	   E next = e;
	   if(e.getLeftFace() == null) {
		   next = e.getNextEdge();
	   } else if(e.getRightFace() == null) {
		   next = e.getOppositeEdge().getPreviousEdge().getOppositeEdge();
	   } else {
		   next = SelectionUtility.getOpposingEdge(e).getOppositeEdge();
	   }
	   return next;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		NURBSSurface surface = new NURBSSurface(controlMesh, uDegree.getNumber().intValue(), vDegree.getNumber().intValue());
		HalfedgeLayer newLayer = new HalfedgeLayer(hif);
		newLayer.setName("NURBS Surface from " + hif.getActiveLayer().getName());
		NurbsSurfaceUtility.addNurbsMesh(surface, newLayer, uSteps.getNumber().intValue(), vSteps.getNumber().intValue());
		hif.addLayer(newLayer);
		hif.activateLayer(newLayer);
	}

	@Override
	protected JPanel getDialogPanel() {
		return dPanel;
	}

	
}
