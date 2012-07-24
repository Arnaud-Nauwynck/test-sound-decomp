package fr.an.tests.sound.testfft.func;

import org.ejml.data.DenseMatrix64F;

public class CosSinPolynom2 {

	private double coefCos2;
	private double coefSin2;
	private double coefCosSin;
	private double coefCos;
	private double coefSin;
	private double coefConst;

	private QuadraticForm quadForm = new QuadraticForm(2);

	// ------------------------------------------------------------------------

	public CosSinPolynom2() {
	}

	// ------------------------------------------------------------------------

	public double getCoefCos2() {
		return coefCos2;
	}

	public void setCoefCos2(double coefCos2) {
		this.coefCos2 = coefCos2;
		quadForm.setQuadCoefs(0, 0, coefCos2);
	}

	public double getCoefSin2() {
		return coefSin2;
	}

	public void setCoefSin2(double coefSin2) {
		this.coefSin2 = coefSin2;
		quadForm.setQuadCoefs(1, 1, coefSin2);
	}

	public double getCoefCosSin() {
		return coefCosSin;
	}

	public void setCoefCosSin(double coefCosSin) {
		this.coefCosSin = coefCosSin;
		quadForm.setQuadCoefs(0, 1, 0.5 * coefCosSin);
		quadForm.setQuadCoefs(1, 0, 0.5 * coefCosSin);
	}

	public double getCoefCos() {
		return coefCos;
	}

	public void setCoefCos(double coefCos) {
		this.coefCos = coefCos;
		quadForm.setLinCoefs(0, coefCos);
	}

	public double getCoefSin() {
		return coefSin;
	}

	public void setCoefSin(double coefSin) {
		this.coefSin = coefSin;
		quadForm.setLinCoefs(1, coefSin);
	}

	public double getCoefConst() {
		return coefConst;
	}

	public void setCoefConst(double coefConst) {
		this.coefConst = coefConst;
		quadForm.setConstCoefs(coefConst);
	}


	// ------------------------------------------------------------------------

	public double eval(double dphi) {
		double cos = Math.cos(dphi);
		double sin = Math.sin(dphi);
		double res = coefCos2 * cos * cos
				+ coefSin2 * sin * sin
				+ coefCosSin * cos * sin
				+ coefCos * cos
				+ coefSin * sin
				+ coefConst;
		return res;
	}

	public double solveArgMin() {
		DenseMatrix64F cosSinDPhi = new DenseMatrix64F(2, 1);
		quadForm.solveArgMin(cosSinDPhi);
		double cosDPhi = cosSinDPhi.get(0);
		double sinDPhi = cosSinDPhi.get(1);
		double dphi = Math.atan2(sinDPhi, cosDPhi);
		return dphi;
	}

	
	public double solveArgMinOld() {
		double res = // solveArgMin_bruteForceRec(3, 10);
				solveArgMin_bruteForce(100);
		return res;
	}
	
	public double solveArgMin_bruteForce(int n) {
		double res = solveArgMin_bruteForce(0, Math.PI * 2, n);
		return res;
	}

	public double solveArgMin_bruteForceRec(int recurseCount, int n) {
		double res = 0.0;
		double minValue = 0.0;
		double maxValue = Math.PI * 2.0;
		for (int i = 0; i < recurseCount; i++) {
			res = solveArgMin_bruteForce(minValue, maxValue, n);

			double newRange2 = (maxValue - minValue) / (2*n);
			minValue = res - newRange2;
			maxValue = res + newRange2;
		}
		return res;
	}

	
	public double solveArgMin_bruteForce(double minValue, double maxValue, int n) {
		double res = 0.0;
		double minSoFar = Double.MAX_VALUE;
		double step_dphi = (maxValue - minValue) / n;
		double dphi = minValue;
		for (int i = 0; i < n; i++,dphi+=step_dphi) {
			double eval_dphi = eval(dphi);
			if (eval_dphi < minSoFar) {
				res = dphi;
				minSoFar = eval_dphi;
			}
		}
		return res;
	}



	
	
}
