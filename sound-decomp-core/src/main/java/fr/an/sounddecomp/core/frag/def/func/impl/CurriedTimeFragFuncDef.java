package fr.an.sounddecomp.core.frag.def.func.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;
import fr.an.sounddecomp.core.frag.def.func.TimeFragFuncDef;
import fr.an.sounddecomp.core.frag.def.func.TimeFragFuncEvaluator;


/**
 * TimeFragFunctorDef sub-class for curried function, using partial parameters feed by constant values
 * 
 * underlyingFunc:   (u1, u1, ...   u_{N+C} ) --> (y1, ..y_P)
 * curried const :   (curried1, ... curried_C) 
 * this curriedFunc: (x1, x2, ..x_N) ---> (y1, ..y_P)
 * 
 * with 
 *  curriedFunc(x1, x2, ..xN)  =  underlyingFunc(u1, u1, ...   u_{N+C})
 *  for all i, it exists j, such that ui = x_j or ui = curried_j  
 * 
 *   
 */
public class CurriedTimeFragFuncDef extends TimeFragFuncDef {

    // cf super.inputParamDefs ... down cast to List<CurriedTimeFragFuncParamDef>
    
    private final List<UnderyingParamDefToSourceMapping> underlyingInputParamDefToParentMappings;
    
    private final TimeFragFuncDef underlyingFuncDef;
    
    // ------------------------------------------------------------------------
    
    public CurriedTimeFragFuncDef(TimeFragmentDefRegisty owner, String name, CurriedTimeFragFuncDef.Builder builder) {
        super(owner, name, builder); //<= chicken and egg problem ... build all child inputParamDefs in parent ctor as new CurriedTimeFragFuncParamDef(this...) objects
        this.underlyingFuncDef = builder.underlyingFunctorDef;
        if (underlyingFuncDef == null) throw new IllegalArgumentException();
        // chicken and egg problem ... build param mappings to paramDef... still in parent ctor!!
        List<UnderyingParamDefToSourceMapping> tmpInputMappings = new ArrayList<UnderyingParamDefToSourceMapping>();
        for(UnderyingParamDefToSourceMapping.Builder mappingBuilder : builder.underlyingInputParamDefToParentMappings) {
            // first step : resolve already created source paramDef
            TimeFragFuncParamDef resolved_sourceParamDefWhenNotCurried = builder.findCreatedInputParamDefFor(this, mappingBuilder.sourceParamDefBuilderWhenNotCurried);
            UnderyingParamDefToSourceMapping mapping = new UnderyingParamDefToSourceMapping(this, mappingBuilder, resolved_sourceParamDefWhenNotCurried);
            tmpInputMappings.add(mapping);
        }
        this.underlyingInputParamDefToParentMappings = Collections.unmodifiableList(tmpInputMappings);
    }
    
    // ------------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    protected List<CurriedTimeFragFuncParamDef> getCurriedInputParamDefs() {
        return (List<CurriedTimeFragFuncParamDef>) (List<?>) super.getInputParamDefs();
    }

    private TimeFragFuncEvaluator innerFuncEvaluator = new TimeFragFuncEvaluator() {
        @Override
        public void evalFunc(TimeFragFuncDef def, Object[] inputValues,
                Object[] outputValues) {
            CurriedTimeFragFuncDef.this.evalFunc(inputValues, outputValues);
        }
    };
    
    @Override
    public TimeFragFuncEvaluator getFuncEvaluator() {
        return innerFuncEvaluator;
    }


    @Override
    public void evalFunc(Object[] inputValues, Object[] outputValues) {
        final int underlyingInputLen = underlyingInputParamDefToParentMappings.size();
        Object[] underlyingInputValues = new Object[underlyingInputLen]; 
        
        // step 1: map inputs : pass param values or curried const values
        int inputIndex = 0;
        for (int i = 0; i < underlyingInputLen; i++) {
            UnderyingParamDefToSourceMapping defMapping = underlyingInputParamDefToParentMappings.get(i);
            Object inputParamValue;
            if (defMapping.isCurriedParameter) {
                inputParamValue = defMapping.constParamValueWhenCurried;
            } else {
                // TODO... may re-order input params!!!
                inputParamValue = inputValues[inputIndex];
                inputIndex++;
            }
            underlyingInputValues[i] = inputParamValue;
        }
        
        // step 2: eval underlying 
        underlyingFuncDef.evalFunc(underlyingInputValues, outputValues);
        
        // step 3: extract output = same as underlying!
    }

    
    // ------------------------------------------------------------------------
    
    /** 
     * design pattern Builder for constructing CurriedTimeFragFunctorDef
     */
    public static class Builder extends TimeFragFuncDef.Builder {
        // cf super.inputParamDefs... overload to use List<CurriedTimeFragFuncParamDef.Builder>
        private List<UnderyingParamDefToSourceMapping.Builder> underlyingInputParamDefToParentMappings = new ArrayList<UnderyingParamDefToSourceMapping.Builder>();
        public TimeFragFuncDef underlyingFunctorDef;
        
        @Override /** override to check sub-class parameter */
        public void addInputParamDef(TimeFragFuncParamDef.Builder p) {
            if (!(p instanceof CurriedTimeFragFuncParamDef.Builder)) throw new IllegalArgumentException();
            super.addInputParamDef(p);
        }
        
        public void addInputParamDefCurriedOrMapped(
                boolean isCurried, Object constParamValueWhenCurried, CurriedTimeFragFuncParamDef.Builder sourceParamDefBuilderWhenNotCurried,
                TimeFragFuncParamDef underlyingParamDef
                ) {
            // create input paramDef when not curried + create reverse mapping underlying->source
            UnderyingParamDefToSourceMapping.Builder mappingBuilder = new UnderyingParamDefToSourceMapping.Builder();
            mappingBuilder.setMapping(isCurried, 
                    constParamValueWhenCurried, sourceParamDefBuilderWhenNotCurried, 
                    underlyingParamDef);
            if (!isCurried) {
                sourceParamDefBuilderWhenNotCurried.underlyingParamDef = underlyingParamDef; // check/force mapping
                super.addInputParamDef(sourceParamDefBuilderWhenNotCurried);
            }
            underlyingInputParamDefToParentMappings.add(mappingBuilder);
        }
        
    }

    // ------------------------------------------------------------------------
    
    /**
     * sub-class of ParamDef for adding mapping to underlyingParamDef info
     */
    public static class CurriedTimeFragFuncParamDef extends TimeFragFuncParamDef {
        private final TimeFragFuncParamDef underlyingParamDef;

        public CurriedTimeFragFuncParamDef(TimeFragFuncDef owner, Builder b) {
            super(owner, b);
            this.underlyingParamDef = b.underlyingParamDef;
        }

        public TimeFragFuncParamDef getUnderlyingParamDef() {
            return underlyingParamDef;
        }
        
        /** design pattern Builder for CurriedTimeFragFuncParamDef */
        public static class Builder extends TimeFragFuncParamDef.Builder {
            public TimeFragFuncParamDef underlyingParamDef;

            public void set(TimeFragFuncParamDef.Builder src, TimeFragFuncParamDef underlyingParamDef) {
                super.set(src);
                this.underlyingParamDef = underlyingParamDef;
            }
            
            @Override
            public TimeFragFuncParamDef build(TimeFragFuncDef owner) {
                return new CurriedTimeFragFuncParamDef(owner, this);
            }
            
            
        }
    }


    // ------------------------------------------------------------------------
    
    /**
     * mapping for parent paramDef or curried param Value to underlying paramDef
     *  
     *  case curried:     curriedValue     0..1 <--
     *                                             \
     *                                              -- mapping ---> 1  underlyingParamDef
     *                                             /
     *  case not curried:  parent paramDef 0..1 <--
     *   
     */
    public static class UnderyingParamDefToSourceMapping {

        private final boolean isCurriedParameter;
        
        private final Object constParamValueWhenCurried;
        private final TimeFragFuncParamDef sourceParamDefWhenNotCurried;

        private final TimeFragFuncParamDef underlyingParamDef;
        
        public UnderyingParamDefToSourceMapping(CurriedTimeFragFuncDef owner, Builder b, TimeFragFuncParamDef resolved_sourceParamDefWhenNotCurried) {
            this.isCurriedParameter = b.isCurriedParameter;
            this.constParamValueWhenCurried = b.constParamValueWhenCurried;
            this.underlyingParamDef = b.underlyingParamDef;
            this.sourceParamDefWhenNotCurried = resolved_sourceParamDefWhenNotCurried;
        }
        

        
        public boolean isCurriedParameter() {
            return isCurriedParameter;
        }

        public Object getConstParamValueWhenCurried() {
            return constParamValueWhenCurried;
        }

        public TimeFragFuncParamDef getSourceParamDefWhenNotCurried() {
            return sourceParamDefWhenNotCurried;
        }

        public TimeFragFuncParamDef getUnderlyingParamDef() {
            return underlyingParamDef;
        }


        /** design pattern Builder for constructing UnderyingParamDefToSourceMapping */
        public static class Builder {
            public boolean isCurriedParameter;
            private Object constParamValueWhenCurried;
            private TimeFragFuncParamDef.Builder sourceParamDefBuilderWhenNotCurried;
            private TimeFragFuncParamDef underlyingParamDef;

            private void setBaseMapping(boolean isCurriedParameter, TimeFragFuncParamDef underlyingParamDef) {
                this.isCurriedParameter = isCurriedParameter;
                this.underlyingParamDef = underlyingParamDef;
            }
            public void setMapping(boolean isCurriedParameter, 
                    Object constParamValueWhenCurried, 
                    TimeFragFuncParamDef.Builder sourceParamDefWhenNotCurried,
                    TimeFragFuncParamDef underlyingParamDef) {
                setBaseMapping(isCurriedParameter, underlyingParamDef);
                this.constParamValueWhenCurried = constParamValueWhenCurried;
                this.sourceParamDefBuilderWhenNotCurried = sourceParamDefWhenNotCurried;
            }
            public void setMappingCurried(Object constParamValueWhenCurried, TimeFragFuncParamDef underlyingParamDef) {
                setBaseMapping(true, underlyingParamDef);
                this.constParamValueWhenCurried = constParamValueWhenCurried;
            }
            public void setMappingNotCurried(
                    TimeFragFuncParamDef.Builder sourceParamDefWhenNotCurried, 
                    TimeFragFuncParamDef underlyingParamDef) {
                setBaseMapping(false, underlyingParamDef);
                this.sourceParamDefBuilderWhenNotCurried = sourceParamDefWhenNotCurried;
            }
        }
        
    }
    
}