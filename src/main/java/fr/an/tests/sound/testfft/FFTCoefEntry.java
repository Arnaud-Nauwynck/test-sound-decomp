package fr.an.tests.sound.testfft;

public class FFTCoefEntry {
	int index;
	
	double x;
	double y;
	
	double coefX;
	double coefY;
	
	double norm;
	double phi;
	double phi_div_2pi;
	
	double cumulatedSquareNorm;

	public FFTCoefEntry(int index) {
		this.index = index;
	}
	
	public void setData(double x, double y, double factorFft) {
    	this.x = x;
    	this.y = y;
    	
		this.coefX = factorFft * x;
    	if (index == 0) {
    		this.coefX *= 0.5;
    	}
    	this.coefY = factorFft * y;
    	this.norm = Math.sqrt(coefX*coefX + coefY * coefY);
    	this.phi = Math.atan2(coefY, coefX);

    	this.phi_div_2pi = phi * FFTCoefPrinter.INV_2PI;
    	
	}
	
	public String toString() {
		return "[" + index + "] r,i:" + FFTCoefPrinter.fmtDouble3(coefX) + "\t" + FFTCoefPrinter.fmtDouble3(coefY) 
				+ " \t\t N,phi: " + FFTCoefPrinter.fmtDouble3(norm) + "\t" + FFTCoefPrinter.fmtDouble3(phi_div_2pi) + "*2pi"
				+ "\t cumulSquare:" + FFTCoefPrinter.fmtDouble3(cumulatedSquareNorm);
	}
}