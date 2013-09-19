package fr.an.sounddecomp.core.frag.ssa;



public class TimeFragSSAVars {


    private static final TimeFragSSAOperation NULL_ALLREADY_SET_DEPENDENCY = 
            new TimeFragSSAOperation(new TimeFragSSAVar<?>[0], null, new TimeFragSSAVar<?>[0]);
    
    // ------------------------------------------------------------------------
    
    public static abstract class AbstractDerivedTimeFragSSAVar<T> extends TimeFragSSAVar<T> {
        
        private TimeFragSSAOperation dependency;
        
        public abstract T getObjectValue();
        protected abstract void doSetVarValue(T value);
        
        public final void ssaSetVarValue(T value) {
            if (dependency == NULL_ALLREADY_SET_DEPENDENCY)
                throw new IllegalStateException("can not set ssa var already set!");
            this.dependency = NULL_ALLREADY_SET_DEPENDENCY;
            doSetVarValue(value);
        }

        public final boolean isSet() {
            return dependency == NULL_ALLREADY_SET_DEPENDENCY;
        }

        public final boolean isForgetDepencyOnSsaSet() {
            return true;
        }

        public final TimeFragSSAOperation getDependency() {
            return (dependency == NULL_ALLREADY_SET_DEPENDENCY)? null : dependency;
        }
        
    }
    
    
    // ------------------------------------------------------------------------
    
    public static abstract class AbstractConstTimeFragSSAVar<T> extends TimeFragSSAVar<T> {
        
        public abstract T getObjectValue();
        
        public final void ssaSetVarValue(T value) {
            throw new IllegalStateException("can not set const var");
        }

        public final boolean isSet() {
            return true;
        }

        public final boolean isForgetDepencyOnSsaSet() {
            return false;
        }

        public final TimeFragSSAOperation getDependency() {
            return null;
        }
        
    }
    
    // ------------------------------------------------------------------------
    
    public static abstract class AbstractConstValueTimeFragSSAVar<T> extends AbstractConstTimeFragSSAVar<T> {
        
        private final T value;

        public AbstractConstValueTimeFragSSAVar(T value) {
            this.value = value;
        }

        @Override
        public T getObjectValue() {
            return value;
        }
        
    }

    // ------------------------------------------------------------------------
    
    public static abstract class AbstractConstDoubleTimeFragSSAVar extends AbstractConstTimeFragSSAVar<Double> {
        
        private final double value;

        public AbstractConstDoubleTimeFragSSAVar(double value) {
            this.value = value;
        }

        @Override
        public Double getObjectValue() {
            return Double.valueOf(value);
        }
        
        public double getDoubleValue() {
            return value;
        }
        
    }

    // ------------------------------------------------------------------------
    
    public static abstract class AbstractConstIntTimeFragSSAVar extends AbstractConstTimeFragSSAVar<Integer> {
        
        private final int value;

        public AbstractConstIntTimeFragSSAVar(int value) {
            this.value = value;
        }

        @Override
        public Integer getObjectValue() {
            return Integer.valueOf(value);
        }
        
        public int getIntValue() {
            return value;
        }
        
    }
    
}
