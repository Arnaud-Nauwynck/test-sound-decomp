package fr.an.tests.sound.testfft.ui.utils;

import org.jfree.data.xy.DefaultXYDataset;

public class JFreeChartUtils {

	public static double[] createLinearDoubleRange(int len) {
		double[] res = new double[len];
		for (int i = 0; i < len; i++) {
			res[i] = i;
		}
		return res;
	}

	public static double[] createLinearDoubleRange(int len, double startValue, double endValue) {
		double[] res = new double[len];
		final double dValue = (endValue - startValue) / len;
		double value = startValue;
		for (int i = 0; i < len; i++,value+=dValue) {
			res[i] = value;
		}
		return res;
	}

	public static void addDefaultXYDatasetSerie(DefaultXYDataset res, String name, double[] xData, double[] yData) {
		double[][] xy = new double[2][];
		xy[0] = xData; 
		xy[1] = yData;
		res.addSeries(name, xy);
	}

	
}
