package fr.an.tests.sound.testfft;

import ca.uol.aig.fftpack.RealDoubleFFT;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Hello world!
 *
 */
public class FFTTestMain 
{
	public static void main( String[] args )
    {
        System.out.println( "Hello FFT World!" );
        
        int inputLen = // 1024; // 2^10
        		// 4096;
        		8192;
        // RealDoubleFFT rdFFT = new RealDoubleFFT(inputLen);
        DoubleFFT_1D fft = new DoubleFFT_1D(inputLen);
        
        double[] data = new double[inputLen];

        FFTCoefPrinter coefPrinter = new FFTCoefPrinter(inputLen);
        double Period = 2*Math.PI;
        double inv_Period = 1.0 / Period; 

    	double amplitudeStart = 1.0;
    	double amplitudeEnd = 1.0; 
    			// 1.3;

        double tau = Period / inputLen;
        double t = 0;
        for (int i = 0; i < inputLen; i++, t += tau) {
        	double ratioT = t * inv_Period;
        	double linear = 1.0; // amplitudeStart + ratioT * (amplitudeEnd - amplitudeStart);
//        	data.x[i] = 10 + 
//        			+ 1 * Math.cos(1 * t) //
//        			+ 3 * Math.cos(5 * t) + 4 * Math.sin(5 * t) 
//        			+ 2 * Math.cos(7 * t) //
//
//        			+ 2 * Math.cos(11 * t + Math.PI*0.6) //
//        			+ 2 * Math.cos(12 * t + Math.PI*0.3) //
//
//        			// + 2 * Math.cos((inputLen + 50) * t); // => spectral replication on [0,N]... => 50!
//        			;
//        	
        	
        	// linear ... function of t ==> not a periodic stationary function.... FFT has noise... 
        	data[i] = linear * ( //
        			0.0 //
        			+ 2.0 * Math.cos(7.0 * t) // 
        			+ 3.0 * Math.cos(12.0 * t + Math.PI*0.6) //
        			);
        	
        }
        // data = new double[inputLen];

                
        // rdFFT.ft(data);
        fft.realForward(data);
        
        coefPrinter.setDataJTransform(data);
        coefPrinter.printData();
    }
    
}
