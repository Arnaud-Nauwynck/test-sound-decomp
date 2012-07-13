package fr.an.tests.sound.testfft.synth;

/**
 * coefficient for a pseudo (perturbated) harmonic, with 2 sub-harmonics
 * <code>a0 sin( w0.t + phi0 ) + a1 sin(2.w0.t + phi1 ) + a2 sin(3.w0.t + phi2 )</code>
 * where a0, w0 are quadratic form on t!
 */
public class PHCoefEntry {

	public static final int MAX_P_LEN = 13;
	
//	private double t0;
	private double[] p = new double[MAX_P_LEN];

	private double residualVar;
	
	public static class IntermediateTParams {
		double[] p;
		
		double absoluteT;
		
		/** homogeneous-time in [0.0, 1.0]  = (t - startTime) / (endTime - startTime) */
		double ht;
		
		double a0, w0, phi0, w0tp0, cos_w0tp0, sin_w0tp0;
		double a1, w1, phi1, w1tp1, cos_w1tp1, sin_w1tp1;
		double a2, w2, phi2, w2tp2, cos_w2tp2, sin_w2tp2;
	}
	
	// ------------------------------------------------------------------------

	public PHCoefEntry() {
	}
	
	// ------------------------------------------------------------------------


	public void setInitGuess(double fftc0_a0, double fftc0_w, double fftc0_phi) {
		for (int i = 0; i < MAX_P_LEN; i++) {
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
		IntermediateTParams itparams = new IntermediateTParams();
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
		IntermediateTParams itparams = new IntermediateTParams();
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

	public double computeResidualValues(double startTime, double endTime, int timeLen, double[] times,
			double[] resultResidu, double[] origData) {
		double varRes = 0.0;
		IntermediateTParams itparams = new IntermediateTParams();
		final double dt = (endTime - startTime) / timeLen;
		final double dht = 1.0 / timeLen;
		double t = startTime;
		double ht = 0.0;
		for (int i = 0, len = times.length; i < len; i++) {
			precomputeForT(itparams, t, ht);
			double value = computeValue(times[i], itparams);
			double err = origData[i] - value; 
			resultResidu[i] = err;
			varRes += err * err; 

			t += dt;
			ht += dht;
		}
		return varRes;
	}

	public double computeValue(double t, double ht) {
		IntermediateTParams it = new IntermediateTParams();
		precomputeForT(it, t, ht);
		double res = computeValue(t, it);
		return res;
	}
	
	public double computeValue(double t, IntermediateTParams it) {
		double res = it.a0 * it.cos_w0tp0 
					+ it.a1 * it.cos_w1tp1 
					+ it.a2 * it.cos_w2tp2;
		return res;
	}
	
	public void precomputeForT(IntermediateTParams it, double absoluteT, double ht) {
		it.p = p;
		it.absoluteT = absoluteT;
		it.ht = ht;
		
		it.a0 = p[0] + ht * (p[3] + ht * p[5]);
		it.w0 = p[1] + ht * (p[4] + ht * p[6]);
		it.phi0 = p[2];
		it.w0tp0 = it.w0 * absoluteT + it.phi0;
		it.cos_w0tp0 = Math.cos(it.w0tp0);
		it.sin_w0tp0 = Math.sin(it.w0tp0);

		it.a1 = p[7] * it.a0 + p[8];
		it.w1 = 2 * it.w0;
		it.phi1 = it.phi0 + p[9];
		it.w1tp1 = it.w1 * absoluteT + it.phi1;
		it.cos_w1tp1 = Math.cos(it.w1tp1);
		it.sin_w1tp1 = Math.sin(it.w1tp1);

		it.a2 = p[10] * it.a0 + p[11];
		it.w2 = 3 * it.w0;
		it.phi2 = it.phi0 + p[12];
		it.w2tp2 = it.w2 * absoluteT + it.phi2;
		it.cos_w2tp2 = Math.cos(it.w2tp2);
		it.sin_w2tp2 = Math.sin(it.w2tp2);
	}


	public void expandVarValues_Quadratic_p0p3p5(
			double startTime, double endTime, int dataLen, double[] times, 
			double[] data, 
			double[][] result_quad_p0p3p5, double[] result_lin_p0p3p5, double[] result_cst) {
		IntermediateTParams it = new IntermediateTParams();
		
		result_quad_p0p3p5[0][0] = 0.0;
		result_quad_p0p3p5[0][0] = 0.0;
		result_quad_p0p3p5[1][1] = 0.0;
		result_quad_p0p3p5[2][2] = 0.0;
		result_quad_p0p3p5[0][1] = 0.0; 
		result_quad_p0p3p5[0][2] = 0.0;
		result_quad_p0p3p5[1][2] = 0.0; 

		result_lin_p0p3p5[0] = 0.0;
		result_lin_p0p3p5[1] = 0.0;
		result_lin_p0p3p5[2] = 0.0;

		double invDuration = 1.0 / (endTime - startTime); 
		for (int i = 0, len = times.length; i < len; i++) {
			double t = times[i];
			double ht = (t - startTime) * invDuration;
			precomputeForT(it, t, ht);
			
			// double value = computeValue(times[i], itparams);
			// value = (p0 + p3.t + p5.t^2) * it.cos_w0tp0 
			//       + ((p0 + p3.t + p5.t^2) * p7  + p8 ) * it.cos_w1tp1 
			//		 + ((p0 + p3.t + p5.t^2) * p10 + p11) * it.cos_w2tp2;
			
			// => value = p0 * c0 + p3 * c3 + p5 * c5 + k 
			double c0 = it.cos_w0tp0
						+ p[7] * it.cos_w1tp1 
						+ p[10] * it.cos_w2tp2;
			double c3 = t * ( 
					  it.cos_w0tp0
					+ p[7] * it.cos_w1tp1 
					+ p[10] * it.cos_w2tp2);
			double c5 = t * t * (
					  it.cos_w0tp0
					+ p[7] * it.cos_w1tp1 
					+ p[10] * it.cos_w2tp2);
			double k = p[8]  * it.cos_w1tp1
					+ p[11] * it.cos_w2tp2;

			// err = data - value  
			//     = (data - (p0 * c0 + p3 * c3 + p5 * c5 + k))
			//     = (d - (p0 * c0 + p3 * c3 + p5 * c5 ))
			// ...where d=data-k
			double d = data[i] - k;
					
			// err^2 =  (d - (p0 * c0 + p3 * c3 + p5 * c5)) ^2
			//       = p0^2 * (c0^2) + p3^2 * c3^2 + p5^2 * c5^2
			//			+ p0*p3 * (2*c0*c3) + p0*p5 * (2*c0*c5) + p3*p5 * (2*c3*c5) 
			// 			+ p0 *(-2*d*c0) + p3 *(-2*d*c3) + p5 * (-2*d*c5) )
			// 			+ d*d
			result_quad_p0p3p5[0][0] += c0 * c0;
			result_quad_p0p3p5[1][1] += c3 * c3;
			result_quad_p0p3p5[2][2] += c5 * c5;
			result_quad_p0p3p5[0][1] += c0 * c3; 
			result_quad_p0p3p5[0][2] += c0 * c5;
			result_quad_p0p3p5[1][2] += c3 * c5; 

			result_lin_p0p3p5[0] += - 2 * d * c0;
			result_lin_p0p3p5[1] += - 2 * d * c3;
			result_lin_p0p3p5[2] += - 2 * d * c5;

			if (result_cst != null) {
				result_cst[0] += d * d;
			}
		}
		result_quad_p0p3p5[1][0] = result_quad_p0p3p5[0][1];
		result_quad_p0p3p5[2][0] = result_quad_p0p3p5[0][2];
		result_quad_p0p3p5[2][1] = result_quad_p0p3p5[1][2];
	}
	
}