package fr.an.sounddecomp.core.algos.sfft;

import fr.an.sounddecomp.core.math.fft.FFT;
import fr.an.sounddecomp.core.utils.DoubleFmtUtil;

public class FFTCoefEntry {
	
	private final FFT fft;
	private final int index;
	
	private double coefCos;
	private double coefSin;
	
	private double norm;
	private double omega;
	private double phi;
	
	private double cumulatedSquareNorm;

	// ------------------------------------------------------------------------
	
	public FFTCoefEntry(FFT fft, int index) {
		this.fft = fft;
		this.index = index;
	}
	
	public static FFTCoefEntry[] newArray(FFT fft, int len) {
		FFTCoefEntry[] res = new FFTCoefEntry[len];
		for (int i = 0; i < len; i++) {
			res[i] = new FFTCoefEntry(fft, i);
		}
		return res;
	}
	
	// ------------------------------------------------------------------------
	
	public void setData(double coefCos, double coefSin, double omega) {
		this.coefCos = coefCos;
    	this.coefSin = coefSin;
    	this.omega = omega;

    	this.norm = Math.sqrt(coefCos * coefCos + coefSin * coefSin);
    	this.phi = Math.atan2(coefSin, coefCos);
	}
	
	public FFT getFft() {
		return fft;
	}

	public int getIndex() {
		return index;
	}

	public double getCoefX() {
		return coefCos;
	}

	public double getCoefY() {
		return coefSin;
	}

	public double getNorm() {
		return norm;
	}

	public double getOmega() {
		return omega;
	}

	public double getPhi() {
		return phi;
	}

	public double getPhi_div_2pi() {
		return phi * FFT.INV_2PI;
	}

	public double getCumulatedSquareNorm() {
		return cumulatedSquareNorm;
	}

	public void setCumulatedSquareNorm(double p) {
		this.cumulatedSquareNorm = p;
	}

	public String toString() {
		return "[" + index + "] omega:" + DoubleFmtUtil.fmtDouble3(omega) 
				+ " freq:" + DoubleFmtUtil.fmtFreqDouble3(omega)
						// * fft.getFragmentLen()
						// * fft.getFragmentLen() * fft.getInvFrameRate()
//				+ " freqN/R:" + DoubleFmtUtil.fmtDouble3(omega * FFT.INV_2PI
//								* (double)fft.getFragmentLen() * fft.getInvFrameRate()
//								) + " Hz"   
				+ " c,s:" + DoubleFmtUtil.fmtDouble3(coefCos) + "\t" + DoubleFmtUtil.fmtDouble3(coefSin) 
				+ " \t\t N,phi: " + DoubleFmtUtil.fmtDouble3(norm) + "\t" + DoubleFmtUtil.fmtPhaseDouble3(phi)
				+ (((cumulatedSquareNorm != 0.0))? "\t cumulSquare:" + DoubleFmtUtil.fmtDouble3(cumulatedSquareNorm) : "");
	}
}