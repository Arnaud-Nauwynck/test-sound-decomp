package fr.an.sounddecomp.core.frag.def.inddef;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;

public class PrevTimeFragIndicatorDef extends TimeFragIndicatorDef {

    private final TimeFragIndicatorDef prevIndicatorDef;
    
    // ------------------------------------------------------------------------
    
    public PrevTimeFragIndicatorDef(TimeFragmentDefRegisty owner, String name, TimeFragIndicatorDef prevIndicatorDef) {
        super(owner, name);
        this.prevIndicatorDef = prevIndicatorDef;
    }

    // ------------------------------------------------------------------------

    public TimeFragIndicatorDef getPrevIndicatorDef() {
        return prevIndicatorDef;
    }

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "PrevTimeFragmentIndicatorDef[" 
                + "prevIndicatorDef=" + prevIndicatorDef 
                + "]";
    }

    
    
    
    
}
