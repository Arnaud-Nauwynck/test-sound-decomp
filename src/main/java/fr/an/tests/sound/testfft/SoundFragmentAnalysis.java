package fr.an.tests.sound.testfft;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import fr.an.tests.sound.testfft.sfft.FFT;
import fr.an.tests.sound.testfft.sfft.FFTCoefFragmentAnalysis;
import fr.an.tests.sound.testfft.synth.PHCoefFragmentAnalysis;

public class SoundFragmentAnalysis {

	private SoundAnalysisModel model;
	private int startFrameIndex;
	private int fragmentLen;
    private double startTime;
	private double dt;
    
    private double[] fragmentData;
	
	private FFT fft;
	
	private FFTCoefFragmentAnalysis fftCoefAnalysisFragment;
	private PHCoefFragmentAnalysis phCoefAnalysisFragment;

	// ------------------------------------------------------------------------

	public SoundFragmentAnalysis(SoundAnalysisModel model,
			int startFrameIndex, int fragmentLen,  
			double startTime, double dt, FFT fft) {
		this.model = model;
    	this.startFrameIndex = startFrameIndex;
    	this.fragmentLen = fragmentLen;
    	this.startTime = startTime;
    	this.dt = dt;
    	
    	this.fft = fft;
    	this.fftCoefAnalysisFragment = new FFTCoefFragmentAnalysis(this, fft);
    	this.phCoefAnalysisFragment = new PHCoefFragmentAnalysis(this);
	}

	public void setData(double[] fragmentData) {
		this.fragmentData = fragmentData;
	}

	// ------------------------------------------------------------------------


	public SoundAnalysisModel getModel() {
		return model;
	}
	
	public int getStartFrameIndex() {
		return startFrameIndex;
	}
	
	public int getFragmentLen() {
		return fragmentLen;
	}
	
	public double getStartTime() {
		return startTime;
	}

	public double getDt() {
		return dt;
	}

	public double getEndTime() {
		return startTime + fragmentLen * dt;
	}

	public double getDuration() {
		return fragmentLen * dt;
	}

	public double[] getFragmentData() {
		return fragmentData;
	}

	public FFT getFft() {
		return fft;
	}

	public FFTCoefFragmentAnalysis getFftCoefAnalysisFragment() {
		return fftCoefAnalysisFragment;
	}

	public PHCoefFragmentAnalysis getPhCoefAnalysisFragment() {
		return phCoefAnalysisFragment;
	}
	

}
