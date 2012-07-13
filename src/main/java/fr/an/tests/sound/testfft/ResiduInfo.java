package fr.an.tests.sound.testfft;

public class ResiduInfo {

	public double cumulSquareNormCoef;
	public double totalSquareNormCoef;

	public void addFragment(ResiduInfo frag) {
		this.cumulSquareNormCoef += frag.cumulSquareNormCoef;
		this.totalSquareNormCoef += frag.totalSquareNormCoef;
	}
	
	public String toStringFFTCoef() {
		String res = "";
		double ratioPercentNormCoef = (totalSquareNormCoef > 0)? cumulSquareNormCoef / totalSquareNormCoef * 100: 0;
		res += DoubleFmtUtil.fmtDouble3(ratioPercentNormCoef) + "% of sum C²:" + DoubleFmtUtil.fmtDouble3(totalSquareNormCoef); 
		return res;
	}
	
}
