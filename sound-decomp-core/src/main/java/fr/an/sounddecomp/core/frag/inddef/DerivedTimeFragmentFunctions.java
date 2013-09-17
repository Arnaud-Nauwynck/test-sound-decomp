package fr.an.sounddecomp.core.frag.inddef;

import java.util.List;

import fr.an.sounddecomp.core.frag.indexaccessor.TimeFragmentDataAccessor;

public class DerivedTimeFragmentFunctions {

    // ------------------------------------------------------------------------

    /** Function for 1 argument: for (TimeFragmentData)->(TimeFragmentData) */  
    public static abstract class DerivedTimeFragmentFunction1<TOut,TIn> extends DerivedTimeFragmentFunction<TOut> {
        @Override
        @SuppressWarnings("unchecked")
        public void eval(TimeFragmentDataAccessor<TOut> dest, List<TimeFragmentDataAccessor<?>> args) {
            TimeFragmentDataAccessor<TIn> arg0 = (TimeFragmentDataAccessor<TIn>) args.get(0);
            eval1(dest, arg0);
        }

        public abstract void eval1(TimeFragmentDataAccessor<TOut> dest, TimeFragmentDataAccessor<TIn> arg0);

    }

    // ------------------------------------------------------------------------

    /** Function for 2 arguments: for (TimeFragmentData, TimeFragmentData)->(TimeFragmentData) */  
    public static abstract class DerivedTimeFragmentFunction2<TOut,TIn0,TIn1> extends DerivedTimeFragmentFunction<TOut> {
        @Override
        @SuppressWarnings("unchecked")
        public void eval(TimeFragmentDataAccessor<TOut> dest, List<TimeFragmentDataAccessor<?>> args) {
            TimeFragmentDataAccessor<TIn0> arg0 = (TimeFragmentDataAccessor<TIn0>) args.get(0);
            TimeFragmentDataAccessor<TIn1> arg1 = (TimeFragmentDataAccessor<TIn1>) args.get(1);
            eval2(dest, arg0, arg1);
        }

        public abstract void eval2(TimeFragmentDataAccessor<TOut> dest, 
                TimeFragmentDataAccessor<TIn0> arg0, TimeFragmentDataAccessor<TIn1> arg1);

    }

    // ------------------------------------------------------------------------

    /** Function for 3 arguments: for (TimeFragmentData, TimeFragmentData, TimeFragmentData)->(TimeFragmentData) */  
    public static abstract class DerivedTimeFragmentFunction3<TOut,TIn0,TIn1,TIn2> extends DerivedTimeFragmentFunction<TOut> {
        @Override
        @SuppressWarnings("unchecked")
        public void eval(TimeFragmentDataAccessor<TOut> dest, List<TimeFragmentDataAccessor<?>> args) {
            TimeFragmentDataAccessor<TIn0> arg0 = (TimeFragmentDataAccessor<TIn0>) args.get(0);
            TimeFragmentDataAccessor<TIn1> arg1 = (TimeFragmentDataAccessor<TIn1>) args.get(1);
            TimeFragmentDataAccessor<TIn2> arg2 = (TimeFragmentDataAccessor<TIn2>) args.get(2);
            eval3(dest, arg0, arg1, arg2);
        }

        public abstract void eval3(TimeFragmentDataAccessor<TOut> dest, 
                TimeFragmentDataAccessor<TIn0> arg0, TimeFragmentDataAccessor<TIn1> arg1, TimeFragmentDataAccessor<TIn2> arg2);

    }
    
}
