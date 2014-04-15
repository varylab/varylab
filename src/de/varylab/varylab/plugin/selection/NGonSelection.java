package de.varylab.varylab.plugin.selection;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.icon.ImageHook;

public class NGonSelection extends AlgorithmDialogPlugin implements ActionListener {

	private final Integer N_GON_CHANNEL_ID = 101010101;

	private JPanel dialogPanel = new JPanel();
	
	private TreeSet<Integer>
		selectedSizes = new TreeSet<>();
		
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "n-gons";
	}
	
	@Override
	public double getPriority() {
		return 1.0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		Selection hes = new Selection();
		for(F f : hds.getFaces()) {
			List<V> boundaryVertices = HalfEdgeUtils.boundaryVertices(f);
			if(selectedSizes.contains(boundaryVertices.size())) {
				hes.add(f,N_GON_CHANNEL_ID);
				hes.addAll(boundaryVertices,N_GON_CHANNEL_ID);
			}
		}
		hif.update();
		hif.addSelection(hes);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeBeforeDialog(final HDS hds, AdapterSet a, HalfedgeInterface hi) {
		selectedSizes.clear();
		
		Runnable dialogUpdateRunner = new Runnable() {
			@Override
			public void run() {
				updateDialogPanel(hds);
				
			}
		};
		SwingUtilities.invokeLater(dialogUpdateRunner);
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void updateDialogPanel(HDS hds) {
		TreeSet<Integer> sizes = new TreeSet<Integer>();
		for(F f : hds.getFaces()) {
			sizes.add(HalfEdgeUtils.boundaryEdges(f).size());
		}
		dialogPanel.removeAll();
		dialogPanel.setLayout(new GridLayout(sizes.size()+1, 1));
		dialogPanel.add(new JLabel("Select faces of size:"));
		for(Integer i : sizes) {
			JCheckBox box = new JCheckBox(i.toString());
			box.addActionListener(this);
			dialogPanel.add(box);
		}
		dialogPanel.revalidate();
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("ngon_selection.png",16,16);
		return info;
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return dialogPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if(s instanceof JCheckBox) {
			JCheckBox b = (JCheckBox)s;
			int i = Integer.parseInt(b.getText());
			if(b.isSelected()) {
				selectedSizes.add(i);
			} else {
				selectedSizes.remove(i);
			}
		}
	}
	
	@Override
	public void install(Controller c) throws Exception {
		c.getPlugin(SelectionInterface.class).registerChannelName(N_GON_CHANNEL_ID, "n-gons");
		super.install(c);
	}
}
