package fr.an.sounddecomp.core.frag.def.functordef.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.sounddecomp.core.frag.def.functordef.TimeFragFunctorDef;
import fr.an.sounddecomp.core.frag.def.typedef.TimeFragIndicatorTypeDef;

public class TimeFragFunctorParamDef {

    public static class Builder {
        public String name;
        public boolean isInput;
        public TimeFragIndicatorTypeDef typeDef;
        
        public TimeFragFunctorParamDef build(TimeFragFunctorDef owner) {
            return new TimeFragFunctorParamDef(owner, this);
        }
        public static Map<String,TimeFragFunctorParamDef> buildMap(TimeFragFunctorDef owner, Map<String,TimeFragFunctorParamDef.Builder> builders) {
            Map<String,TimeFragFunctorParamDef> res = new LinkedHashMap<String,TimeFragFunctorParamDef>();
            for(TimeFragFunctorParamDef.Builder b : builders.values()) {
                res.put(b.name, b.build(owner));
            }
            return res;
        }
        
    }
    
    protected final TimeFragFunctorDef owner;
    protected final String name;
    protected final boolean isInput;
    protected final TimeFragIndicatorTypeDef typeDef;
    
    // ------------------------------------------------------------------------

    /** cf Builder.build(), called from Registry only */
    protected TimeFragFunctorParamDef(TimeFragFunctorDef owner, Builder b) {
        this.owner = owner;
        this.name = b.name;
        this.isInput = b.isInput;
        this.typeDef = b.typeDef;
    }

    // ------------------------------------------------------------------------
    
    public TimeFragFunctorDef getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public boolean isInput() {
        return isInput;
    }

    public TimeFragIndicatorTypeDef getTypeDef() {
        return typeDef;
    }

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return ((isInput)? "Input" : "Output") + "ParamDef[" + name + "]";
    }
    
}
