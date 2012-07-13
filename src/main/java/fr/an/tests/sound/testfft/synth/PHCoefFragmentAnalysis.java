package fr.an.tests.sound.testfft.synth;

import java.util.ArrayList;
import java.util.List;

import Jama.CholeskyDecomposition;
import Jama.Matrix;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import fr.an.tests.sound.testfft.ResiduInfo;
import fr.an.tests.sound.testfft.sfft.FFTCoefEntry;
import fr.an.tests.sound.testfft.sfft.FFTCoefFragmentAnalysis;
import fr.an.tests.sound.testfft.synth.PHCoefEntry.IntermediateTParams;

public class PHCoefFragmentAnalysis {

	private double startTime;
	private double endTime;
	
	private int dataLen;
	private double[] data;
//	FFTCoefFragmentAnalysis fftFragmentAnalysis;
	
	private static final int MAX_PH_COEF_LEN = 10;
	
	private PHCoefEntry[] sortedCoefEntries;
//	private double cumulatedSquareNorm;
//	private double[] residuSquareNorm;

	
	// ------------------------------------------------------------------------

	public PHCoefFragmentAnalysis(double startTime, double endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	// ------------------------------------------------------------------------


	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public double getDuration() {
		return endTime - startTime;
	}
	
	public int getDataLen() {
		return dataLen;
	}

	public void setDataLen(int dataLen) {
		this.dataLen = dataLen;
	}

	public PHCoefEntry[] getSortedCoefEntries() {
		return sortedCoefEntries;
	}

	public void setSortedCoefEntries(PHCoefEntry[] sortedCoefEntries) {
		this.sortedCoefEntries = sortedCoefEntries;
	}

	public void setData(double[] data) {
		this.data = data;
		this.dataLen = data.length;
		int fftLen = dataLen / 2; 
		
		double[] fftData = new double[dataLen];
		DoubleFFT_1D fft = new DoubleFFT_1D(dataLen);
		
		List<PHCoefEntry> coefEntries = new ArrayList<PHCoefEntry>(MAX_PH_COEF_LEN);
		
		double dt = (endTime - startTime) / dataLen;
		double currTime = startTime;
		double[] times = new double[dataLen];
		for (int i = 0; i < dataLen; i++,currTime+=dt) {
			times[i] = currTime;
		}
		double[] residualData = new double[dataLen];
		System.arraycopy(data, 0, residualData, 0, dataLen);
		
		double[] tmpResidualData = new double[dataLen];
		
		double[][] result_quad_p0p3p5 = new double[][] {
				new double[3], new double[3], new double[3] 
		};
		double[] result_lin_p0p3p5 = new double[3];
		double[] result_cst_p0p3p5 = new double[1];
		
		for (int i = 0; i < MAX_PH_COEF_LEN; i++) {
			PHCoefEntry currCoefEntry = new PHCoefEntry();
			
			System.arraycopy(residualData, 0, fftData, 0, dataLen);
			fft.realForward(fftData);
			FFTCoefFragmentAnalysis fftFragmentAnalysis = new FFTCoefFragmentAnalysis(dataLen);
			fftFragmentAnalysis.setFFTData(fftData);

			FFTCoefEntry fftCoef0 = fftFragmentAnalysis.getSortedCoefEntries()[0];
			double fftc0_a0 = fftCoef0.getNorm();
			if (Math.abs(fftc0_a0) < 1e-6) {
				break;
			}
			double fftc0_w = fftCoef0.getOmega();
			double fftc0_phi = fftCoef0.getPhi();
			
			currCoefEntry.setInitGuess(fftc0_a0, fftc0_w, fftc0_phi);

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

				currCoefEntry.setInitGuess2W(initguess_p7, initguess_p9, initguess_p10, initguess_p12);
			}
			
			// *** The Biggy ***
			// TODO ... iterate for solving pertubation params...
			
			currCoefEntry.expandVarValues_Quadratic_p0p3p5(startTime, endTime, dataLen, times, 
					residualData,
					result_quad_p0p3p5, result_lin_p0p3p5, result_cst_p0p3p5);
			
			// => pseudo inverse matrix p0p3p5 (quadratic form)
			// TODO 
			CholeskyDecomposition cholesky_quad_p0p3p5 = new CholeskyDecomposition(new Matrix(result_quad_p0p3p5));
			if (cholesky_quad_p0p3p5.isSPD()) {
				Matrix p0p3p5_solved = cholesky_quad_p0p3p5.solve(new Matrix(result_lin_p0p3p5, 3));
				double new_p0 = - 0.5 * p0p3p5_solved.get(0, 0);
				double new_p3 = - 0.5 * p0p3p5_solved.get(1, 0);
				double new_p5 = - 0.5 * p0p3p5_solved.get(2, 0);
				
				double[] p = currCoefEntry.getP();
				if (Math.abs(new_p0 - p[0]) < 50.0) {
					p[0] = new_p0;
				}
				if (Math.abs(new_p3 - p[3]) < 10.0) {
					p[3] = new_p3;
				}
				if (Math.abs(new_p5 - p[5]) < 5.0) {
					p[5] = new_p5;
				}
			}
			
			// compute residualData for next iteration
			double var = currCoefEntry.computeResidualValues(startTime, endTime, dataLen, times, 
					tmpResidualData, residualData);
			currCoefEntry.setResidualVar(var);
			double[] tmp = tmpResidualData;
			tmpResidualData = residualData; 
			residualData = tmp;
			
			coefEntries.add(currCoefEntry);
		}
		sortedCoefEntries = coefEntries.toArray(new PHCoefEntry[coefEntries.size()]);
	}


	public void getReconstructedMainHarmonics(final int harmonicCount, 
			double startTime, double endTime, 
			int resultStartIndex, int resultEndIndex, double[] resultData,
			ResiduInfo residuInfo) {
		final double dt = (endTime - startTime) / (resultEndIndex - resultStartIndex);
		final double dht = 1.0 / (resultEndIndex - resultStartIndex);
		double t = startTime;
		double ht = 0.0;
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
			
			t += dt;
			ht += dht;
		}

//		residuInfo.cumulSquareNormCoef = ;
//		residuInfo.totalSquareNormCoef = ;
	}

}
