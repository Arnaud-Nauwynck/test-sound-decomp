package fr.an.sounddecomp.core.frag.indexaccessor.impl;

import fr.an.sounddecomp.core.frag.indexaccessor.TimeFragDoubleIndexedAccessor;
import fr.an.sounddecomp.core.frag.indexaccessor.TimeFragIndexRange;


public class TimeFragmentDoubleAccessors {

    // ------------------------------------------------------------------------
    
    public static class SimpleArrayTimeFragmentDoubleAccessor extends TimeFragDoubleIndexedAccessor {

        private double[] data;
        
        public SimpleArrayTimeFragmentDoubleAccessor(TimeFragIndexRange index, double[] data) {
            super(index);
            this.data = data;
        }
        
        
        @Override
        public double getAt(int index) {
            return data[index];
        }

        @Override
        public void setAt(int index, double value) {
            data[index] = value;
        }
        
    }

    // ------------------------------------------------------------------------
    
    public static class OffsetArrayTimeFragmentDoubleAccessor extends TimeFragDoubleIndexedAccessor {

        private double[] data;
        private int offsetData;
        
        public OffsetArrayTimeFragmentDoubleAccessor(TimeFragIndexRange index, double[] data, int offsetData) {
            super(index);
            this.data = data;
            this.offsetData = offsetData;
        }
        
        
        @Override
        public double getAt(int index) {
            return data[index - offsetData];
        }

        @Override
        public void setAt(int index, double value) {
            data[index - offsetData] = value;
        }
        
    }

    // ------------------------------------------------------------------------
    
    public static class MixedArrayOffsetTimeFragmentDoubleAccessor<T> extends TimeFragDoubleIndexedAccessor {

        private double[] data;
        private int offsetData;
        private int rowDataSize;
        
        public MixedArrayOffsetTimeFragmentDoubleAccessor(TimeFragIndexRange index, double[] data, int offsetData, int rowDataSize) {
            super(index);
            this.data = data;
            this.offsetData = offsetData;
            this.rowDataSize = rowDataSize;
        }
        
        
        @Override
        public double getAt(int index) {
            return data[index*rowDataSize - offsetData];
        }

        @Override
        public void setAt(int index, double value) {
            data[index*rowDataSize - offsetData] = value;
        }

        
    }
    
    // ------------------------------------------------------------------------
    
    public static class ConstTimeFragmentDataAccessor<T> extends TimeFragDoubleIndexedAccessor {

        private double data;
         
        public ConstTimeFragmentDataAccessor(TimeFragIndexRange index, double data) {
            super(index);
            this.data = data;
        }
        
        @Override
        public double getAt(int index) {
            return data;
        }

        @Override
        public void setAt(int index, double value) {
            data = value;
        }
        
    }
    


}
