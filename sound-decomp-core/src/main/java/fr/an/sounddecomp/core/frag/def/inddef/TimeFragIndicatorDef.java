package fr.an.sounddecomp.core.frag.def.inddef;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;


public abstract class TimeFragIndicatorDef {

    private final TimeFragmentDefRegisty owner;
    private final String name;
    
    // ------------------------------------------------------------------------
    
    public TimeFragIndicatorDef(TimeFragmentDefRegisty owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    // ------------------------------------------------------------------------

    public TimeFragmentDefRegisty getOwner() {
        return owner;
    }
    
    public String getName() {
        return name;
    }
    
    
}
