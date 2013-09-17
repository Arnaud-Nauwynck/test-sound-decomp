package fr.an.sounddecomp.core.frag.inddef;

public class PrevTimeFragmentIndicatorDef extends TimeFragmentIndicatorDef {

    private final TimeFragmentIndicatorDef prevIndicatorDef;
    
    // ------------------------------------------------------------------------
    
    public PrevTimeFragmentIndicatorDef(TimeFragmentIndicatorDefRegisty owner, String name, TimeFragmentIndicatorDef prevIndicatorDef) {
        super(owner, name);
        this.prevIndicatorDef = prevIndicatorDef;
    }

    // ------------------------------------------------------------------------

    public TimeFragmentIndicatorDef getPrevIndicatorDef() {
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
