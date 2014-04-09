package de.varylab.varylab.plugin.remeshing;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class DomainCutPlugin extends ShrinkPanelPlugin implements ActionListener {

	private HalfedgeInterface 
		hif = null;
	
	private JCheckBox
		segmentBox = new JCheckBox("Segment");
	
	private JButton
		cutButton = new JButton("Cut");
	
	public DomainCutPlugin() {
		shrinkPanel.add(segmentBox);
		shrinkPanel.add(cutButton);
		cutButton.addActionListener(this);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		
		if(cutButton == s) {
			VHDS hds = hif.get(new VHDS());
			Set<VVertex> vSet = hif.getSelection().getVertices(hds);
			if (vSet.isEmpty()) {
				Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
				JOptionPane.showMessageDialog(w, "Please select vertices:\n - one vertex to define direction cut\n - two to define cut along line");
				return;
			}
			
			AdapterSet a = hif.getAdapters();
			 
			double[][] segment = new double[2][];
			if(vSet.size() < 2) {
				Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
				JOptionPane.showMessageDialog(w, "Please select vertices:\n - one vertex to define direction cut\n - two to define cut along line");
				return;
			}
			Iterator<VVertex> selectedIterator = vSet.iterator();;
			VVertex start = selectedIterator.next();
			double[] p1 = a.getD(TexturePosition2d.class, start);
			segment[0] = new double[]{p1[0], p1[1], 1};
			VVertex target = selectedIterator.next();
			double[] p2 = a.getD(TexturePosition2d.class, target);
			segment[1] = new double[]{p2[0], p2[1], 1}; 
			Set<VVertex> result = new HashSet<VVertex>();
			result.add(start);
			result.add(target);
			TextureUtility.createIntersectionVertices(segment, 1E-8, hds, a, result, segmentBox.isSelected());
			LinkedList<VVertex> orderedResult = new LinkedList<VVertex>(result);
			Collections.sort(orderedResult,new SegmentComparator(Rn.subtract(null, p2, p1), a));
			HalfedgeSelection cutSelection = new HalfedgeSelection();
			cutSelection.addAll(result);
			VVertex v1 = null;
			for (Iterator<VVertex> it = orderedResult.iterator(); it.hasNext();) {
				if(v1 == null) {
					v1 = it.next();
				}
				if(it.hasNext()) {
					VVertex v2 = it.next();
					VFace f = HalfEdgeUtilsExtra.findCommonFace(v1,v2);
					if(f != null) {
						cutSelection.add(RemeshingUtility.splitFaceAt(f, v1, v2));
					}
					v1 = v2;
				}
			}
			hif.setSelection(cutSelection);
			hif.update();
		}		
	}
	
	@Override
	public void install(Controller c) throws Exception {
		hif = c.getPlugin(HalfedgeInterface.class);
		super.install(c);
	}

	private class SegmentComparator implements Comparator<VVertex> {

		double[] v = new double[]{1.0, 0.0};
		AdapterSet as = null;
		
		public SegmentComparator(double[] v, AdapterSet as) {
			this.v = Rn.normalize(null, v);
			this.as = as;
		}

		@Override
		public int compare(VVertex o1, VVertex o2) {
			double[] t1 = as.getD(TexturePosition2d.class, o1);
			double[] t2 = as.getD(TexturePosition2d.class, o2);
			return Double.compare(Rn.innerProduct(v, t1), Rn.innerProduct(v, t2)); 
		}
		
	}
}
