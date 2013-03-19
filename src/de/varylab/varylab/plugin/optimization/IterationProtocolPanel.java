package de.varylab.varylab.plugin.optimization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.optimization.IterationProtocol;
import de.varylab.varylab.optimization.ProtocolValue;
import de.varylab.varylab.plugin.VarylabShrinkPlugin;

public class IterationProtocolPanel extends VarylabShrinkPlugin implements ActionListener {

	private int
		activeIteration = 0;
	private Map<Long, XYPlot>
		plotMap = new HashMap<Long, XYPlot>();
	private Map<Long, XYSeries>
		seriesMap = new HashMap<Long, XYSeries>();
	private ValueAxis 
		domainAxis = new NumberAxis();
	private SamplingXYLineRenderer
		plotRenderer = new SamplingXYLineRenderer();
	private CombinedDomainXYPlot
		plot = new CombinedDomainXYPlot(domainAxis);
	private JFreeChart
		chart = new JFreeChart(plot);
	private ChartPanel
		chartPanel = new ChartPanel(chart);
	private JPanel
		controlsPanel = new JPanel();
	private JButton
		resetButton = new JButton("Clear Protocol");
	
	public IterationProtocolPanel() {
		setInitialPosition(SHRINKER_TOP);
		shrinkPanel.setTitle("Optimization Protocol");
		shrinkPanel.setLayout(new BorderLayout(2, 2));
		shrinkPanel.add(chartPanel, BorderLayout.CENTER);
		shrinkPanel.add(controlsPanel, BorderLayout.WEST);
		controlsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		controlsPanel.add(resetButton, c);
		
		resetButton.addActionListener(this);
		
		plot.setRenderer(plotRenderer);
		plot.setBackgroundPaint(Color.DARK_GRAY);
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		chart.setBackgroundPaint(Color.DARK_GRAY);
		resetProtokoll();
		checkPreferredSize();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (resetButton == e.getSource()) {
			resetProtokoll();
		}
	}
	
	
	private void checkPreferredSize() {
		int numPlots = plot.getSubplots().size();
		numPlots = numPlots == 0 ? 1 : numPlots;
		chartPanel.setMinimumSize(new Dimension(100, 100 + 180 * numPlots));
		shrinkPanel.revalidate();
	}
	
	
	private void checkLegendItems() {
		LegendItemCollection lic = plot.getLegendItems();
		for (int i = 0; i < lic.getItemCount(); i++) {
			LegendItem item = lic.get(i);
			item.setFillPaint(Color.DARK_GRAY);
			item.setLabelPaint(Color.DARK_GRAY);
			item.setLineVisible(true);
			item.setShapeVisible(false);
		}
	}
	
	public void appendIterationProtocol(List<IterationProtocol> pList) {
		for (IterationProtocol p : pList) {
			String optName = p.getOptimizer().getName();
			long optId = p.getSeriesId();
			if (!plotMap.containsKey(optId)) {
				XYPlot subPlot = new XYPlot();
				subPlot.setBackgroundPaint(Color.DARK_GRAY);
				plot.add(subPlot);
				plotMap.put(optId, subPlot);
				checkPreferredSize();
			}
			for (ProtocolValue pv : p.getValues()) {
				long id = pv.getSeriesId();
				String name = optName + " " + pv.getName();
				if (!seriesMap.containsKey(id)) {
					XYPlot subPlot = plotMap.get(optId);
					// add a new axis
					int index = subPlot.getSeriesCount();
					NumberAxis axis = new NumberAxis(name);
					subPlot.setRangeAxis(index, axis);
					// data set
					XYSeriesCollection xyDataSet = new XYSeriesCollection();
					subPlot.setDataset(index, xyDataSet);
					subPlot.mapDatasetToRangeAxis(index, index);
					// renderer
					SamplingXYLineRenderer renderer = new SamplingXYLineRenderer();
					subPlot.setRenderer(index, renderer);
					//data series
					XYSeries s = new XYSeries(name);
					xyDataSet.addSeries(s);
					seriesMap.put(id, s);
					checkLegendItems();
				}
				XYSeries s = seriesMap.get(id);
				s.add(activeIteration, pv.getValue());
			}
		}
		activeIteration++;
	}
	public void resetProtokoll() {
		for (XYPlot p : plotMap.values()) {
			plot.remove(p);
		}
		plotMap.clear();
		seriesMap.clear();
		activeIteration = 0;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.vendorName = "Stefan Sechelmann";
		info.email = "sechel@math.tu-berlin.de";
		info.name = "Optimization Protocol Panel";
		return info;
	}
	
}
