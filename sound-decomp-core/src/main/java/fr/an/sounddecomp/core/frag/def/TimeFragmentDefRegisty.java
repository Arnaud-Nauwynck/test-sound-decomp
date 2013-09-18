package fr.an.sounddecomp.core.frag.def;

import java.util.HashMap;
import java.util.Map;

import fr.an.sounddecomp.core.frag.def.inddef.MainArrayDataTimeFragIndicatorDef;
import fr.an.sounddecomp.core.frag.def.inddef.PrevTimeFragIndicatorDef;
import fr.an.sounddecomp.core.frag.def.inddef.TimeFragIndicatorDef;

public class TimeFragmentDefRegisty {

    private Map<String,TimeFragIndicatorDef> indicatorDefs = new HashMap<String,TimeFragIndicatorDef>();
    private final int fragmentSize;
    
    // ------------------------------------------------------------------------

    public TimeFragmentDefRegisty(int fragmentSize) {
        this.fragmentSize = fragmentSize;
        MainArrayDataTimeFragIndicatorDef mainDataDef = new MainArrayDataTimeFragIndicatorDef(this, "mainData");
        registerIndicatorDef(mainDataDef);
        registerIndicatorDef(new PrevTimeFragIndicatorDef(this, "prev-mainData", mainDataDef));
    }

    // ------------------------------------------------------------------------

    private void registerIndicatorDef(TimeFragIndicatorDef def) {
        indicatorDefs.put(def.getName(), def);
    }

    public int getFragmentSize() {
        return fragmentSize;
    }
    
}
