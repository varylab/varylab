package de.varylab.varylab.plugin.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.JButton;

import de.jreality.plugin.basic.View;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.smoothing.LaplacianSmoothing;

public class Toolbox extends ShrinkPanelPlugin implements ActionListener {

	JButton
		selectGeodesicButton = new JButton("Select Geodesic"),
		selectLatticeButton = new JButton("Select Lattice"),
		smoothCombButton = new JButton("Smooth (Comb.)");
	
	
	HalfedgeInterface 
		hif = null;
	
	public Toolbox() {
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		shrinkPanel.add(selectGeodesicButton,gbc2);
		shrinkPanel.add(selectLatticeButton,gbc2);
		shrinkPanel.add(smoothCombButton,gbc2);
		selectGeodesicButton.addActionListener(this);
		selectLatticeButton.addActionListener(this);
		smoothCombButton.addActionListener(this);
	}
	
	private void selectGeodesic() {
		VHDS hds = hif.get(new VHDS());
		HalfedgeSelection hes = hif.getSelection();
		Set<VEdge> all = new HashSet<VEdge>();
		for(VEdge e : hif.getSelection().getEdges(hds)) {
			all.addAll(selectGeodesic(e, hds));
		}
		hes.addAll(all);
		hif.setSelection(hes);
	}

	private void selectLattice() {
		VHDS hds = hif.get(new VHDS());
		HalfedgeSelection hes = hif.getSelection();
		Set<VVertex> all = new HashSet<VVertex>();
		for(VVertex v : hif.getSelection().getVertices(hds)) {
			all.addAll(selectSublattice(v, hds));
		}
		hes.addAll(all);
		hif.setSelection(hes);
	}
	private Set<VVertex> selectSublattice(VVertex v, VHDS hds) {
		Set<VVertex> sl = new HashSet<VVertex>();
		Stack<VVertex> queue = new Stack<VVertex>();
		queue.add(v);
		while(!queue.isEmpty()) {
			VVertex av = queue.pop();
			if(sl.contains(av)) {
				continue;
			}
			for(VEdge e : HalfEdgeUtilsExtra.get1Ring(av)) {
				if(HalfEdgeUtils.isBoundaryEdge(e)) {
					continue;
				}
				VVertex tv = e.getOppositeEdge().getNextEdge().getTargetVertex();
				queue.add(tv);
			}
			sl.add(av);
		}
		return sl;
	}
	
	private Set<VEdge> selectGeodesic(VEdge e, VHDS hds) {
		Set<VEdge> geodesic = new HashSet<VEdge>();
		VEdge next = e;
		geodesic.add(next);
		geodesic.add(next.getOppositeEdge());
		while(!HalfEdgeUtils.isBoundaryVertex(next.getTargetVertex())) {
			next = getOpposingEdge(next);
			if(next == null) break;
			if(!geodesic.add(next)) break;
			next = next.getOppositeEdge();
			if(!geodesic.add(next)) break;
		}
		next = e.getOppositeEdge();
		while(!HalfEdgeUtils.isBoundaryVertex(next.getTargetVertex())) {
			next = getOpposingEdge(next);
			if(next == null) break;
			if(!geodesic.add(next)) break;
			next = next.getOppositeEdge();
			if(!geodesic.add(next)) break;
		}
		return geodesic;
	}

	private VEdge getOpposingEdge(VEdge next) {
		VVertex v = next.getTargetVertex();
		VEdge opposite = next;
		int degree = HalfEdgeUtilsExtra.getDegree(v);
		
		if(degree%2 != 0) return null;
		
		for(int i = 0;i < degree/2; ++i) {
			opposite = opposite.getNextEdge().getOppositeEdge();
		}
		
		return opposite;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Geodesic Selector", "Thilo Roerig");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(selectGeodesicButton == src) {
			selectGeodesic();
		} else if (selectLatticeButton == src) {
			selectLattice();
		} else if (smoothCombButton == src) {
			VHDS hds = hif.get(new VHDS());
			LaplacianSmoothing.smoothCombinatorially(hds, hif.getAdapters(), true);
			hif.set(hds);
		}
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
}
	
