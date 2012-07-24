package fr.an.tests.sound.testfft.utils;

public class DoubleUtil {


	public static double computeVar(double[] datas, double avg) {
		final int len = datas.length;
		double res = 0.0;
		for (int i = 0; i < len; i++) {
			double err = (datas[i] - avg) * (datas[i] - avg); 
			res += err;
		}
		return res;
	}
	
}
