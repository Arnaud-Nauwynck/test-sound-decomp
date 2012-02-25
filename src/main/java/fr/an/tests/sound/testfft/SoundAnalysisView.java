package fr.an.tests.sound.testfft;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.event.DatasetChangeListener;
import org.jfree.data.xy.DefaultXYDataset;

public class SoundAnalysisView {

	private SoundAnalysisModel model;

	private JTabbedPane tabbedPane;
	
	private double[] xData;
	
	private DefaultXYDataset mainDataset;
	private JFreeChart mainChart;
	private ChartPanel mainChartPanel;
	
	private DefaultXYDataset residuDataset;
	private JFreeChart residualChart;
	private ChartPanel residualChartPanel;
	
	private double[] approxData;
	private double[] residualData;
	
	
//	private JSpinner mainHarmonicCountSpinner;
	private JSlider mainHarmonicCountSlider;
	private JLabel harmonicCountValueLabel;
	private JLabel harmonicResiduInfoLabel;
	
	// ------------------------------------------------------------------------
	
	public SoundAnalysisView(SoundAnalysisModel model) {
		super();
		this.model = model;
	
		this.tabbedPane = new JTabbedPane();

		JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tabbedPane.add("data", vertSplit);
        // vertSplit.setDividerSize(100);
        vertSplit.setDividerLocation(0.5);
        
		int frameLength = model.getFrameLength();
		this.xData = JFreeChartUtils.createLinearDoubleRange(frameLength);

		this.approxData = new double[frameLength];
		this.residualData = new double[frameLength];
		
        { // mainChartPanel + reconstructed
        	this.mainDataset = new DefaultXYDataset();
			JFreeChartUtils.addDefaultXYDatasetSerie(mainDataset, "raw", xData, model.getAudioDataAsDouble());
			JFreeChartUtils.addDefaultXYDatasetSerie(mainDataset, "approx", xData, approxData);

			JFreeChartUtils.addDefaultXYDatasetSerie(mainDataset, "residu", xData, residualData);


//	        mainDataset.addChangeListener(new DatasetChangeListener() {
//				public void datasetChanged(DatasetChangeEvent event) {
//					onMainChartDatasetChanged(event);
//				}
//			});
	        
			// JFreeChart
	        this.mainChart = ChartFactory.createXYLineChart(model.getName(), "time", "amplitude", mainDataset, true);
	
	        // JPanel panel = new JPanel();
	        this.mainChartPanel = new ChartPanel(mainChart); 
	
	        vertSplit.add(mainChartPanel);
        }

        JPanel reconstResiduPanel = new JPanel(new BorderLayout());
        vertSplit.add(reconstResiduPanel);
        
        { // reconstruction parameters
        	JPanel reconstPanel = new JPanel(new FlowLayout());
        	//tabbedPane.add("reconst-params", reconstPanel);
        	reconstResiduPanel.add(reconstPanel, BorderLayout.NORTH);
        	
        	
        	JButton butDomainZoomOut = new JButton("-");
        	reconstPanel.add(butDomainZoomOut);
        	butDomainZoomOut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					shiftDomainRange(+0.25, -0.25);
				}
			});
        	JButton butDomainZoomIn = new JButton("+");
        	reconstPanel.add(butDomainZoomIn);
        	butDomainZoomIn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					shiftDomainRange(-0.5, +0.5);
				}
			});
        	JButton butDomainZoomLeft = new JButton("<");
        	reconstPanel.add(butDomainZoomLeft);
        	butDomainZoomLeft.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					shiftDomainRange(-1, -1);
				}
			});
        	JButton butDomainZoomRight = new JButton(">");
        	reconstPanel.add(butDomainZoomRight);
        	butDomainZoomRight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					shiftDomainRange(+1, +1);
				}
			});
        	
        	this.mainHarmonicCountSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 5);
        	mainHarmonicCountSlider.getModel().addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					updateReconstData();
				}
			});
        	this.harmonicCountValueLabel = new JLabel();
        	reconstPanel.add(harmonicCountValueLabel);
        	
        	reconstPanel.add(new JLabel("harmonics"));
        	
        	reconstPanel.add(mainHarmonicCountSlider);
        	
        	this.harmonicResiduInfoLabel = new JLabel();
        	reconstPanel.add(harmonicResiduInfoLabel);
        }
        
        { // residual chart panel
			this.residuDataset = new DefaultXYDataset();
			JFreeChartUtils.addDefaultXYDatasetSerie(residuDataset, "residu", xData, residualData);
			
			// JFreeChart
			this.residualChart = ChartFactory.createXYLineChart(model.getName(), "time", "amplitude", residuDataset, true);
	
	        // JPanel panel = new JPanel();
	        this.residualChartPanel = new ChartPanel(residualChart); 
	
			ValueAxis residualRangeAxis = residualChart.getXYPlot().getDomainAxis();
			residualRangeAxis.setAutoRange(false);

	        residuDataset.addChangeListener(new DatasetChangeListener() {
				public void datasetChanged(DatasetChangeEvent event) {
					// onMainChartDatasetChanged(event);
				}
			});
	        
	        reconstResiduPanel.add(residualChartPanel, BorderLayout.CENTER);
			
        }

        mainChart.addChangeListener(new ChartChangeListener() {
			public void chartChanged(ChartChangeEvent event) {
				onMainChartChanged(event);
			}
		});

        
        updateReconstData();
        
	}

	
	private void updateReconstData() {
		int harmonicCount = (Integer) mainHarmonicCountSlider.getValue();
		this.harmonicCountValueLabel.setText(Integer.toString(harmonicCount));

		FFTResiduInfo residuInfo = new FFTResiduInfo();

		model.getReconstructedMainHarmonics(harmonicCount,
				approxData,
				residualData,
				residuInfo);

		mainDataset.seriesChanged(null);
		residuDataset.seriesChanged(null);
		
		String residuInfoText = residuInfo.toStringFFTCoef();
		this.harmonicResiduInfoLabel.setText(residuInfoText);
	}

	
	// ------------------------------------------------------------------------
	
	public JComponent getJComponent() {
		return tabbedPane;
	}
	
	private void shiftDomainRange(double leftLenIncr, double rightLenIncr) {
		XYPlot xyPlot = mainChart.getXYPlot();
		ValueAxis xAxis = xyPlot.getDomainAxis();
		Range xrange = xAxis.getRange();
		double lowerBound = xrange.getLowerBound();
		double upperBound = xrange.getUpperBound();
		double len = upperBound - lowerBound;
		double newLowerBound = lowerBound + leftLenIncr * len;
		double newUpperBound = upperBound + rightLenIncr * len;
		
		xAxis.setRange(newLowerBound, newUpperBound);
	}
	
	private void onMainChartChanged(ChartChangeEvent event) {
		XYPlot xyPlot = mainChart.getXYPlot();
		ValueAxis xAxis = xyPlot.getDomainAxis();
		Range xrange = xAxis.getRange();
		double lowerBound = xrange.getLowerBound();
		double upperBound = xrange.getUpperBound();
		
		ValueAxis residualXAxis = residualChart.getXYPlot().getDomainAxis();
		residualXAxis.setRange(lowerBound, upperBound);
		
	}
}
