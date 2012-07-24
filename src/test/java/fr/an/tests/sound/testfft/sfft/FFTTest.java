package fr.an.tests.sound.testfft.sfft;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import fr.an.tests.sound.testfft.SoundAnalysisModel;
import fr.an.tests.sound.testfft.SoundFragmentAnalysis;
import fr.an.tests.sound.testfft.sfft.FFT;
import fr.an.tests.sound.testfft.sfft.FFTCoefEntry;
import fr.an.tests.sound.testfft.sfft.FFTCoefFragmentAnalysis;
import fr.an.tests.sound.testfft.sfft.FFTCoefFragmentAnalysis.FFTCoefEntryNormDescComparator;

public class FFTTest {

	@Test
	public void testFFT() {
        int fragmentLen = 
        		// 1024 // 2^10
        		// 2048
        		// 4096
        		// 8192
        		16384
        		;
        
        int frameRate = 8000;
        FFT fft = new FFT(fragmentLen, frameRate);
        
        
        double[] data = new double[fragmentLen];

        double PI_2 = 2.0 * Math.PI;

        double w1 = 2.0 * PI_2
        		* (double)frameRate
        	; // / (double)frameRate;
        	// / (double)fragmentLen; // * frameRate / fragmentLen;   

        double phi1 = 0.0;
        

        final double dt = 1.0 / frameRate;
        double t = 0.0;
        for (int i = 0; i < fragmentLen; i++, t += dt) {
        	data[i] = ( //
        			0.0 //
        			// + 2.0 * Math.cos(i) //
        			+ 2.0 * Math.cos(w1 * t + phi1) // 
        			// + 5.0 * Math.cos(3*w1 * t + phi1*2) //
        			);
        	
        }

        double[] fftData = new double[fragmentLen];
        int fftLen = fragmentLen/2;
        FFTCoefEntry[] coefEntries = FFTCoefEntry.newArray(fft, fftLen);
        
        fft.realForward(data, 0, fftData, coefEntries);

        SortedSet<FFTCoefEntry> sortedCoefs = new TreeSet<FFTCoefEntry>(FFTCoefEntryNormDescComparator.INSTANCE);
        sortedCoefs.addAll(Arrays.asList(coefEntries));

        int sortIndex = 0;
        FFTCoefEntry[] sortedCoefEntries = new FFTCoefEntry[fftLen];
        for(FFTCoefEntry e : sortedCoefs) {
        	sortedCoefEntries[sortIndex] = e;
        	sortIndex++;
        }

        // TODO ... asserts...
        for (int i = 0; i < 10; i++) {
        	FFTCoefEntry e = sortedCoefEntries[i];
        	if (e.getNorm() < 0.05) {
        		break;
        	}
        	System.out.println("sortFFT[" + i + "] " + e);
        }
        
        
	}
}
