package fr.an.tests.sound.testfft.math;

import fr.an.tests.sound.testfft.math.func.FragmentDataTime;

public class WindowAverageUtils {

//	public static void computeAverage(double[] fragmentData,
//			int windowSize,
//			double[] resultAmplitude, double[] resultAverage
//			) {
//		final int fragLen = fragmentData.length;
//		
//		final int midWindowSize = windowSize / 2;
//		
//		for (int i = 0; i < fragLen; i++) {
//			int windowStart = Math.max(0, i - midWindowSize);
//			int windowEnd = windowStart + windowSize;
//			if (windowEnd >= fragLen) {
//				windowStart = fragLen - windowSize;
//				windowEnd = fragLen;
//			}
//			double sumPositive = 0.0;
//			int countPositive = 0;
//			double sumNegative = 0.0;
//			int countNegative = 0;
//
//			// iter in window around index i: [windowStart, windowEnd]
//			for (int windowIndex = windowStart; windowIndex < windowEnd; windowIndex++) {
//				double dataT = fragmentData[windowIndex];
//				// TODO... apply coefWindow...
//				if (dataT > 0) {
//					sumPositive += dataT;
//					countPositive++;
//				} else {
//					sumNegative += dataT;
//					countNegative++;					
//				}
//			}
//			
//			double tmpMax = (countPositive!=0)? sumPositive / countPositive : 0;
//			double tmpMin = (countNegative!=0)? sumNegative / countNegative : 0;
//			resultAmplitude[i] = (tmpMax - tmpMin);
//			resultAverage[i] = (tmpMax + tmpMin) * 0.5;
//		}
//	}

	
	public static void computeAverage(FragmentDataTime fragmentDataTime,
			int windowSize,
			double[] resultAmplitude, double[] resultAverage
			) {
		final double[] compactArray = fragmentDataTime.getTimeDataArray();
		final int fragLen = fragmentDataTime.getFragmentLen();
		
		final int midWindowSize = windowSize / 2;
		
		for (int i = 0; i < fragLen; i++) {
			int windowStart = Math.max(0, i - midWindowSize);
			int windowEnd = windowStart + windowSize;
			if (windowEnd >= fragLen) {
				windowStart = fragLen - windowSize;
				windowEnd = fragLen;
			}
//			double sumPositive = 0.0;
//			int countPositive = 0;
//			double sumNegative = 0.0;
//			int countNegative = 0;

			double sumValue = 0.0;
			double SumSquareValue = 0.0;
			
			// iter in window around index i: [windowStart, windowEnd]
			int windowStartCompactIndex = windowStart * FragmentDataTime.INCR;
			int windowEndCompactIndex = windowEnd * FragmentDataTime.INCR;
			for (int compactIndex = windowStartCompactIndex; compactIndex < windowEndCompactIndex; compactIndex+=FragmentDataTime.INCR) {
				double dataT = compactArray[compactIndex+FragmentDataTime.OFFSET_DATA];
				// TODO... apply coefWindow...

				sumValue += dataT;
				SumSquareValue += dataT * dataT;

//				if (dataT > 0) {
//					sumPositive += dataT;
//					countPositive++;
//				} else {
//					sumNegative += dataT;
//					countNegative++;					
//				}
			}
			
			double linAvg = sumValue / windowSize;
			double squareStddev = Math.sqrt((SumSquareValue - linAvg*linAvg) / windowSize);
			resultAverage[i] = linAvg;
			resultAmplitude[i] = squareStddev;
			
//			double tmpMax = (countPositive!=0)? sumPositive / countPositive : 0;
//			double tmpMin = (countNegative!=0)? sumNegative / countNegative : 0;
//			resultAverage[i] = (tmpMax + tmpMin) * 0.5;
//			resultAmplitude[i] = (tmpMax - tmpMin);
			
			
		}
	}
}
