package de.varylab.varylab.plugin.topology;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.Length;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class IdentifyVerticesPlugin extends AlgorithmDialogPlugin implements ChangeListener {

	private JPanel
		panel = new JPanel();
	
	private SpinnerNumberModel
		distanceModel = new SpinnerNumberModel(0.0,0.0,Double.POSITIVE_INFINITY,0.1);
	
	private JSpinner
		distanceSpinner = new JSpinner(distanceModel);
	
	private HashMap<Vertex<?,?,?>, Vertex<?,?,?>>
		identificationMap = new HashMap<Vertex<?,?,?>, Vertex<?,?,?>>();
	
	private JLabel
		infoLabel = new JLabel("Vertex pairs found:"),
		minLengthLabel = new JLabel(),
		minDistanceLabel = new JLabel();

	private JCheckBox
		noEdgeCollapseChecker = new JCheckBox("no edge collapse"),
		boundaryOnlyChecker = new JCheckBox("Boundary only");
	
	private Selection 
		oldSelection = null;
	
	private double[][] distances = null;

	private double minEdgeLength;

	private double minDistance;

	public IdentifyVerticesPlugin() {
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = GridBagConstraints.RELATIVE;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		distanceSpinner.addChangeListener(this);
		
		panel.add(noEdgeCollapseChecker,gbc2);
		panel.add(boundaryOnlyChecker,gbc2);
		noEdgeCollapseChecker.setSelected(true);
		boundaryOnlyChecker.setSelected(true);
		panel.add(new JLabel("minimum edge length"),gbc1);
		panel.add(minLengthLabel, gbc2);
		panel.add(new JLabel("minimum vertex distance"),gbc1);
		panel.add(minDistanceLabel, gbc2);
		panel.add(new JLabel("Distance"), gbc1);
		panel.add(distanceSpinner, gbc2);
		
		panel.add(infoLabel,gbc2);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		TypedAdapterSet<double[]> da = a.querySet(double[].class);
		if(calculateAndShowIdentification(hds, hif, da)) {
			HashSet<Vertex<?,?,?>> alreadyMerged = new HashSet<Vertex<?,?,?>>();
			for(Vertex<?,?,?> v : identificationMap.keySet()) {
				if(alreadyMerged.contains(v)) {
					continue;
				}

				Vertex<?,?,?> w = identificationMap.get(v);
				E edge = HalfEdgeUtils.findEdgeBetweenVertices((V)v,(V)w);
				if(edge != null) {
					TopologyAlgorithms.collapseEdge(edge);
				} else {
					StitchingUtility.stitch(hds, (V)v, (V)w, da);
				}
				alreadyMerged.add(v);
				alreadyMerged.add(w);
			}

			hif.set(hds);
		}
		hif.setSelection(oldSelection);
		distances = null;
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Topology;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Identify Vertices";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Identify Vertices", "Thilo Roerig");
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		TypedAdapterSet<double[]> a = hcp.getAdapters().querySet(double[].class);
		calculateAndShowIdentification(hcp.get(), hcp, a);
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> boolean calculateAndShowIdentification(HDS hds, HalfedgeInterface hif, TypedAdapterSet<double[]> a) {
		double distance = distanceModel.getNumber().doubleValue();
		identificationMap.clear();
		infoLabel.setText("");
		Selection identifySel = new Selection();
//		KdTree<V, E, F> kdtree = new KdTree<V, E, F>(hds, a, 5, false);
//		for(V v : hds.getVertices()) {
//			if(boundaryOnlyChecker.isSelected() && !HalfEdgeUtils.isBoundaryVertex(v)) {
//				continue;
//			}
//			double[] hv = a.get(Position3d.class, v);
//			for(V near : kdtree.collectKNearest(hv, 3)) {
//				double[] nearP = a.get(Position3d.class, near);
//				double dist = kdtree.distance2(nearP, hv);
//				if(near != v && (dist <= distance)) {
//					if(noEdgeCollapseChecker.isSelected()) {
//						if(HalfEdgeUtils.findEdgeBetweenVertices((V)v, (V)near) != null) {
//							continue;
//						}
//					}
//					identifySel.setSelected(v, true);
//					identifySel.setSelected((Vertex<?,?,?>)near, true);
//					if((identificationMap.containsKey(v) && identificationMap.get(v) != near) ||
//							(identificationMap.containsKey(near) && identificationMap.get(near) != v)) {
//						infoLabel.setText("identification impossible - not unique");
//						return false;
//					}
//					identificationMap.put((Vertex<?, ?, ?>) near, v);
//					identificationMap.put(v, (Vertex<?, ?, ?>) near);
//				}
//			}
//		}
//		} else {
			List<V> vertices = hds.getVertices();
			for(V v : vertices) {
				if(boundaryOnlyChecker.isSelected() && !HalfEdgeUtils.isBoundaryVertex(v)) {
					continue;
				}
				for(V w : vertices) {
					if(boundaryOnlyChecker.isSelected() && !HalfEdgeUtils.isBoundaryVertex(w)) {
						continue;
					}
					if(v.getIndex() < w.getIndex()) {
//						System.out.println(v +" - "+w+": "+dist);
						if(distances[w.getIndex()][v.getIndex()] <= distance) {
							if(noEdgeCollapseChecker.isSelected()) {
								if(HalfEdgeUtils.findEdgeBetweenVertices(v, w) != null) {
									continue;
								}
							}
							identifySel.add(v);
							identifySel.add(w);
							if((identificationMap.containsKey(v) && identificationMap.get(v) != w) ||
									(identificationMap.containsKey(w) && identificationMap.get(w) != v)){
								infoLabel.setText("identification impossible - not unique");
								return false;
							}
							identificationMap.put(w, v);
							identificationMap.put(v, w);
							identifySel.add(v);
							identifySel.add(w);
						}
					}
				}
			}
//		}
		hif.setSelection(identifySel);
		infoLabel.setText("Vertex pairs found:" + identificationMap.size()/2);
		return true;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeBeforeDialog(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		distances = new double[hds.numVertices()][];
		oldSelection = hcp.getSelection();
		minEdgeLength = Double.POSITIVE_INFINITY;
		minDistance = Double.POSITIVE_INFINITY;
		for(E e : hds.getPositiveEdges()) {
			Double length = a.get(Length.class, e, Double.class);
			if(length < minEdgeLength) {
				minEdgeLength = length;
			}
		}
		for(int i = 0; i < hds.numVertices(); ++i) {
			for(int j = 0; j < i; ++j) {
				if(j == 0) {
					distances[i] = new double[i];			
				}
				double[] 
						pi = a.getD(Position3d.class, hds.getVertex(i)),
						pj = a.getD(Position3d.class, hds.getVertex(j));
				distances[i][j] = Rn.euclideanDistance(pi, pj);
				if(distances[i][j] < minDistance) {
					minDistance = distances[i][j];
				}
			}
		}
		minLengthLabel.setText(Double.toString(minEdgeLength));
		minDistanceLabel.setText(Double.toString(minDistance));
	}
}
