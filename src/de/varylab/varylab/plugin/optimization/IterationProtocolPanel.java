package de.varylab.varylab.plugin.optimization;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;

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
	private Map<Long, Set<Integer>>
		plotAxisIndicesMap = new HashMap<Long, Set<Integer>>();
	private Map<Long, Integer>
		axisIndexMap = new HashMap<Long, Integer>();
	private ValueAxis 
		domainAxis = new NumberAxis();
	private DecimalFormat
		rangeFormat = new DecimalFormat("0.0E0");
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
	private JCheckBox
		activeChecker = new JCheckBox("Protocol", true);
	
	public IterationProtocolPanel() {
		setInitialPosition(SHRINKER_TOP);
		shrinkPanel.setTitle("Optimization Protocol");
		shrinkPanel.setLayout(new BorderLayout(2, 2));
		shrinkPanel.add(chartPanel, BorderLayout.CENTER);
		shrinkPanel.add(controlsPanel, BorderLayout.SOUTH);
		controlsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.insets = new Insets(1, 2, 1, 2);
		controlsPanel.add(activeChecker, c);
		controlsPanel.add(resetButton, c);
		
		resetButton.addActionListener(this);
		
		chartPanel.setPreferredSize(new Dimension(100, 450));
		plot.setRenderer(plotRenderer);
		plot.setGap(10.0);
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		chart.removeLegend();
		RenderingHints hints = new RenderingHints(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
		hints.put(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
		chart.setRenderingHints(hints);
		resetProtokoll();
		updateBackgroundColors();
	}

	public void updateBackgroundColors() {
		Color bgColor = UIManager.getDefaults().getColor("Panel.background");
		plot.setBackgroundPaint(bgColor);
		chart.setBackgroundPaint(bgColor);
		for (XYPlot p : plotMap.values()) {
			p.setBackgroundPaint(bgColor);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (resetButton == e.getSource()) {
			resetProtokoll();
		}
	}
	
	public int getPlotAxisIndex(long protocolId, long valueId) {
		if (axisIndexMap.containsKey(valueId)) {
			return axisIndexMap.get(valueId);
		}
		if (!plotAxisIndicesMap.containsKey(protocolId)) {
			Set<Integer> indexSet = new HashSet<Integer>();
			plotAxisIndicesMap.put(protocolId, indexSet);
		}
		Set<Integer> axesSet = plotAxisIndicesMap.get(protocolId);
		int index = axesSet.size();
		axesSet.add(index);
		return index;
	}
	
	
	public XYPlot getPlotForValue(IterationProtocol p, ProtocolValue v) {
		long id = p.getSeriesId();
		if (!plotMap.containsKey(id)) {
			XYPlot subPlot = new XYPlot();
			String plotName = p.getOptimizer().getName();
			TextTitle plotTitle = new TextTitle(plotName);
		    XYTitleAnnotation plotTitleAnnotation = new XYTitleAnnotation(0.95, 0.95, plotTitle, RectangleAnchor.TOP_RIGHT);
			subPlot.addAnnotation(plotTitleAnnotation);
			subPlot.setBackgroundPaint(plot.getBackgroundPaint());
			plot.add(subPlot);
			plotMap.put(id, subPlot);
		}
		return plotMap.get(id);
	}
	
	public XYSeries getSeriesForValue(IterationProtocol p, ProtocolValue v) {
		long pId = p.getSeriesId();
		long vId = v.getSeriesId();
		if (!seriesMap.containsKey(vId)) {
			XYPlot subPlot = getPlotForValue(p, v);
			// add a new axis
			int index = getPlotAxisIndex(pId, vId);
			System.out.println("index of " + v.getName() + ": " + index);
			String name = v.getName();
			NumberAxis axis = new NumberAxis(name);
			axis.setNumberFormatOverride(rangeFormat);
			axis.setLabelPaint(v.getColor());
			subPlot.setRangeAxis(index, axis);
			subPlot.setRangeAxisLocation(index, AxisLocation.BOTTOM_OR_LEFT);
			// data set
			XYSeriesCollection xyDataSet = new XYSeriesCollection();
			subPlot.setDataset(index, xyDataSet);
			subPlot.mapDatasetToRangeAxis(index, index);
			// renderer
			SamplingXYLineRenderer renderer = new SamplingXYLineRenderer();
			renderer.setSeriesPaint(0, v.getColor());
			subPlot.setRenderer(index, renderer);
			//data series
			XYSeries s = new XYSeries(name);
			xyDataSet.addSeries(s);
			seriesMap.put(vId, s);
		}
		return seriesMap.get(vId);
	}
	
	
	public void appendIterationProtocol(List<IterationProtocol> pList) {
		for (IterationProtocol p : pList) {
			for (ProtocolValue v : p.getValues()) {
				XYSeries s = getSeriesForValue(p, v);
				s.add(activeIteration, v.getValue());
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
		axisIndexMap.clear();
		plotAxisIndicesMap.clear();
		activeIteration = 0;
	}
	
	public boolean isProtocolActive() {
		return activeChecker.isSelected();
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
