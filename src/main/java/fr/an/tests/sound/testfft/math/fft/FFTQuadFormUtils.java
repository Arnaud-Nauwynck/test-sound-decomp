package fr.an.tests.sound.testfft.math.fft;

import fr.an.tests.sound.testfft.math.func.CosSinPolynom2;
import fr.an.tests.sound.testfft.math.func.FragmentDataTime;
import fr.an.tests.sound.testfft.math.func.QuadraticForm;

public class FFTQuadFormUtils {

	private static final boolean DEBUG = true;
	
	/**
	 * 
	 */
	public static void expandDL_dOmega_dPhi(FragmentDataTime fragmentDataTime,
			final double a, final double omega0, final double phi0,
			QuadraticForm resultQuad) {
		
		double shiftT = 0.0;

		double phi0_shiftT = omega0 * fragmentDataTime.getStartTime() + phi0;
		
		double tmpres_quadCoefs00 = 0;
		double tmpres_quadCoefs01 = 0;
		double tmpres_quadCoefs11 = 0;
		double tmpres_linCoefs0 = 0;
		double tmpres_linCoefs1 = 0;
		double tmpres_constCoef = 0;
		
		double checkWithDOmega= 0.00;
		double checkWithDPhi= 0.001;
		
		
		final double[] compactArray = fragmentDataTime.getCompactTimeData4Array();
		int compactIndexLast = compactArray.length;
		for (int compactIndex = 0; compactIndex < compactIndexLast; compactIndex+=4) {
			double absoluteT = compactArray[compactIndex];
			double dataT = compactArray[compactIndex+2];
			// r = data[i] - a.cos(omega.t + phi)
			//   = data[i] - a.cos((omega0 + domega).t + (phi0+dphi))
			//   = data[i] - a.[ cos(omega0.t + phi0)) cos(domega.t + dphi)
			//				   - sin(omega0.t + phi0)) sin(domega.t + dphi) ]
			//  ~=  data[i] - a.[ cos(omega0.t + phi0)) (1 - 0.5 . domega^2.t^2 - 0.5 .dphi^2 - domega.t.dphi)
			//				   -  sin(omega0.t + phi0)) (domega.t + dphi) ]
			// def .. A=(a00, a01, a10, a00) B=(b0 b1) C=(c)
			//   = t^(domega dphi) A (domega dphi) + t^B (domega dphi) + C
			
			double cosw0stp = Math.cos(omega0 * shiftT + phi0_shiftT);
			double sinw0stp = Math.cos(omega0 * shiftT + phi0_shiftT);
			double a00 = 0.5 * a * cosw0stp * shiftT * shiftT;
			double a01 = a * cosw0stp * shiftT;
			double a11 = 0.5 * a * cosw0stp;
			double b0 = a * sinw0stp * shiftT;
			double b1 = a * sinw0stp;
			double c = dataT - a * cosw0stp;
			
			// => r^2
			// r^2 =   t^(domega dphi) [ C.A + B.t^B ]  (domega dphi)
			//       + C . t^B (domega dphi)
			//   	 + C^2
			// 		 + ...o(2)
			double tmp_quadCoefs00 = c * a00 + c * b0 * b0;
			double tmp_quadCoefs01 = c * a01 + c * b0 * b1;
			double tmp_quadCoefs11 = c * a11 + c * b1 * b1;
			double tmp_linCoefs0 = 2 * c * b0;
			double tmp_linCoefs1 = 2 * c * b1;
			double tmp_constCoef = c * c;

			if (DEBUG) {
				double coswdwstpdp = Math.cos((omega0 + checkWithDOmega) * absoluteT + phi0 + checkWithDPhi);
				double r = dataT - a * coswdwstpdp;
				double varElt = r * r;
				double checkR =  a00 * checkWithDOmega * checkWithDOmega
						+ 2 * a01 * checkWithDOmega * checkWithDPhi
						+ a11 * checkWithDPhi * checkWithDPhi
						+ b0 * checkWithDOmega
						+ b1 * checkWithDPhi
						+ c;
				if (Math.abs(r - checkR) > 1e-3) {
					System.out.println("Should not occur: expandDL_dOmega_dPhi r:" + r + " <> checkR:" + checkR);
				}
				double checkVarElt = tmp_quadCoefs00 * checkWithDOmega * checkWithDOmega
						+ 2 * tmp_quadCoefs01 * checkWithDOmega * checkWithDPhi
						+ tmp_quadCoefs11 * checkWithDPhi * checkWithDPhi
						+ tmp_linCoefs0 * checkWithDOmega
						+ tmp_linCoefs1 * checkWithDPhi
						+ tmp_constCoef;
				if (Math.abs(checkVarElt - varElt) > 1e-2) {
					System.out.println("Should not occur: expandDL_dOmega_dPhi varElt:" + varElt + " <> checkVarElt:" + checkVarElt);
				}
			}
			tmpres_quadCoefs00 += tmp_quadCoefs00;
			tmpres_quadCoefs01 += tmp_quadCoefs01;
			tmpres_quadCoefs11 += tmp_quadCoefs11;
			tmpres_linCoefs0 += tmp_linCoefs0;
			tmpres_linCoefs1 += tmp_linCoefs1;
			tmpres_constCoef += tmp_constCoef;
		}

		resultQuad.setQuadCoefs(0, 0, tmpres_quadCoefs00);
		resultQuad.setQuadCoefs(0, 1, tmpres_quadCoefs01);
		resultQuad.setQuadCoefs(1, 0, tmpres_quadCoefs01);
		resultQuad.setQuadCoefs(1, 1, tmpres_quadCoefs11);
		resultQuad.setLinCoefs(0, tmpres_linCoefs0);
		resultQuad.setLinCoefs(1, tmpres_linCoefs1);
		resultQuad.setConstCoefs(tmpres_constCoef);
	}


	
	/**
	 * r = data[i] - a.cos(omega.t + phi) ...where phi=phi0 + dphi
	 * expand sum_i r^2 ... as a polynom in cos(dphi),sin(dphi)
	 */
	public static void expand_CosSinPolynom2_dPhi(FragmentDataTime fragmentDataTime,
			final double a0, final double omega0, final double phi0,
			CosSinPolynom2 result) {
		
		double tmp_coefCos2 = 0.0;
		double tmp_coefSin2 = 0.0;
		double tmp_coefCosSin_div2 = 0.0;
		double tmp_coefCos_div2 = 0.0;
		double tmp_coefSin_div2 = 0.0;
		double tmp_coefConst = 0.0;
		
		final double[] compactArray = fragmentDataTime.getCompactTimeData4Array();
		int compactIndexLast = compactArray.length;
		for (int compactIndex = 0; compactIndex < compactIndexLast; compactIndex+=4) {
			double absoluteT = compactArray[compactIndex];
			double dataT = compactArray[compactIndex+2];
			
			// r = data[i] - a0.cos(omega0.t + phi)
			//   = data[i] - a0.cos(omega0.t + (phi0+dphi))
			//   = data[i] - a0.[ cos(omega0.t + phi0)) cos(dphi) - sin(omega0.t + phi0)) sin(dphi) ]
			// def c_i,s_i=...
			// r= data[i] + c_i.cos(dphi) + s_i.sin(dphi) 
			
			double cosw0tp = Math.cos(omega0 * absoluteT + phi0);
			double sinw0tp = Math.sin(omega0 * absoluteT + phi0);
			double c_i = - a0 * cosw0tp;
			double s_i = a0 * sinw0tp;
			
			// => r^2
			// r^2 = (data[i] + c_i.cos(dphi) + s_i.sin(dphi)) * (data[i] + c_i.cos(dphi) + s_i.sin(dphi))
			//  = data[i]^2 
			//    + cos(dphi).cos(dphi) [ c_i . c_i ]
			//    + cos(dphi).sin(dphi) [ 2. c_i . s_i ]
			//    + sin(dphi).sin(dphi) [ s_i . s_i ]
			//    + cos(dphi) [ 2. data[i] . c_i ]
			//    + sin(dphi) [ 2. data[i] . s_i ]]

			tmp_coefCos2 += c_i * c_i;
			tmp_coefSin2 += s_i * s_i;
			tmp_coefCosSin_div2 += c_i * s_i;
			tmp_coefCos_div2 += dataT * c_i;
			tmp_coefSin_div2 += dataT * s_i;
			tmp_coefConst += dataT * dataT; 

		}

		result.setCoefCos2(tmp_coefCos2);
		result.setCoefCosSin(2.0 * tmp_coefCosSin_div2);
		result.setCoefSin2(tmp_coefSin2);
		result.setCoefCos(2.0 * tmp_coefCos_div2);
		result.setCoefSin(2.0 * tmp_coefSin_div2);
		result.setCoefConst(tmp_coefConst);
		
	}

	
	/**
	 * 
	 */
	public static double computeResidualVar(FragmentDataTime dataTime,
			final double a0, final double omega0, final double phi0) {
		double res = 0.0;
		
		final double[] compactArray = dataTime.getCompactTimeData4Array();
		final int compactIndexLast = compactArray.length;
		for (int compactIndex=0; compactIndex < compactIndexLast; compactIndex+=4) {
			double absoluteT = compactArray[compactIndex];
			double dataT = compactArray[compactIndex + 2];
			
			// r = data[t] - a.cos(omega.t + phi)
			double cosw0tp = Math.cos(omega0 * absoluteT + phi0);
			double r = dataT - a0 * cosw0tp;
			res += r * r;
			
		}
		return res;
	}
	
	
	/**
	 * given a non-integer frequency <code>omega0</code> and phase <code>phi0</code>,
	 * compute <code>N</code> Fourier coefficients, for frequencies 1.w, 2.w, 3.w, ..N.w and SAME phase 
	 */
	public static void computeFourierNSubHarmonicCoefs(FragmentDataTime fragmentDataTime,
			final double w0, final double phi0,
			double[] resultFourierSubCoefs) {
		final int K = resultFourierSubCoefs.length;

		final double[] compactArray = fragmentDataTime.getCompactTimeData4Array();
		int compactIndexLast = compactArray.length;

		for (int k = 1; k < K; k++) {
			resultFourierSubCoefs[k] = 0.0;
		}
		for (int compactIndex = 0; compactIndex < compactIndexLast; compactIndex+=4) {
			double absoluteT = compactArray[compactIndex];
			double dataT = compactArray[compactIndex+2];

			// r = data[i] - a.cos(omega.t + phi)
			for (int k = 1; k < K; k++) {
				double ck = dataT * Math.cos(k * w0 * absoluteT + phi0);
				resultFourierSubCoefs[k] += ck;
			}
		}
		double ratioFft = 2.0 / ( fragmentDataTime.getFragmentLen() - 1); // -1 ??
		for (int k = 1; k < K; k++) {
			resultFourierSubCoefs[k] *= ratioFft;
		}
	}
	
	
	/**
	 * given a non-integer frequency <code>omega0</code> and phase <code>phi0</code>,
	 * compute Fourier coefficient
	 * <code>c = sum_t c_t</code>  with <code>c_t = a.cos(w0.t + phi0)</code> 
	 */
	public static double computeFourierCoef(double startTime, double endTime, int dataLen,  
			double[] data,
			final double w0, final double phi0) {
		final double dt = (endTime - startTime) / dataLen;
		double absoluteT = startTime;
		
		double res = 0.0;
		for (int i = 0; i < dataLen; i++) {
			res += data[i] * Math.cos(w0 * absoluteT + phi0);
			
			// next
			absoluteT += dt;
		}
		return res;
	}
	
}
