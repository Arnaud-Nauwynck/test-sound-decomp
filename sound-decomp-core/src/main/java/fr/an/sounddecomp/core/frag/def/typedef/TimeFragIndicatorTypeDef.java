package fr.an.sounddecomp.core.frag.def.typedef;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;

public abstract class TimeFragIndicatorTypeDef {

    protected final TimeFragmentDefRegisty owner;
    protected final String name;

    // ------------------------------------------------------------------------
    
    public TimeFragIndicatorTypeDef(TimeFragmentDefRegisty owner, String name) {
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
