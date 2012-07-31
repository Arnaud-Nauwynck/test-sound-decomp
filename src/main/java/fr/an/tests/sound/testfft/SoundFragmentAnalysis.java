package fr.an.tests.sound.testfft;

import fr.an.tests.sound.testfft.algos.ph.PHCoefFragmentAnalysisAlgo;
import fr.an.tests.sound.testfft.algos.sfft.FFTCoefFragmentAnalysisAlgo;
import fr.an.tests.sound.testfft.math.fft.FFT;
import fr.an.tests.sound.testfft.math.func.FragmentDataTime;

public class SoundFragmentAnalysis {

	private SoundAnalysis model;
	
	private FragmentDataTime fragmentDataTime;
    
    private double[] fragmentData;
	
	private FFT fft;
	
	private FFTCoefFragmentAnalysisAlgo fftCoefAnalysisFragment;
	private PHCoefFragmentAnalysisAlgo phCoefAnalysisFragment;

	// ------------------------------------------------------------------------

	public SoundFragmentAnalysis(SoundAnalysis model,
			FragmentDataTime fragmentDataTime,
			FFT fft) {
		this.model = model;
		this.fragmentDataTime = fragmentDataTime;
    	
    	this.fft = fft;
    	this.fftCoefAnalysisFragment = new FFTCoefFragmentAnalysisAlgo(this, fft);
    	this.phCoefAnalysisFragment = new PHCoefFragmentAnalysisAlgo(this);
	}

	public void setData(double[] fragmentData) {
		this.fragmentData = fragmentData;
	}

	// ------------------------------------------------------------------------


	public SoundAnalysis getModel() {
		return model;
	}
	
	public FragmentDataTime getFragmentDataTime() {
		return fragmentDataTime;
	}

	public int getFragmentLen() {
		return fragmentDataTime.getFragmentLen();
	}

	public double[] getFragmentData() {
		return fragmentData;
	}

	public FFT getFft() {
		return fft;
	}

	public FFTCoefFragmentAnalysisAlgo getFftCoefAnalysisFragment() {
		return fftCoefAnalysisFragment;
	}

	public PHCoefFragmentAnalysisAlgo getPhCoefAnalysisFragment() {
		return phCoefAnalysisFragment;
	}
	

}
