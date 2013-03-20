package de.varylab.varylab.plugin;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.optimization.IterationProtocol;
import de.varylab.varylab.optimization.ProtocolValue;
import de.varylab.varylab.optimization.array.ArrayDomainValue;
import de.varylab.varylab.optimization.array.ArrayEnergy;
import de.varylab.varylab.optimization.array.ArrayGradient;

public abstract class VarylabOptimizerPlugin extends VarylabPlugin implements UIFlavor {

	private final static Random
		idRnd = new Random();
	protected final long
		energyId = idRnd.nextLong(),
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
	
	public IterationProtocol getIterationProtocol(double[] solution, VHDS hds) {
		Functional<VVertex, VEdge, VFace> f = getFunctional(hds);
		ArrayDomainValue x = new ArrayDomainValue(solution);
		double[] Garr = new double[f.getDimension(hds)];
		ArrayGradient G = new ArrayGradient(Garr);
		ArrayEnergy E = new ArrayEnergy();
		f.evaluate(hds, x, E, G, null);
		double gNorm = Rn.euclideanNorm(Garr);
		ProtocolValue gVal = new ProtocolValue(gNorm, "Gradient Norm", gradientId);
		ProtocolValue eVal = new ProtocolValue(E.get(), "Energy Value", energyId);
		gVal.setColor(Color.MAGENTA);
		eVal.setColor(Color.GREEN);
		List<ProtocolValue> pList = new LinkedList<ProtocolValue>();
		pList.add(gVal);
		pList.add(eVal);
		return new IterationProtocol(this, pList, protocolId);
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		if (getOptionPanel() != null) {
			SwingUtilities.updateComponentTreeUI(getOptionPanel());
		}
	}
	
}
