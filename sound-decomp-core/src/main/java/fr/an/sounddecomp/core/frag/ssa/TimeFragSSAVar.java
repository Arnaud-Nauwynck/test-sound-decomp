package fr.an.sounddecomp.core.frag.ssa;

public abstract class TimeFragSSAVar<T> {
    
    public abstract T get();
    
    /** SSA set ... may be called only once, and only on derived var !!*/
    public abstract void set(T value);
    
    // utility type-cast helpers..
    public abstract double getDouble();
    public abstract void setDouble(double value);
    public abstract int getInt();
    public abstract void setInt(int value);
    
    
    public abstract boolean isSet();
    public abstract boolean isForgetDepencyOnSsaSet();
    
    /** after being computed, a Var can forget its dependency formula ... so getDependency() may become null */
    public abstract TimeFragSSAOperation getDependency();

    
    // TOADD ??? metadata description / Id 
    // TimeFragId
    // IndicatorDef
    
}
