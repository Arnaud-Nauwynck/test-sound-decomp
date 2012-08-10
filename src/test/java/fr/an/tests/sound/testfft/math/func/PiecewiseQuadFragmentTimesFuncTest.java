package fr.an.tests.sound.testfft.math.func;

import junit.framework.Assert;

import org.junit.Test;


public class PiecewiseQuadFragmentTimesFuncTest {

	@Test
	public void test1() {
		
		int fragmentLen = 10;
		double startTime = 0.0;
		double endTime = 1.0;
		double[] pieceTime = new double[] { startTime, endTime };
		double startValue = 10;
		double endValue = startValue + fragmentLen;
		double[] pieceValue = new double[] { startValue, endValue };
		double[] pieceQuadMidValue = null;
		
		FragmentDataTime fragment = new FragmentDataTime(fragmentLen, startTime, endTime, null, null, null);
		
		PiecewiseQuadFragmentTimesFunc func = new PiecewiseQuadFragmentTimesFunc(
				pieceTime, pieceValue, pieceQuadMidValue, fragment);

		double[] result = new double[fragmentLen];
		double[] compactTime4Array = fragment.getCompactTimeData4Array();
		for (int i = 0,compactIndex = 0; i < fragmentLen; i++,compactIndex+=FragmentDataTime.INCR) {
			double t = compactTime4Array[compactIndex];
			double ht = compactTime4Array[compactIndex + FragmentDataTime.OFFSET_HOMOGENEOUSTIME];
			
			result[i] = func.eval(fragment, i, t, ht);
		}

		Assert.assertEquals(startValue, result[0]);
		for (int i = 0; i < fragmentLen; i++) {
			double alpha = (double) i / (fragmentLen-1);
			double expected = (1-alpha) * startValue + alpha * endValue;
			Assert.assertEquals(expected, result[i]);
		}
		Assert.assertEquals(endValue, result[fragmentLen-1], 1e-6);
	}
	
	@Test
	public void test2() {
		
		double[] pieceTime = new double[] { 0.0, 1.0, 2.0 };
		double[] pieceValue = new double[] { 10.0, 11.0, 10.0 };
		double[] pieceQuadMidValue = null;
		
		int fragmentLen = 8;
		double startTime = -1.0;
		double endTime = 2.5;
		FragmentDataTime fragment = new FragmentDataTime(fragmentLen, startTime, endTime, null, null, null);
		
		PiecewiseQuadFragmentTimesFunc func = new PiecewiseQuadFragmentTimesFunc(
				pieceTime, pieceValue, pieceQuadMidValue, fragment);

		double[] result = new double[fragmentLen];
		double[] compactTime4Array = fragment.getCompactTimeData4Array();
		for (int i = 0,compactIndex = 0; i < fragmentLen; i++,compactIndex+=FragmentDataTime.INCR) {
			double t = compactTime4Array[compactIndex];
			double ht = compactTime4Array[compactIndex + FragmentDataTime.OFFSET_HOMOGENEOUSTIME];
			
			result[i] = func.eval(fragment, i, t, ht);
		}

		Assert.assertEquals(10.0, result[0]);
		assertArrayEquals(new double[] {
			10.0, 
			10.0, 
			10.0, // time[0]=0.0
			10.5,
			11.0, // time[1]=1.0
			10.5,
			10.0, // time[2]=2.0
			10.0
		}, result, 1e-6);
	}

	
	@Test
	public void testQuad() {
		
		int fragmentLen = 5;
		double startTime = 0.0;
		double endTime = 1.0;
		double[] pieceTime = new double[] { startTime, endTime };
		double startValue = 0;
		double endValue = 0;
		double midValue = -1;
		
		double[] pieceValue = new double[] { startValue, endValue };
		double[] pieceQuadMidValue = new double[] { midValue, Double.NaN };
		
		FragmentDataTime fragment = new FragmentDataTime(fragmentLen, startTime, endTime, null, null, null);
		
		PiecewiseQuadFragmentTimesFunc func = new PiecewiseQuadFragmentTimesFunc(
				pieceTime, pieceValue, pieceQuadMidValue, fragment);

		double[] result = new double[fragmentLen];
		double[] compactTime4Array = fragment.getCompactTimeData4Array();
		for (int i = 0,compactIndex = 0; i < fragmentLen; i++,compactIndex+=FragmentDataTime.INCR) {
			double t = compactTime4Array[compactIndex];
			double ht = compactTime4Array[compactIndex + FragmentDataTime.OFFSET_HOMOGENEOUSTIME];
			
			result[i] = func.eval(fragment, i, t, ht);
		}

		assertArrayEquals(new double[] {
				0.0, // time[0]=0 
				-0.75, 
				-1, // .. mid[0]
				-0.75,
				0.0 // time[1]=1.0
			}, result, 1e-6);
	}
	
	private static void assertArrayEquals(double[] expected, double[] actual, double epsilon) {
		if (expected == null && actual == null) return;
		Assert.assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], actual[i], epsilon);
		}
	}
	
	
}
