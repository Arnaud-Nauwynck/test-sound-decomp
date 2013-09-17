package fr.an.sounddecomp.core.frag.indexaccessor;

/**
 * typed sub-class for TimeFragmentDataAccessor<Double>
 */
public abstract class TimeFragmentDoubleAccessor extends TimeFragmentDataAccessor<Double> {
    
    // ------------------------------------------------------------------------
    
    public TimeFragmentDoubleAccessor(TimeFragmentIndex index) {
        super(index);
    }

    // ------------------------------------------------------------------------

    public Double getValueAt(int index) {
        return getAt(index); 
    }

    public void setValueAt(int index, Double value) {
        setAt(index, value);
    }


    public abstract double getAt(int index);

    public abstract void setAt(int index, double value);
    
    /** overrideable (optim) for getAt() */ 
    public void getArrayAt(double[] result, int resultOffset, int indexStart, int len) {
        final int indexEnd = indexStart + len;
        int destIndex = resultOffset;
        for (int i = indexStart; i < indexEnd; i++,destIndex++) {
            result[destIndex] = getAt(i);
        }
    }
    
    /** overrideable (optim) for setAt() */ 
    public void setArrayAt(int indexStart, int len, double[] srcValue, int srcOffset) {
        final int indexEnd = indexStart + len;
        int srcIndex = srcOffset;
        for (int i = indexStart; i < indexEnd; i++,srcIndex++) {
            setAt(i, srcValue[srcIndex]);
        }
    }
}