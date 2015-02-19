package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;
import de.jreality.plugin.JRViewerUtility;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.SpringFunctional;
import de.varylab.varylab.functional.adapter.FaceLengthAdapter;
import de.varylab.varylab.functional.adapter.Length;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.AdaptedEdgeWeightFunction;
import de.varylab.varylab.halfedge.adapter.ConstantLengthAdapter;
import de.varylab.varylab.halfedge.adapter.ConstantWeight;
import de.varylab.varylab.halfedge.adapter.DiscreteLengthAdapter;
import de.varylab.varylab.halfedge.adapter.LengthRangeAdapter;
import de.varylab.varylab.halfedge.adapter.RoundingMode;
import de.varylab.varylab.halfedge.adapter.OriginalLength;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class SpringOptimizer extends VarylabOptimizerPlugin implements ChangeListener, ActionListener { 
	
	private final String 
		AVERAGE = "average",
		FACE_AVERAGE = "face average",
		ORIGINAL = "original",
		CONSTANT = "constant",
		RANGE = "range",
		DISCRETE = "discrete";
	
	private JPanel
		panel = new JPanel();
	
	private JButton
		updateButton = new JButton("update");
	
	private SpinnerNumberModel
		minLengthModel = new SpinnerNumberModel(1.0,0.,100.,.1),
		maxLengthModel = new SpinnerNumberModel(1.0,0.,100.,.1),
		stepsModel = new SpinnerNumberModel(5, 1, 100, 1),
		springModel = new SpinnerNumberModel(1.,0.,100.,1.);
	
	private JSpinner
		minLengthSpinner = new JSpinner(minLengthModel),
		maxLengthSpinner = new JSpinner(maxLengthModel),
		stepsSpinner = new JSpinner(stepsModel),
		springSpinner = new JSpinner(springModel);
	
	private JRadioButton 
		faceAverageButton = new JRadioButton("F-const."),
		averageButton = new JRadioButton("avg."),
		originalButton = new JRadioButton("orig."),
		constantButton = new JRadioButton("const."),
		rangeButton = new JRadioButton("range"),
		discreteButton = new JRadioButton("discr.");
	
	private ButtonGroup
		edgeLengthGroup = new ButtonGroup();
		
	private Length<VEdge> 
		la = new ConstantLengthAdapter(0.0);
	
	private SpringFunctional<VVertex, VEdge, VFace>
		functional = new SpringFunctional<VVertex, VEdge, VFace>(
			la, new ConstantWeight(1, true),false);

	private JCheckBox
		updateLength = new JCheckBox("c-update"),
		diagonalsBox = new JCheckBox("diag's");

	private HalfedgeInterface hif;

	private JComboBox<RoundingMode>
		modeCombo = new JComboBox<>(RoundingMode.values());
	
	public SpringOptimizer() {
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(1,1,1,1);
		c1.fill = GridBagConstraints.BOTH;
		c1.anchor = GridBagConstraints.WEST;
		c1.weightx = 1.0;
		c1.gridwidth = 1;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(1,1,1,1);
		c2.fill = GridBagConstraints.BOTH;
		c2.anchor = GridBagConstraints.WEST;
		c2.weightx = 1.0;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		
		panel.setLayout(new GridBagLayout());
		c1.weightx = 2.0;
		panel.add(diagonalsBox, c1);
		c2.weightx = 2.0;
		updateLength.setToolTipText("continously update target lengths");
		panel.add(updateLength ,c1);
		c2.weightx = 1.0;
		panel.add(updateButton ,c2);
		
		c2.weightx = 1.0;
		c1.weightx = 1.0;
		panel.add(constantButton,c1);
		panel.add(averageButton,c1);
		panel.add(faceAverageButton,c2);
		originalButton.setSelected(true);
		panel.add(originalButton,c1);
		panel.add(rangeButton,c1);
		panel.add(discreteButton,c1);
		panel.add(modeCombo,c2);
		
		panel.add(new JLabel("Length"),c1);
		panel.add(minLengthSpinner,c1);
		panel.add(maxLengthSpinner,c1);
		panel.add(stepsSpinner, c2);
		maxLengthSpinner.setEnabled(rangeButton.isSelected());
		stepsSpinner.setEnabled(discreteButton.isSelected());
		
		panel.add(new JLabel("Strength"),c1);
		panel.add(springSpinner,c2);

		edgeLengthGroup.add(averageButton);
		averageButton.setActionCommand(AVERAGE);
		averageButton.addActionListener(this);
		
		edgeLengthGroup.add(faceAverageButton);
		faceAverageButton.setActionCommand(FACE_AVERAGE);
		faceAverageButton.addActionListener(this);
		
		
		edgeLengthGroup.add(originalButton);
		originalButton.setActionCommand(ORIGINAL);
		originalButton.addActionListener(this);
		
		edgeLengthGroup.add(constantButton);
		constantButton.setActionCommand(CONSTANT);
		constantButton.addActionListener(this);
		
		edgeLengthGroup.add(rangeButton);
		rangeButton.setActionCommand(RANGE);
		rangeButton.addActionListener(this);
		
		edgeLengthGroup.add(rangeButton);
		rangeButton.setActionCommand(RANGE);
		rangeButton.addActionListener(this);
		
		edgeLengthGroup.add(discreteButton);
		discreteButton.setActionCommand(DISCRETE);
		discreteButton.addActionListener(this);
		
		minLengthSpinner.addChangeListener(this);
		maxLengthSpinner.addChangeListener(this);
		springSpinner.addChangeListener(this);
		
		updateButton.addActionListener(this);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		if(updateLength.isSelected() || constantButton.isSelected()){
			updateLength(hds);
		}
		functional.setDiagonals(diagonalsBox.isSelected());
		AdapterSet aSet = hif.getAdapters();
		functional.setWeight(new AdaptedEdgeWeightFunction(aSet));
		return functional;
	}

	private void updateLength(VHDS hds) {
		double l = 0.0, lmin = 0.0, lmax = 0.0;
		RoundingMode roundingMode;
		Length<VEdge> length = null;
		switch (edgeLengthGroup.getSelection().getActionCommand()) {
		case AVERAGE:
			for (VEdge e : hds.getPositiveEdges()) {
				double[] s = e.getStartVertex().getP();
				double[] t = e.getTargetVertex().getP();
				l += Rn.euclideanDistance(s, t);
			}
			l /= hds.numEdges() / 2.0;
			minLengthModel.setValue(l);
			maxLengthSpinner.setEnabled(false);
			length = new ConstantLengthAdapter(l);
			break;
		case CONSTANT:
			l = minLengthModel.getNumber().doubleValue();
			maxLengthSpinner.setEnabled(false);
			length = new ConstantLengthAdapter(l);
			break;
		case ORIGINAL:
			maxLengthSpinner.setEnabled(false);
			length = new OriginalLength(hds);
			break;
		case RANGE:
			lmin = minLengthModel.getNumber().doubleValue();
			lmax = maxLengthModel.getNumber().doubleValue();
			AdapterSet aSet = hif.getAdapters();
			length = new LengthRangeAdapter(lmin, lmax, aSet);
			break;
		case DISCRETE:
			lmin = minLengthModel.getNumber().doubleValue();
			lmax = maxLengthModel.getNumber().doubleValue();
			int steps = stepsModel.getNumber().intValue();
			roundingMode = (RoundingMode)modeCombo.getSelectedItem();
			length = new DiscreteLengthAdapter(lmin, lmax, steps, roundingMode, hds);
			break;
		case FACE_AVERAGE:
			roundingMode = (RoundingMode)modeCombo.getSelectedItem();
			length = new FaceLengthAdapter(hds, roundingMode);
			break;
		}
		functional.setLength(length);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "mode", edgeLengthGroup.getSelection().getActionCommand());
		c.storeProperty(getClass(), "minLength", minLengthModel.getNumber().doubleValue());
		c.storeProperty(getClass(), "maxLength", maxLengthModel.getNumber().doubleValue());
		c.storeProperty(getClass(), "springConstant", springModel.getNumber().doubleValue());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		String actString = c.getProperty(getClass(), "mode", edgeLengthGroup.getSelection().getActionCommand());
		averageButton.setSelected(actString.equals(AVERAGE));
		constantButton.setSelected(actString.equals(CONSTANT));
		originalButton.setSelected(actString.equals(ORIGINAL));
		rangeButton.setSelected(actString.equals(RANGE));
		minLengthModel.setValue(c.getProperty(getClass(), "minLength", minLengthModel.getNumber().doubleValue()));
		maxLengthModel.setValue(c.getProperty(getClass(), "maxLength", maxLengthModel.getNumber().doubleValue()));
		springModel.setValue(c.getProperty(getClass(), "springConstant", springModel.getNumber().doubleValue()));
		maxLengthSpinner.setEnabled(rangeButton.isSelected());
	}
	
	
	@Override
	public String getName() {
		return "Springs";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Spring Energy Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("spring.png",16,16);
		return info;
	}

	@Override
	public JPanel getOptionPanel() {
		return panel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if(minLengthSpinner == src) {
			functional.setLength(new ConstantLengthAdapter(minLengthModel.getNumber().doubleValue()));
		} else if(springSpinner == src) {
			functional.setWeight(new ConstantWeight(springModel.getNumber().doubleValue(),true));
		}
	}

	@Override
	public void install(Controller c) throws Exception {
		c.getPlugin(HalfedgeInterface.class);
		JRViewerUtility.getContentPlugin(c);
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(rangeButton == src) {
			maxLengthSpinner.setEnabled(true);
			maxLengthSpinner.setValue(minLengthModel.getNumber().doubleValue());
		} else if(discreteButton == src) {
			maxLengthSpinner.setEnabled(true);
			stepsSpinner.setEnabled(true);
		} else if(
				constantButton == src ||
				originalButton == src ||
				averageButton == src) {
			maxLengthSpinner.setEnabled(false);
			stepsSpinner.setEnabled(false);
		} else if(updateButton == src) {
			updateLength(hif.get(new VHDS()));
		}
	}
}
