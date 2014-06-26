package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.BetaSquaredFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class BetaSquaredOptimizer extends VarylabOptimizerPlugin implements ActionListener {

	private final String 
		NORMAL = "normal",
		SQUARES = "squares",
		WEIGHTED = "weighted";
	
	private JPanel 
		panel = new JPanel();
	
	private JRadioButton
		normalButton = new JRadioButton("normal mode"),
		squaresButton = new JRadioButton("squared angles"),
		weightedSquaresButton = new JRadioButton("weighted squared angles");
	
	private ButtonGroup
		modeGroup = new ButtonGroup();
	
	private BetaSquaredFunctional<VVertex, VEdge, VFace> 
		functional = new BetaSquaredFunctional<VVertex, VEdge, VFace>();
	
	public BetaSquaredOptimizer() {
		panel.setLayout(new GridLayout(3,1));
		
		normalButton.setSelected(true);
		panel.add(normalButton);
		panel.add(squaresButton);
		panel.add(weightedSquaresButton);
		
		modeGroup.add(normalButton);
		normalButton.setActionCommand(NORMAL);
		normalButton.addActionListener(this);
		
		modeGroup.add(squaresButton);
		squaresButton.setActionCommand(SQUARES);
		squaresButton.addActionListener(this);
		
		modeGroup.add(weightedSquaresButton);
		weightedSquaresButton.setActionCommand(WEIGHTED);
		weightedSquaresButton.addActionListener(this);
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}

	
	@Override
	public String getName() {
		return "Beta Squared";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Beta Squared Optimizer", "Martin Weidner, Stefan Sechelmann, Thilo Roerig");
		info.icon = ImageHook.getIcon("willmore.png");
		return info;
	}

	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src == normalButton || src == squaresButton || src == weightedSquaresButton) {
			functional.setMode(e.getActionCommand());
		}
	}


}
