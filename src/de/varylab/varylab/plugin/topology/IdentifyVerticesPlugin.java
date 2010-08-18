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
import java.util.List;
import java.util.Vector;

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
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.discreteconformal.heds.bsp.HasBspPos;
import de.varylab.discreteconformal.heds.bsp.KdTree;

public class IdentifyVerticesPlugin extends AlgorithmDialogPlugin implements ChangeListener {

	private JPanel
		panel = new JPanel();
	
	private SpinnerNumberModel
		distanceModel = new SpinnerNumberModel(1E-6,0.0,1.0,0.1);
	
	private JSpinner
		distanceSpinner = new JSpinner(distanceModel);
	
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
		
		panel.add(new JLabel("Distance"), gbc1);
		panel.add(distanceSpinner, gbc2);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, CalculatorSet c, HalfedgeInterface hif) throws CalculatorException {
		HalfedgeSelection oldSel = hif.getSelection();
		HalfedgeSelection identifySel = new HalfedgeSelection();
		double distance = distanceModel.getNumber().doubleValue();
		List<V> vertices = hds.getVertices();
		if(vertices.get(0) instanceof HasBspPos) {
			KdTree<HasBspPos> kdtree = new KdTree<HasBspPos>((List<HasBspPos>)vertices, 5, false);
			for(V v : vertices) {
				HasBspPos hv = (HasBspPos)v;
				for(HasBspPos near : kdtree.collectKNearest(hv, 3)) {
					double dist = kdtree.distance2(near, hv);
					if(near != v && (dist <= distance)) {
						identifySel.setSelected(v, true);
					}
				}
			}
		} else {
			VertexPositionCalculator vc = c.get(hds.getVertexClass(), VertexPositionCalculator.class);
			if(vc == null) {
				throw new CalculatorException("No VertexPositionCalculators found for " + hds.getVertexClass());
			}
			for(V v : vertices) {
				for(V w : vertices) {
					if(v.getIndex() < w.getIndex()) {
						double dist = Rn.euclideanDistance(vc.get(v), vc.get(w));
						if(dist <= distance) {
							identifySel.setSelected(v, true);
							identifySel.setSelected(w, true);
						}
					}
				}
			}
		}
		hif.setSelection(identifySel);
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
		executeAfterDialog(hcp.get(), hcp.getCalculators(), hcp);
		
	}
}
