package fr.an.sounddecomp.core.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.ejml.data.DenseMatrix64F;

import fr.an.sounddecomp.core.math.fft.FFT;
import fr.an.sounddecomp.core.math.func.CosSinPolynom2;
import fr.an.sounddecomp.core.math.func.QuadraticForm;

public class DoubleFmtUtil {

    private static final NumberFormat DBL_FMT3 = new DecimalFormat("#.###");

    public static String fmtDouble3(double v) {
    	return DBL_FMT3.format(v);
    }

    public static String fmtPhaseDouble3(double v) {
    	return fmtDouble3(v * FFT.INV_PI) + "*pi";
    }

    public static String fmtFreqDouble3(double v) {
    	return fmtDouble3(v * FFT.INV_2PI) + " Hz"; // TOCHECH  frameRate/fragmentLen ...
    }
    
    public static String fmtDouble3(double[] vect) {
    	StringBuilder sb = new StringBuilder();
    	fmtDouble3(sb, vect);
    	return sb.toString();
    }
    
    public static void fmtDouble3(StringBuilder sb, double[] vect) {
    	for (int i = 0; i < vect.length; i++) {
    		sb.append(fmtDouble3(vect[i]));
    		if (i+1 < vect.length) {
    			sb.append(' ');
    		}
    	}
    }

    public static String fmtDouble3(double[][] array) {
    	StringBuilder sb = new StringBuilder();
    	fmtDouble3(sb, array);
    	return sb.toString();
    }
    
    public static void fmtDouble3(StringBuilder sb, double[][] array) {
    	for (int i = 0; i < array.length; i++) {
    		sb.append(fmtDouble3(array[i]));
    		sb.append('\n');
    	}
    }

	public static String fmtDouble3(DenseMatrix64F array) {
    	StringBuilder sb = new StringBuilder();
    	fmtDouble3(sb, array);
    	return sb.toString();
    }
    
    public static void fmtDouble3(StringBuilder sb, DenseMatrix64F array) {
    	final int nrows = array.getNumRows();
    	final int ncols = array.getNumCols();
    	if (ncols == 1) {
    		//print vector horizontally
    		for (int row = 0; row < nrows; row++) {
    			sb.append(fmtDouble3(array.get(row, 0)));
	    		if (row+1 < nrows) {
	    			sb.append(' ');
	    		}
    		}
    	} else {
	    	for (int row = 0; row < nrows; row++) {
	    		for (int col = 0; col < ncols; col++) {
	    			sb.append(fmtDouble3(array.get(row, col)));
		    		if (col+1 < ncols) {
		    			sb.append(' ');
		    		}
	    		}
	    		sb.append('\n');
	    	}
    	}
    }

    public static String fmtDouble3(QuadraticForm quadForm) {
    	StringBuilder sb = new StringBuilder();
    	fmtDouble3(sb, quadForm);
    	return sb.toString();
    }
    
	public static void fmtDouble3(StringBuilder sb, QuadraticForm quadForm) {
		sb.append("quadcoefs: \n" 
				+ DoubleFmtUtil.fmtDouble3(quadForm.getQuadCoefs()) 
				+ "linCoefs: " + DoubleFmtUtil.fmtDouble3(quadForm.getLinCoefs())
				+ "\nconstCoef:" + DoubleFmtUtil.fmtDouble3(quadForm.getConstCoef()));
	}

	public static String fmtDouble3(CosSinPolynom2 p) {
    	StringBuilder sb = new StringBuilder();
    	fmtDouble3(sb, p);
    	return sb.toString();
    }

	public static void fmtDouble3(StringBuilder sb, CosSinPolynom2 p) {
    	sb.append("cc:" + fmtDouble3(p.getCoefCos2())
    			+ " cs:" + fmtDouble3(p.getCoefCosSin())
    			+ " ss:" + fmtDouble3(p.getCoefSin2())
    			+ " c:" + fmtDouble3(p.getCoefCos())
    			+ " s:" + fmtDouble3(p.getCoefSin())
    			+ " k:" + fmtDouble3(p.getCoefConst()));
    }

}