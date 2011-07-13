/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

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
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class IdentifyVerticesPlugin extends AlgorithmDialogPlugin implements ChangeListener {

	private JPanel
		panel = new JPanel();
	
	private SpinnerNumberModel
		distanceModel = new SpinnerNumberModel(0.0,0.0,1.0,0.1);
	
	private JSpinner
		distanceSpinner = new JSpinner(distanceModel);
	
	private HashMap<Vertex<?,?,?>, Vertex<?,?,?>>
		identificationMap = new HashMap<Vertex<?,?,?>, Vertex<?,?,?>>();
	
	private JLabel
		infoLabel = new JLabel("Vertex pairs found:");

	private JCheckBox
		noEdgeCollapseChecker = new JCheckBox("no edge collapse"),
		boundaryOnlyChecker = new JCheckBox("Boundary only");
	
	private HalfedgeSelection 
		oldSelection = null;
	
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

				StitchingUtility.stitch(hds, (V)v, (V)w, da);
				alreadyMerged.add(v);
				alreadyMerged.add(w);
			}

			hif.set(hds);
		}
		hif.setSelection(oldSelection);
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
		HalfedgeSelection identifySel = new HalfedgeSelection();
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
						double dist = Rn.euclideanDistance(a.get(Position3d.class, v), a.get(Position.class, w));
						System.out.println(v +" - "+w+": "+dist);
						if(dist <= distance) {
							if(noEdgeCollapseChecker.isSelected()) {
								if(HalfEdgeUtils.findEdgeBetweenVertices((V)v, (V)w) != null) {
									continue;
								}
							}
							identifySel.setSelected(v, true);
							identifySel.setSelected(w, true);
							if((identificationMap.containsKey(v) && identificationMap.get(v) != w) ||
									(identificationMap.containsKey(w) && identificationMap.get(w) != v)){
								infoLabel.setText("identification impossible - not unique");
								return false;
							}
							identificationMap.put(w, v);
							identificationMap.put(v, w);
							identifySel.setSelected(v, true);
							identifySel.setSelected(w, true);
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
		oldSelection = hcp.getSelection();
		double minEdgeLength = Double.POSITIVE_INFINITY;
		for(E e : hds.getPositiveEdges()) {
			double length = a.get(Length.class, e, Double.class);
			if(length < minEdgeLength) {
				minEdgeLength = length;
			}
		}
//		distanceModel.setMaximum(5*minEdgeLength);
//		distanceModel.setStepSize(minEdgeLength/5.0);
//		TypedAdapterSet<double[]> da = a.querySet(double[].class);
//		calculateAndShowIdentification(hds,hcp, da);
	}
}
