package fr.an.sounddecomp.core.frag.def.functordef.impl;

import java.util.Map;

import fr.an.sounddecomp.core.frag.def.functordef.TimeFragFunctor;
import fr.an.sounddecomp.core.frag.def.functordef.impl.UserDefinedTimeFragFunctorDef.UserDefinedTimeFragFunctorParamDef;

/**
 * TimeFragFunctor sub-class for user-defined java functor class implementing "run()"
 * 
 * ... use Wrapper + delegate instead of forcing user-defined code to extends TimeFragFunctor!
 */
public class UserDefinedTimeFragFunctor<T extends Runnable> extends TimeFragFunctor {

    private final T wrappedObject;
    
    public UserDefinedTimeFragFunctor(UserDefinedTimeFragFunctorDef<?> def, T wrappedObject) {
        super(def);
        this.wrappedObject = wrappedObject;
    }
    
    public void run() {
        wrappedObject.run();
    }

    @Override
    public void setInputValues(Map<TimeFragFunctorParamDef, Object> input) {
        for(Map.Entry<TimeFragFunctorParamDef, Object> e : input.entrySet()) {
            UserDefinedTimeFragFunctorParamDef paramDef = (UserDefinedTimeFragFunctorParamDef) e.getKey();
            Object paramValue = e.getValue();
            paramDef.setFieldValue(wrappedObject, paramValue);
        }
    }

    @Override
    public void getOutputValues(Map<TimeFragFunctorParamDef, Object> output) {
        for(TimeFragFunctorParamDef paramDef : getTimeFragFunctorDef().getOutputParamDefs().values()) {
            UserDefinedTimeFragFunctorParamDef paramDef2 = (UserDefinedTimeFragFunctorParamDef) paramDef;
            Object paramValue = paramDef2.getFieldValue(wrappedObject);
            output.put(paramDef, paramValue);
        }
    }
    
    
}