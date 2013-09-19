package fr.an.sounddecomp.core.frag.def.func.impl;

import java.util.ArrayList;
import java.util.List;

import fr.an.sounddecomp.core.frag.def.func.TimeFragFuncDef;
import fr.an.sounddecomp.core.frag.def.typedef.TimeFragIndicatorTypeDef;

public class TimeFragFuncParamDef {
    
    protected final TimeFragFuncDef owner;
    
    protected final String name;
    
    protected final boolean isInput;
    
    protected final TimeFragIndicatorTypeDef typeDef;
    
    // ------------------------------------------------------------------------

    /** cf Builder.build(), called from Registry only */
    protected TimeFragFuncParamDef(TimeFragFuncDef owner, Builder b) {
        this.owner = owner;
        this.name = b.name;
        this.isInput = b.isInput;
        this.typeDef = b.typeDef;
    }

    // ------------------------------------------------------------------------
    
    public TimeFragFuncDef getOwner() {
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

    // ------------------------------------------------------------------------

    public static class Builder {
        // field "owner" not present in Builder... chicken and egg problem!
        public String name;
        public boolean isInput;
        public TimeFragIndicatorTypeDef typeDef;
        
        public void set(String name, boolean isInput, TimeFragIndicatorTypeDef typeDef) {
            this.name = name;
            this.isInput = isInput;
            this.typeDef = typeDef;
        }
        public void set(Builder src) {
            this.name = src.name;
            this.isInput = src.isInput;
            this.typeDef = src.typeDef;
        }
        
        public TimeFragFuncParamDef build(TimeFragFuncDef owner) {
            return new TimeFragFuncParamDef(owner, this);
        }
        public static List<TimeFragFuncParamDef> buildList(TimeFragFuncDef owner, List<TimeFragFuncParamDef.Builder> builders) {
            List<TimeFragFuncParamDef> res = new ArrayList<TimeFragFuncParamDef>(builders.size());
            for(TimeFragFuncParamDef.Builder b : builders) {
                res.add(b.build(owner));
            }
            return res;
        }
        
    }
}
