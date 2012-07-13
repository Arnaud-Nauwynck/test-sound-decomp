package fr.an.tests.sound.testfft.sfft;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import fr.an.tests.sound.testfft.ResiduInfo;


public class FFTCoefFragmentAnalysis {

    public static final double INV_2PI = 1.0 / (2*Math.PI);
    public static final double CST_2_PI = 2*Math.PI;

    
	private final int dataLen;
	private final int fftLen;
	
	double[] fftData;
	
	private double factorFft;
	private FFTCoefEntry[] coefEntries;
	private FFTCoefEntry[] sortedCoefEntries;

	private double cumulatedSquareNormCoefs = 0.0;
	
	// ------------------------------------------------------------------------
	
    public FFTCoefFragmentAnalysis(int len) {
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

	public int getDataLen() {
		return dataLen;
	}

    
	public void setFFTData(double[] fftData) {
		this.fftData = fftData;
		
		double cstBaseFrequency = CST_2_PI / fftData.length;
		this.coefEntries[0].setData(fftData[fftLen], 0, factorFft, cstBaseFrequency);
		
		for (int i = 0; i < fftLen; i++) {
			this.coefEntries[i].setData(fftData[2*i], fftData[2*i+1], factorFft, cstBaseFrequency);
        }

		sortCoefs();
	}
	
	public FFTCoefEntry[] getCoefEntries() {
		return coefEntries;
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
        	if ((e.cumulatedSquareNorm > cumulatedSquareNormMaxRatio && i > 10)
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
			int resultStartIndex, int resultEndIndex, double[] resultData, ResiduInfo residuInfo) {
		double tau = 1.0; //  / len;   TODO ??????????????
		double ti = 0; // ????? resultStartIndex * tau;
		for (int i = resultStartIndex; i < resultEndIndex; i++, ti+=tau) {
			double tmpapprox = 0;
			for (int k = 0; k < harmonicCount; k++) {
				FFTCoefEntry e = sortedCoefEntries[k];
				tmpapprox += e.norm * Math.cos(e.omega * ti + e.phi);
			}
			resultData[i] = tmpapprox;
		}

		residuInfo.cumulSquareNormCoef = sortedCoefEntries[harmonicCount].cumulatedSquareNorm;
		residuInfo.totalSquareNormCoef = cumulatedSquareNormCoefs;
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

}
