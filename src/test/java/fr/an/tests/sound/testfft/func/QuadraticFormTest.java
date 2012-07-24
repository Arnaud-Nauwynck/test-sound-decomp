package fr.an.tests.sound.testfft.func;

import java.util.Random;

import org.ejml.data.DenseMatrix64F;
import org.junit.Assert;
import org.junit.Test;

import fr.an.tests.sound.testfft.func.QuadraticForm;


public class QuadraticFormTest {

	private static final double EPSILON = 1e-6;

	@Test
	public void testIdentity3() {
		QuadraticForm q = new QuadraticForm(3);
		double[][] quadData = new double[][] {
			{ 1.0, 0.0, 0.0 },
			{ 0.0, 1.0, 0.0 },
			{ 0.0, 0.0, 1.0 },
		};
		double[] linData = new double[] { 0.0, 0.0, 0.0 };
		double constData = 0.0;
		q.setData(quadData, linData , constData);
		DenseMatrix64F x = new DenseMatrix64F(3, 1); 
		
		Random rand = new Random(0);
		for (int i = 0; i < 10; i++) {
			x.set(0, rand.nextDouble());
			x.set(1, rand.nextDouble());
			x.set(2, rand.nextDouble());
			double quadVal = q.eval(x);
			double checkQuadVal = square(x.get(0)) + square(x.get(1)) + square(x.get(2));
			assertDoubleEquals(quadVal, checkQuadVal);
		}
		
		q.solveArgMin(x);
		assertDoubleEquals(0.0, x.get(0));
		assertDoubleEquals(0.0, x.get(1));
		assertDoubleEquals(0.0, x.get(2));
		
	}
	
	

	@Test
	public void testIdentityShift3() {
		QuadraticForm q = new QuadraticForm(3);
		// q = (x-1)^2 + (y-2)^2 + (z-3)^2
		//   =  x^2-2.x+1 + y^2-4y+4 + z^2-6z+9
		double[][] quadData = new double[][] {
			{ 1.0, 0.0, 0.0 },
			{ 0.0, 1.0, 0.0 },
			{ 0.0, 0.0, 1.0 },
		};
		double[] linData = new double[] { -2.0, -4.0, -6.0 };
		double constData = 14.0;
		q.setData(quadData, linData , constData);
		DenseMatrix64F x = new DenseMatrix64F(3, 1); 
		
		Random rand = new Random(0);
		for (int i = 0; i < 10; i++) {
			x.set(0, rand.nextDouble());
			x.set(1, rand.nextDouble());
			x.set(2, rand.nextDouble());
			double quadVal = q.eval(x);
			double checkQuadVal = square(x.get(0)-1) + square(x.get(1)-2) + square(x.get(2)-3);
			assertDoubleEquals(quadVal, checkQuadVal);
		}
		
		q.solveArgMin(x);
		
		double solvedX = x.get(0);
		double solvedY = x.get(1);
		double solvedZ = x.get(2);
		assertDoubleEquals(1.0, solvedX);
		assertDoubleEquals(2.0, solvedY);
		assertDoubleEquals(3.0, solvedZ);
	}
	

	@Test
	public void testHomotetieShift3() {
		QuadraticForm q = new QuadraticForm(3);
		// q = 2.(x-1)^2 + 3.(y-2)^2 + 4.(z-3)^2
		//   =  2.x^2-4.x+2 + 3.y^2-12y+12 + 4.z^2-24z+36
		double[][] quadData = new double[][] {
			{ 2.0, 0.0, 0.0 },
			{ 0.0, 3.0, 0.0 },
			{ 0.0, 0.0, 4.0 },
		};
		double[] linData = new double[] { -4.0, -12.0, -24.0 };
		double constData = 50.0;
		q.setData(quadData, linData , constData);
		DenseMatrix64F x = new DenseMatrix64F(3, 1); 
		
		Random rand = new Random(0);
		for (int i = 0; i < 10; i++) {
			x.set(0, rand.nextDouble());
			x.set(1, rand.nextDouble());
			x.set(2, rand.nextDouble());
			double quadVal = q.eval(x);
			double checkQuadVal = 2.0*square(x.get(0)-1) + 3.0*square(x.get(1)-2) + 4.0*square(x.get(2)-3);
			assertDoubleEquals(quadVal, checkQuadVal);
		}
		
		q.solveArgMin(x);
		
		double solvedX = x.get(0);
		double solvedY = x.get(1);
		double solvedZ = x.get(2);
		assertDoubleEquals(1.0, solvedX);
		assertDoubleEquals(2.0, solvedY);
		assertDoubleEquals(3.0, solvedZ);
	}
	

	@Test
	public void testSingular() {
		QuadraticForm q = new QuadraticForm(3);
		// q = 2.(x-1)^2 + 3.(y-2)^2 
		//   =  2.x^2-4.x+2 + 3.y^2-12y+12
		double[][] quadData = new double[][] {
			{ 2.0, 0.0, 0.0 },
			{ 0.0, 3.0, 0.0 },
			{ 0.0, 0.0, 0.0 },
		};
		double[] linData = new double[] { -4.0, -12.0, 0.0 };
		double constData = 14.0;
		q.setData(quadData, linData , constData);
		DenseMatrix64F x = new DenseMatrix64F(3, 1); 
		
		Random rand = new Random(0);
		for (int i = 0; i < 10; i++) {
			x.set(0, rand.nextDouble());
			x.set(1, rand.nextDouble());
			x.set(2, rand.nextDouble());
			double quadVal = q.eval(x);
			double checkQuadVal = 2.0*square(x.get(0)-1) + 3.0*square(x.get(1)-2);
			assertDoubleEquals(quadVal, checkQuadVal);
		}
		
		boolean solvedOK = q.solveArgMinQrPivot(x);
		if (solvedOK) {
			double solvedX = x.get(0);
			double solvedY = x.get(1);
			double solvedZ = x.get(2);
			// TODO does not work!!!
//			assertDoubleEquals(1.0, solvedX);
//			assertDoubleEquals(2.0, solvedY);
//			assertDoubleEquals(0.0, solvedZ);
			
			// => Inifinity, NaN, 0.0
		} else {
			// ??
		}
		
		q.solveArgMinPseudoInverse(x);
		double solvedpiX = x.get(0);
		double solvedpiY = x.get(1);
		double solvedpiZ = x.get(2);
		assertDoubleEquals(1.0, solvedpiX);
		assertDoubleEquals(2.0, solvedpiY);
		assertDoubleEquals(0.0, solvedpiZ);
		
	}
	
	
	public static double square(double x) {
		return x *x;
	}
	
	public static void assertDoubleEquals(double expected, double actual) {
		Assert.assertEquals(expected, actual, EPSILON);
	}
	
}
