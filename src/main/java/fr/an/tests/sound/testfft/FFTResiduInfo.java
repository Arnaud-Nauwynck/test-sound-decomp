package fr.an.tests.sound.testfft;

public class FFTResiduInfo {

	public double cumulSquareNormCoef;
	public double totalSquareNormCoef;

	public String toStringFFTCoef() {
		String res = "";
		double ratioPercentNormCoef = (totalSquareNormCoef > 0)? cumulSquareNormCoef / totalSquareNormCoef * 100: 0;
		res += FFTCoefAnalysis.fmtDouble3(ratioPercentNormCoef) + "% of sum C²:" + FFTCoefAnalysis.fmtDouble3(totalSquareNormCoef); 
		return res;
	}
	
}
