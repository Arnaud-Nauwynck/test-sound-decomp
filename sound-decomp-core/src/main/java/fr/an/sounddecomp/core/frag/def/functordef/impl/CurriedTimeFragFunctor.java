package fr.an.sounddecomp.core.frag.def.functordef.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;
import fr.an.sounddecomp.core.frag.def.functordef.TimeFragFunctor;
import fr.an.sounddecomp.core.frag.def.functordef.TimeFragFunctorDef;

/**
 * TimeFragFunctor sub-class for curried function, using partial parameters feed by constant values  
 */
public class CurriedTimeFragFunctor extends TimeFragFunctor {

    /**
     * TimeFragFunctorDef sub-class for curried function, using partial parameters feed by constant values  
     */
    public static class CurriedTimeFragFunctorDef extends TimeFragFunctorDef {

        public static class Builder extends TimeFragFunctorDef.Builder {
            public Map<TimeFragFunctorParamDef,Object> underlyingCurriedParameters = new HashMap<TimeFragFunctorParamDef,Object>();
            public TimeFragFunctorDef underlyingFunctorDef;
        }
        
        private final Map<TimeFragFunctorParamDef,Object> underlyingCurriedParameters; 
        private final TimeFragFunctorDef underlyingFunctorDef;
        
        public CurriedTimeFragFunctorDef(TimeFragmentDefRegisty owner, String name, CurriedTimeFragFunctorDef.Builder builder) {
            super(owner, name, builder);
            this.underlyingCurriedParameters = Collections.unmodifiableMap(new HashMap<TimeFragFunctorParamDef,Object>(builder.underlyingCurriedParameters));
            this.underlyingFunctorDef = builder.underlyingFunctorDef;
            if (underlyingFunctorDef == null) throw new IllegalArgumentException(); 
        }

        @Override
        public TimeFragFunctor createInstance() {
            TimeFragFunctor underlyingFunctor = underlyingFunctorDef.createInstance();
            CurriedTimeFragFunctor res = new CurriedTimeFragFunctor(this, underlyingCurriedParameters, underlyingFunctor);
            return res;
        }
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * the underlying parameter values to curry
     */
    private Map<TimeFragFunctorParamDef,Object> underlyingCurriedParameters; 
    
    /**
     * the underlying functor to call
     */
    private TimeFragFunctor underlyingFunctor;
    
    // ------------------------------------------------------------------------
    
    public CurriedTimeFragFunctor(TimeFragFunctorDef def,
            Map<TimeFragFunctorParamDef, Object> underlyingCurriedParameters,
            TimeFragFunctor targetFunctor) {
        super(def);
        this.underlyingCurriedParameters = underlyingCurriedParameters;
        this.underlyingFunctor = targetFunctor;
        underlyingFunctor.setInputValues(underlyingCurriedParameters);
    }

    // ------------------------------------------------------------------------
    
    @Override
    public void setInputValues(Map<TimeFragFunctorParamDef, Object> input) {
        // convert from this functor paramDef, to underlyingParamDef + complete by underlyingCurriedParamDef
        Map<TimeFragFunctorParamDef, Object> underlyingInput = new HashMap<TimeFragFunctorParamDef,Object>(input);
        underlyingInput.putAll(underlyingCurriedParameters);
        // convert input defs
        TimeFragFunctorDef underlyingFunctorDef = underlyingFunctor.getTimeFragFunctorDef(); 
        for(Map.Entry<TimeFragFunctorParamDef, Object> e : input.entrySet()) {
            TimeFragFunctorParamDef underlyingParamDef = underlyingFunctorDef.getInputParamDef(e.getKey().getName());
            underlyingInput.put(underlyingParamDef, e.getValue());
        }
        // delegate
        underlyingFunctor.setInputValues(underlyingInput);
    }

    public void run() {
        underlyingFunctor.run();
    }
    
    @Override
    public void getOutputValues(Map<TimeFragFunctorParamDef, Object> output) {
        Map<TimeFragFunctorParamDef, Object> underlyingOutput = new LinkedHashMap<TimeFragFunctorParamDef, Object>();
        // delegate
        underlyingFunctor.getOutputValues(underlyingOutput);
        // convert from underlyingParamDef to this functor paramDef 
        TimeFragFunctorDef functorDef = this.getTimeFragFunctorDef(); 
        for(Map.Entry<TimeFragFunctorParamDef, Object> e : underlyingOutput.entrySet()) {
            TimeFragFunctorParamDef paramDef = functorDef.getOutputParamDef(e.getKey().getName());
            output.put(paramDef, e.getValue());
        }
    }
    
}