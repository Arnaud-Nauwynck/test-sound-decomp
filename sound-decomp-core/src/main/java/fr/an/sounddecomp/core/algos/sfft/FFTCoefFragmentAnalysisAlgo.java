package fr.an.sounddecomp.core.algos.sfft;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import fr.an.sounddecomp.core.SoundFragmentAnalysis;
import fr.an.sounddecomp.core.SoundFragmentAnalysisAlgo;
import fr.an.sounddecomp.core.math.fft.FFT;
import fr.an.sounddecomp.core.math.func.FragmentDataTime;
import fr.an.sounddecomp.core.utils.DoubleFmtUtil;
import fr.an.sounddecomp.core.utils.ResiduInfo;


public class FFTCoefFragmentAnalysisAlgo implements SoundFragmentAnalysisAlgo {

    private SoundFragmentAnalysis fragment;

	private int fftLen;

	double[] fftData;
	
	private FFTCoefEntry[] coefEntries;
	private FFTCoefEntry[] sortedCoefEntries;

	private double cumulatedSquareNormCoefs = 0.0;
	
	private FFT fft;
	
	// ------------------------------------------------------------------------
	
    public FFTCoefFragmentAnalysisAlgo(SoundFragmentAnalysis fragment, FFT fft) {
    	this.fragment = fragment;
    	int fragmentLen = fragment.getFragmentLen();
    	this.fft = fft;
    	if (fft == null) {
    		this.fft = new FFT(fragmentLen, fragment.getModel().getFrameRate());
    	}
		this.fftData = new double[fragmentLen];
    	this.fftLen = fragmentLen / 2;
    	coefEntries = FFTCoefEntry.newArray(fft, fftLen);
    	sortedCoefEntries = new FFTCoefEntry[fftLen];
    }

    // ------------------------------------------------------------------------

	public void computeAnalysis(PrintWriter debugPrinter) {
		if (debugPrinter != null) {
			debugPrinter.println("FFT");
		}
		fft.realForward(fragment.getFragmentData(), 0, fftData, coefEntries);

		// sort coefs
        SortedSet<FFTCoefEntry> sortedCoefs = new TreeSet<FFTCoefEntry>(FFTCoefEntryNormDescComparator.INSTANCE);
        sortedCoefs.addAll(Arrays.asList(coefEntries));
        
        { double tmpSum = 0.0;
	        int i = 0;
	        for(FFTCoefEntry e : sortedCoefs) {
	        	sortedCoefEntries[i] = e;
	        	double norm = e.getNorm();
	        	tmpSum += norm * norm;
	        	e.setCumulatedSquareNorm(tmpSum);
	        	i++;
	        }
	        this.cumulatedSquareNormCoefs = tmpSum;
        }
        
        if (debugPrinter != null) {
        	for (int i = 0; i < 10; i++) {
            	FFTCoefEntry e = sortedCoefEntries[i];
            	if (e.getNorm() < 0.05 || i >= 5) {
            		break;
            	}
            	debugPrinter.println("sortFFT[" + i + "] " + e);
            }
        }
	}

	
	public double[] getFftData() {
		return fftData;
	}

	public FFTCoefEntry[] getCoefEntries() {
		return coefEntries;
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
        	double e_cumulSquareNorm = e.getCumulatedSquareNorm(); 
        	res += "rank:" + i + " " + e + " (" + DoubleFmtUtil.fmtDouble3(100.0 * e_cumulSquareNorm * inv_cumulatedSquareNormCoefs) + "%)\n";
        	if ((e_cumulSquareNorm > cumulatedSquareNormMaxRatio && i > 10)
        			|| (e_cumulSquareNorm > cumulatedSquareNormMaxRatio98)) {
        		break;
        	}
        	if (i >= 10) {
        		break;
        	}
        }
        return res;
	}

	public void getReconstructedMainHarmonics(final int harmonicCount, 
			FragmentDataTime fragDataTime, final int startIndexROI, final int endIndexROI,  
			double[] resultData, final int resultStartIndex, 
			ResiduInfo residuInfo) {
		int resultIndex = resultStartIndex;
		final double[] compactArray = fragDataTime.getTimeDataArray();
		for (int i = startIndexROI,compactIndex = FragmentDataTime.INCR*startIndexROI; i < endIndexROI; i++,compactIndex+=FragmentDataTime.INCR,resultIndex++) {
			double tmpapprox = 0;
			double absoluteT = compactArray[compactIndex];
			for (int k = 0; k < harmonicCount; k++) {
				FFTCoefEntry e = sortedCoefEntries[k];
				tmpapprox += e.getNorm() * Math.cos(e.getOmega() * absoluteT + e.getPhi());
			}
			resultData[resultIndex] = tmpapprox;
		}

		residuInfo.cumulSquareNormCoef = sortedCoefEntries[harmonicCount].getCumulatedSquareNorm();
		residuInfo.totalSquareNormCoef = cumulatedSquareNormCoefs;
	}



    public static class FFTCoefEntryNormDescComparator implements Comparator<FFTCoefEntry> {
    	public static final FFTCoefEntryNormDescComparator INSTANCE = new FFTCoefEntryNormDescComparator();
		public int compare(FFTCoefEntry o1, FFTCoefEntry o2) {
			if (o1 == o2) return 0;
			int res;
			if (o2.getNorm() <= o1.getNorm()) {
				res = -1; // reverse.. o1.compareTo(o2); 
			} else {
				res = +1;
			}
			return res; 
		}
    	
    }




}
