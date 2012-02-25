package fr.an.tests.sound.testfft;

import org.jfree.data.xy.DefaultXYDataset;

public class JFreeChartUtils {

	public static double[] createLinearDoubleRange(int len) {
		double[] res = new double[len];
		for (int i = 0; i < len; i++) {
			res[i] = i;
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
