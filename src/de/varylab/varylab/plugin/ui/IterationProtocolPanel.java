package de.varylab.varylab.plugin.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.optimization.IterationProtocol;
import de.varylab.varylab.plugin.VarylabShrinkPlugin;

public class IterationProtocolPanel extends VarylabShrinkPlugin {

	private List<List<IterationProtocol>>
		protocol = new LinkedList<List<IterationProtocol>>();
	private NumberAxis 
		domainAxis = new NumberAxis(),
		rangeAxis = new NumberAxis();
	private XYSeriesCollection
		dataSet = new XYSeriesCollection();
	private SamplingXYLineRenderer
		plotRenderer = new SamplingXYLineRenderer();
	private XYPlot
		plot = new XYPlot(dataSet, domainAxis, rangeAxis, plotRenderer);
	private JFreeChart
		chart = new JFreeChart(plot);
	private ChartPanel
		chartPanel = new ChartPanel(chart);
	
	public IterationProtocolPanel() {
		setInitialPosition(SHRINKER_TOP);
		shrinkPanel.setTitle("Optimization Protocol");
		shrinkPanel.setLayout(new GridLayout());
		shrinkPanel.add(chartPanel);
		chartPanel.setPreferredSize(new Dimension(100, 150));
	}
	
	public void appendIterationProtocol(List<IterationProtocol> p) {
		protocol.add(p);
	}
	public void resetProtokoll() {
		protocol.clear();
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
