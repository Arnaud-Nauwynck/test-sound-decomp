package fr.an.tests.sound.testfft.math.func;

public class ArrayFragmentTimesFunc extends FragmentTimesFunc {

	private double[] data;

	// ------------------------------------------------------------------------
	
	public ArrayFragmentTimesFunc(double[] data) {
		super();
		this.data = data;
	}

	// ------------------------------------------------------------------------

	public double eval(FragmentDataTime fragment, 
						int index, double time, double ht) {
		return data[index];
	}

}
