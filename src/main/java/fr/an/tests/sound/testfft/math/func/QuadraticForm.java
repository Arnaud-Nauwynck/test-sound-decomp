package fr.an.tests.sound.testfft.math.func;

import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.dense.linsol.LinearSolverFactory;
import org.ejml.data.DenseMatrix64F;

import fr.an.tests.sound.testfft.utils.DoubleFmtUtil;

public class QuadraticForm {
	
	private DenseMatrix64F quadCoefs;
	private DenseMatrix64F linCoefs;
	private double constCoef;
	
	LinearSolver<DenseMatrix64F> solver;
	private DenseMatrix64F solved;
	
	// ------------------------------------------------------------------------
	
	public QuadraticForm(int dim) {
		this.quadCoefs = new DenseMatrix64F(dim, dim);
		this.linCoefs = new DenseMatrix64F(dim, 1);
		
	}

	// ------------------------------------------------------------------------

	public DenseMatrix64F getQuadCoefs() {
		return quadCoefs;
	}

	public DenseMatrix64F getLinCoefs() {
		return linCoefs;
	}

	public double getConstCoef() {
		return constCoef;
	}

	public void setQuadCoefs(int row, int col, double data) {
		this.quadCoefs.set(row, col, data);
	}

	public void setLinCoefs(int row, double data) {
		this.linCoefs.set(row, data);
	}

	public void setConstCoefs(double data) {
		this.constCoef = data;
	}

	public void setData(double[][] quadData, double[] linData, double constData) {
		final int dim = quadCoefs.getNumRows();
		for (int row = 0; row < dim; row++) {
			for (int col = 0; col < dim; col++) {
				this.quadCoefs.set(row, col, quadData[row][col]);
			}
		}
		for (int row = 0; row < dim; row++) {
			this.linCoefs.set(row, linData[row]);
		}
		this.constCoef = constData;
	}
	
	public double eval(DenseMatrix64F x) {
		double res = constCoef;
		final int dim = quadCoefs.getNumRows();
		for (int row = 0; row < dim; row++) {
			res += this.quadCoefs.get(row, row) * x.get(row) * x.get(row);
			for (int col = row+1; col < dim; col++) {
				res += 2.0 * this.quadCoefs.get(row, col) * x.get(row) * x.get(col);
			}
		}
		for (int row = 0; row < dim; row++) {
			res += linCoefs.get(row) * x.get(row);  
		}
		return res; 
	}
	
	public boolean solveArgMinQrPivot(DenseMatrix64F x) {
		int dim = quadCoefs.getNumRows();
		LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.leastSquaresQrPivot(dim);

		boolean res = solver.setA(quadCoefs);
		solver.solve(linCoefs, x);
		
		for (int row = 0; row < dim; row++) {
			double xrow = x.get(row); 
			x.set(row, -0.5 * xrow);  
		}
		
		return res;
	}
	
	public boolean solveArgMinPseudoInverse(DenseMatrix64F x) {
		int dim = quadCoefs.getNumRows();
		LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.pseudoInverse();

		boolean res = solver.setA(quadCoefs);
		solver.solve(linCoefs, x);
		
		for (int row = 0; row < dim; row++) {
			double xrow = x.get(row); 
			x.set(row, -0.5 * xrow);  
		}
		
		return res;
	}

	public boolean solveArgMin(DenseMatrix64F x) {
		return solveArgMinQrPivot(x);
	}

	public String toString() {
		return "quadcoefs: \n" 
				+ DoubleFmtUtil.fmtDouble3(quadCoefs) 
				+ "linCoefs: " + DoubleFmtUtil.fmtDouble3(linCoefs)
				+ "\nconstCoef:" + DoubleFmtUtil.fmtDouble3(constCoef);
	}
}
