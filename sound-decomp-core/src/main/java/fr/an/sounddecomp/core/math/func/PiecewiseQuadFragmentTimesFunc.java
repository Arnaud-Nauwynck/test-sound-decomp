package fr.an.sounddecomp.core.math.func;

public class PiecewiseQuadFragmentTimesFunc extends FragmentTimesFunc {

	/**
	 * <PRE>
	 *                  ---- +Y(t[i+1])
	 *     Y(Mid[t[i])+    / |
	 *             /  | /    |
	 *            /  /       |
	 *           |/          |
	 *          +Y(t[i])     |
	 *          |            |
	 *          |            |              |
	 * ---------+------------+--------------+------  ..
	 *          t[i]        t[i+1]        t[i+2]    ..
	 * </PRE>
	 * 
	 * linear part: using change of variable   alpha = (t-ti)/(ti+1 - ti) ... alpha in [0,1]
	 * y= (1-alpha) * Y(ti) + alpha * Y(ti+1)
	 *  
	 * when defining pieceQuadMidValue, it adds a quadratic part:  
	 * dy= beta * alpha*(1-alpha)
	 * where Y(midti-ti+1)= y+dy(0.5) =  1/2 (Y(ti)+Y(ti+1)) + beta * 1/4
	 *  =>  beta =  4 * ( Y(midti-ti+1) - 1/2 (Y(ti)+Y(ti+1)) )
	 * 
	 */
	private double[] pieceTime;
	private double[] pieceValue;
	private double[] pieceQuadMidValue;

	private FragmentDataTime fragment;
	// private int[] computedIndexToPieceIndex;
	private double[] computedIndexToValue;
	
	// ------------------------------------------------------------------------
	
	public PiecewiseQuadFragmentTimesFunc(
			double[] pieceTime,
			double[] pieceValue,
			double[] pieceQuadMidValue,
			FragmentDataTime fragment) {
		this.pieceTime = pieceTime;
		this.pieceValue = pieceValue;
		this.pieceQuadMidValue = pieceQuadMidValue;

		this.fragment = fragment;
		int fragmentLen = fragment.getFragmentLen();
		this.computedIndexToValue = new double[fragmentLen];
		
		int currPieceIndex = 0;
		double prevTime = - Double.MAX_VALUE;
		double prevValue = pieceValue[currPieceIndex];
		double nextTime = pieceTime[0];
		double nextValue = pieceValue[0];
		double currBeta = 0.0;
		double inv_currTimeRange = 0.0;
		
		double[] compactArr = fragment.getTimeDataArray();

		int i = 0;
		int compactIndex=0;

		// time before first pieceTime[0]
		for (; i < fragmentLen; i++,compactIndex+=FragmentDataTime.INCR) {
			double t = compactArr[compactIndex];
			if (t >= prevTime) {
				break;
			}
			computedIndexToValue[i] = prevTime;
		}
		
		// time in range pieceTime[0]..pieceTime[pieceTime.length-1]
		for (; i < fragmentLen; i++,compactIndex+=FragmentDataTime.INCR) {
			double t = compactArr[compactIndex];
			if (t >= nextTime) {
				// switch to next piece
				currPieceIndex++;
				if (currPieceIndex >= pieceTime.length) {
					prevTime = nextTime;
					prevValue = nextValue;
					break;
				}
				prevTime = nextTime;
				prevValue = nextValue;
				nextTime = pieceTime[currPieceIndex];
				nextValue = pieceValue[currPieceIndex];
				inv_currTimeRange = (nextTime-prevTime != 0.0)? 1.0 / (nextTime - prevTime) : 0.0;
				if (pieceQuadMidValue != null) {
					currBeta = 4 * ( pieceQuadMidValue[currPieceIndex-1] - 0.5*( prevValue + nextValue) );
				}
			}
			// interpolate 
			double alpha = (t - prevTime) * inv_currTimeRange;
			computedIndexToValue[i] = (1.0-alpha) * prevValue + alpha * nextValue;
			if (pieceQuadMidValue != null) {
				computedIndexToValue[i] += currBeta * alpha*(1.0-alpha);
			}
		}
		
		// time after last pieceTime[]
		for (; i < fragmentLen; i++) {
			computedIndexToValue[i] = prevValue;
		}
		
	}

	// ------------------------------------------------------------------------

	public double eval(FragmentDataTime fragment, 
						int index, double time, double ht) {
		return computedIndexToValue[index];
	}

	// ------------------------------------------------------------------------

	public double[] getPieceTime() {
		return pieceTime;
	}

	public double[] getPieceValue() {
		return pieceValue;
	}

	public double[] getPieceQuadMidValue() {
		return pieceQuadMidValue;
	}

	public FragmentDataTime getFragment() {
		return fragment;
	}

	public double[] getComputedIndexToValue() {
		return computedIndexToValue;
	}

}
