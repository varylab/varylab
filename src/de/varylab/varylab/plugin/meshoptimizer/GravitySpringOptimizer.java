package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.formfinding.GravitySpringFunctional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.plugin.meshoptimizer.EdgeLengthOptimizer.LengthAdapter;
import de.varylab.varylab.ui.image.ImageHook;

public class GravitySpringOptimizer extends OptimizerPlugin{

	private JPanel
		panel = new JPanel();
	
	private SpinnerNumberModel
		edgeLengthModel = new SpinnerNumberModel(1.0,0.,100.,1.),
		springModel = new SpinnerNumberModel(1.,0.,100.,1.),
		gravityModel = new SpinnerNumberModel(9.81,0,100,1.0);
	
	private JSpinner
		edgeLengthSpinner = new JSpinner(edgeLengthModel),
		springSpinner = new JSpinner(springModel),
		gravitySpinner = new JSpinner(gravityModel);
	
	private NumberFormat 
		coordFormat = NumberFormat.getNumberInstance();
	
	private JFormattedTextField
		dirX = new JFormattedTextField(coordFormat),
		dirY = new JFormattedTextField(coordFormat),
		dirZ = new JFormattedTextField(coordFormat);

	private JCheckBox
		averageBox = new JCheckBox();
	
	public GravitySpringOptimizer() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.0;
		
		gbc.insets = new Insets(2, 2, 2, 2);
		panel.setLayout(new GridBagLayout());
		
		gbc.gridwidth = 3;
		panel.add(new JLabel("Gravity"),gbc);
		gbc.weightx = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(gravitySpinner,gbc);

		gbc.weightx = 0;
		gbc.gridwidth = 3;
		panel.add(new JLabel("Edge Length"),gbc);
		
		gbc.weightx = 1.;
		gbc.gridwidth = 1;
		averageBox.setIcon(ImageHook.getIcon("average16w.png"));
		averageBox.setSelectedIcon(ImageHook.getIcon("average16.png"));
		panel.add(averageBox,gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER; 
		panel.add(edgeLengthSpinner,gbc);

		gbc.weightx = .0;
		gbc.gridwidth = 3;
		panel.add(new JLabel("Spring Constant"),gbc);
		gbc.weightx = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(springSpinner,gbc);

		gbc.weightx = 0.0;
		gbc.gridwidth = 3;
		panel.add(new JLabel("Direction"),gbc);
		
		gbc.weightx = 1.0;
		dirX.setColumns(4);
		dirX.setText("0.0");
		panel.add(dirX,gbc);
		
		dirY.setText("1.0");
		dirY.setColumns(4);
		panel.add(dirY,gbc);

		dirZ.setText("0.0");
		dirZ.setColumns(4);
		panel.add(dirZ,gbc);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> createFunctional(VHDS hds) {
		double l = 0.0;
		if(averageBox.isSelected()){
			for (VEdge e : hds.getPositiveEdges()) {
				double[] s = e.getStartVertex().position;
				double[] t = e.getTargetVertex().position;
				l += Rn.euclideanDistance(s, t);
			}
			l /= hds.numEdges() / 2.0;
			edgeLengthModel.setValue(l);
		} else {
			l = edgeLengthModel.getNumber().doubleValue();
		}
		
		
		LengthAdapter la = new LengthAdapter(l);
		double springConstant = springModel.getNumber().doubleValue();
		return new GravitySpringFunctional<VVertex, VEdge, VFace>(
				la,
				new EdgeLengthOptimizer.ConstantWeight(springConstant, true),
				gravityModel.getNumber().doubleValue()*1E-3,
				new double[]{
					Double.parseDouble(dirX.getText()),
					Double.parseDouble(dirY.getText()),
					Double.parseDouble(dirZ.getText())});
	}

	@Override
	public String getName() {
		return "Gravity Spring Energy";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Gravity Spring Energy Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("gravity.png");
		return info;
	}

	public JPanel getOptionPanel() {
		return panel;
	}
}
