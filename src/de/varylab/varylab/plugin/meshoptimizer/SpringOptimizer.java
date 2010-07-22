package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
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
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.AdaptedWeightFunction;
import de.varylab.varylab.hds.adapter.ConstantLengthAdapter;
import de.varylab.varylab.hds.adapter.ConstantWeight;
import de.varylab.varylab.hds.adapter.OriginalLength;
import de.varylab.varylab.math.functional.SpringFunctional;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.Length;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class SpringOptimizer extends OptimizerPlugin implements ChangeListener{ 
	
	private final String AVERAGE = "average";

	private final String ORIGINAL = "original";

	private final String CONSTANT = "constant";

	private JPanel
		panel = new JPanel();
	
	private SpinnerNumberModel
		edgeLengthModel = new SpinnerNumberModel(1.0,0.,100.,1.),
		springModel = new SpinnerNumberModel(1.,0.,100.,1.);
	
	private JSpinner
		edgeLengthSpinner = new JSpinner(edgeLengthModel),
		springSpinner = new JSpinner(springModel);
	
	private JRadioButton 
		averageButton = new JRadioButton("avg."),
		originalButton = new JRadioButton("orig."),
		constantButton = new JRadioButton("const.");
	
	private ButtonGroup
		edgeLengthGroup = new ButtonGroup();
		
	private Length<VEdge> 
		la = new ConstantLengthAdapter(0.0);
	
	private SpringFunctional<VVertex, VEdge, VFace>
		functional = new SpringFunctional<VVertex, VEdge, VFace>(
			la, new ConstantWeight(1, true),false);

	private JCheckBox
		setLengthBox = new JCheckBox("update"),
		diagonalsBox = new JCheckBox("diagonals");

	private HalfedgeInterface hif;

	public SpringOptimizer() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.0;
		
		gbc.insets = new Insets(2, 2, 2, 2);
		panel.setLayout(new GridBagLayout());
		
		gbc.weightx = 0;
		gbc.gridwidth = 3;
		panel.add(new JLabel("Edge Length"),gbc);
		
		gbc.weightx = 1.;
		gbc.gridwidth = 1;
//		averageBox.setIcon(ImageHook.getIcon("average16w.png"));
//		averageBox.setSelectedIcon(ImageHook.getIcon("average16.png"));
//		panel.add(averageBox,gbc);
		panel.add(constantButton,gbc);
		panel.add(averageButton,gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		originalButton.setSelected(true);
		panel.add(originalButton,gbc);
		gbc.gridwidth = 3;
		panel.add(setLengthBox ,gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(edgeLengthSpinner,gbc);

		gbc.weightx = .0;
		gbc.gridwidth = 3;
		panel.add(new JLabel("Spring Constant"),gbc);
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(springSpinner,gbc);

		edgeLengthGroup.add(averageButton);
		averageButton.setActionCommand(AVERAGE);
		edgeLengthGroup.add(originalButton);
		originalButton.setActionCommand(ORIGINAL);
		edgeLengthGroup.add(constantButton);
		constantButton.setActionCommand(CONSTANT);
		panel.add(diagonalsBox, gbc);
		
		edgeLengthSpinner.addChangeListener(this);
		springSpinner.addChangeListener(this);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		if(setLengthBox.isSelected()){
			updateLength(hds);
		}
		functional.setDiagonals(diagonalsBox.isSelected());
		AdapterSet aSet = hif.getAdapters();
		functional.setWeight(new AdaptedWeightFunction(aSet));
		return functional;
	}

	private void updateLength(VHDS hds) {
		String st = edgeLengthGroup.getSelection().getActionCommand();
		if(st == AVERAGE) {
			double l = 0.0;
			for (VEdge e : hds.getPositiveEdges()) {
				double[] s = e.getStartVertex().position;
				double[] t = e.getTargetVertex().position;
				l += Rn.euclideanDistance(s, t);
			}
			l /= hds.numEdges() / 2.0;
			edgeLengthModel.setValue(l);
			functional.setLength(new ConstantLengthAdapter(l));
		} else if(st == CONSTANT) {
			double l = edgeLengthModel.getNumber().doubleValue();
			functional.setLength(new ConstantLengthAdapter(l));
		} else if(st == ORIGINAL) {
			functional.setLength(new OriginalLength(hds));
		}
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "mode", edgeLengthGroup.getSelection().getActionCommand());
		c.storeProperty(getClass(), "constantLength", edgeLengthModel.getNumber().doubleValue());
		c.storeProperty(getClass(), "springConstant", springModel.getNumber().doubleValue());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		String actString = c.getProperty(getClass(), "mode", edgeLengthGroup.getSelection().getActionCommand());
		averageButton.setSelected(actString.equals(AVERAGE));
		constantButton.setSelected(actString.equals(CONSTANT));
		originalButton.setSelected(actString.equals(ORIGINAL));
		edgeLengthModel.setValue(c.getProperty(getClass(), "constantLength", edgeLengthModel.getNumber().doubleValue()));
		springModel.setValue(c.getProperty(getClass(), "springConstant", springModel.getNumber().doubleValue()));
	}
	
	
	@Override
	public String getName() {
		return "Spring Energy";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Spring Energy Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("spring.png");
		return info;
	}

	@Override
	public JPanel getOptionPanel() {
		return panel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		if(edgeLengthSpinner == src) {
			functional.setLength(new ConstantLengthAdapter(edgeLengthModel.getNumber().doubleValue()));
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

}
