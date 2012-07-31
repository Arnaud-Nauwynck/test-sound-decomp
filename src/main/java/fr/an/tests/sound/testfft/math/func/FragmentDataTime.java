package fr.an.tests.sound.testfft.math.func;

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
	 *  [4*k + 0] => time
	 *  [4*k + 1] => homogeneous time in [-1,1]
	 *  [4*k + 2] => data
	 *  [4*k + 3] => coefTimeData
	 */
	private double[] compactTimeData4Array;

	// ------------------------------------------------------------------------
	
	public FragmentDataTime(
			int fragmentLen, 
			double startTime, double endTime,
			double[] data, double[] coefTimeData
			) {
		this(fragmentLen, startTime, endTime,
				startTime, endTime, 0, fragmentLen,
				data, coefTimeData);
	}

	public FragmentDataTime(
			FragmentDataTime src,
			double[] data
			) {
		this(src.fragmentLen, src.startTime, src.endTime,
				src.startTimeROI, src.endTimeROI, src.startIndexROI, src.endIndexROI,
				null, null);
		for (int i = 0,compactIndex = 0; i < fragmentLen; i++,compactIndex+=4) {
			compactTimeData4Array[compactIndex + 2] = (data != null)? data[i] : 0.0;
			compactTimeData4Array[compactIndex + 3] = src.compactTimeData4Array[compactIndex + 3];
		}
	}
	

	
	public FragmentDataTime(
			int fragmentLen, 
			double startTime, double endTime,
			double startTimeROI, double endTimeROI, int startIndexROI, int endIndexROI, 
			double[] data, double[] coefTimeData
			) {
		this.fragmentLen = fragmentLen;		
		this.startTime = startTime;
		this.endTime = endTime;
		this.startTimeROI = startTimeROI;
		this.endTimeROI = endTimeROI;
		this.startIndexROI = startIndexROI;
		this.endIndexROI = endIndexROI;
		
		this.compactTimeData4Array = new double[4*fragmentLen];
		final double dt = (endTime - startTime) / fragmentLen;
		final double dht = 2.0 / fragmentLen;
		for (int i = 0,compactIndex = 0; i < fragmentLen; i++,compactIndex+=4) {
			compactTimeData4Array[compactIndex] = startTime + i * dt;
			compactTimeData4Array[compactIndex + 1] = -1.0 + i * dht;
			compactTimeData4Array[compactIndex + 2] = (data != null)? data[i] : 0.0;
			compactTimeData4Array[compactIndex + 3] = (coefTimeData != null)? coefTimeData[i] : 0.0;
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

	public double[] getCompactTimeData4Array() {
		return compactTimeData4Array;
	}

	// ------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "FragmentDataTime[" + fragmentLen 
				+ ", time=" + startTime + "..." + endTime 
				+ "]";
	}
	
}
