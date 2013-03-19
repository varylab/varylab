package de.varylab.varylab.plugin;

import static de.jtem.jpetsc.NormType.NORM_FROBENIUS;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jpetsc.Vec;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.optimization.IterationProtocol;
import de.varylab.varylab.optimization.ProtocolValue;
import de.varylab.varylab.optimization.tao.TaoDomainValue;
import de.varylab.varylab.optimization.tao.TaoEnergy;
import de.varylab.varylab.optimization.tao.TaoGradient;

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
	
	public IterationProtocol getIterationProtocol(Vec solution, VHDS hds) {
		Functional<VVertex, VEdge, VFace> f = getFunctional(hds);
		TaoDomainValue x = new TaoDomainValue(solution);
		Vec Gvec = new Vec(f.getDimension(hds));
		TaoGradient G = new TaoGradient(Gvec);
		TaoEnergy E = new TaoEnergy();
		f.evaluate(hds, x, E, G, null);
		double gNorm = Gvec.norm(NORM_FROBENIUS);
		ProtocolValue gVal = new ProtocolValue(gNorm, "Gradient Norm", gradientId);
		ProtocolValue eVal = new ProtocolValue(E.get(), "Energy Value", energyId);
		List<ProtocolValue> pList = new LinkedList<ProtocolValue>();
		pList.add(eVal);
		pList.add(gVal);
		return new IterationProtocol(this, pList, protocolId);
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		if (getOptionPanel() != null) {
			SwingUtilities.updateComponentTreeUI(getOptionPanel());
		}
	}
	
}
