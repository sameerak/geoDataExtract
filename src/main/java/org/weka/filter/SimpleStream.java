package org.weka.filter;

import weka.core.*;
import weka.core.Capabilities.*;
import weka.filters.*;

import java.util.Random;

public class SimpleStream
        extends SimpleStreamFilter {

    protected Random m_Random;

    public String globalInfo() {
        return   "A simple stream filter that adds an attribute 'bla' at the end "
                + "containing a random number.";
    }

    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.enableAllAttributes();
        result.enableAllClasses();
        result.enable(Capability.NO_CLASS);  //// filter doesn't need class to be set//
        return result;
    }

    protected void reset() {
        super.reset();
        m_Random = new Random(1);
    }

    protected Instances determineOutputFormat(Instances inputFormat) {
        Instances result = new Instances(inputFormat, 0);
        result.insertAttributeAt(new Attribute("bla"), result.numAttributes());
        return result;
    }

    protected Instance process(Instance inst) {
        double[] values = new double[inst.numAttributes() + 1];
        for (int n = 0; n < inst.numAttributes(); n++)
            values[n] = inst.value(n);
        values[values.length - 1] = m_Random.nextInt();
        Instance result = new Instance(1, values);
        return result;
    }

    public static void main(String[] args) {
        runFilter(new SimpleStream(), args);
    }
}
