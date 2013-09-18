package fr.an.sounddecomp.core.frag.inds.ampl;

import fr.an.sounddecomp.core.frag.indexaccessor.TimeFragmentDataAccessor;
import fr.an.sounddecomp.core.frag.indexaccessor.TimeFragmentDoubleAccessor;

/**
 * Affine Continuous Amplitude Function
 *
 */
public class AffineC0AmplitudeUpDownTimeFragmentFunctor implements Runnable {

    public static class AffineC0AmplitudeUpDownFragData {
        double startAmplUp;
        double startAmplDown;
        double endAmplUp;
        double endAmplDown;
        
        public AffineC0AmplitudeUpDownFragData(double startAmplUp, double startAmplDown, double endAmplUp, double endAmplDown) {
            super();
            this.startAmplUp = startAmplUp;
            this.startAmplDown = startAmplDown;
            this.endAmplUp = endAmplUp;
            this.endAmplDown = endAmplDown;
        }
        
    }

    
    private TimeFragmentDataAccessor<AffineC0AmplitudeUpDownFragData> outputResult;
    
    private TimeFragmentDataAccessor<AffineC0AmplitudeUpDownFragData> inputPrevAmplC0Result;
    
    private TimeFragmentDoubleAccessor inputData;
    
    // ------------------------------------------------------------------------

    public AffineC0AmplitudeUpDownTimeFragmentFunctor() {
    }

    // ------------------------------------------------------------------------

    @Override
    public void run() {
        int startIndex = outputResult.getStartIndex();
        int endIndex = outputResult.getEndIndex();

        AffineC0AmplitudeUpDownFragData prevAffineAmplData = inputPrevAmplC0Result.getValueAt(endIndex);
        double prevAmplUp = prevAffineAmplData.endAmplUp;
        double prevAmplDown = prevAffineAmplData.endAmplDown;
        
        // compute integral and coef of  
        double sumCoefAmplUp = 0.0;
        double sumCoefAmplDown = 0.0;
        double sumAmplUpMinusStart = 0.0; // = sum_{0 <= ht <= 1, data(ht)>0 }{ coef . (data(ht) - prevAmplUp) . ht . dht)
        double sumAmplDownMinusStart = 0.0;
//        double sumAmplUp = 0.0;
//        double sumAmplDown = 0.0;
        
        double dht = inputData.getIndex().getIncrHomogeneousTime(); // = 1.0 / (endIndex - startIndex);
        double ht = 0.0;
        for (int index = startIndex; index < endIndex; index++,ht+=dht) {
            double value = inputData.getAt(index);
            double coef = 1.0 * ht;
            if (value < 0) {
                // down
                sumCoefAmplDown += coef;
//                sumAmplDown += coef * value;
                sumAmplDownMinusStart += coef * (value - prevAmplDown);
            } else {
                // up
                sumCoefAmplUp += coef;
//                sumAmplUp += coef * value;
                sumAmplUpMinusStart += coef * (value - prevAmplUp);
            }
        }
        
        // formula for least square error on amplitude => idem linear regression
        double resStartAmplUp = prevAmplUp; // shift time, continuous
        double resStartAmplDown = prevAmplDown; // shift time, continuous
        
        if (sumCoefAmplUp == 0) sumCoefAmplUp = 1;
        if (sumCoefAmplDown == 0) sumCoefAmplDown = 1;
        
        // TODO TOCHECK
        double resEndAmplUp = prevAmplUp + 1.0/3 * sumAmplUpMinusStart / sumCoefAmplUp;
        double resEndAmplDown = prevAmplDown + 1.0/3 * sumAmplDownMinusStart / sumCoefAmplDown;
        
        AffineC0AmplitudeUpDownFragData res = new AffineC0AmplitudeUpDownFragData(resStartAmplUp, resStartAmplDown, resEndAmplUp, resEndAmplDown);
        
        // set output result
        outputResult.setValueAt(startIndex, res);
    }

    // ------------------------------------------------------------------------
    
    public TimeFragmentDataAccessor<AffineC0AmplitudeUpDownFragData> getOutputResult() {
        return outputResult;
    }

    public void setOutputResult(
            TimeFragmentDataAccessor<AffineC0AmplitudeUpDownFragData> outputResult) {
        this.outputResult = outputResult;
    }

    public TimeFragmentDataAccessor<AffineC0AmplitudeUpDownFragData> getInputPrevAmplC0Result() {
        return inputPrevAmplC0Result;
    }

    public void setInputPrevAmplC0Result(
            TimeFragmentDataAccessor<AffineC0AmplitudeUpDownFragData> inputPrevAmplC0Result) {
        this.inputPrevAmplC0Result = inputPrevAmplC0Result;
    }

    public TimeFragmentDoubleAccessor getInputData() {
        return inputData;
    }

    public void setInputData(TimeFragmentDoubleAccessor inputData) {
        this.inputData = inputData;
    }
    
}
