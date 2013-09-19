package fr.an.sounddecomp.core.frag.indexaccessor.impl;

import fr.an.sounddecomp.core.frag.indexaccessor.TimeFragIndexedAccessor;
import fr.an.sounddecomp.core.frag.indexaccessor.TimeFragIndexRange;

public class TimeFragmentValueAccessors {

    // ------------------------------------------------------------------------
    
    public static class SimpleArrayTimeFragmentValueAccessor<T> extends TimeFragIndexedAccessor<T> {

        private T[] data;
        
        public SimpleArrayTimeFragmentValueAccessor(TimeFragIndexRange index, T[] data) {
            super(index);
            this.data = data;
        }
        
        
        @Override
        public T getValueAt(int index) {
            return data[index];
        }

        @Override
        public void setValueAt(int index, T value) {
            data[index] = value;
        }
        
    }

    // ------------------------------------------------------------------------
    
    public static class OffsetArrayTimeFragmentValueAccessor<T> extends TimeFragIndexedAccessor<T> {

        private T[] data;
        private int offsetData;
        
        public OffsetArrayTimeFragmentValueAccessor(TimeFragIndexRange index, T[] data, int offsetData) {
            super(index);
            this.data = data;
            this.offsetData = offsetData;
        }
        
        
        @Override
        public T getValueAt(int index) {
            return data[index - offsetData];
        }

        @Override
        public void setValueAt(int index, T value) {
            data[index - offsetData] = value;
        }
        
    }

    // ------------------------------------------------------------------------
    
    public static class MixedArrayOffsetTimeFragmentValueAccessor<T> extends TimeFragIndexedAccessor<T> {

        private T[] data;
        private int offsetData;
        private int rowDataSize;
        
        public MixedArrayOffsetTimeFragmentValueAccessor(TimeFragIndexRange index, T[] data, int offsetData, int rowDataSize) {
            super(index);
            this.data = data;
            this.offsetData = offsetData;
            this.rowDataSize = rowDataSize;
        }
        
        
        @Override
        public T getValueAt(int index) {
            return data[index*rowDataSize - offsetData];
        }

        @Override
        public void setValueAt(int index, T value) {
            data[index*rowDataSize - offsetData] = value;
        }

        
    }
    
    // ------------------------------------------------------------------------
    
    public static class ConstTimeFragmentDataAccessor<T> extends TimeFragIndexedAccessor<T> {

        private T data;
         
        public ConstTimeFragmentDataAccessor(TimeFragIndexRange index, T data) {
            super(index);
            this.data = data;
        }
        
        @Override
        public T getValueAt(int index) {
            return data;
        }

        @Override
        public void setValueAt(int index, T value) {
            data = value;
        }
        
    }
    


}
