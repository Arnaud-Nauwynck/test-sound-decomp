package fr.an.tests.sound.testfft.synth;

import org.ejml.data.DenseMatrix64F;

import fr.an.tests.sound.testfft.func.QuadraticForm;

/**
 * coefficient for a pseudo (perturbated) harmonic, with 2 sub-harmonics
 * <code>a1 sin( w0.t + phi0 ) + a2 sin(2.w0.t + phi2 ) + a3 sin(3.w0.t + phi3 )</code>
 * where a0, w0 are quadratic form on t!
 */
public class PHCoefEntry {

	private static final boolean DEBUG = true;
	
	
//	private double t0;
	private double[] p;

	private double residualVar;
	
	private int kCount = 5;
	
	
	public static class IntermediateTParams {
		double[] p;
		
		double absoluteT;
		
		/** homogeneous-time in [0.0, 1.0]  = (t - startTime) / (endTime - startTime) */
		double ht;
		
		int kCount;
		double w0;
		double phi0;
		double[] ak;
		double[] cos_kwtp;
		double[] sin_kwtp;
		
		public IntermediateTParams(int kCount) {
			super();
			this.kCount = kCount;
			this.ak = new double[kCount];
			this.cos_kwtp = new double[kCount];
			this.sin_kwtp = new double[kCount];
		}

		public int getSubHarmonicCount() {
			return kCount;
		}
		
		
	}
	
	// ------------------------------------------------------------------------

	public PHCoefEntry() {
		this.kCount = 5;
		this.p = new double[7 + 3*(kCount-1)];
	}
	
	// ------------------------------------------------------------------------

	public int getSubHarmonicCount() {
		return kCount;
	}
	
	public int getPLen() {
		return p.length;
	}
	
	public void setInitGuess(double fftc0_a0, double fftc0_w, double fftc0_phi) {
		for (int i = 0; i < p.length; i++) {
			p[i] = 0.0;
		}
		p[0] = fftc0_a0;
		p[1] = fftc0_w;
		p[2] = fftc0_phi;
	}

	public void setInitGuess2W(double initguess_p7, double initguess_p9,
			double initguess_p10, double initguess_p12) {
		p[7] = initguess_p7;
		p[9] = initguess_p9;
		p[10] = initguess_p10;
		p[12] = initguess_p12;
	}
	
	public void setInitGuessSubHarmonicCoefs(double[] fourierSubHarmonicCoefs) {
		for (int k = 2; k < kCount; k++) {
			int indexPk = 7+3*(k-2);
			p[indexPk] = fourierSubHarmonicCoefs[k];
		}
	}

	
	public double[] getP() {
		return p;
	}

	public void setP(double[] p) {
		this.p = p;
	}

	public void setResidualVar(double var) {
		this.residualVar = var;
	}

	public double getResidualVar() {
		return residualVar;
	}

	public void computeValues(double startTime, double endTime, int timeLen, double[] times, double[] res) {
		IntermediateTParams itparams = new IntermediateTParams(kCount);
		final double dt = (endTime - startTime) / timeLen;
		final double dht = 1.0 / timeLen;
		double t = startTime;
		double ht = 0.0;
		for (int i = 0, len = times.length; i < len; i++) {
			precomputeForT(itparams, t, ht);
			res[i] = computeValue(times[i], itparams);
			
			t += dt;
			ht += dht;
		}
	}

	public double computeVarValues(double startTime, double endTime, int timeLen, double[] times, double[] origData) {
		double varRes = 0.0;
		IntermediateTParams itparams = new IntermediateTParams(kCount);
		final double dt = (endTime - startTime) / timeLen;
		final double dht = 1.0 / timeLen;
		double t = startTime;
		double ht = 0.0;
		for (int i = 0, len = times.length; i < len; i++) {
			precomputeForT(itparams, t, ht);
			double value = computeValue(times[i], itparams);
			double err = origData[i] - value; 
			varRes += err * err;

			t += dt;
			ht += dht;
		}
		return varRes;
	}

	public double computeResidualValues(double startTime, double endTime, int timeLen, 
			double[] resultResidu, double[] origData) {
		double varRes = 0.0;
		IntermediateTParams itparams = new IntermediateTParams(kCount);
		final double dt = (endTime - startTime) / timeLen;
		final double dht = 1.0 / timeLen;
		double absoluteT = startTime;
		double ht = 0.0;
		for (int i = 0, len = timeLen; i < len; i++) {
			precomputeForT(itparams, absoluteT, ht);
			double value = computeValue(absoluteT, itparams);
			double err = origData[i] - value; 
			resultResidu[i] = err;
			varRes += err * err; 

			absoluteT += dt;
			ht += dht;
		}
		return varRes;
	}

	public double computeValue(double t, double ht) {
		IntermediateTParams it = new IntermediateTParams(kCount);
		precomputeForT(it, t, ht);
		double res = computeValue(t, it);
		return res;
	}
	
	public double computeValue(double t, IntermediateTParams it) {
		double res = 0.0;
		for (int k = 1; k < it.kCount; k++) {
			res += it.ak[k] * it.cos_kwtp[k];
		}
		return res;
	}
	
	public void precomputeForT(IntermediateTParams it, double absoluteT, double ht) {
		it.p = p;
		it.absoluteT = absoluteT;
		it.ht = ht;

		it.w0    = p[1] + ht * (p[4] + ht * p[6]);
		it.phi0  = p[2];

		it.ak[1] = p[0] + ht * (p[3] + ht * p[5]);
		double wtp = it.w0 * absoluteT + it.phi0;
		it.cos_kwtp[1] = Math.cos(wtp);
		it.sin_kwtp[1] = Math.sin(wtp);
		
		for (int k = 2; k < it.kCount; k++) {
			int indexPk = 7+3*(k-2);
			it.ak[k] = it.ak[1] * p[indexPk] + p[indexPk+1];
			double kwtp = k * it.w0 * absoluteT + it.phi0 + p[indexPk+2];
			it.cos_kwtp[k] = Math.cos(kwtp);
			it.sin_kwtp[k] = Math.sin(kwtp);
		}
	}


	public void expandVarValues_Quadratic_p0p3p5(
			double startTime, double endTime, int dataLen,  
			double[] data, 
			QuadraticForm result_quad) {
		IntermediateTParams it = new IntermediateTParams(kCount);

		double tmpres_quad00 = 0.0;
		double tmpres_quad01 = 0.0;
		double tmpres_quad02 = 0.0;
		double tmpres_quad11 = 0.0;
		double tmpres_quad12 = 0.0;
		double tmpres_quad22 = 0.0;

		double tmpres_lin0 = 0.0;
		double tmpres_lin1 = 0.0;
		double tmpres_lin2 = 0.0;
		double tmpres_cst = 0.0;
		
		boolean doCheck = true;
		double totalVar = 0.0;
		
		final double dt = (endTime - startTime) / dataLen;
		final double invDuration = 1.0 / (endTime - startTime);
		double absoluteT = startTime;
		for (int i = 0; i < dataLen; i++,absoluteT+=dt) {
			double ht = (absoluteT - startTime) * invDuration;
			precomputeForT(it, absoluteT, ht);
			
			// double value = computeValue(times[i], itparams);
			// value = ((p0 + p3.ht + p5.ht^2)            ) * it.cos_1wtp 
			//       + ((p0 + p3.ht + p5.ht^2) * p7  + p8 ) * it.cos_2wtp 
			//		 + ((p0 + p3.ht + p5.ht^2) * p10 + p11) * it.cos_3wtp
			//		 + ((p0 + p3.ht + p5.ht^2) * p13 + p14) * it.cos_4wtp
			//		 + ((p0 + p3.ht + p5.ht^2) * p16 + p17) * it.cos_5wtp
			
			// => value = p0 * c0 + p3 * c3 + p5 * c5 + cconst 
			
//			double check_c0 =   it.cos_kwtp[1]
//						+ p[7]  * it.cos_kwtp[2] 
//						+ p[10] * it.cos_kwtp[3]
//						+ p[13] * it.cos_kwtp[4];
//			double check_c3 = ht * ( 
//					          it.cos_kwtp[1]
//					+ p[7]  * it.cos_kwtp[2] 
//					+ p[10] * it.cos_kwtp[3]
//					+ p[13] * it.cos_kwtp[4] );
//					// = ht * c0; 			
//			double check_c5 = ht * ht * check_c0;
//			double check_cconst = p[8] * it.cos_kwtp[2]
//					+ p[11] * it.cos_kwtp[3]
//					+ p[14] * it.cos_kwtp[4];

			double sum_alin_coskwtp = it.cos_kwtp[1];
			double sum_const_coskwtp = 0;		
			for (int k = 2; k < kCount; k++) {
				int indexPK = 7+3*(k-2);
				sum_alin_coskwtp  += p[indexPK] * it.cos_kwtp[k];
				sum_const_coskwtp += p[indexPK+1] * it.cos_kwtp[k];
			}

			final double c0 = sum_alin_coskwtp;
			final double c3 = ht * c0;
			final double c5 = ht * ht * c0;
			final double cconst = sum_const_coskwtp;
			
			double value = 0.0;
			if (DEBUG && doCheck) {
				double checkValue = p[0] * c0 + p[3] * c3 + p[5] * c5 + cconst;
				value = computeValue(absoluteT, it);
				if (Math.abs(value - checkValue) > 1e-3) {
					System.err.println("should not occur: checkValue != value ...");
				} 
			}
			// err = data - value  
			//     = (data - (p0 * c0 + p3 * c3 + p5 * c5 + k))
			//     = (d - (p0 * c0 + p3 * c3 + p5 * c5 ))
			// ...where d=data-k
			double d = data[i] - cconst;
					
			// err^2 =  (d - (p0 * c0 + p3 * c3 + p5 * c5)) ^2
			//       = p0^2 * (c0^2) + p3^2 * c3^2 + p5^2 * c5^2
			//			+ p0*p3 * (2*c0*c3) + p0*p5 * (2*c0*c5) + p3*p5 * (2*c3*c5) 
			// 			+ p0 *(-2*d*c0) + p3 *(-2*d*c3) + p5 * (-2*d*c5) )
			// 			+ d*d
			tmpres_quad00 += c0 * c0;
			tmpres_quad11 += c3 * c3;
			tmpres_quad22 += c5 * c5;
			tmpres_quad01 += c0 * c3; 
			tmpres_quad02 += c0 * c5;
			tmpres_quad12 += c3 * c5; 

			tmpres_lin0 += d * c0;
			tmpres_lin1 += d * c3;
			tmpres_lin2 += d * c5;

			tmpres_cst += d * d;
			
			if (DEBUG && doCheck) {
				double checkVar = c0*c0*p[0]*p[0] + c3*c3*p[3]*p[3] + c5*c5*p[5]*p[5]
						+ 2.0 * ( c0*c3*p[0]*p[3] + c0*c5*p[0]*p[5] + c3*c5*p[3]*p[5])
						- 2.0 * d*(c0*p[0] + c3*p[3] + c5*p[5])
						+ d * d;
				double var = (data[i] - value) * (data[i] - value);
				totalVar += var; 
				if (Math.abs(var - checkVar) > 1e-4) {
					System.err.println("should not occur: checkVar != var ...");
				}
			}
		}
		
		result_quad.setQuadCoefs(0, 0, tmpres_quad00);
		result_quad.setQuadCoefs(1, 1, tmpres_quad11);
		result_quad.setQuadCoefs(2, 2, tmpres_quad22);
		
		result_quad.setQuadCoefs(0, 1, tmpres_quad01); 
		result_quad.setQuadCoefs(0, 2, tmpres_quad02);
		result_quad.setQuadCoefs(1, 2, tmpres_quad12); 
		
		result_quad.setQuadCoefs(1, 0, tmpres_quad01);
		result_quad.setQuadCoefs(2, 0, tmpres_quad02);
		result_quad.setQuadCoefs(2, 1, tmpres_quad12);

		result_quad.setLinCoefs(0, -2.0 * tmpres_lin0);
		result_quad.setLinCoefs(1, -2.0 * tmpres_lin1);
		result_quad.setLinCoefs(2, -2.0 * tmpres_lin2);
		
		result_quad.setConstCoefs(tmpres_cst);
		
		
		if (DEBUG && doCheck) {
			DenseMatrix64F x = new DenseMatrix64F(3);
			x.set(0, p[0]);
			x.set(1, p[3]);
			x.set(2, p[5]);
			double checkTotalVar = result_quad.eval(x);
			if (Math.abs(totalVar - checkTotalVar) > 1e-3) {
				System.err.println("should not occur: checkTotalVar != totalVar ...");
			}

		}
	}
	
}
