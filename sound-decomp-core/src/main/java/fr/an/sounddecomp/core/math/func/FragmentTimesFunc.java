package fr.an.sounddecomp.core.math.func;

public abstract class FragmentTimesFunc {

	public abstract double eval(
			FragmentDataTime fragment, 
			int index, double time, double ht);

//	// non-optimized function eval...
//	public abstract double eval(double time);

}
