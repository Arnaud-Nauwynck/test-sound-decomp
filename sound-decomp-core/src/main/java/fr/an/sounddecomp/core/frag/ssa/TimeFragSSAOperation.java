package fr.an.sounddecomp.core.frag.ssa;

import fr.an.sounddecomp.core.frag.def.func.TimeFragFuncDef;

public class TimeFragSSAOperation {

    private TimeFragSSAVar<?>[] inputVars;

    private TimeFragFuncDef funcDef;
    
    private TimeFragSSAVar<?>[] outputVars;

    // ------------------------------------------------------------------------

    public TimeFragSSAOperation(TimeFragSSAVar<?>[] inputVars,
            TimeFragFuncDef funcDef, TimeFragSSAVar<?>[] outputVars) {
        super();
        this.inputVars = inputVars;
        this.funcDef = funcDef;
        this.outputVars = outputVars;
    }

    // ------------------------------------------------------------------------
    
    public TimeFragSSAVar<?>[] getInputVars() {
        return inputVars;
    }

    public TimeFragFuncDef getFuncDef() {
        return funcDef;
    }

    public TimeFragSSAVar<?>[] getOutputVars() {
        return outputVars;
    }
    
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void evalFuncAndAssignOutputVars() {
        // step 1: get input values from input ssa vars, allocate ouput values array
        final int inputVarLen = inputVars.length;
        Object[] inputValues = new Object[inputVarLen];
        for (int i = 0; i < inputVarLen; i++) {
            inputValues[i] = inputVars[i].get();
        }
        
        final int outputVarLen = outputVars.length;
        Object[] outputValues = new Object[outputVarLen];
        
        // step 2: eval funcDef
        funcDef.evalFunc(inputValues, outputValues);
        
        // step 3: fill output ssa vars with output values
        for (int i = 0; i < outputVarLen; i++) {
            ((TimeFragSSAVar<Object>) outputVars[i]).set(outputValues[i]);
        }
    }

}
