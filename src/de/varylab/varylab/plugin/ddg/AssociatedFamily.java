package de.varylab.varylab.plugin.ddg;

import static java.lang.Math.PI;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class AssociatedFamily extends ShrinkPanelPlugin implements ActionListener, ChangeListener {

	private HalfedgeInterface
		hif = null;
	private ChristoffelTransfom
		christoffelTransfom = null;
	private JButton
		loadGeometryButton = new JButton("Initialize");
	private JSlider
		phiSlider = new JSlider(0, 179, 0);
	private SpinnerNumberModel
		phiModel = new SpinnerNumberModel(0, 0, 179, 0.1);
	private JSpinner
		phiSpinner = new JSpinner(phiModel);
	private JLabel
		angleLabel = new JLabel("Angle");
	private boolean
		blockListeners = false;
	
	private AdapterSet
		aSet = null;
	private VHDS
		dualSurface = null,
		surface = null;
	private int
		rootIndex = 0;
	private double[][]
		fixingPoints = new double[3][]; 
	private int[]
	    fixingIndices = new int[3];
	private HalfedgeSelection
		sel = null;
	private Random
		rnd = new Random();
	
	public AssociatedFamily() {
		shrinkPanel.setTitle("Associated Family");
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		shrinkPanel.add(loadGeometryButton, rc);
		shrinkPanel.add(angleLabel, lc);
		shrinkPanel.add(phiSpinner, rc);
		shrinkPanel.add(phiSlider, rc);
		phiSlider.setEnabled(false);
		angleLabel.setEnabled(false);
		phiSpinner.setEnabled(false);
		
		loadGeometryButton.addActionListener(this);
		phiSlider.addChangeListener(this);
		phiSpinner.addChangeListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		surface = hif.get(new VHDS());
		aSet = hif.getAdapters();
		dualSurface = new VHDS();
		HalfEdgeUtils.copy(surface, dualSurface);
		for (VVertex v : dualSurface.getVertices()) {
			VVertex vv = surface.getVertex(v.getIndex());
			double[] pos = aSet.getD(Position3d.class, vv);
			aSet.set(Position.class, v, pos);
		}
		VVertex v0 = christoffelTransfom.guessRootVertex(dualSurface, 100);
		rootIndex = v0.getIndex();
		christoffelTransfom.transfom(dualSurface, v0, aSet, 0, false);
		phiSlider.setEnabled(true);
		angleLabel.setEnabled(true);
		phiSpinner.setEnabled(true);
		
		int numVerts = surface.numVertices();
		fixingIndices[0] = rnd.nextInt(numVerts);
		fixingIndices[1] = rnd.nextInt(numVerts);
		fixingIndices[2] = rnd.nextInt(numVerts);
		fixingPoints[0] = aSet.getD(Position3d.class, surface.getVertex(fixingIndices[0]));
		fixingPoints[1] = aSet.getD(Position3d.class, surface.getVertex(fixingIndices[1]));
		fixingPoints[2] = aSet.getD(Position3d.class, surface.getVertex(fixingIndices[2]));
		
		sel = new HalfedgeSelection();
		for (int i : fixingIndices) {
			sel.add(surface.getVertex(i));
		}
		
		phiModel.setValue(0.0);
		hif.setSelection(sel);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (blockListeners) return;
		blockListeners = true;
		if (phiSlider == e.getSource()) {
			double phiGrad = phiSlider.getValue();
			phiModel.setValue(phiGrad);
		}
		if (phiSpinner == e.getSource()) {
			int phiGrad = phiModel.getNumber().intValue();
			phiSlider.setValue(phiGrad);
		}
		blockListeners = false;

		double phi = phiModel.getNumber().doubleValue() * PI / 180;
		updateAssociatedFamily(phi);
	}
	
	private void updateAssociatedFamily(double phi) {
		if (dualSurface == null) return;
		VHDS tmpSurface = new VHDS();
		HalfEdgeUtils.copy(dualSurface, tmpSurface);
		for (VVertex v : tmpSurface.getVertices()) {
			VVertex vv = dualSurface.getVertex(v.getIndex());
			double[] pos = aSet.getD(Position3d.class, vv);
			aSet.set(Position.class, v, pos);
		}
		VVertex v0 = tmpSurface.getVertex(rootIndex);
		christoffelTransfom.transfom(tmpSurface, v0, aSet, phi, false);
		
		double[][] newfixingPoints = new double[3][];
		newfixingPoints[0] = aSet.getD(Position3d.class, tmpSurface.getVertex(fixingIndices[0]));
		newfixingPoints[1] = aSet.getD(Position3d.class, tmpSurface.getVertex(fixingIndices[1]));
		newfixingPoints[2] = aSet.getD(Position3d.class, tmpSurface.getVertex(fixingIndices[2]));
		
		MatrixBuilder mb = MatrixBuilder.euclidean();
		double[] vec1 = Rn.subtract(null, newfixingPoints[1], newfixingPoints[0]);
//		double[] vec2 = Rn.subtract(null, newfixingPoints[2], newfixingPoints[0]);
		double[] vec3 = Rn.subtract(null, fixingPoints[1], fixingPoints[0]);
//		double[] vec4 = Rn.subtract(null, fixingPoints[2], fixingPoints[0]);
		double[] zero = Rn.negate(null, newfixingPoints[0]);
		mb.translate(fixingPoints[0]);
//		mb.rotateFromTo(vec2, vec4);
		mb.rotateFromTo(vec1, vec3);
		mb.translate(zero);
		Matrix T = mb.getMatrix();
		
		for (VVertex v : surface.getVertices()) {
			VVertex vv = tmpSurface.getVertex(v.getIndex());
			double[] pos = aSet.getD(Position3d.class, vv);
			pos = Pn.homogenize(null, pos);
			T.transformVector(pos);
			aSet.set(Position.class, v, pos);
		}
		hif.set(surface);
		hif.setSelection(sel);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		christoffelTransfom = c.getPlugin(ChristoffelTransfom.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Associated Family");
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
