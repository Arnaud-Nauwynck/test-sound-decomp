package fr.an.tests.sound.testfft.sfft;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class FFT {

    public static final double INV_PI = 1.0 / Math.PI;
    public static final double INV_2PI = 0.5 / Math.PI;
    public static final double CST_2_PI = 2.0 * Math.PI;


	/** power of 2 for performing SFFT calculations, ex: 2048 */ 
	private final int fragmentLen;
	
	/** sampling frequency of sound  (ex: 8000 Hz on android) */
	private final int frameRate;
	/** = 1.0/frameRate */
	private final double invFrameRate;
	
	private DoubleFFT_1D fft; // = new DoubleFFT_1D(fragmentLen);

	private final int fftLen; // = fragmentLen/2
	private final double factorFft; // = 2/fragmentLen
	
	// ------------------------------------------------------------------------
	
	public FFT(int fragmentLen, int frameRate) {
		super();
		this.fragmentLen = fragmentLen;
		this.frameRate = frameRate;
		
		this.invFrameRate = 1.0 / (double) frameRate;
		this.fftLen = fragmentLen/2;
		this.fft = new DoubleFFT_1D(fragmentLen);
    	this.factorFft = 2.0 / fragmentLen;

	}
	
	public int getFragmentLen() {
		return fragmentLen;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public double getInvFrameRate() {
		return invFrameRate;
	}

	public void realForward(double[] data, int startIndex, double[] fftData, FFTCoefEntry[] coefEntries) {
		System.arraycopy(data, startIndex, fftData, 0, fragmentLen);
		fft.realForward(fftData);
		
		// extract raw fft data into FFTCoefEntry structs
		double factorOmega = 1.0 
				* CST_2_PI 
				//  
				* (double)frameRate / (double)fragmentLen
				;
		
		coefEntries[0].setData(0.5 * fftData[0] * factorFft, 0.0, 0.0);  // ??? TOCHECK   fftData[fftLen] ???
		
		for (int i = 1; i < fftLen; i++) {
			double coefCos = fftData[2*i] * factorFft;
			double coefSin = fftData[2*i+1] * factorFft;
			double omega = (double)i * factorOmega;
			coefEntries[i].setData(coefCos, coefSin, omega);
        }
    	
	}
	
	
}
