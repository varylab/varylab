package de.varylab.varylab.plugin.nurbs.plugin;

import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.ui.LayoutFactory;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.opennurbs.ONX_Model;
import de.varylab.opennurbs.ON_ArcCurve;
import de.varylab.opennurbs.ON_Curve;
import de.varylab.opennurbs.ON_LineCurve;
import de.varylab.opennurbs.ON_NurbsCurve;
import de.varylab.opennurbs.ON_PolylineCurve;
import de.varylab.opennurbs.OpenNurbsIO;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.plugin.io.NurbsIO;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;
import de.varylab.varylab.utilities.OpenNurbsUtility;

public class NurbsIOPlugin extends ShrinkPanelPlugin implements HalfedgeListener, ChangeListener {

	private static Logger logger = Logger.getLogger(NurbsIOPlugin.class.getName());
	
	private JFileChooser 
		chooser = new JFileChooser();

	private JButton
		exportButton = new JButton(new ExportAction()),
		importButton = new JButton(new ImportAction());

	private HalfedgeInterface 
		hif = null;
	
	private NurbsUVAdapter
		activeNurbsAdapter = null;

	private boolean 
		jOpenNurbs = true;

	private SpinnerNumberModel
		uModel = new SpinnerNumberModel(10, 0, 100, 5),
		vModel = new SpinnerNumberModel(10, 0, 100, 5);
	
	private JSpinner
		uSpinner = new JSpinner(uModel),
		vSpinner = new JSpinner(vModel);
	
	private ShrinkPanel
		infoPanel = new ShrinkPanel("Mesh parameters");
	
	private boolean
		loading = false;

	public NurbsIOPlugin() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = 1;
		
		configureFileChooser();
		importButton.setToolTipText("Load Nurbs surface");
		
		shrinkPanel.add(new JLabel("NURBS surface"),c);
		c.weightx = 0.0;
		shrinkPanel.add(importButton,c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		shrinkPanel.add(exportButton,c);
		exportButton.setEnabled(false);

		uModel.setMinimum(2);
		vModel.setMinimum(2);
		uModel.setMaximum(Integer.MAX_VALUE);
		vModel.setMaximum(Integer.MAX_VALUE);

		infoPanel.setLayout(new GridBagLayout());
		uSpinner.addChangeListener(this);
		vSpinner.addChangeListener(this);
		c.gridwidth = 1;
		infoPanel.add(new JLabel("u-lines"),c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		infoPanel.add(uSpinner,c);
		c.gridwidth = 1;
		infoPanel.add(new JLabel("v-lines"),c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		infoPanel.add(vSpinner,c);
		infoPanel.setShrinked(true);
		shrinkPanel.add(infoPanel,c);
	}
	
	private void configureFileChooser() {
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "Wavefront OBJ (*.obj)";
			}
			
			@Override
			public String toString() {
				return getDescription();
			}
		});
		if(jOpenNurbs) {
			chooser.addChoosableFileFilter(new FileFilter(){
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().toLowerCase().endsWith(".3dm");
				}

				@Override
				public String getDescription() {
					return "OpenNurbs 3dm (*.3dm)";
				}

				@Override
				public String toString() {
					return getDescription();
				}
			});
		}
	}	
	
	private class ImportAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public ImportAction() {
			putValue(SMALL_ICON, ImageHook.getIcon("folder.png"));
//			putValue(NAME, "Import");
			putValue(SHORT_DESCRIPTION, "Import");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			loading = true;
			Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			chooser.setDialogTitle("Import Into Layer");
			int result = chooser.showOpenDialog(w);
			if (result != JFileChooser.APPROVE_OPTION) return;
			File file = chooser.getSelectedFile();
			try {
				if (file.getName().toLowerCase().endsWith(".obj")) {
					NURBSSurface surface = NurbsIO.readNURBS(new FileReader(file));
					System.out.println("surface to String");
					System.out.println(surface.toString());
					System.out.println("surface to obj");
					System.out.println(surface.toObj());
					System.out.println("surface to code");
					System.out.println(surface.toReadableInputString());
					if(surface.getClosingDir() == ClosingDir.uClosed){
						logger.info("surface.isClosedUDir()");
					}
					if(surface.getClosingDir() == ClosingDir.vClosed){
						logger.info("surface.isClosedVDir()");
					}
					surface.setName(file.getName());
					logger.info(surface.toObj());
					logger.info("\n");
					Icon icon = getPluginInfo().icon != null ? getPluginInfo().icon : ImageHook.getIcon("folder.png");
					NurbsParameterPanel npp = new NurbsParameterPanel(surface);
					int dialogOk = JOptionPane.showConfirmDialog(
						w, npp, getPluginInfo().name, OK_CANCEL_OPTION,	PLAIN_MESSAGE, icon);
					if(dialogOk == JOptionPane.OK_OPTION) {
						NurbsSurfaceUtility.addNurbsMesh(surface, hif.getActiveLayer(),npp.getU(),npp.getV());

						uModel.setValue(npp.getU());
						vModel.setValue(npp.getV());
					}
				} else if(jOpenNurbs && file.getName().toLowerCase().endsWith(".3dm")) {
					ONX_Model model = OpenNurbsIO.readFile(file.getPath());
					List<NURBSSurface> nsurfaces = OpenNurbsUtility.getNurbsSurfaces(model);
					int i = 1;
					for(NURBSSurface surface : nsurfaces) {
						if(!surface.hasClampedKnotVectors()) {
							surface.repairKnotVectors();
						}

						HalfedgeLayer newLayer = new HalfedgeLayer(hif);
						newLayer.setName(file.getName() + " surface " + i++);
						NurbsSurfaceUtility.addNurbsMesh(surface,newLayer);
						hif.addLayer(newLayer);
					}
					List<ON_Curve> curves = OpenNurbsUtility.getCurves(model);
					for(ON_Curve curve : curves) {
						// TODO: Put different object onto different layers
						if(curve instanceof ON_PolylineCurve) {
							ON_PolylineCurve plc = (ON_PolylineCurve) curve;
							OpenNurbsUtility.addPolylineCurve(plc, hif.get(new VHDS()), hif.getAdapters());
						} else if(curve instanceof ON_ArcCurve) {
							ON_ArcCurve ac = (ON_ArcCurve) curve;
							OpenNurbsUtility.addArcCurve(ac, hif.get(new VHDS()), hif.getAdapters());
						} else if(curve instanceof ON_LineCurve) {
							ON_LineCurve ac = (ON_LineCurve) curve;
							OpenNurbsUtility.addLineCurve(ac, hif.get(new VHDS()), hif.getAdapters());
						} else if(curve instanceof ON_NurbsCurve) {
							ON_NurbsCurve ac = (ON_NurbsCurve) curve;
							OpenNurbsUtility.addNurbsCurve(ac, hif.get(new VHDS()), hif.getAdapters());
						}
					}
					List<IndexedFaceSet> meshes = OpenNurbsUtility.getMeshes(model);
					for(IndexedFaceSet mesh : meshes) {
						HalfedgeLayer newLayer = new HalfedgeLayer(hif);
						newLayer.setName(file.getName() + " mesh " + i++);
						newLayer.set(mesh);
						hif.addLayer(newLayer);
					}
					model.dispose();
					hif.update();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(w, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
				loading = false;
			}
			loading = false;
		}
		
		private class NurbsParameterPanel extends JPanel {
			
			private static final long serialVersionUID = 1L;

			private SpinnerNumberModel
				uModel = new SpinnerNumberModel(11,0,Integer.MAX_VALUE,2),
				vModel = new SpinnerNumberModel(11,0,Integer.MAX_VALUE,2);
			
			private JSpinner
				uSpinner = new JSpinner(uModel),
				vSpinner = new JSpinner(vModel);
			
			private JPanel
				infoPanel = new JPanel(),
				paramPanel = new JPanel();
	
			public NurbsParameterPanel(NURBSSurface surf) {
				super(new GridBagLayout());
				GridBagConstraints rc = LayoutFactory.createRightConstraint();
				
				add(new JLabel("Surface info"),rc);
				
				infoPanel.setLayout(new GridLayout(4, 1));
				infoPanel.add(new JLabel("u-Degree: " + surf.getUDegree())); 
				infoPanel.add(new JLabel("u-Knots:  " + surf.getUKnotVector().length));
				infoPanel.add(new JLabel("v-Degree: " + surf.getVDegree())); 
				infoPanel.add(new JLabel("v-Knots:  " + surf.getVKnotVector().length));
				
				add(infoPanel,rc);
				
				add(new JSeparator(SwingConstants.HORIZONTAL),rc);
				
				add(new JLabel("Parameters:"),rc);
				
				paramPanel.setLayout(new GridLayout(2,2));
				uModel.setValue(Math.min(2*surf.getNumUPoints(),100));
				paramPanel.add(new JLabel("u-Lines")); 
				paramPanel.add(uSpinner);
				vModel.setValue(Math.min(2*surf.getNumVPoints(),100));
				paramPanel.add(new JLabel("v-Lines")); 
				paramPanel.add(vSpinner);
				
				add(paramPanel,rc);
			}
			
			public int getU() {
				return uModel.getNumber().intValue();
			}
			
			public int getV() {
				return vModel.getNumber().intValue();
			}
		}
	}
	
	private class ExportAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ExportAction() {
//			putValue(NAME, "Export");
			putValue(SMALL_ICON, ImageHook.getIcon("disk.png"));
			putValue(SHORT_DESCRIPTION, "Export NURBS Surface");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			chooser.setDialogTitle("Export Layer Geometry");
			chooser.setPreferredSize(new Dimension(800, 700));
			int result = chooser.showSaveDialog(w);
			if (result != JFileChooser.APPROVE_OPTION)
				return;
			File file = chooser.getSelectedFile();

			String name = file.getName().toLowerCase();
			if (!(name.endsWith(".obj") || name.endsWith(".3dm"))) {
				file = new File(file.getAbsoluteFile() + ".obj");
			}
			if (file.exists()) {
				int result2 = JOptionPane.showConfirmDialog(w,
						"File " + file.getName() + " exists. Overwrite?",
						"Overwrite?", JOptionPane.YES_NO_OPTION);
				if (result2 != JOptionPane.YES_OPTION)
					return;
			}
			try {
				if(name.endsWith(".obj")) {
					NurbsIO.writeOBJ(activeNurbsAdapter.getSurface(),file);	
				} else if(jOpenNurbs && name.endsWith(".3dm")) {
					OpenNurbsUtility.write(activeNurbsAdapter.getSurface(), file);
				}
				
			} catch (final Exception ex) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(w, ex.getMessage(), ex
								.getClass().getSimpleName(), ERROR_MESSAGE);
					}
				};
				EventQueue.invokeLater(r);
			}
		}
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		String chooserDir = chooser.getCurrentDirectory().getAbsolutePath();
		c.storeProperty(getClass(), "importExportLocation", chooserDir);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		String chooserDir = System.getProperty("user.dir");
		chooserDir = c.getProperty(getClass(), "importExportLocation", chooserDir);
		File chooserDirFile = new File(chooserDir);
		if (chooserDirFile.exists()) {
			chooser.setCurrentDirectory(chooserDirFile);
		}
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void dataChanged(HalfedgeLayer layer) {
		updateExportButton(layer);
		
	}

	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		updateExportButton(layer);
	}

	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		if(old == active) {
			return;
		}
		updateExportButton(active);
	}

	@Override
	public void layerCreated(HalfedgeLayer layer) {
		updateExportButton(layer);
	}

	@Override
	public void layerRemoved(HalfedgeLayer layer) {
		updateExportButton(layer);
	}

	private void updateExportButton(HalfedgeLayer layer) {
		AdapterSet as = layer.getAdapters();
		as.addAll(layer.getVolatileAdapters());
		NurbsUVAdapter nurbsUVAdapter = as.query(NurbsUVAdapter.class);
		if(nurbsUVAdapter == null) {
			nurbsUVAdapter = layer.getActiveAdapters().query(NurbsUVAdapter.class);
		}
		activeNurbsAdapter = nurbsUVAdapter;
		if(activeNurbsAdapter != null) {
			uSpinner.setValue(activeNurbsAdapter.getULineCount());
			vSpinner.setValue(activeNurbsAdapter.getVLineCount());
		}
		exportButton.setEnabled(activeNurbsAdapter != null);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
	}
	
	public static void main(String[] args) {
		NativePathUtility.set("native");
		boolean jOpenNurbsEnabled = true;
		try {
			System.loadLibrary("jopennurbs");
		} catch (UnsatisfiedLinkError e) {
			logger.severe("Could not find libjopennurbs in natives path.");
			jOpenNurbsEnabled = false;
		}
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.addBasicUI();
		NurbsIOPlugin p = new NurbsIOPlugin();
		p.withJOpenNurbs(jOpenNurbsEnabled);
		v.registerPlugin(p);
		v.startup();
	}

	public void withJOpenNurbs(boolean jOpenNurbsEnabled) {
		this.jOpenNurbs = jOpenNurbsEnabled;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(!loading && (activeNurbsAdapter != null)) {
			NurbsSurfaceUtility.addNurbsMesh(activeNurbsAdapter.getSurface(), hif.getActiveLayer(),uModel.getNumber().intValue(),vModel.getNumber().intValue());
		}
	}
	
}
