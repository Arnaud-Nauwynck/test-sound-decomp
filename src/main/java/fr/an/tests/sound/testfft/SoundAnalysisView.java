package fr.an.tests.sound.testfft;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;

import fr.an.tests.sound.testfft.sfft.FFTCoefEntry;
import fr.an.tests.sound.testfft.sfft.FFTCoefFragmentAnalysis;
import fr.an.tests.sound.testfft.synth.PHCoefFragmentAnalysis;
import fr.an.tests.sound.testfft.utils.JFreeChartUtils;

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

	private double[] fftxData;
	private double[] fftData;
	private DefaultXYDataset fftDataset;
	private JFreeChart fftChart;
	private ChartPanel fftChartPanel;
	private JTextPane fftTextPane;
	
	private double[] approxData;
	private double[] residualData;

	private double[] approxDataPH;
	private double[] residualDataPH;

	
//	private JSpinner mainHarmonicCountSpinner;
	private JSlider mainHarmonicCountSlider;
	private JLabel harmonicCountValueLabel;
	private JLabel harmonicResiduInfoLabel;
	private JLabel harmonicResiduInfoPHLabel;
	
	// ------------------------------------------------------------------------
	
	public SoundAnalysisView(SoundAnalysisModel model) {
		super();
		this.model = model;
	
		this.tabbedPane = new JTabbedPane();

		JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tabbedPane.add("data", vertSplit);
        
		int frameLength = model.getFrameLength();
		this.xData = JFreeChartUtils.createLinearDoubleRange(frameLength);

		this.approxData = new double[frameLength];
		this.residualData = new double[frameLength];

		this.approxDataPH = new double[frameLength];
		this.residualDataPH = new double[frameLength];

        { // mainChartPanel + reconstructed
        	this.mainDataset = new DefaultXYDataset();
			JFreeChartUtils.addDefaultXYDatasetSerie(mainDataset, "raw", xData, model.getAudioDataAsDouble());
			JFreeChartUtils.addDefaultXYDatasetSerie(mainDataset, "approx", xData, approxData);
			JFreeChartUtils.addDefaultXYDatasetSerie(mainDataset, "residu", xData, residualData);

			JFreeChartUtils.addDefaultXYDatasetSerie(mainDataset, "approxPH", xData, approxDataPH);
			JFreeChartUtils.addDefaultXYDatasetSerie(mainDataset, "residuPH", xData, residualDataPH);

			
			// JFreeChart
	        this.mainChart = ChartFactory.createXYLineChart(model.getName(), "time", "amplitude", mainDataset, true);

	        mainChart.addChangeListener(new ChartChangeListener() {
				public void chartChanged(ChartChangeEvent event) {
					onMainChartChanged(event);
				}
			});
	
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
        	
        	JButton dumpBut = new JButton("dump");
        	reconstPanel.add(dumpBut);
        	dumpBut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					FFTCoefFragmentAnalysis fftFrag = findCurrPlotFFTFragment();
					if (fftFrag == null) {
						System.err.println("FFT frag not found");
						return; // SHOULD NOT OCCUR
					}
					
					StringWriter buffer = new StringWriter();
					PrintWriter debugPrinter = new PrintWriter(buffer); 
					
					fftFrag.computeAnalysis(debugPrinter);
					
					debugPrinter.flush();
					String msg = buffer.toString();
					System.out.println("dumpPH");
					System.out.println(msg);
					System.out.println();
				}
			});
        	JButton dumpPHBut = new JButton("dumpPH");
        	reconstPanel.add(dumpPHBut);
        	dumpPHBut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PHCoefFragmentAnalysis phFrag = findCurrPlotPHFragment();
					if (phFrag == null) {
						System.err.println("PH frag not found");
						return; // SHOULD NOT OCCUR
					}
					StringWriter buffer = new StringWriter();
					PrintWriter debugPrinter = new PrintWriter(buffer); 
					
					phFrag.computeAnalysis(debugPrinter);
					
					debugPrinter.flush();
					String msg = buffer.toString();
					System.out.println("dumpPH");
					System.out.println(msg);
					System.out.println();
				}
			});
        	
        	
        	
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
        	
        	this.harmonicResiduInfoPHLabel = new JLabel();
        	reconstPanel.add(harmonicResiduInfoPHLabel);
        	
        }
        
        { // residual chart panel
			this.residuDataset = new DefaultXYDataset();
			JFreeChartUtils.addDefaultXYDatasetSerie(residuDataset, "residu", xData, residualData);
			JFreeChartUtils.addDefaultXYDatasetSerie(residuDataset, "residuPH", xData, residualDataPH);
			
			// JFreeChart
			this.residualChart = ChartFactory.createXYLineChart(model.getName(), "time", "amplitude", residuDataset, true);
	
			XYPlot residualChartPlot = (XYPlot) residualChart.getPlot();
			XYItemRenderer residualChartRenderer = residualChartPlot.getRenderer(0);
					
			XYPlot mainChartPlot = (XYPlot) mainChart.getPlot();
//			XYItemRenderer mainChartRenderer = mainChartPlot.getRenderer();
//
//			XYItemRenderer mainChartFFTResidualRenderer = mainChartPlot.getRenderer(2);
//			XYItemRenderer mainChartPHResidualRenderer = mainChartPlot.getRenderer(4);
//			TODO null?!.. 			
//			Paint mainResidualFFTPaint = mainChartRenderer.getSeriesPaint(2);
//			Paint mainResidualPHPaint = mainChartRenderer.getSeriesPaint(4);
//			residualFFTRenderer.setBasePaint(mainChartPlot.getRenderer(2).getBasePaint());
//			residualPHRenderer.setBasePaint(mainChartPlot.getRenderer(4).getBasePaint());

//			residualChartRenderer.setSeriesPaint(0, mainResidualFFTPaint);
//			residualChartRenderer.setSeriesPaint(1, mainResidualPHPaint);

			//?? hard-coded ..
			residualChartRenderer.setSeriesPaint(0, Color.GREEN);
			residualChartRenderer.setSeriesPaint(1, Color.MAGENTA);

	        // JPanel panel = new JPanel();
	        this.residualChartPanel = new ChartPanel(residualChart); 
	
			ValueAxis residualRangeAxis = residualChart.getXYPlot().getDomainAxis();
			residualRangeAxis.setAutoRange(false);
	        
	        reconstResiduPanel.add(residualChartPanel, BorderLayout.CENTER);
			
        }

        { // FFT tab
        	JSplitPane fftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        	tabbedPane.add("fft", fftPanel);
        	 
        	
			this.fftDataset = new DefaultXYDataset();
			this.fftData = new double[model.getFragmentLen() / 2]; // model.getFFTData();
        	this.fftxData = JFreeChartUtils.createLinearDoubleRange(fftData.length, 0, fftData.length * 4); // ?? * Math.PI
			JFreeChartUtils.addDefaultXYDatasetSerie(fftDataset, "fft", fftxData, fftData);
			
			// JFreeChart
			this.fftChart = ChartFactory.createXYLineChart(model.getName(), "frequency", "amplitude", fftDataset, true);
	
	        // JPanel panel = new JPanel();
	        this.fftChartPanel = new ChartPanel(fftChart);
	        fftPanel.add(fftChartPanel);
	        
	        {
	        JScrollPane scrollPane = new JScrollPane();
	        this.fftTextPane = new JTextPane();
	        scrollPane.setViewportView(fftTextPane);
	        fftPanel.add(scrollPane);
	        }
	        fftPanel.setDividerLocation(400);
        }
        
        vertSplit.setDividerLocation(400);
        
        // this.tabbedPane.invalidate(); //?? 
        updateReconstData();
	}

	
	private void updateReconstData() {
		int harmonicCount = (Integer) mainHarmonicCountSlider.getValue();
		this.harmonicCountValueLabel.setText(Integer.toString(harmonicCount));

		ResiduInfo residuInfo = new ResiduInfo();
		ResiduInfo residuInfoPH = new ResiduInfo();

		int frameLength = model.getFrameLength();
		double startTime = 0.0;
		// double endTime = startTime + model.getFrameDuration();
		double dt = 1.0 / model.getFrameRate();
		
		model.getReconstructedMainHarmonics(harmonicCount,
				0, frameLength, startTime, dt,
				approxData,
				residualData,
				residuInfo);

		model.getReconstructedMainHarmonicsPH(harmonicCount,
				0, frameLength, startTime, dt, 
				approxDataPH,
				residualDataPH,
				residuInfoPH);

		mainDataset.seriesChanged(null);
		residuDataset.seriesChanged(null);
		
		String residuInfoText = residuInfo.toStringFFTCoef();
		this.harmonicResiduInfoLabel.setText(residuInfoText);

		String residuInfoPHText = residuInfoPH.toStringFFTCoef();
		this.harmonicResiduInfoPHLabel.setText(residuInfoPHText);

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

	private int getCurrPlotMidTimeIndex() {
		XYPlot xyPlot = mainChart.getXYPlot();
		ValueAxis xAxis = xyPlot.getDomainAxis();
		Range xrange = xAxis.getRange();
		double lowerBound = xrange.getLowerBound();
		double upperBound = xrange.getUpperBound();
		int mid = (int) (lowerBound + upperBound) / 2;
		return mid;
	}

	private double getCurrPlotMidTime() {
		int timeIndex = getCurrPlotMidTimeIndex();
		double res = (double) timeIndex / model.getFrameRate();
		return res;
	}

	private PHCoefFragmentAnalysis findCurrPlotPHFragment() {
		int timeIndex = getCurrPlotMidTimeIndex();
		PHCoefFragmentAnalysis res = model.findPHFragmentAtIndex(timeIndex);
		return res;
	}


	private FFTCoefFragmentAnalysis findCurrPlotFFTFragment() {
		int timeIndex = getCurrPlotMidTimeIndex();
		FFTCoefFragmentAnalysis res = model.findFFTFragmentAtIndex(timeIndex);
		return res;
	}

	
	private void onMainChartChanged(ChartChangeEvent event) {
		XYPlot xyPlot = mainChart.getXYPlot();
		ValueAxis xAxis = xyPlot.getDomainAxis();
		Range xrange = xAxis.getRange();
		double lowerBound = xrange.getLowerBound();
		double upperBound = xrange.getUpperBound();
		
		ValueAxis residualXAxis = residualChart.getXYPlot().getDomainAxis();
		residualXAxis.setRange(lowerBound, upperBound);
		
		// display info of FFT in the current centered fragment
		FFTCoefFragmentAnalysis currFFTFrag = findCurrPlotFFTFragment();
		
		if (currFFTFrag != null) {
			String fftText = currFFTFrag.toStringData();
			fftTextPane.setText(fftText);
			// System.arraycopy(currFFTFrag.getFftData(), 0, fftData, 0, fftData.length);
			
			FFTCoefEntry[] fftCoefEntries = currFFTFrag.getCoefEntries();
			for (int i = 0; i < fftData.length; i++) {
				fftData[i] = fftCoefEntries[i].getNorm();
			}
			
		} else {
			fftTextPane.setText("");
			for (int i = 0; i < fftData.length; i++) {
				fftData[i] = 0;
			}
		}
		fftDataset.seriesChanged(null);
	}
}
