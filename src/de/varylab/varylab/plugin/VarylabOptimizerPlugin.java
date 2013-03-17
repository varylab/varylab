package de.varylab.varylab.plugin;

import java.util.Random;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;
import de.jtem.jtao.Tao;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.math.IterationProtocol;

public abstract class VarylabOptimizerPlugin extends VarylabPlugin implements UIFlavor {

	private final static Random
		idRnd = new Random();
	protected final long
		gradientId = idRnd.nextLong(),
		protocolId = idRnd.nextLong();
	
	public abstract Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds);
	public abstract String getName();
	
	public JPanel getOptionPanel() {
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public IterationProtocol getIterationProtocol(Tao solver) {
		return null;
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		if (getOptionPanel() != null) {
			SwingUtilities.updateComponentTreeUI(getOptionPanel());
		}
	}
	
}
