package de.varylab.varylab.plugin.generator;

import java.util.Random;

import javax.swing.JOptionPane;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class RandomPointsUnitDisc extends AlgorithmPlugin {

	private Random 
		rnd = new Random();
	
	private int
		extraPoints = 20;

	public String getCategory() {
		return "Hyperbolic Delaunay";
	}

	@Override
	public String getAlgorithmName() {
		return "Random Points in UnitDisc";
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(RandomPointsUnitDisc.class, "numPoints", extraPoints);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		extraPoints = c.getProperty(RandomPointsUnitDisc.class, "numPoints", 20);
	}
	

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		String numString = JOptionPane.showInputDialog(getOptionParent(), "Number of points", extraPoints);
		if (numString == null) return;
		extraPoints = Integer.parseInt(numString);
		HDS r = hi.createEmpty(hds);
		double[][] sites = new double[extraPoints][2];
		int i = 0;
		while(i < extraPoints) {
			sites[i] = new double[]{-1.0+2*rnd.nextDouble(), -1.0+2*rnd.nextDouble()};
			double sq = Rn.euclideanNormSquared(sites[i]);
			if(sq >= 1) { continue; }
			V v = r.addNewVertex();
			a.set(Position.class, v, sites[i]);
			++i;
		}
		hi.set(r);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Create hyperbolic Delaunay complex of random pointset");
		info.icon = ImageHook.getIcon("RandomSphere.png",16,16);
		return info;
	}
	
	public void setSeed(int seed) {
		rnd.setSeed(seed);
	}

}