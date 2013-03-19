package de.varylab.varylab.plugin.optimization;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.optimization.IterationProtocol;
import de.varylab.varylab.optimization.ProtocolValue;
import de.varylab.varylab.plugin.VarylabShrinkPlugin;

public class IterationProtocolPanel extends VarylabShrinkPlugin {

	private Map<Long, XYSeries>
		seriesMap = new HashMap<Long, XYSeries>();
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
		chartPanel.setPreferredSize(new Dimension(100, 250));
	}
	
	public void appendIterationProtocol(List<IterationProtocol> pList) {
		for (IterationProtocol p : pList) {
			String optName = p.getOptimizer().getName();
			for (ProtocolValue pv : p.getValues()) {
				long id = pv.getSeriesId();
				if (!seriesMap.containsKey(id)) {
					XYSeries s = new XYSeries(optName + " " + pv.getName());
					dataSet.addSeries(s);
					seriesMap.put(id, s);
				}
				XYSeries s = seriesMap.get(id);
				int x = s.getItemCount() + 1;
				s.add(x, pv.getValue());
			}
		}
	}
	public void resetProtokoll() {
		dataSet.removeAllSeries();
		seriesMap.clear();
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
