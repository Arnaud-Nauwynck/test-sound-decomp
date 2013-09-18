package fr.an.sounddecomp.core.frag.def.inddef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;
import fr.an.sounddecomp.core.frag.def.functordef.TimeFragFunctorDef;

public class DerivedTimeFragIndicatorDef extends TimeFragIndicatorDef {

    private final List<TimeFragIndicatorDef> dependOf;
    
    private final TimeFragFunctorDef evalFunctorDef;
    
    // ------------------------------------------------------------------------
    
    public DerivedTimeFragIndicatorDef(TimeFragmentDefRegisty owner, String name, 
            List<TimeFragIndicatorDef> dependOf,
            TimeFragFunctorDef evalFunctorDef) {
        super(owner, name);
        this.dependOf = Collections.unmodifiableList(new ArrayList<TimeFragIndicatorDef>(dependOf));
        this.evalFunctorDef = evalFunctorDef;
    }

    // ------------------------------------------------------------------------

    public List<TimeFragIndicatorDef> getDependOf() {
        return dependOf;
    }

    public TimeFragFunctorDef getEvalFunctorDef() {
        return evalFunctorDef;
    }
    
    
}
