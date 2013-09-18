package fr.an.sounddecomp.core.frag.def.typedef;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;

public class TimeFragIndicatorTypeDefs {

    // ------------------------------------------------------------------------
    
    /**
     * TimeFragIndicatorTypeDef sub-class for object type: "T"  (java object on heap)
     * @param <T>
     */
    public static class SimpleClassTimeFragIndicatorTypeDef<T> extends TimeFragIndicatorTypeDef {

        private final Class<T> valueType;

        public SimpleClassTimeFragIndicatorTypeDef(TimeFragmentDefRegisty owner, String name, Class<T> valueType) {
            super(owner, name);
            this.valueType = valueType;
        }

        public Class<T> getValueType() {
            return valueType;
        }
        
        public T createObjectValue() {
            try {
                return (T) valueType.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Failed to create object " + valueType, ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Failed to create object " + valueType, ex);
            }
        }
        
    }

    // ------------------------------------------------------------------------
    
    /**
     * default sub-class of TimeFragIndicatorTypeDef for object type: "T[]"  (java array object on heap)
     * @param <T>
     */
    public static class SimpleArrayTimeFragIndicatorTypeDef<T> extends TimeFragIndicatorTypeDef {

        private final Class<T> valueElementType;

        public SimpleArrayTimeFragIndicatorTypeDef(TimeFragmentDefRegisty owner, String name, Class<T> valueElementType) {
            super(owner, name);
            this.valueElementType = valueElementType;
        }

        public Class<T> getValueElementType() {
            return valueElementType;
        }
        
        public T createObjectValue() {
            int fragmentSize = owner.getFragmentSize();
            throw new UnsupportedOperationException("TODO NOT_IMPLEMENTED YET ... new T[" + fragmentSize + "]");
        }
        
    }
    
}
