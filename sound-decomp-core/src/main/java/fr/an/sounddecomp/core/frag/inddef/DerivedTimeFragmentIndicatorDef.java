package fr.an.sounddecomp.core.frag.inddef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DerivedTimeFragmentIndicatorDef extends TimeFragmentIndicatorDef {

    private final List<TimeFragmentIndicatorDef> dependOf;
    
    private final DerivedTimeFragmentFunction<?> evalFunc;
    
    // ------------------------------------------------------------------------
    
    public DerivedTimeFragmentIndicatorDef(TimeFragmentIndicatorDefRegisty owner, String name, 
            List<TimeFragmentIndicatorDef> dependOf,
            DerivedTimeFragmentFunction<?> evalFunc) {
        super(owner, name);
        this.dependOf = Collections.unmodifiableList(new ArrayList<TimeFragmentIndicatorDef>(dependOf));
        this.evalFunc = evalFunc;
    }

    // ------------------------------------------------------------------------

    public List<TimeFragmentIndicatorDef> getDependOf() {
        return dependOf;
    }

    public DerivedTimeFragmentFunction<?> getEvalFunc() {
        return evalFunc;
    }
    
    
}
