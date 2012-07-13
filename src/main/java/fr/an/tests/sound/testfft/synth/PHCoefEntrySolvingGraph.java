package fr.an.tests.sound.testfft.synth;

public class PHCoefEntrySolvingGraph {

	private double centeredResultVar;
	
	private double[][] resultVarForPShift = new double[PHCoefEntry.MAX_P_LEN][];

	private double[] solveMinP = new double[PHCoefEntry.MAX_P_LEN];
	private double[] solveMaxP = new double[PHCoefEntry.MAX_P_LEN];
	private double[] solveStepP = new double[PHCoefEntry.MAX_P_LEN];
	
	// ------------------------------------------------------------------------

	public PHCoefEntrySolvingGraph() {
	}
	
	// ------------------------------------------------------------------------
	

	public void setSolveInitRange(double[] p) {

		solveMinP[0] = 0.7 * p[0];
		solveMaxP[0] = 1.3 * p[0];
		solveStepP[0] = 0.6 * p[0] / 50;

		solveMinP[1] = 0.8 * p[1];
		solveMaxP[1] = 1.2 * p[1];
		solveStepP[1] = 0.4 * p[1] / 50;

		solveMinP[2] = p[2] - Math.PI/6;
		solveMaxP[2] = p[2] + Math.PI/6;
		solveStepP[2] = 2*Math.PI/6 / 50;

		// TODO
		for (int i = 3; i < PHCoefEntry.MAX_P_LEN; i++) {
			solveMinP[i] = -1.0;
			solveMaxP[i] = +1.0;
			solveStepP[i] = 2.0 / 50;
		}
	}
	
	public void computeGraphPShift(PHCoefEntry centeredCoef, 
			double startTime, double endTime, int timeLen, double[] times, 
			double[] data) {
		double[] centeredCoefP = centeredCoef.getP();
		for (int i = 0; i < PHCoefEntry.MAX_P_LEN; i++) {
			double minP = solveMinP[i];
			// double maxP = solveMaxP[i];
			double stepP = solveStepP[i];
			
			double prevCenteredPi = centeredCoefP[i]; // tmp backup
			
			double[] resultVars = resultVarForPShift[i];
			int varLen = resultVars.length;
			double pi = minP;
			for(int shiftIndex= 0; shiftIndex < varLen; shiftIndex++,pi+=stepP) {
				centeredCoefP[i] = pi;
				resultVars[shiftIndex] = centeredCoef.computeVarValues(startTime, endTime, timeLen, times, data);
			}
			
			centeredCoefP[i] = prevCenteredPi; // restore from tmp			
		}
	}
	
}
