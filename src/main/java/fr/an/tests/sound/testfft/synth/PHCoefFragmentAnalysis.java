package fr.an.tests.sound.testfft.synth;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.dense.linsol.LinearSolverFactory;
import org.ejml.data.DenseMatrix64F;

import Jama.CholeskyDecomposition;
import Jama.Matrix;
import fr.an.tests.sound.testfft.DoubleFmtUtil;
import fr.an.tests.sound.testfft.DoubleUtil;
import fr.an.tests.sound.testfft.QuadraticForm;
import fr.an.tests.sound.testfft.ResiduInfo;
import fr.an.tests.sound.testfft.SoundFragmentAnalysis;
import fr.an.tests.sound.testfft.sfft.FFT;
import fr.an.tests.sound.testfft.sfft.FFTCoefEntry;
import fr.an.tests.sound.testfft.sfft.FFTCoefFragmentAnalysis;
import fr.an.tests.sound.testfft.synth.PHCoefEntry.IntermediateTParams;

public class PHCoefFragmentAnalysis {

	private SoundFragmentAnalysis fragment;
	
	private static final int MAX_PH_COEF_LEN = 10;
	
	private PHCoefEntry[] sortedCoefEntries;
//	private double cumulatedSquareNorm;
//	private double[] residuSquareNorm;

	
	// ------------------------------------------------------------------------

	public PHCoefFragmentAnalysis(SoundFragmentAnalysis fragment) {
		this.fragment = fragment;
	}
	
	// ------------------------------------------------------------------------


	public PHCoefEntry[] getSortedCoefEntries() {
		return sortedCoefEntries;
	}

	public void setSortedCoefEntries(PHCoefEntry[] sortedCoefEntries) {
		this.sortedCoefEntries = sortedCoefEntries;
	}

	
	public void computeAnalysis(PrintWriter debugPrinter) {
		int fragmentLen = fragment.getFragmentLen();
		int fftLen = fragmentLen / 2; 
		
		FFT fft = fragment.getFft();
		
		List<PHCoefEntry> coefEntries = new ArrayList<PHCoefEntry>(MAX_PH_COEF_LEN);
		
		double startTime = fragment.getStartTime();
		double endTime = fragment.getEndTime();
		double dt = (endTime - startTime) / fragmentLen;
		double currTime = startTime;
		double[] times = new double[fragmentLen];
		for (int i = 0; i < fragmentLen; i++,currTime+=dt) {
			times[i] = currTime;
		}
		double[] residualData = new double[fragmentLen];

		double[] tmpResidualData = new double[fragmentLen];
		
		QuadraticForm quad_p0p3p5 = new QuadraticForm(3);
		DenseMatrix64F solved_p0p3p5 = new DenseMatrix64F(3, 1);
		
		if (debugPrinter != null) {
			debugPrinter.print("init: set curr residu = data");
		}
		System.arraycopy(fragment.getFragmentData(), 0, residualData, 0, fragmentLen);
		
		double var0 = DoubleUtil.computeVar(residualData, 0.0);
		if (debugPrinter != null) {
			debugPrinter.println("residu var0:" + var0);
		}
		
		
		
		for (int i = 0; i < MAX_PH_COEF_LEN; i++) {
			PHCoefEntry currCoefEntry = new PHCoefEntry();
		
			double[] stepResidualData = new double[fragmentLen];
			System.arraycopy(residualData, 0, stepResidualData, 0, fragmentLen);
			SoundFragmentAnalysis stepResidualFragment = new SoundFragmentAnalysis(fragment.getModel(), fragment.getStartFrameIndex(), fragment.getFragmentLen(),
					fragment.getStartTime(), fragment.getDt(), fft);
			stepResidualFragment.setData(stepResidualData);
			FFTCoefFragmentAnalysis fftFragmentAnalysis = new FFTCoefFragmentAnalysis(stepResidualFragment, fft);
			fftFragmentAnalysis.computeAnalysis(null);
			
			FFTCoefEntry fftCoef0 = fftFragmentAnalysis.getSortedCoefEntries()[0];

			if (debugPrinter != null) {
				debugPrinter.println("step PH coef[" + i + "], SFFT on curr residu => sort 1st harmonic:" + fftCoef0);
			}

			// TODO...
//			int index0 = fftCoef0.getIndex();
//			FFTCoefEntry fftCoef0_m1 = (index0 > 0)? fftFragmentAnalysis.getCoefEntries()[index0 - 1] : null;
//			FFTCoefEntry fftCoef0_p1 = (index0 +1 < fftFragmentAnalysis.getCoefEntries().length)? fftFragmentAnalysis.getCoefEntries()[index0 + 1] : null;
			double fftc0_a0 = fftCoef0.getNorm();
			double fftc0_w = fftCoef0.getOmega();
			double fftc0_phi = fftCoef0.getPhi();

			// TEMPORARY FOR TEST
//			fftc0_a0 = 1.0;
//			fftc0_w = 440.0 * (2.0*Math.PI);
//			fftc0_phi = 0.0;

			if (Math.abs(fftc0_a0) < 1e-6) {
				break;
			}

			if (debugPrinter != null) {
				debugPrinter.println("PH coef: set init guess p0,p1,p2: " + fftc0_a0 + ", " + fftc0_w + ", " + fftc0_phi);
			}
			currCoefEntry.setInitGuess(fftc0_a0, fftc0_w, fftc0_phi);

			boolean usep7p12 = false;
			if (usep7p12) {
				double initguess_p7 = 0.0; 
				double initguess_p9 = 0.0; 
				double initguess_p10 = 0.0; 
				double initguess_p12 = 0.0; 
				int fftCoef0_windex = fftCoef0.getIndex();
				if (fftCoef0_windex * 2 < fftLen) {
					FFTCoefEntry coef2W = fftFragmentAnalysis.getCoefEntries()[fftCoef0_windex * 2];
					initguess_p7 = coef2W.getNorm() / fftc0_a0;
					initguess_p9 = coef2W.getPhi() - fftc0_phi;
	
					if (fftCoef0_windex * 3 < fftLen) {
						FFTCoefEntry coef3W = fftFragmentAnalysis.getCoefEntries()[fftCoef0_windex * 3];
						initguess_p7 = coef3W.getNorm() / fftc0_a0;
						initguess_p9 = coef3W.getPhi() - fftc0_phi;
					}
	
					if (debugPrinter != null) {
						debugPrinter.println("PH coef: set init guess 2*freq => p7:" + initguess_p7 
								+ ", p9:" + initguess_p9 + ", p10:" + initguess_p10
								+ ", p12:" + initguess_p12);
					}
					currCoefEntry.setInitGuess2W(initguess_p7, initguess_p9, initguess_p10, initguess_p12);
				}
			}
			
			// *** The Biggy ***
			// TODO ... iterate for solving pertubation params...
			
			currCoefEntry.expandVarValues_Quadratic_p0p3p5(startTime, endTime, fragmentLen, times, 
					residualData,
					quad_p0p3p5);

			if (debugPrinter != null) {
				debugPrinter.println("compute quad form for p0,p3,p5\n" + quad_p0p3p5);
			}
			
			double[] p = currCoefEntry.getP();

			// => pseudo inverse matrix p0p3p5 (quadratic form)
			boolean solvedOK = quad_p0p3p5.solveArgMin(solved_p0p3p5);
			if (solvedOK) {
				double ejml_new_p0 = solved_p0p3p5.get(0);
				double ejml_new_p3 = solved_p0p3p5.get(1);
				double ejml_new_p5 = solved_p0p3p5.get(2);
				
				double expectedSolvedVar = quad_p0p3p5.eval(solved_p0p3p5);
				if (debugPrinter != null) {
					debugPrinter.println("solve quad min (pseudo inverse) => "
							+ "p0,p3,p5:" + DoubleFmtUtil.fmtDouble3(solved_p0p3p5)
							+ ", expected residu var:" + DoubleFmtUtil.fmtDouble3(expectedSolvedVar));
				}
				
				if (Math.abs(ejml_new_p0 - p[0]) < 50.0
						&& Math.abs(ejml_new_p3 - p[3]) < 50.0
						&& Math.abs(ejml_new_p5 - p[5]) < 50.0) {
					p[0] = ejml_new_p0;
					p[3] = ejml_new_p3;
					p[5] = ejml_new_p5;
				} else {
					if (debugPrinter != null) debugPrinter.println("REJECTED !");
				}
				
			
			} else {
				if (debugPrinter != null) {
					debugPrinter.println("*** quad matrix NOT sym definite positive!?");
					coefEntries.add(currCoefEntry);
				}
			}
			
			// compute residualData for next iteration
			double var = currCoefEntry.computeResidualValues(startTime, endTime, fragmentLen,  
					tmpResidualData, residualData);
			currCoefEntry.setResidualVar(var);
			double[] tmp = tmpResidualData;
			tmpResidualData = residualData; 
			residualData = tmp;
			
			if (debugPrinter != null) {
				debugPrinter.println("end step PH coef[" + i + "], residu var=" + var + "\n");
			}
			coefEntries.add(currCoefEntry);
		}
		sortedCoefEntries = coefEntries.toArray(new PHCoefEntry[coefEntries.size()]);
	}



	public void getReconstructedMainHarmonics(final int harmonicCount, 
			final int resultStartIndex, final int resultEndIndex, final double startTime, final double dt, 
			double[] resultData,
			ResiduInfo residuInfo) {
		final double dht = 1.0 / (resultEndIndex - resultStartIndex);
		double t = startTime;
		double ht = 0.0;
		int hi = 0;
		int maxHarmonicCount = Math.min(harmonicCount, sortedCoefEntries.length);
		IntermediateTParams it = new IntermediateTParams();
		for (int i = resultStartIndex; i < resultEndIndex; i++) {
			double tmpapprox = 0;
			for (int k = 0; k < maxHarmonicCount; k++) {
				PHCoefEntry e = sortedCoefEntries[k];
				e.precomputeForT(it, t, ht);
				tmpapprox += e.computeValue(t, ht);
			}
			resultData[i] = tmpapprox;
			
//			t += dt;
//			ht += dht;
			// same but less rounding errors?
			hi++;
			t = startTime + dt * hi;
			ht = dht * hi;
		}

//		residuInfo.cumulSquareNormCoef = ;
//		residuInfo.totalSquareNormCoef = ;
	}

}
