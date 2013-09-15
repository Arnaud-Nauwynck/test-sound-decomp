package fr.an.sounddecomp.core.algos.ph;

public class RunningAverageEntry {

	private double[] runningWindowAmplitude;
	private double[] runningWindowAverage;
	
	// ------------------------------------------------------------------------
	
	public RunningAverageEntry(double[] runningWindowAmplitude, double[] runningWindowAverage) {
		super();
		this.runningWindowAmplitude = runningWindowAmplitude;
		this.runningWindowAverage = runningWindowAverage;
	}

	public RunningAverageEntry(int len) {
		this(new double[len], new double[len]);
	}

	// ------------------------------------------------------------------------

	public double[] getRunningWindowAmplitude() {
		return runningWindowAmplitude;
	}

	public void setRunningWindowAmplitude(double[] runningWindowAmplitude) {
		this.runningWindowAmplitude = runningWindowAmplitude;
	}

	public double[] getRunningWindowAverage() {
		return runningWindowAverage;
	}

	public void setRunningWindowAverage(double[] runningWindowAverage) {
		this.runningWindowAverage = runningWindowAverage;
	}
	
	// ------------------------------------------------------------------------
	
	
}
