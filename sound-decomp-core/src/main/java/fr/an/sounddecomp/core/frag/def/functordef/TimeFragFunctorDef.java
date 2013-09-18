package fr.an.sounddecomp.core.frag.def.functordef;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;
import fr.an.sounddecomp.core.frag.def.functordef.impl.TimeFragFunctorParamDef;

public abstract class TimeFragFunctorDef {

    public static class Builder {
        private final Map<String,TimeFragFunctorParamDef.Builder> inputParamDefs = new LinkedHashMap<String,TimeFragFunctorParamDef.Builder>();
        private final Map<String,TimeFragFunctorParamDef.Builder> outputParamDefs = new LinkedHashMap<String,TimeFragFunctorParamDef.Builder>();
        
        public void addInputParamDef(TimeFragFunctorParamDef.Builder p) {
            inputParamDefs.put(p.name, p);
        }
        public void addOutputParamDef(TimeFragFunctorParamDef.Builder p) {
            outputParamDefs.put(p.name, p);
        }
    }
    
    private final TimeFragmentDefRegisty owner;
    private final String name;
    // private Callable<TimeFragFunctor> instanceFactory;
    
    private final Map<String,TimeFragFunctorParamDef> inputParamDefs;
    private final Map<String,TimeFragFunctorParamDef> outputParamDefs;
    
    // ------------------------------------------------------------------------

    public TimeFragFunctorDef(TimeFragmentDefRegisty owner, String name, Builder builder) {
        this.owner = owner;
        this.name = name;
        // chicken and egg problem: build child objects, assign immutable "this" as child.owner
        this.inputParamDefs = Collections.unmodifiableMap(TimeFragFunctorParamDef.Builder.buildMap(this, builder.inputParamDefs));
        this.outputParamDefs = Collections.unmodifiableMap(TimeFragFunctorParamDef.Builder.buildMap(this, builder.outputParamDefs));
    }

    // ------------------------------------------------------------------------

    public TimeFragmentDefRegisty getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }
    
    public Map<String, TimeFragFunctorParamDef> getInputParamDefs() {
        return inputParamDefs;
    }

    public Map<String, TimeFragFunctorParamDef> getOutputParamDefs() {
        return outputParamDefs;
    }

    public TimeFragFunctorParamDef getInputParamDef(String name) {
        return inputParamDefs.get(name);
    }

    public TimeFragFunctorParamDef getOutputParamDef(String name) {
        return outputParamDefs.get(name);
    }

    // ------------------------------------------------------------------------
    
    public abstract TimeFragFunctor createInstance();

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TimeFragFunctorDef[" + name + "]";
    }
    
}
