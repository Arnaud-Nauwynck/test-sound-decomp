package fr.an.sounddecomp.core.frag.indexaccessor;

public class TimeFragIndexRange {

    private int overlappedStartIndex;
    private double overlappedStartTime;
    private double[] overlappedStartCoef;
    
    private int startIndex;
    private double startTime;

    private int endIndex;
    private double endTime;

    private int overlappedEndIndex;
    private double overlappedEndTime;
    private double[] overlappedEndCoef;
    
    double[] timeByIndex;
    double[] homogeneousTimeByIndex;
    
    // ------------------------------------------------------------------------
    
    public TimeFragIndexRange(
            int overlappedStartIndex, double overlappedStartTime, 
            double[] overlappedStartCoef,
            int startIndex, double startTime,
            int endIndex, double endTime, 
            int overlappedEndIndex, double overlappedEndTime,
            double[] overlappedEndCoef
            ) {
        super();
        this.overlappedStartIndex = overlappedStartIndex;
        this.overlappedStartTime = overlappedStartTime;
        this.overlappedStartCoef = overlappedStartCoef;
        this.startIndex = startIndex;
        this.startTime = startTime;
        this.endIndex = endIndex;
        this.endTime = endTime;
        this.overlappedEndIndex = overlappedEndIndex;
        this.overlappedEndTime = overlappedEndTime;
        this.overlappedEndCoef = overlappedEndCoef;
        
        int len = overlappedEndIndex - overlappedStartIndex; 
        int offset = overlappedStartIndex;
        this.timeByIndex = new double[len];
        fillLinearValues(overlappedStartTime,   startTime,          timeByIndex, overlappedStartIndex-offset, startIndex-offset);
        fillLinearValues(startTime,             endTime,            timeByIndex, startIndex-offset, endIndex-offset);
        fillLinearValues(endTime,               overlappedEndTime,  timeByIndex, endIndex-offset, overlappedEndIndex-offset);

        this.homogeneousTimeByIndex = new double[len];
        fillLinearValues(-1,    0,  homogeneousTimeByIndex, overlappedStartIndex-offset, startIndex-offset);
        fillLinearValues( 0,   +1,  homogeneousTimeByIndex, startIndex-offset, endIndex-offset);
        fillLinearValues(+1,   +2,  homogeneousTimeByIndex, endIndex-offset, overlappedEndIndex-offset);

    }

    public static void fillLinearValues(double startValue, double endValue, 
            double[] dest, int destStartIndex, int destEndIndex) {
        double len = destEndIndex - destStartIndex;
        double inv_len = 1.0 / (len != 0? len : 1);
        double incr = (endValue - startValue) * inv_len;
        double value = startValue;
        for (int i = destStartIndex; i < destEndIndex; i++,value+=incr) {
            dest[i] = value;  // = startValue + (i - startValue) * inv_len; //rounding error?
        }
    }
    
    // ------------------------------------------------------------------------
    
    
    public int getOverlappedStartIndex() {
        return overlappedStartIndex;
    }

    public double getOverlappedStartTime() {
        return overlappedStartTime;
    }

    public double[] getOverlappedStartCoef() {
        return overlappedStartCoef;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public double getStartTime() {
        return startTime;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public double getEndTime() {
        return endTime;
    }

    public int getOverlappedEndIndex() {
        return overlappedEndIndex;
    }

    public double getOverlappedEndTime() {
        return overlappedEndTime;
    }

    public double[] getOverlappedEndCoef() {
        return overlappedEndCoef;
    }

    public double[] getTimeByIndex() {
        return timeByIndex;
    }

    public double[] getHomogeneousTimeByIndex() {
        return homogeneousTimeByIndex;
    }


    public double getTimeByIndex(int index) {
        return timeByIndex[index - overlappedStartIndex];
    }

    public double getHomogeneousTimeByIndex(int index) {
        return homogeneousTimeByIndex[index - overlappedStartIndex];
    }

    public double getIncrHomogeneousTime() {
        return 1.0 / (endIndex - startIndex);
    }
    
}
