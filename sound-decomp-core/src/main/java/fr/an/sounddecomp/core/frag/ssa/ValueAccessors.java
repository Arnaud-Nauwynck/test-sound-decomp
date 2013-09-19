package fr.an.sounddecomp.core.frag.ssa;

public class ValueAccessors {

    /* private to force all static */
    private ValueAccessors() {}
    
    
    public static interface ObjectValueGetter {
        Object getObjectValue();
    }

    public static interface DoubleGetter {
        double getDouble();
    }

    public static interface DoubleSetter {
        void setDouble(double value);
    }

    public static interface IntGetter {
        int getInt();
    }

    public static interface IntSetter {
        void setInt(int value);
    }

}
