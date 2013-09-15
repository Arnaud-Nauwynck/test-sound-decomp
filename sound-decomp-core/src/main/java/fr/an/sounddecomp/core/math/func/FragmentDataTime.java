package fr.an.sounddecomp.core.math.func;

public class FragmentDataTime {

	private int fragmentLen;

	private double startTime;
	private double endTime;

	private double startTimeROI;
	private double endTimeROI;

	/** offset in fragment for start of Region Of Interrest */
	private int startIndexROI;
	/** offset in fragment for end of Region Of Interrest */
	private int endIndexROI;
	
	/**
	 * array containing
	 *  [5*k + 0] => time
	 *  [5*k + 1] => homogeneous time in [-1,1]
	 *  [5*k + 2] => data
	 *  [5*k + 3] => coefTimeData
	 *  [5*k + 4] => localAmplitude
	 */
	private double[] timeDataArray;

	public static final int OFFSET_TIME = 0;
	public static final int OFFSET_HOMOGENEOUSTIME = 1;
	public static final int OFFSET_DATA = 2;
	public static final int OFFSET_COEF_TIME = 3;
	public static final int OFFSET_LOCAL_AMPLITUDE = 4;

	public static final int INCR = 5;
	
	// ------------------------------------------------------------------------
	
	public FragmentDataTime(
			int fragmentLen, 
			double startTime, double endTime,
			double[] data, double[] coefTimeData, double[] localAmplitudeData
			) {
		this(fragmentLen, startTime, endTime,
				startTime, endTime, 0, fragmentLen,
				data, coefTimeData, localAmplitudeData);
	}

	public FragmentDataTime(
			FragmentDataTime src,
			double[] data
			) {
		this(src.fragmentLen, src.startTime, src.endTime,
				src.startTimeROI, src.endTimeROI, src.startIndexROI, src.endIndexROI,
				null, null, null);
		for (int i = 0,compactIndex = 0; i < fragmentLen; i++,compactIndex+=FragmentDataTime.INCR) {
			timeDataArray[compactIndex + OFFSET_DATA] = (data != null)? data[i] : 0.0;
			timeDataArray[compactIndex + OFFSET_COEF_TIME] = src.timeDataArray[compactIndex + OFFSET_COEF_TIME];
			timeDataArray[compactIndex + OFFSET_LOCAL_AMPLITUDE] = src.timeDataArray[compactIndex + OFFSET_LOCAL_AMPLITUDE];
		}
	}
	

	
	public FragmentDataTime(
			int fragmentLen, 
			double startTime, double endTime,
			double startTimeROI, double endTimeROI, int startIndexROI, int endIndexROI, 
			double[] data, double[] coefTimeData, double[] localAmplitudeData
			) {
		this.fragmentLen = fragmentLen;		
		this.startTime = startTime;
		this.endTime = endTime;
		this.startTimeROI = startTimeROI;
		this.endTimeROI = endTimeROI;
		this.startIndexROI = startIndexROI;
		this.endIndexROI = endIndexROI;
		
		this.timeDataArray = new double[INCR*fragmentLen];
		final double dt = (endTime - startTime) / fragmentLen;
		final double dht = 2.0 / fragmentLen;
		for (int i = 0,compactIndex = 0; i < fragmentLen; i++,compactIndex+=INCR) {
			timeDataArray[compactIndex] = startTime + i * dt;
			timeDataArray[compactIndex + OFFSET_HOMOGENEOUSTIME] = -1.0 + i * dht;
			timeDataArray[compactIndex + OFFSET_DATA] = (data != null)? data[i] : 0.0;
			timeDataArray[compactIndex + OFFSET_COEF_TIME] = (coefTimeData != null)? coefTimeData[i] : 0.0;
			timeDataArray[compactIndex + OFFSET_LOCAL_AMPLITUDE] = (localAmplitudeData != null)? localAmplitudeData[i] : 0.0;
		}
	}

	// ------------------------------------------------------------------------

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public double getStartTimeROI() {
		return startTimeROI;
	}

	public double getEndTimeROI() {
		return endTimeROI;
	}

	public int getStartIndexROI() {
		return startIndexROI;
	}

	public int getEndIndexROI() {
		return endIndexROI;
	}

	public int getFragmentLen() {
		return fragmentLen;
	}

	public double[] getTimeDataArray() {
		return timeDataArray;
	}

	// ------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "FragmentDataTime[" + fragmentLen 
				+ ", time=" + startTime + "..." + endTime 
				+ "]";
	}
	
}
