package fr.an.tests.sound.testfft;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DoubleFmtUtil {

    private static final NumberFormat DBL_FMT3 = new DecimalFormat("#.####");

    public static String fmtDouble3(double v) {
    	return DBL_FMT3.format(v);
    }

}
