package fr.an.sounddecomp.core.frag.inddef;


public class TimeFragmentIndicatorDef {

    private final TimeFragmentIndicatorDefRegisty owner;
    private final String name;
    
    // ------------------------------------------------------------------------
    
    public TimeFragmentIndicatorDef(TimeFragmentIndicatorDefRegisty owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    // ------------------------------------------------------------------------

    public TimeFragmentIndicatorDefRegisty getOwner() {
        return owner;
    }
    
    public String getName() {
        return name;
    }
    
    
}
