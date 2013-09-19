package fr.an.sounddecomp.core.frag.def.func;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.an.sounddecomp.core.frag.def.TimeFragmentDefRegisty;
import fr.an.sounddecomp.core.frag.def.func.impl.TimeFragFuncParamDef;

public abstract class TimeFragFuncDef {

    private final TimeFragmentDefRegisty owner;
    private final String name;
    
    private final List<TimeFragFuncParamDef> inputParamDefs;
    private final List<TimeFragFuncParamDef> outputParamDefs;
    
    // ------------------------------------------------------------------------

    public TimeFragFuncDef(TimeFragmentDefRegisty owner, String name, Builder builder) {
        this.owner = owner;
        this.name = name;
        // chicken and egg problem: build child objects, assign immutable "this" as child.owner
        this.inputParamDefs = Collections.unmodifiableList(TimeFragFuncParamDef.Builder.buildList(this, builder.inputParamDefs));
        this.outputParamDefs = Collections.unmodifiableList(TimeFragFuncParamDef.Builder.buildList(this, builder.outputParamDefs));
    }

    // ------------------------------------------------------------------------

    public TimeFragmentDefRegisty getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }
    
    public List<TimeFragFuncParamDef> getInputParamDefs() {
        return inputParamDefs;
    }

    public List<TimeFragFuncParamDef> getOutputParamDefs() {
        return outputParamDefs;
    }

    // ------------------------------------------------------------------------
    
    public abstract TimeFragFuncEvaluator getFuncEvaluator();

    /**
     * evaluate function : compute output from input values 
     * pure function: no side-effects, repeatable !!
     * 
     * @param inputValues
     * @param outputValues
     */
    public void evalFunc(Object[] inputValues, Object[] outputValues) {
        TimeFragFuncEvaluator evaluator = getFuncEvaluator();
        evaluator.evalFunc(this, inputValues, outputValues);
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TimeFragFuncDef[" + name + "]";
    }

    // ------------------------------------------------------------------------

    /**
     * design pattern Builder for creating TimeFragFuncDef
     */
    public static class Builder {
        private final List<TimeFragFuncParamDef.Builder> inputParamDefs = new ArrayList<TimeFragFuncParamDef.Builder>();
        private final List<TimeFragFuncParamDef.Builder> outputParamDefs = new ArrayList<TimeFragFuncParamDef.Builder>();
        
        public void addInputParamDef(TimeFragFuncParamDef.Builder p) {
            inputParamDefs.add(p);
        }
        public void addOutputParamDef(TimeFragFuncParamDef.Builder p) {
            outputParamDefs.add(p);
        }
        
        public TimeFragFuncParamDef findCreatedInputParamDefFor(TimeFragFuncDef owner, TimeFragFuncParamDef.Builder builder) {
            return findCreatedParamDefFor(owner.getInputParamDefs(), inputParamDefs, builder);
        }
        public TimeFragFuncParamDef findCreatedOutputParamDefFor(TimeFragFuncDef owner, TimeFragFuncParamDef.Builder builder) {
            return findCreatedParamDefFor(owner.getOutputParamDefs(), outputParamDefs, builder);
        }
        private static TimeFragFuncParamDef findCreatedParamDefFor(List<TimeFragFuncParamDef> createdList, List<TimeFragFuncParamDef.Builder> builderList, TimeFragFuncParamDef.Builder builder) {
            // find index in list => get corresponding elt in created list
            int index =  builderList.indexOf(builder); 
            if (index == -1) throw new IllegalArgumentException();
            return createdList.get(index);
        }
    }
    
}
