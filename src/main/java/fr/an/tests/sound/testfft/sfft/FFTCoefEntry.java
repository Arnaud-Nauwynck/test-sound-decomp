package fr.an.tests.sound.testfft.sfft;

import fr.an.tests.sound.testfft.DoubleFmtUtil;

public class FFTCoefEntry {
	int index;
	
	double x;
	double y;
	
	double coefX;
	double coefY;
	
	double norm;
	double omega;
	double phi;
	double phi_div_2pi;
	
	double cumulatedSquareNorm;

	// ------------------------------------------------------------------------
	
	public FFTCoefEntry(int index) {
		this.index = index;
	}
	
	// ------------------------------------------------------------------------
	
	public void setData(double x, double y, double factorFft, double cstBaseFrequency) {
    	this.x = x;
    	this.y = y;
    	
		this.coefX = factorFft * x;
    	if (index == 0) {
    		this.coefX *= 0.5;
    	}
    	this.coefY = factorFft * y;
    	this.omega = index * cstBaseFrequency;
    	this.norm = Math.sqrt(coefX*coefX + coefY * coefY);
    	this.phi = Math.atan2(coefY, coefX);

    	this.phi_div_2pi = phi * FFTCoefFragmentAnalysis.INV_2PI;
    	
	}
	
	
	public int getIndex() {
		return index;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getCoefX() {
		return coefX;
	}

	public double getCoefY() {
		return coefY;
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
		return phi_div_2pi;
	}

	public double getCumulatedSquareNorm() {
		return cumulatedSquareNorm;
	}

	public String toString() {
		return "[" + index + "] r,i:" + DoubleFmtUtil.fmtDouble3(coefX) + "\t" + DoubleFmtUtil.fmtDouble3(coefY) 
				+ " \t\t N,phi: " + DoubleFmtUtil.fmtDouble3(norm) + "\t" + DoubleFmtUtil.fmtDouble3(phi_div_2pi) + "*2pi"
				+ "\t cumulSquare:" + DoubleFmtUtil.fmtDouble3(cumulatedSquareNorm);
	}
}