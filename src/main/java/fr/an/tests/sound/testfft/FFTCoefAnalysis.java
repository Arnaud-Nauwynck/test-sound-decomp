package fr.an.tests.sound.testfft;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;


public class FFTCoefAnalysis {

    public static final double INV_2PI = 1.0 / (2*Math.PI);
    public static final double CST_2_PI = 2*Math.PI;


    private static final NumberFormat DBL_FMT3 = new DecimalFormat("#.####");
    
    public static String fmtDouble3(double v) {
    	return DBL_FMT3.format(v);
    }

	public static class FFTCoefEntryNormDescComparator implements Comparator<FFTCoefEntry> {
    	public static final FFTCoefEntryNormDescComparator INSTANCE = new FFTCoefEntryNormDescComparator();
		public int compare(FFTCoefEntry o1, FFTCoefEntry o2) {
			if (o1 == o2) return 0;
			int res;
			if (o2.norm <= o1.norm) {
				res = -1; // reverse.. o1.compareTo(o2); 
			} else {
				res = +1;
			}
			return res; 
		}
    	
    }

	private final int dataLen;
	private final int fftLen;
	
	double[] fftData;
	
	private double factorFft;
	private FFTCoefEntry[] coefEntries;
	private FFTCoefEntry[] sortedCoefEntries;

	private double cumulatedSquareNormCoefs = 0.0;
	
	// ------------------------------------------------------------------------
	
    public FFTCoefAnalysis(int len) {
    	this.dataLen = len;
    	this.fftLen = len / 2;
    	this.factorFft = 2.0 / len;
    	coefEntries = new FFTCoefEntry[fftLen];
	    for (int i = 0; i < fftLen; i++) {
	    	coefEntries[i] = new FFTCoefEntry(i);
	    }
    	sortedCoefEntries = new FFTCoefEntry[fftLen];
    }

    // ------------------------------------------------------------------------
    
	public void setDataJTransform(double[] data) {
		fftData = data;
		
		double cstBaseFrequency = CST_2_PI / data.length;
		coefEntries[0].setData(data[fftLen], 0, factorFft, cstBaseFrequency);
		
		for (int i = 0; i < fftLen; i++) {
			coefEntries[i].setData(data[2*i], data[2*i+1], factorFft, cstBaseFrequency);
        }

		sortCoefs();
	}
	
	protected void sortCoefs() {
        SortedSet<FFTCoefEntry> sortedCoefs = new TreeSet<FFTCoefEntry>(FFTCoefEntryNormDescComparator.INSTANCE);
        for (int i = 0; i < fftLen; i++) {
        	FFTCoefEntry e = coefEntries[i];
        	sortedCoefs.add(e);
        }
        
        double tmpSum = 0.0;
        int i = 0;
        for(FFTCoefEntry e : sortedCoefs) {
        	sortedCoefEntries[i] = e;
        	tmpSum += e.norm * e.norm;
        	e.cumulatedSquareNorm = tmpSum;
        	i++;
        }
        this.cumulatedSquareNormCoefs = tmpSum;
	}

	public FFTCoefEntry[] getSortedCoefEntries() {
		return sortedCoefEntries;
	}

	public void printData() {
		System.out.println(toStringData());
	}
	
	public String toStringData() {
		String res = "";
		res += "Total sum square norm coefs=" + cumulatedSquareNormCoefs + "\n";
        if (Math.abs(cumulatedSquareNormCoefs) < 1e-6) {
        	res += "************* NULL SIGNAL\n";
        	return res;
        }
        res += "coefs sorted by descending norm:\n";
        
        
        double cumulatedSquareNormMaxRatio = 0.80 * cumulatedSquareNormCoefs;
        double cumulatedSquareNormMaxRatio98 = 0.98 * cumulatedSquareNormCoefs;
        double inv_cumulatedSquareNormCoefs = 1.0 / cumulatedSquareNormCoefs;
        for (int i = 0; i < fftLen; i++) {
        	FFTCoefEntry e = sortedCoefEntries[i];
        	res += "rank:" + i + " " + e + " (" + (100 * e.cumulatedSquareNorm * inv_cumulatedSquareNormCoefs) + "%)\n";
        	if ((e.cumulatedSquareNorm > cumulatedSquareNormMaxRatio && i > 5)
        			|| (e.cumulatedSquareNorm > cumulatedSquareNormMaxRatio98)) {
        		break;
        	}
        	if (i >= 10) {
        		break;
        	}
        }
        return res;
	}

	public void getReconstructedMainHarmonics(final int harmonicCount, 
			int resultStartIndex, int resultEndIndex, double[] approxData, FFTResiduInfo residuInfo) {
		double tau = 1.0; //  / len;
		double ti = resultStartIndex * tau;
		for (int i = resultStartIndex; i < resultEndIndex; i++, ti+=tau) {
			double tmpapprox = 0;
			for (int k = 0; k < harmonicCount; k++) {
				FFTCoefEntry e = sortedCoefEntries[k];
				tmpapprox += e.norm * Math.cos(e.omega * ti + e.phi);
			}
			approxData[i] = tmpapprox;
		}

		residuInfo.cumulSquareNormCoef = sortedCoefEntries[harmonicCount].cumulatedSquareNorm;
		residuInfo.totalSquareNormCoef = cumulatedSquareNormCoefs;
	}

	
}
