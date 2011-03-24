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
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Area;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class CollapseTrianglesPlugin extends AlgorithmDialogPlugin implements ChangeListener {

	private JPanel
		panel = new JPanel();
	
	private SpinnerNumberModel
		areaModel = new SpinnerNumberModel(0.0,0.0,1.0,0.1);
	
	private JSpinner
		areaSpinner = new JSpinner(areaModel);
	
	private Set<Face<?,?,?>>
		collapseSet = new HashSet<Face<?,?,?>>();
	
	private JLabel
		infoLabel = new JLabel("Vertex pairs found:"),
		minLabel = new JLabel("Minimal triangle area:");

	private HalfedgeSelection 
		oldSelection = null;
	
	public CollapseTrianglesPlugin() {
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
		
		areaSpinner.addChangeListener(this);
		
		panel.add(new JLabel("Area"), gbc1);
		panel.add(areaSpinner, gbc2);
		
		panel.add(minLabel,gbc2);
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
		while(calculateAndShowTriangles(hds, hif, a)) {
			for(Face<?,?,?> f : collapseSet) {
				double[] p = a.get(BaryCenter3d.class, f, double[].class);
				double[] tp = a.get(TexturePosition2d.class, f, double[].class);
				V v = TopologyAlgorithms.collapseFace((F)f);
				a.set(Position.class, v, p);
				a.set(TexturePosition.class, v, tp);
				break;
			}
		}
		hif.set(hds);
		hif.setSelection(oldSelection);
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Topology;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Small Triangles";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Collapse small Triangles", "Thilo Roerig");
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		calculateAndShowTriangles(hcp.get(), hcp, hcp.getAdapters());
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> boolean calculateAndShowTriangles(HDS hds, HalfedgeInterface hif, AdapterSet a) {
		boolean smallTrianglesFound = false;
		double distance = areaModel.getNumber().doubleValue();
		collapseSet.clear();
		infoLabel.setText("");
		HalfedgeSelection identifySel = new HalfedgeSelection();
		for(F f : hds.getFaces()) {
			double area = a.get(Area.class, f, Double.class);
			if(area < distance) {
				identifySel.add(f);
				collapseSet.add(f);
				smallTrianglesFound = true;
			}
		}
		hif.setSelection(identifySel);
		infoLabel.setText("Small triangles found:" + collapseSet.size());
		return smallTrianglesFound;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeBeforeDialog(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		oldSelection = hcp.getSelection();
		double minTriangleArea = Double.POSITIVE_INFINITY;
		for(F f : hds.getFaces()) {
			double area = a.get(Area.class, f, Double.class);
			if(area < minTriangleArea) {
				minTriangleArea = area;
			}
		}
		minLabel.setText("Minimal triangle area:" + minTriangleArea);
		areaModel.setStepSize(minTriangleArea);
	}
}
