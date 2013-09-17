package fr.an.sounddecomp.core.frag.inddef;

import java.util.HashMap;
import java.util.Map;

public class TimeFragmentIndicatorDefRegisty {

    private Map<String,TimeFragmentIndicatorDef> defs = new HashMap<String,TimeFragmentIndicatorDef>();
    
    // ------------------------------------------------------------------------

    public TimeFragmentIndicatorDefRegisty() {
        MainArrayDataTimeFragmentIndicatorDef mainDataDef = new MainArrayDataTimeFragmentIndicatorDef(this, "mainData");
        register(mainDataDef);
        register(new PrevTimeFragmentIndicatorDef(this, "prev-mainData", mainDataDef));
    }

    // ------------------------------------------------------------------------

    private void register(TimeFragmentIndicatorDef def) {
        defs.put(def.getName(), def);
    }
    
}
