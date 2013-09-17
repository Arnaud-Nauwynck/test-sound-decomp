package fr.an.sounddecomp.core.frag.inddef;

import java.util.List;

import fr.an.sounddecomp.core.frag.indexaccessor.TimeFragmentDataAccessor;

public abstract class DerivedTimeFragmentFunction<TOut> {

    public abstract void eval(TimeFragmentDataAccessor<TOut> dest, List<TimeFragmentDataAccessor<?>> args);
    
}
