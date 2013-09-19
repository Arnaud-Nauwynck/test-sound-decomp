package fr.an.sounddecomp.core.frag.indexaccessor;

/**
 * Index accessor for accessing (getAt()/setAt()) data inside a Time Serie Fragment
 */
public abstract class TimeFragIndexedAccessor<T> {

    private TimeFragIndexRange indexRange;
    
    // ------------------------------------------------------------------------
    
    public TimeFragIndexedAccessor(TimeFragIndexRange indexRange) {
        super();
        this.indexRange = indexRange;
    }

    // ------------------------------------------------------------------------
    
    public TimeFragIndexRange getIndex() {
        return indexRange;
    }

    // ------------------------------------------------------------------------
    
    public abstract T getValueAt(int index);

    public abstract void setValueAt(int index, T value);

    
    /** overrideable (optim) for getAt() */ 
    public void getArrayValueAt(T[] result, int resultOffset, int indexStart, int len) {
        final int indexEnd = indexStart + len;
        int destIndex = resultOffset;
        for (int i = indexStart; i < indexEnd; i++,destIndex++) {
            result[destIndex] = getValueAt(i);
        }
    }
    
    /** overrideable (optim) for setAt() */ 
    public void setArrayValueAt(int indexStart, int len, T[] srcValue, int srcOffset) {
        final int indexEnd = indexStart + len;
        int srcIndex = srcOffset;
        for (int i = indexStart; i < indexEnd; i++,srcIndex++) {
            setValueAt(i, srcValue[srcIndex]);
        }
    }

    

    public int getStartIndex() {
        return indexRange.getStartIndex();
    }

    public int getEndIndex() {
        return indexRange.getEndIndex();
    }

    public double getTimeByIndex(int i) {
        return indexRange.getTimeByIndex(i);
    }

    public double getHomogeneousTimeByIndex(int i) {
        return indexRange.getHomogeneousTimeByIndex(i);
    }

}
