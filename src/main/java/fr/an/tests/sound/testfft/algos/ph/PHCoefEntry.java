package fr.an.tests.sound.testfft.algos.ph;

import fr.an.tests.sound.testfft.math.func.FragmentDataTime;
import fr.an.tests.sound.testfft.math.func.FragmentTimesFunc;

/**
 * coefficient for a pseudo (perturbated) harmonic, with N sub-harmonics, and time-varying parameters
 * 
 * <code>a1 sin( w0.t + phi0 ) + a2 sin(2.w0.t + phi2 ) + a3 sin(3.w0.t + phi3 )</code>
 * 
 * where ak(t), w1(t) are depending of t !!
 * 
 * ak(t) = timeAmplitudeFactor(t) * a1 * correl_ak_a1 + offset_ak_a1 )
 * 
 * timeAmplitudeFactor(t) can be set externally as a result of a floating window amplitude calculation... 
 */
public class PHCoefEntry {

	private static final boolean DEBUG = true;
	
	private int kCount;
	
	/**
	 * main amplitude of this entry (mult factor for all others sub-harmonics)
	 * = fourier coef (w1, phi1) : <code>sum_t S(t) cos(w1.t + phi1)</code> 
	 */
	private double a1;

	private double w1;
	
	private double phi1;
	
	/** */
	private FragmentTimesFunc timeAmplitudeFactor;
	
	/** ratio of k-th Fourier coef (k.wO, phi0) : <code>sum_t S(t) cos(k.w0.t + phi0)</code>, with 1-st fourier coef (w0,phi0) */
	private double[] correl_ak_a1;

	private double[] offset_phik_phi1;

	private double[] offset_ak_a1;

	
	
	
	// user data storage..
	private double residualVar;
		
	// ------------------------------------------------------------------------

	public PHCoefEntry() {
		this(5);
	}
	
	public PHCoefEntry(int kCount) {
		this.kCount = kCount;
		this.correl_ak_a1 = new double[kCount];
		this.offset_ak_a1 = new double[kCount];
		this.offset_phik_phi1 = new double[kCount];
	}
	
	// ------------------------------------------------------------------------

	public int getSubHarmonicCount() {
		return kCount;
	}

	public int getSubHarmonicCountNonNulls() {
		int res = 0;
		if (Math.abs(a1) < 1e-6) return 0;
		for (int k = 2; k < kCount; k++) {
			if (Math.abs(correl_ak_a1[k] / a1) < 1e-2
					&&  Math.abs(offset_ak_a1[k]) < 1e-6
					) {
				res = k-1;
				break;
			}
		}
		return res;
	}
	
	public void setInitGuess(double a1, double w0, double phi0) {
		this.w1 = w0;
		this.phi1 = phi0;
		this.a1 = a1;
		
		for (int i = 2; i < correl_ak_a1.length; i++) {
			this.correl_ak_a1[i] = 0.0;
			this.offset_ak_a1[i] = 0.0;
			this.offset_phik_phi1[i] = 0.0;
		}
	}

//	public void setInitGuess2W(double initguess_p7, double initguess_p9,
//			double initguess_p10, double initguess_p12) {
//		p[7] = initguess_p7;
//		p[9] = initguess_p9;
//		p[10] = initguess_p10;
//		p[12] = initguess_p12;
//	}

	public void setInitGuessSubHarmonicCoefs(double[] fourierSubHarmonicCoefs) {
		final int maxK = Math.min(fourierSubHarmonicCoefs.length, correl_ak_a1.length);
		double harmonic1 = fourierSubHarmonicCoefs[1];
		double inv_harmonic1 = (Math.abs(harmonic1) < 1e-6)? 1.0/harmonic1 : 0.0;
		this.a1 = harmonic1;
		for (int k = 2; k < maxK; k++) {
			this.correl_ak_a1[k] = fourierSubHarmonicCoefs[k] * inv_harmonic1;
		}
		for (int k = maxK; k < correl_ak_a1.length; k++) {
			this.correl_ak_a1[k] = 0;
		}
	}
	
	public FragmentTimesFunc getTimeAmplitudeFactor() {
		return timeAmplitudeFactor;
	}

	public void setTimeAmplitudeFactor(FragmentTimesFunc timeAmplitudeFactor) {
		this.timeAmplitudeFactor = timeAmplitudeFactor;
	}

	public void setResidualVar(double var) {
		this.residualVar = var;
	}

	public double getResidualVar() {
		return residualVar;
	}

	public void computeValues(
			FragmentDataTime fragDataTime, int fragStartROI, int fragEndROI, 
			double[] result, int resultStartOffset
			) {
		IntermediateTParams itparams = new IntermediateTParams(this);
		int resultIndex = resultStartOffset;
		for (int index = fragStartROI, compactIndex = FragmentDataTime.INCR*fragStartROI; index < fragEndROI; index++,compactIndex+=FragmentDataTime.INCR,resultIndex++) {
			itparams.setPrecomputeForT(fragDataTime, index, compactIndex);
			
			result[resultIndex] = computeValue(itparams);
			
		}
	}

	public double computeVarValues(
			FragmentDataTime fragDataTime, int fragStartROI, int fragEndROI) {
		double varRes = 0.0;
		IntermediateTParams itparams = new IntermediateTParams(this);
		final double[] compactArray = fragDataTime.getTimeDataArray();
		for (int index = fragStartROI, compactIndex = FragmentDataTime.INCR*fragStartROI; index < fragEndROI; index++,compactIndex+=FragmentDataTime.INCR) {
			itparams.setPrecomputeForT(fragDataTime, index, compactIndex);

			double value = computeValue(itparams);
			double dataT = compactArray[compactIndex + FragmentDataTime.OFFSET_DATA];
			double err = dataT - value; 
			varRes += err * err;
		}
		return varRes;
	}

	public double computeResidualValues(FragmentDataTime fragDataTime, int fragStartROI, int fragEndROI, 
			double[] resultResidu, int resultStartIndex) {
		double varRes = 0.0;
		IntermediateTParams itparams = new IntermediateTParams(this);
		final double[] compactArray = fragDataTime.getTimeDataArray();
		int resultIndex = resultStartIndex;
		for (int index = fragStartROI, compactIndex = FragmentDataTime.INCR*fragStartROI; index < fragEndROI; index++,compactIndex+=FragmentDataTime.INCR,resultIndex++) {
			itparams.setPrecomputeForT(fragDataTime, index, compactIndex);

			double value = computeValue(itparams);
			double dataT = compactArray[compactIndex + FragmentDataTime.OFFSET_DATA];
			double err = dataT - value;
			resultResidu[resultIndex] = err;
			varRes += err * err; 
		}
		return varRes;
	}

//	public double computeValue(double t, double ht) {
//		IntermediateTParams it = new IntermediateTParams(this);
//		it.setPrecomputeForT(t, ht);
//		double res = computeValue(t, it);
//		return res;
//	}
	
	public double computeValue(IntermediateTParams it) {
		double res = 0.0;
		for (int k = 1; k < kCount; k++) {
			double tmpelt = it.akt[k] * it.cos_kwtp[k];
			res += tmpelt;
		}
		return res;
	}
	
	public double computeValueMainHarmonics(IntermediateTParams it, int mainHarmonicCount) {
		double res = 0.0;
		for (int k = 1; k < mainHarmonicCount+1; k++) {
			double tmpelt = it.akt[k] * it.cos_kwtp[k];
			res += tmpelt;
		}
		return res;
	}
	

//	public void expandVarValues_Quadratic_p0p3p5(
//			double startTime, double endTime, int dataLen,  
//			double[] data, 
//			QuadraticForm result_quad) {
//		IntermediateTParams it = new IntermediateTParams(this);
//
//		double tmpres_quad00 = 0.0;
//		double tmpres_quad01 = 0.0;
//		double tmpres_quad02 = 0.0;
//		double tmpres_quad11 = 0.0;
//		double tmpres_quad12 = 0.0;
//		double tmpres_quad22 = 0.0;
//
//		double tmpres_lin0 = 0.0;
//		double tmpres_lin1 = 0.0;
//		double tmpres_lin2 = 0.0;
//		double tmpres_cst = 0.0;
//		
//		boolean doCheck = true;
//		double totalVar = 0.0;
//		
//		final double dt = (endTime - startTime) / dataLen;
//		final double invDuration = 1.0 / (endTime - startTime);
//		double absoluteT = startTime;
//		for (int i = 0; i < dataLen; i++,absoluteT+=dt) {
//			double ht = (absoluteT - startTime) * invDuration;
//			precomputeForT(it, absoluteT, ht);
//			
//			// double value = computeValue(times[i], itparams);
//			// value = ((p0 + p3.ht + p5.ht^2)            ) * it.cos_1wtp 
//			//       + ((p0 + p3.ht + p5.ht^2) * p7  + p8 ) * it.cos_2wtp 
//			//		 + ((p0 + p3.ht + p5.ht^2) * p10 + p11) * it.cos_3wtp
//			//		 + ((p0 + p3.ht + p5.ht^2) * p13 + p14) * it.cos_4wtp
//			//		 + ((p0 + p3.ht + p5.ht^2) * p16 + p17) * it.cos_5wtp
//			
//			// => value = p0 * c0 + p3 * c3 + p5 * c5 + cconst 
//			
////			double check_c0 =   it.cos_kwtp[1]
////						+ p[7]  * it.cos_kwtp[2] 
////						+ p[10] * it.cos_kwtp[3]
////						+ p[13] * it.cos_kwtp[4];
////			double check_c3 = ht * ( 
////					          it.cos_kwtp[1]
////					+ p[7]  * it.cos_kwtp[2] 
////					+ p[10] * it.cos_kwtp[3]
////					+ p[13] * it.cos_kwtp[4] );
////					// = ht * c0; 			
////			double check_c5 = ht * ht * check_c0;
////			double check_cconst = p[8] * it.cos_kwtp[2]
////					+ p[11] * it.cos_kwtp[3]
////					+ p[14] * it.cos_kwtp[4];
//
//			double sum_alin_coskwtp = it.cos_kwtp[1];
//			double sum_const_coskwtp = 0;		
//			for (int k = 2; k < kCount; k++) {
//				int indexPK = 7+3*(k-2);
//				sum_alin_coskwtp  += p[indexPK] * it.cos_kwtp[k];
//				sum_const_coskwtp += p[indexPK+1] * it.cos_kwtp[k];
//			}
//
//			final double c0 = sum_alin_coskwtp;
//			final double c3 = ht * c0;
//			final double c5 = ht * ht * c0;
//			final double cconst = sum_const_coskwtp;
//			
//			double value = 0.0;
//			if (DEBUG && doCheck) {
//				double checkValue = p[0] * c0 + p[3] * c3 + p[5] * c5 + cconst;
//				value = computeValue(absoluteT, it);
//				if (Math.abs(value - checkValue) > 1e-3) {
//					System.err.println("should not occur: checkValue != value ...");
//				} 
//			}
//			// err = data - value  
//			//     = (data - (p0 * c0 + p3 * c3 + p5 * c5 + k))
//			//     = (d - (p0 * c0 + p3 * c3 + p5 * c5 ))
//			// ...where d=data-k
//			double d = data[i] - cconst;
//					
//			// err^2 =  (d - (p0 * c0 + p3 * c3 + p5 * c5)) ^2
//			//       = p0^2 * (c0^2) + p3^2 * c3^2 + p5^2 * c5^2
//			//			+ p0*p3 * (2*c0*c3) + p0*p5 * (2*c0*c5) + p3*p5 * (2*c3*c5) 
//			// 			+ p0 *(-2*d*c0) + p3 *(-2*d*c3) + p5 * (-2*d*c5) )
//			// 			+ d*d
//			tmpres_quad00 += c0 * c0;
//			tmpres_quad11 += c3 * c3;
//			tmpres_quad22 += c5 * c5;
//			tmpres_quad01 += c0 * c3; 
//			tmpres_quad02 += c0 * c5;
//			tmpres_quad12 += c3 * c5; 
//
//			tmpres_lin0 += d * c0;
//			tmpres_lin1 += d * c3;
//			tmpres_lin2 += d * c5;
//
//			tmpres_cst += d * d;
//			
//			if (DEBUG && doCheck) {
//				double checkVar = c0*c0*p[0]*p[0] + c3*c3*p[3]*p[3] + c5*c5*p[5]*p[5]
//						+ 2.0 * ( c0*c3*p[0]*p[3] + c0*c5*p[0]*p[5] + c3*c5*p[3]*p[5])
//						- 2.0 * d*(c0*p[0] + c3*p[3] + c5*p[5])
//						+ d * d;
//				double var = (data[i] - value) * (data[i] - value);
//				totalVar += var; 
//				if (Math.abs(var - checkVar) > 1e-4) {
//					System.err.println("should not occur: checkVar != var ...");
//				}
//			}
//		}
//		
//		result_quad.setQuadCoefs(0, 0, tmpres_quad00);
//		result_quad.setQuadCoefs(1, 1, tmpres_quad11);
//		result_quad.setQuadCoefs(2, 2, tmpres_quad22);
//		
//		result_quad.setQuadCoefs(0, 1, tmpres_quad01); 
//		result_quad.setQuadCoefs(0, 2, tmpres_quad02);
//		result_quad.setQuadCoefs(1, 2, tmpres_quad12); 
//		
//		result_quad.setQuadCoefs(1, 0, tmpres_quad01);
//		result_quad.setQuadCoefs(2, 0, tmpres_quad02);
//		result_quad.setQuadCoefs(2, 1, tmpres_quad12);
//
//		result_quad.setLinCoefs(0, -2.0 * tmpres_lin0);
//		result_quad.setLinCoefs(1, -2.0 * tmpres_lin1);
//		result_quad.setLinCoefs(2, -2.0 * tmpres_lin2);
//		
//		result_quad.setConstCoefs(tmpres_cst);
//		
//		
//		if (DEBUG && doCheck) {
//			DenseMatrix64F x = new DenseMatrix64F(3);
//			x.set(0, p[0]);
//			x.set(1, p[3]);
//			x.set(2, p[5]);
//			double checkTotalVar = result_quad.eval(x);
//			if (Math.abs(totalVar - checkTotalVar) > 1e-3) {
//				System.err.println("should not occur: checkTotalVar != totalVar ...");
//			}
//
//		}
//	}

	
	// ------------------------------------------------------------------------
	

	/** helper class for pre-computing time-coefs, cos(k.w.t+phik), sin(k.w.t+phik) .. */
	public static class IntermediateTParams {
		final PHCoefEntry owner;
		
		FragmentDataTime fragDataTime;
		int index;
		int compactIndex;

		double absoluteT;
		double ht;
				
		// intermediate params, computed from params+time
		double[] akt;
		double[] cos_kwtp;
		double[] sin_kwtp;
		
		public IntermediateTParams(PHCoefEntry owner) {
			this.owner = owner;
			int kCount = owner.kCount; 
			this.akt = new double[kCount];
			this.cos_kwtp = new double[kCount];
			this.sin_kwtp = new double[kCount];
		}
		
		public void setPrecomputeForT(FragmentDataTime fragDataTime, int index, int compactIndex) {
			this.fragDataTime = fragDataTime;
			this.index = index;
			this.compactIndex = compactIndex;

			double[] compactArray = fragDataTime.getTimeDataArray();
			double absoluteT = compactArray[compactIndex]; 
			double ht = compactArray[compactIndex+FragmentDataTime.OFFSET_HOMOGENEOUSTIME];
			
			this.absoluteT = absoluteT;
			this.ht = ht;
			
			PHCoefEntry e = owner;
//			it.w0    = p[1] + ht * (p[4] + ht * p[6]);
//			it.phi0  = p[2];

			double a1Ampl = e.a1;
			if (e.timeAmplitudeFactor != null) {
				a1Ampl *= e.timeAmplitudeFactor.eval(fragDataTime, index, absoluteT, ht);
			}

			this.akt[1] = a1Ampl;
			double wtp = e.w1 * absoluteT + e.phi1;
			this.cos_kwtp[1] = Math.cos(wtp);
			this.sin_kwtp[1] = Math.sin(wtp);
			
			for (int k = 2; k < e.kCount; k++) {
				this.akt[k] = a1Ampl * e.correl_ak_a1[k] + e.offset_ak_a1[k];
				double kwtp = k * e.w1 * absoluteT + e.phi1 + e.offset_phik_phi1[k];
				this.cos_kwtp[k] = Math.cos(kwtp);
				this.sin_kwtp[k] = Math.sin(kwtp);
			}
		}
		
	}
	
}
