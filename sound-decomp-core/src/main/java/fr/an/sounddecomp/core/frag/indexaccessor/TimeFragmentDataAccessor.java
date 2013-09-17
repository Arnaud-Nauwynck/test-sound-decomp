package fr.an.sounddecomp.core.frag.indexaccessor;

/**
 * Array accessor for indexing data inside a Time Serie Fragment
 */
public abstract class TimeFragmentDataAccessor<T> {

    private TimeFragmentIndex index;
    
    // ------------------------------------------------------------------------
    
    public TimeFragmentDataAccessor(TimeFragmentIndex index) {
        super();
        this.index = index;
    }

    // ------------------------------------------------------------------------
    
    public TimeFragmentIndex getIndex() {
        return index;
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
        return index.getStartIndex();
    }

    public int getEndIndex() {
        return index.getEndIndex();
    }

    public double getTimeByIndex(int i) {
        return index.getTimeByIndex(i);
    }

    public double getHomogeneousTimeByIndex(int i) {
        return index.getHomogeneousTimeByIndex(i);
    }

}
