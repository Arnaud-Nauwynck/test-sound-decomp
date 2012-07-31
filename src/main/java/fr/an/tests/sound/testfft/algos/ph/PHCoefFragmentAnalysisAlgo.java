package fr.an.tests.sound.testfft.algos.ph;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.ejml.data.DenseMatrix64F;

import fr.an.tests.sound.testfft.SoundFragmentAnalysis;
import fr.an.tests.sound.testfft.SoundFragmentAnalysisAlgo;
import fr.an.tests.sound.testfft.algos.ph.PHCoefEntry.IntermediateTParams;
import fr.an.tests.sound.testfft.algos.sfft.FFTCoefEntry;
import fr.an.tests.sound.testfft.algos.sfft.FFTCoefFragmentAnalysisAlgo;
import fr.an.tests.sound.testfft.math.fft.FFT;
import fr.an.tests.sound.testfft.math.fft.FFTQuadFormUtils;
import fr.an.tests.sound.testfft.math.func.CosSinPolynom2;
import fr.an.tests.sound.testfft.math.func.FragmentDataTime;
import fr.an.tests.sound.testfft.math.func.QuadraticForm;
import fr.an.tests.sound.testfft.utils.DoubleFmtUtil;
import fr.an.tests.sound.testfft.utils.DoubleUtil;
import fr.an.tests.sound.testfft.utils.ResiduInfo;

public class PHCoefFragmentAnalysisAlgo implements SoundFragmentAnalysisAlgo {

	private static boolean DEBUG = true;
	private static boolean DEBUG_CHEAT_440 = false;

	private static boolean adjustPhi = true;
	
	private static boolean adjustOmegaHeuristicNeighboor = false;
	
	private static boolean adjustOmegaWithSteps = true;
	private static int adjustOmegaStepsCount = 5;
	private static int adjustOmegaStepsRecurseCount = 5;
	private static int computeFourierSubHarmonicCoefs = 6;  //TODO

	private static boolean adjustOmegaPhi = false;

	
	private SoundFragmentAnalysis fragment;
	
	private static final int MAX_PH_COEF_LEN = 10;
	
	private PHCoefEntry[] sortedCoefEntries;
//	private double cumulatedSquareNorm;
//	private double[] residuSquareNorm;

	
	// ------------------------------------------------------------------------

	public PHCoefFragmentAnalysisAlgo(SoundFragmentAnalysis fragment) {
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
		
		double _startTime = fragment.getFragmentDataTime().getStartTime();
		double endTime = fragment.getFragmentDataTime().getEndTime();
//		double dt = (endTime - startTime) / fragmentLen;
//		double currTime = startTime;
//		double[] times = new double[fragmentLen];
//		for (int i = 0; i < fragmentLen; i++,currTime+=dt) {
//			times[i] = currTime;
//		}
		double[] residualData = new double[fragmentLen];

		double[] tmpResidualData = new double[fragmentLen];

		CosSinPolynom2 cossinPolynom2_dphi = new CosSinPolynom2();

		QuadraticForm quad_domega_dphi = new QuadraticForm(2);
		DenseMatrix64F solved_domega_dphi = new DenseMatrix64F(2, 1); 
		
		QuadraticForm quad_p0p3p5 = new QuadraticForm(3);
		DenseMatrix64F solved_p0p3p5 = new DenseMatrix64F(3, 1);
		
		double[] fourierSubHarmonicCoefs = (computeFourierSubHarmonicCoefs > 1)? new double[computeFourierSubHarmonicCoefs] : null;

		
		
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
			FragmentDataTime residualTimeData = new FragmentDataTime(fragment.getFragmentDataTime(), stepResidualData); 
			SoundFragmentAnalysis stepResidualFragment = new SoundFragmentAnalysis(fragment.getModel(), residualTimeData, fft);
			stepResidualFragment.setData(stepResidualData);
			FFTCoefFragmentAnalysisAlgo fftFragmentAnalysis = new FFTCoefFragmentAnalysisAlgo(stepResidualFragment, fft);
			fftFragmentAnalysis.computeAnalysis(null);
			
			FFTCoefEntry fftCoef0 = fftFragmentAnalysis.getSortedCoefEntries()[0];

			if (debugPrinter != null) {
				debugPrinter.println("step PH coef[" + i + "], SFFT on curr residu => sort 1st harmonic:" + fftCoef0);
			}

			// TODO...
			final double fftc0_a0 = fftCoef0.getNorm();
			final double fftc0_w0 = fftCoef0.getOmega();
			final double fftc0_phi0 = fftCoef0.getPhi();

			double varC0 = 0.0;
			if (debugPrinter != null) {
				varC0 = FFTQuadFormUtils.computeResidualVar(residualTimeData,
					fftc0_a0, fftc0_w0, fftc0_phi0);
			}
			
			if (Math.abs(fftc0_a0) < 1e-6) {
				break;
			}

			// adjust omega and phi   (for non integer 2.k.pi/N)
			double fftc0_w = fftc0_w0;
			double fftc0_phi = fftc0_phi0;

			// adjust using neighboor FFT coef + barycenter...  (wrong heuristic !!)
			if (adjustOmegaHeuristicNeighboor) {
				int index0 = fftCoef0.getIndex();
				FFTCoefEntry fftCoef0_m1 = (index0 > 0)? fftFragmentAnalysis.getCoefEntries()[index0 - 1] : null;
				FFTCoefEntry fftCoef0_p1 = (index0 +1 < fftFragmentAnalysis.getCoefEntries().length)? fftFragmentAnalysis.getCoefEntries()[index0 + 1] : null;
				if (fftCoef0_m1 != null && fftCoef0_p1 != null) {
					if (fftCoef0_m1.getNorm() > fftCoef0_p1.getNorm()) {
						// freq between index0-1 and index0
						fftc0_w = (fftc0_w * fftc0_a0
								+ fftCoef0_m1.getOmega() * fftCoef0_m1.getNorm())
								/ (fftc0_a0 + fftCoef0_m1.getNorm());
					} else {
						// freq between index0 and index0+1
						fftc0_w = (fftc0_w * fftc0_a0
								+ fftCoef0_p1.getOmega() * fftCoef0_p1.getNorm())
								/ (fftc0_a0 + fftCoef0_p1.getNorm());
					}
					if (debugPrinter != null) {
						// double var = currCoefEntry.computeVarValues(startTime, endTime, timeLen, times, origData)Var(startTime, endTime, fragmentLen, residualData);
						debugPrinter.println("adjust omega barycenter => omega:" + DoubleFmtUtil.fmtFreqDouble3(fftc0_w));			
					}
				}
			}
			
			
			if (adjustPhi) {
				FFTQuadFormUtils.expand_CosSinPolynom2_dPhi(residualTimeData,
						fftc0_a0, fftc0_w, fftc0_phi,
						cossinPolynom2_dphi);
				
				double solved_dphi = cossinPolynom2_dphi.solveArgMin();
				double solved_phi = fftc0_phi + solved_dphi;
				
				if (debugPrinter != null) {
					debugPrinter.println("expand dphi using cos,sin polynom2:"
							+ " " + DoubleFmtUtil.fmtDouble3(cossinPolynom2_dphi));

					double varNullDPhi = cossinPolynom2_dphi.eval(0);
					
					double expectedVarPhi = cossinPolynom2_dphi.eval(solved_dphi);
					double varPhi = FFTQuadFormUtils.computeResidualVar(residualTimeData,
							fftc0_a0, fftc0_w, solved_phi);
					if (Math.abs(expectedVarPhi - varPhi) > 1e-2) {
						System.err.println("adjustPhi  unexpected diff" 
								+ " expectedVarPhi:" + expectedVarPhi + " <> varPhi:" + varPhi);
					}
					debugPrinter.println("adjust dphi using cos,sin polynom2:" 
							+ DoubleFmtUtil.fmtPhaseDouble3(fftc0_phi)
							+ " prev var:" + DoubleFmtUtil.fmtDouble3(varNullDPhi)
							+ " => " + DoubleFmtUtil.fmtPhaseDouble3(solved_phi)
							+ " expected var:" + DoubleFmtUtil.fmtDouble3(expectedVarPhi)
							+ " var:" + DoubleFmtUtil.fmtDouble3(varPhi));
				}
				fftc0_phi = solved_phi;
			}
			

			if (adjustOmegaWithSteps) {
				double foundOmegaArgMinVar = fftc0_w;
				double foundPhiArgMinVar = fftc0_phi;

				FFTQuadFormUtils.expand_CosSinPolynom2_dPhi(residualTimeData,
						fftc0_a0, fftc0_w, fftc0_phi,
						cossinPolynom2_dphi);
				double foundMinVar = cossinPolynom2_dphi.eval(fftc0_phi + cossinPolynom2_dphi.solveArgMin());
				
				int index0 = fftCoef0.getIndex();
				FFTCoefEntry fftCoef0_m1 = (index0 > 0)? fftFragmentAnalysis.getCoefEntries()[index0 - 1] : null;
				FFTCoefEntry fftCoef0_p1 = (index0 +1 < fftFragmentAnalysis.getCoefEntries().length)? fftFragmentAnalysis.getCoefEntries()[index0 + 1] : null;
				double omegaSolveRange = 0.9 * (fftCoef0_p1.getOmega() - fftCoef0_m1.getOmega());
				// double omegaSolveRangeMin = fftCoef0.getOmega() - 0.5 * omegaSolveRange;
				for (int rec = 0; rec < adjustOmegaStepsRecurseCount; rec++) {
					final double omegaSolveStep = omegaSolveRange / (adjustOmegaStepsCount-1); // -1 ???
					if (debugPrinter != null) {
						debugPrinter.println("solve min for " + DoubleFmtUtil.fmtFreqDouble3(foundOmegaArgMinVar)
								+ " +/- " + adjustOmegaStepsCount + "*" + DoubleFmtUtil.fmtFreqDouble3(omegaSolveStep)
								+ " ... expected var: " + DoubleFmtUtil.fmtDouble3(foundMinVar));
					}
					double currSolveOmega = foundOmegaArgMinVar - 0.5 * omegaSolveRange;
					for (int omegaSolveIndex = 0; omegaSolveIndex < adjustOmegaStepsCount; omegaSolveIndex++,currSolveOmega+=omegaSolveStep) {
						// for each omega value, solve phi and get var
						FFTQuadFormUtils.expand_CosSinPolynom2_dPhi(residualTimeData,
								fftc0_a0, currSolveOmega, fftc0_phi,
								cossinPolynom2_dphi);
						double currOmega_dphi = cossinPolynom2_dphi.solveArgMin();
						double currOmega_phi = fftc0_phi + currOmega_dphi;
						double currOmega_Var = cossinPolynom2_dphi.eval(currOmega_dphi);
						if (currOmega_Var < foundMinVar) {
							foundMinVar = currOmega_Var;
							
							foundOmegaArgMinVar = currSolveOmega;
							foundPhiArgMinVar = currOmega_phi;
						}
					}
					
					omegaSolveRange /= adjustOmegaStepsCount;
				}

				if (debugPrinter != null) {
					debugPrinter.println("solved omega with N steps wO-1,w0+1 ... foreach omega, phi=argmin var =>"
							+ " w:" + DoubleFmtUtil.fmtFreqDouble3(foundOmegaArgMinVar)
							+ " phi:" + DoubleFmtUtil.fmtPhaseDouble3(foundPhiArgMinVar)
							+ " ... expected var:" + DoubleFmtUtil.fmtDouble3(foundMinVar));
				}
				
				fftc0_w = foundOmegaArgMinVar;
				fftc0_phi = foundPhiArgMinVar;
			}
			
			
			
			if (DEBUG_CHEAT_440 && i == 0 && Math.abs(fftc0_w0 - 440 * FFT.CST_2_PI) < 10) {
				fftc0_w = 440 * FFT.CST_2_PI;
			}

			if (adjustOmegaPhi) {
				for (int repeatAdjustdOmegadPhi = 0; repeatAdjustdOmegadPhi < 3; repeatAdjustdOmegadPhi++) {
					FFTQuadFormUtils.expandDL_dOmega_dPhi(residualTimeData,
							fftc0_a0, fftc0_w, fftc0_phi,
							quad_domega_dphi);
	
					if (debugPrinter != null) {
						// double var = currCoefEntry.computeVarValues(startTime, endTime, timeLen, times, origData)Var(startTime, endTime, fragmentLen, residualData);
						debugPrinter.println("adjust quadratic "
								+ " dw,dphi: " + DoubleFmtUtil.fmtDouble3(quad_domega_dphi));
					}
	
					quad_domega_dphi.solveArgMin(solved_domega_dphi);
					double solved_domega = solved_domega_dphi.get(0);
					double solved_dphi = solved_domega_dphi.get(1);

					if (Math.abs(solved_domega) > Math.PI*0.005) {
						if (debugPrinter != null) {
							debugPrinter.println("adjust quadratic .. reject domega:" + solved_domega);
						}
						double quad11 = quad_domega_dphi.getQuadCoefs().get(1, 1);
						if (Math.abs(quad11) > 1e-5) {
							solved_dphi = -0.5 * quad_domega_dphi.getLinCoefs().get(1) / quad11;
							fftc0_phi += solved_dphi;
							
							double varDPhi = FFTQuadFormUtils.computeResidualVar(residualTimeData,
									fftc0_a0, fftc0_w0, fftc0_phi0 + solved_dphi);
							
							if (debugPrinter != null) {
								debugPrinter.println("adjust quadratic .. reject domega, use dphi "
										+ DoubleFmtUtil.fmtFreqDouble3(solved_dphi) 
										+ "( w0,phi0:" + DoubleFmtUtil.fmtFreqDouble3(fftc0_w0) + ", " + DoubleFmtUtil.fmtPhaseDouble3(fftc0_phi0) + ")"
										+ " => use phi " + DoubleFmtUtil.fmtPhaseDouble3(fftc0_phi)
										+ " var:" + DoubleFmtUtil.fmtDouble3(varC0) + " => " + DoubleFmtUtil.fmtDouble3(varDPhi)
										);
							}
							break;
						} else {
							if (debugPrinter != null) {
								debugPrinter.println("ignore domega, dphi");
							}
							break;
						}
						
					} else {
						fftc0_w += solved_domega;
						fftc0_phi += solved_dphi;
						if (debugPrinter != null) {
							// double var = currCoefEntry.computeVarValues(startTime, endTime, timeLen, times, origData)Var(startTime, endTime, fragmentLen, residualData);
															
							double var = FFTQuadFormUtils.computeResidualVar(residualTimeData,
									fftc0_a0, fftc0_w, fftc0_phi);
							debugPrinter.println("adjust quadratic "
									+ " dw,dphi:" + DoubleFmtUtil.fmtFreqDouble3(solved_domega) + ", " + DoubleFmtUtil.fmtPhaseDouble3(solved_dphi)
									+ "( w0,phi0:" + DoubleFmtUtil.fmtFreqDouble3(fftc0_w0) + ", " + DoubleFmtUtil.fmtPhaseDouble3(fftc0_phi0) + ")"
									+ " => " + DoubleFmtUtil.fmtFreqDouble3(fftc0_w) + ", " + DoubleFmtUtil.fmtPhaseDouble3(fftc0_phi)
									+ " var:" + DoubleFmtUtil.fmtDouble3(varC0) + " => " + DoubleFmtUtil.fmtDouble3(var)
									);
						}
					}
				}
			}
			

			if (debugPrinter != null) {
				debugPrinter.println("PH coef: set init guess a,omega,phi: " 
						+ DoubleFmtUtil.fmtDouble3(fftc0_a0) 
						+ ", " + DoubleFmtUtil.fmtFreqDouble3(fftc0_w) 
						+ ", " + DoubleFmtUtil.fmtPhaseDouble3(fftc0_phi));
			}
			currCoefEntry.setInitGuess(fftc0_a0, fftc0_w, fftc0_phi);

			if (computeFourierSubHarmonicCoefs > 1) {
				FFTQuadFormUtils.computeFourierNSubHarmonicCoefs(residualTimeData,
						fftc0_w, fftc0_phi,
						fourierSubHarmonicCoefs);

				if (debugPrinter != null) {
					debugPrinter.println("eval Fourier sub-harmonic coefs: " 
							+ ", " + DoubleFmtUtil.fmtDouble3(fourierSubHarmonicCoefs));
				}
				
				currCoefEntry.setInitGuessSubHarmonicCoefs(fourierSubHarmonicCoefs);
			}
				
			
//			boolean usep7p12 = false;
//			if (usep7p12) {
//				double initguess_correl_a2_a1 = 0.0;
//				double initguess_correl_a3_a1 = 0.0; 
//				double initguess_ = 0.0; 
//				double initguess_ = 0.0; 
//				int fftCoef0_windex = fftCoef0.getIndex();
//				if (fftCoef0_windex * 2 < fftLen) {
//					FFTCoefEntry coef2W = fftFragmentAnalysis.getCoefEntries()[fftCoef0_windex * 2];
//					initguess_correl_a2_a1 = coef2W.getNorm() / fftc0_a0;
//					initguess_p9 = coef2W.getPhi() - fftc0_phi;
//	
//					if (fftCoef0_windex * 3 < fftLen) {
//						FFTCoefEntry coef3W = fftFragmentAnalysis.getCoefEntries()[fftCoef0_windex * 3];
//						initguess_p7 = coef3W.getNorm() / fftc0_a0;
//						initguess_p9 = coef3W.getPhi() - fftc0_phi;
//					}
//	
//					if (debugPrinter != null) {
//						debugPrinter.println("PH coef: set init guess 2*freq => p7:" + initguess_p7 
//								+ ", p9:" + initguess_p9 + ", p10:" + initguess_p10
//								+ ", p12:" + initguess_p12);
//					}
//					currCoefEntry.setInitGuess2W(initguess_correl_a2_a1, initguess_p9, initguess_p10, initguess_p12);
//				}
//			}
			
			
//			currCoefEntry.expandVarValues_Quadratic_p0p3p5(startTime, endTime, fragmentLen, 
//					residualData,
//					quad_p0p3p5);
//
//			if (debugPrinter != null) {
//				debugPrinter.println("compute quad form for p0,p3,p5\n" + quad_p0p3p5);
//			}
//
//			// => pseudo inverse matrix p0p3p5 (quadratic form)
//			boolean solvedOK = quad_p0p3p5.solveArgMin(solved_p0p3p5);
//			if (solvedOK) {
//				double ejml_new_p0 = solved_p0p3p5.get(0);
//				double ejml_new_p3 = solved_p0p3p5.get(1);
//				double ejml_new_p5 = solved_p0p3p5.get(2);
//				
//				double expectedSolvedVar = quad_p0p3p5.eval(solved_p0p3p5);
//				if (debugPrinter != null) {
//					debugPrinter.println("solve quad min (pseudo inverse) => "
//							+ "p0,p3,p5:" + DoubleFmtUtil.fmtDouble3(solved_p0p3p5)
//							+ ", expected residu var:" + DoubleFmtUtil.fmtDouble3(expectedSolvedVar));
//				}
//				
//				if (Math.abs(ejml_new_p0 - p[0]) < 50.0
//						&& Math.abs(ejml_new_p3 - p[3]) < 50.0
//						&& Math.abs(ejml_new_p5 - p[5]) < 50.0) {
//					p[0] = ejml_new_p0;
//					p[3] = ejml_new_p3;
//					p[5] = ejml_new_p5;
//				} else {
//					if (debugPrinter != null) debugPrinter.println("REJECTED !");
//				}
//				
//			
//			} else {
//				if (debugPrinter != null) {
//					debugPrinter.println("*** quad matrix NOT sym definite positive!?");
//					coefEntries.add(currCoefEntry);
//				}
//			}
			
			// compute residualData for next iteration
			double var = currCoefEntry.computeResidualValues(residualTimeData, residualTimeData.getStartIndexROI(), residualTimeData.getEndIndexROI(),
					tmpResidualData, 0);
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
			FragmentDataTime fragDataTime, int startIndexROI, int endIndexROI, 
			double[] resultData, final int resultStartIndex, 
			ResiduInfo residuInfo) {
		
		int tmpmaxPHCount = Math.min(harmonicCount, sortedCoefEntries.length);
		int maxPHCount = tmpmaxPHCount;
		int tmpHarmonicCount = 0;
		int truncateLastPHSubHarmonicCount = 0;
		for (int ph = 0; ph < tmpmaxPHCount; ph++) {
			PHCoefEntry e = sortedCoefEntries[ph];

			int eHCount = e.getSubHarmonicCountNonNulls();
			// TODO.... solve on sub harmonic... use  
			tmpHarmonicCount += eHCount;
			if (tmpHarmonicCount > harmonicCount) {
				truncateLastPHSubHarmonicCount = tmpHarmonicCount - harmonicCount;
				maxPHCount = ph + 1;
				break;
			} else if (ph + 1 == tmpmaxPHCount) {
				truncateLastPHSubHarmonicCount = eHCount;
				break;
			}
		}
		IntermediateTParams[] it_ph = new IntermediateTParams[maxPHCount];
		for (int ph = 0; ph < maxPHCount; ph++) {
			PHCoefEntry e = sortedCoefEntries[ph];
			it_ph[ph] = new IntermediateTParams(e);
		}
		
		final int compactIndexLast = startIndexROI + 4 * (endIndexROI - startIndexROI);
		int resultIndex = resultStartIndex;
		for (int i = startIndexROI,compactIndex = 4*startIndexROI; compactIndex < compactIndexLast; i++,compactIndex+=4,resultIndex++) {
			double tmpapprox = 0;
			for (int ph = 0; ph < maxPHCount; ph++) {
				PHCoefEntry e = sortedCoefEntries[ph];
				it_ph[ph].setPrecomputeForT(fragDataTime, i, compactIndex);
				if (ph + 1 == maxPHCount) {
					// TODO... truncate last harmonics params for last ph only
					tmpapprox += e.computeValueMainHarmonics(it_ph[ph], truncateLastPHSubHarmonicCount);
					break;
				}
				
				tmpapprox += e.computeValue(it_ph[ph]);
			}
			resultData[resultIndex] = tmpapprox;
		}

//		residuInfo.cumulSquareNormCoef = ;
//		residuInfo.totalSquareNormCoef = ;
	}

}
