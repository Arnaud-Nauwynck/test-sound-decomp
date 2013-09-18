package fr.an.sounddecomp.core.frag.def.functordef;

import java.util.Map;

import fr.an.sounddecomp.core.frag.def.functordef.impl.TimeFragFunctorParamDef;

public abstract class TimeFragFunctor {

    private final TimeFragFunctorDef timeFragFunctorDef;
    
    // ------------------------------------------------------------------------

    public TimeFragFunctor(TimeFragFunctorDef timeFragFunctorDef) {
        this.timeFragFunctorDef = timeFragFunctorDef;
    }

    // ------------------------------------------------------------------------

    public final TimeFragFunctorDef getTimeFragFunctorDef() {
        return timeFragFunctorDef;
    }

    
    public abstract void setInputValues(Map<TimeFragFunctorParamDef,Object> input);
    
    public abstract void run();
    
    public abstract void getOutputValues(Map<TimeFragFunctorParamDef,Object> output);

    
    public void eval(Map<TimeFragFunctorParamDef,Object> input, Map<TimeFragFunctorParamDef,Object> output) {
        setInputValues(input);
        run();
        getOutputValues(output);
    }

}
