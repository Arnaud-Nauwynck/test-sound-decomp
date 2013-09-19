package fr.an.sounddecomp.core.frag.def.inddef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;
import fr.an.sounddecomp.core.frag.def.func.TimeFragFuncDef;

public class DerivedTimeFragIndicatorDef extends TimeFragIndicatorDef {

    private final List<TimeFragIndicatorDef> dependOf;
    
    private final TimeFragFuncDef evalFuncDef;
    
    // ------------------------------------------------------------------------
    
    public DerivedTimeFragIndicatorDef(TimeFragmentDefRegisty owner, String name, 
            List<TimeFragIndicatorDef> dependOf,
            TimeFragFuncDef evalFunctorDef) {
        super(owner, name);
        this.dependOf = Collections.unmodifiableList(new ArrayList<TimeFragIndicatorDef>(dependOf));
        this.evalFuncDef = evalFunctorDef;
    }

    // ------------------------------------------------------------------------

    public List<TimeFragIndicatorDef> getDependOf() {
        return dependOf;
    }

    public TimeFragFuncDef getEvalFuncDef() {
        return evalFuncDef;
    }
    
    
}
